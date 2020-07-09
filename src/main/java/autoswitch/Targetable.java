package autoswitch;

import autoswitch.config.AutoSwitchConfig;
import autoswitch.config.ToolHandler;
import autoswitch.util.SwitchDataStorage;
import autoswitch.util.SwitchUtil;
import autoswitch.util.TargetableUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Saddleable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Parent class for Targetable type. Used to establish shared functions and parameters that are used for manipulating
 * the player's selected slot.
 */
@Environment(EnvType.CLIENT)
public abstract class Targetable {
    ConcurrentHashMap<Object, CopyOnWriteArrayList<UUID>> toolTargetLists = AutoSwitch.data.toolTargetLists;
    Map<UUID, CopyOnWriteArrayList<Integer>> toolLists = Collections.synchronizedMap(AutoSwitch.data.toolLists);
    //Rating for tool effectiveness - ie. speed for blocks or enchantment level
    ConcurrentHashMap<Integer, Double> toolRating = new ConcurrentHashMap<>();
    PlayerEntity player;
    AutoSwitchConfig cfg;
    Boolean onMP;


    /**
     * Base constructor for Targetable, initializes the class parameters and
     * fetches the target map and initial tool map based on configs passed to it
     *
     * @param player player this will effect
     * @param onMP   whether the player is on a remote server. If given null, will assume that AutoSwitch is allowed
     */
    public Targetable(PlayerEntity player, Boolean onMP) {
        this.cfg = AutoSwitch.cfg;
        this.onMP = (onMP != null ? onMP : false);
        this.player = player;
    }


    /**
     * The "of" methods send the target to the correct function to handle it
     *
     * @return returns the correct Targetable subclass to handle the operation
     */
    protected static Targetable of(Entity target, PlayerEntity player, Boolean onMP) {
        return new TargetableEntity(target, player, onMP);
    }

    protected static Targetable of(BlockState target, PlayerEntity player, Boolean onMP) {
        return new TargetableMaterial(target, player, onMP);
    }

    public static Targetable use(Object protoTarget, PlayerEntity player, Boolean onMP) {
        return new TargetableUsable(player, onMP, protoTarget);
    }

    public static Targetable of(int prevSlot, PlayerEntity player) {
        return new TargetableNone(prevSlot, player);
    }

    public static Targetable of(Object protoTarget, PlayerEntity player, boolean onMP) {
        if (protoTarget instanceof BlockState) return new TargetableMaterial((BlockState) protoTarget, player, onMP);
        if (protoTarget instanceof Entity) return new TargetableEntity((Entity) protoTarget, player, onMP);

        AutoSwitch.logger.error("Tried to switch for nothing recognizable!");
        return null;
    }


    /**
     * Pulls the list of ItemStacks from the player's hotbar and send the stack and slot number
     * to populate the tool map. Sends an air item if the slot is empty.
     *
     * @param player player whose inventory will be checked
     */
    public void populateToolLists(PlayerEntity player) {
        List<ItemStack> hotbar = player.inventory.main.subList(0, PlayerInventory.getHotbarSize());
        for (int slot = 0; slot < PlayerInventory.getHotbarSize(); slot++) {
            if (!(AutoSwitch.cfg.useNoDurablityItemsWhenUnspecified() && hotbar.get(slot).getMaxDamage() == 0) && (hotbar.get(slot).getMaxDamage() - hotbar.get(slot).getDamage() < 3) && this.cfg.tryPreserveDamagedTools()) {
                continue;
            }
            populateToolSelection(hotbar.get(slot), slot);

        }

    }


    /**
     * Change the players selected slot based on the results of findSlot().
     * Checks if there is a slot to change to first.
     *
     * @return If no slot to change to, returns empty Otherwise returns true if the slot changed, false if it didn't
     * @see autoswitch.Targetable#findSlot()
     */
    public Optional<Boolean> changeTool() {
        return findSlot().map(slot -> {
            int currentSlot = this.player.inventory.selectedSlot;
            if (slot == currentSlot) {
                //No need to change slot!
                return Optional.of(false);
            }

            //Loop over it since scrollInHotbar only moves one pos
            for (int i = Math.abs(currentSlot - slot); i > 0; i--) {
                this.player.inventory.scrollInHotbar(currentSlot - slot);
            }
            return Optional.of(true); //Slot changed
        }).orElseGet(Optional::empty); //if nothing to change to, return empty

    }


    /**
     * @return returns true if the config allows autoswitch to happen; false otherwise.
     * Does not take into account toggle (AutoSwitch#doAS)
     */
    protected Boolean switchAllowed() {
        return ((!this.player.isCreative() || this.cfg.switchInCreative()) &&
                (switchTypeAllowed() && (!onMP || this.cfg.switchInMP())));
    }

    //Overrides

    /**
     * Populate the tool map with the right tools for that type based on subclass
     *
     * @param stack ItemStack to be checked if it is valid
     * @param slot  slot of stack, to be inserted into map if it is valid
     */
    abstract void populateToolSelection(ItemStack stack, int slot);

    /**
     * Find the optimal tool slot. Return empty if there isn't one
     *
     * @return Returns empty if autoswitch is not allowed or there is no slot to change to
     */
    Optional<Integer> findSlot() {
        if (this.toolRating.isEmpty() || !switchAllowed()) {
            return Optional.empty();
        }

        AutoSwitch.logger.debug(toolRating);
        for (Map.Entry<UUID, CopyOnWriteArrayList<Integer>> toolList : toolLists.entrySet()) { //type of tool, slots that have it
            if (!toolList.getValue().isEmpty()) {
                for (Integer slot : toolList.getValue()) {
                    if (slot.equals(Collections.max(this.toolRating.entrySet(),
                            Comparator.comparingDouble(Map.Entry::getValue)).getKey())) {
                        return Optional.of(slot);
                    }
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Add tools to map that can handle this target
     *
     * @param protoTarget Entity or BlockState being attacked
     * @param stack       item in hotbar slot to check for usage
     * @param slot        hotbar slot number
     */
    void populateToolSelectionCore(Object protoTarget, ItemStack stack, int slot) {
        Item item = stack.getItem();

        // Establish base value to add to the tool rating, promoting higher priority tools from the config in the selection
        AtomicReference<Float> counter = new AtomicReference<>((float) PlayerInventory.getHotbarSize());

        Object target = TargetableUtil.getTarget(protoTarget);

        // Evaluate target and find tools

        // Short circuit as no target and no non-damageable fallback desired
        if (!AutoSwitch.cfg.useNoDurablityItemsWhenUnspecified() && this.toolTargetLists.get(target) == null) return;

        this.toolTargetLists.getOrDefault(target, SwitchDataStorage.blank).forEach(uuid -> {
            if (uuid == null) {
                return;
            }
            counter.updateAndGet(v -> (float) (v - 0.25)); //tools later in the config list are not preferred
            String tool;
            CopyOnWriteArrayList<Enchantment> enchants;
            if (uuid != SwitchDataStorage.blank.get(0)) {
                Pair<String, CopyOnWriteArrayList<Enchantment>> pair = AutoSwitch.data.enchantToolMap.get(uuid);
                tool = pair.getLeft();
                enchants = pair.getRight();
            } else { // Handle case of no target but user desires fallback to items
                tool = "blank";
                enchants = null;
            }

            if (ToolHandler.isCorrectType(tool, item) && TargetableUtil.isRightTool(stack, protoTarget)) {
                new TargetableMapUtil().updateToolListsAndRatings(stack, uuid, tool, enchants, slot, protoTarget, counter, false);
            }
        });

    }


    /**
     * Determine config value for switching for mobs/blocks
     *
     * @return true if that type of switch is allowed in the config
     */
    abstract Boolean switchTypeAllowed();


    // Helper class for switching
    class TargetableMapUtil {
        /**
         * Moves some core switch logic out of the lambda to better reuse it in both attack and use switching
         */
        public void updateToolListsAndRatings(ItemStack stack, UUID uuid, String tool, CopyOnWriteArrayList<Enchantment> enchants, int slot, Object protoTarget, AtomicReference<Float> counter, boolean useAction) {
            double rating = 0;
            boolean stackEnchants = true;

            // Evaluate enchantment
            if (enchants == null) {
                rating += 1; //promote tool in ranking as it is the correct one
                stackEnchants = false; // items without the enchant shouldn't stack with ones that do
            } else {
                double enchantRating = 0;
                for (Enchantment enchant : enchants) {
                    if (EnchantmentHelper.getLevel(enchant, stack) > 0) {
                        enchantRating += 1.1 * EnchantmentHelper.getLevel(enchant, stack);
                    } else return; // Don't further consider this tool as it does not have the enchantment needed
                }
                rating += enchantRating;
                AutoSwitch.logger.debug("Slot: {}; EnchantRating: {}", slot, enchantRating);
            }

            // Add tool to selection
            Targetable.this.toolLists.putIfAbsent(uuid, new CopyOnWriteArrayList<>());
            Targetable.this.toolLists.get(uuid).add(slot);
            if (!useAction) {
                if (Targetable.this.cfg.preferMinimumViableTool() && rating != 0D) {
                    rating += -1 * Math.log10(rating); // reverse and clamp tool
                }
                rating += TargetableUtil.getTargetRating(protoTarget, stack) + counter.get();

                if (!tool.equals("blank") && ((stack.getItem().getMaxDamage() == 0))) { // Fix ignore overrides
                    rating = 0.1;
                }
            }

            //prefer current slot. Has outcome of making undamageable item fallback not switching if it can help it
            if (Targetable.this.player.inventory.selectedSlot == slot) {
                rating += 0.1;
            }
            double finalRating = rating;
            boolean finalStackEnchants = stackEnchants;
            AutoSwitch.logger.debug("Rating: {}; Slot: {}", rating, slot);

            Targetable.this.toolRating.computeIfPresent(slot, (iSlot, oldRating) -> TargetableUtil.toolRatingChange(oldRating, finalRating, stack, finalStackEnchants));
            Targetable.this.toolRating.putIfAbsent(slot, rating);
        }
    }


}

class TargetableUsable extends Targetable {
    Object target;

    /**
     * Base constructor for Targetable, initializes the class parameters and
     * fetches the target map and initial tool map based on configs passed to it
     *
     * @param player player this will effect
     * @param onMP   whether the player is on a remote server. If given null, will assume that AutoSwitch is allowed
     */
    public TargetableUsable(PlayerEntity player, Boolean onMP, Object target) {
        super(player, onMP);
        this.target = target;
        populateToolLists(player);
    }

    @Override
    void populateToolSelection(ItemStack stack, int slot) {

        AtomicReference<Float> counter = new AtomicReference<>((float) PlayerInventory.getHotbarSize());

        Object target = TargetableUtil.getUseTarget(this.target);

        if (AutoSwitch.cfg.checkSaddlableEntitiesForSaddle() &&
                this.target instanceof Saddleable && !((Saddleable) this.target).isSaddled()) {
            //Don't switch if the target isn't saddled. Assumes only use for saddleable entity would be to ride it
            return;
        }

        if (AutoSwitch.data.useMap.get(target) == null) return;
        AutoSwitch.data.useMap.get(target).forEach(uuid -> {
            if (uuid == null) {
                return;
            }
            counter.updateAndGet(v -> (float) (v - 0.25)); //tools later in the config list are not preferred
            String tool;
            CopyOnWriteArrayList<Enchantment> enchant;
            Pair<String, CopyOnWriteArrayList<Enchantment>> pair = AutoSwitch.data.enchantToolMap.get(uuid);
            tool = pair.getLeft();
            enchant = pair.getRight();

            if (ToolHandler.isCorrectUseType(tool, stack.getItem())) {
                new TargetableMapUtil().updateToolListsAndRatings(stack, uuid, tool, enchant, slot, this.target, counter, true);
            }
        });
    }

    @Override
    Boolean switchTypeAllowed() {
        return this.cfg.switchUseActions();
    }

}

/**
 * Implementation of changeSlot when there is no target. Intended for switchback feature
 */
@SuppressWarnings("WeakerAccess")
class TargetableNone extends Targetable {
    int prevSlot;


    public TargetableNone(int prevSlot, PlayerEntity player) {
        super(player, null);
        this.prevSlot = prevSlot;
    }

    @Override
    void populateToolSelection(ItemStack stack, int i) {

    }

    @Override
    Optional<Integer> findSlot() {
        return Optional.of(this.prevSlot);
    }

    @Override
    Boolean switchTypeAllowed() {
        return true;
    }
}

/**
 * Used when targeting an entity
 */
@SuppressWarnings("WeakerAccess")
class TargetableEntity extends Targetable {
    private final Entity entity;

    public TargetableEntity(Entity target, PlayerEntity player, Boolean onMP) {
        super(player, onMP);
        this.entity = target;
        this.player = player;
        populateToolLists(this.player);

    }


    /**
     * Checks against enchants on the stack and TridentItem class
     *
     * @param stack ItemStack to be checked if it is valid
     * @param slot  slot of stack, to be inserted into map if it is valid
     */
    @Override
    void populateToolSelection(ItemStack stack, int slot) {
        populateToolSelectionCore(this.entity, stack, slot);

    }


    @Override
    Boolean switchTypeAllowed() {
        return this.cfg.switchForMobs();
    }
}

/**
 * Targetable instance for targeting a block
 */
@SuppressWarnings("WeakerAccess")
class TargetableMaterial extends Targetable {
    private final BlockState bs;

    public TargetableMaterial(BlockState target, PlayerEntity player, Boolean onMP) {
        super(player, onMP);
        this.player = player;
        this.bs = target;
        populateToolLists(player);
    }

    /**
     * Checks against Item and enchantments on the stack
     *
     * @param stack ItemStack to be checked if it is valid
     * @param slot  slot of stack, to be inserted into map if it is valid
     */
    @Override
    void populateToolSelection(ItemStack stack, int slot) {
        populateToolSelectionCore(this.bs, stack, slot);

    }

    @Override
    Boolean switchTypeAllowed() {
        return this.cfg.switchForBlocks();
    }
}
