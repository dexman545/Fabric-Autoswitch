package autoswitch;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Parent class for Targetable type. Used to establish shared functions and parameters
 * that are used for manipulating the player's selected slot
 *
 */
@SuppressWarnings("WeakerAccess")
@Environment(EnvType.CLIENT)
abstract class Targetable {
    HashMap<Object, ArrayList<UUID>> toolTargetLists = AutoSwitch.data.toolTargetLists;
    LinkedHashMap<UUID, ArrayList<Integer>> toolLists = AutoSwitch.data.toolLists;
    HashMap<Integer, Double> toolRating = new HashMap<>(); //Rating for tool effectiveness - ie. speed for blocks or enchantment level
    PlayerEntity player;
    AutoSwitchConfig cfg;
    Boolean onMP;


    /**
     * Base constructor for Targetable, initializes the class parameters and
     * fetches the target map and initial tool map based on configs passed to ti
     *
     * @param player player this will effect
     * @param onMP whether the player is on a remote server. If given nul, will assume that AutoSwitch is allowed
     */
    public Targetable(PlayerEntity player, Boolean onMP) {
        this.cfg = AutoSwitch.cfg;
        this.onMP = (onMP != null ? onMP : false);
        this.player = player;
    }


    /**
     * the of methods send the target to the correct function to handle it
     * @return returns the correct Targetable subclass to handle the operation
     */
    static Targetable of(Entity target, PlayerEntity player, Boolean onMP) {
        return new TargetableEntity(target, player, onMP);
    }

    static Targetable of(BlockState target, PlayerEntity player, Boolean onMP) {
        return new TargetableMaterial(target, player, onMP);
    }

    static Targetable of(int prevSlot, PlayerEntity player) {
        return new TargetableNone(prevSlot, player);
    }


    /**
     * Pulls the list of itemstacks from the player's hotbar and send the stack and slot number
     * to populate the tool map. Sends an air item if the slot is empty.
     * @param player player whose inventory will be checked
     */
    public void populateToolLists(PlayerEntity player) {
        List<ItemStack> hotbar = player.inventory.main.subList(0, PlayerInventory.getHotbarSize());
        for (int i=0; i<PlayerInventory.getHotbarSize(); i++) {
            if ((hotbar.get(i).getMaxDamage() - hotbar.get(i).getDamage() < 3) && this.cfg.tryPreserveDamagedTools()) {
                continue;
            }
            populateTargetTools(hotbar.get(i), i);

        }

    }


    /**
     * Change the players selected slot based on the results of findSlot().
     * Checks if there is a slot to change to first.
     * @see autoswitch.Targetable#findSlot()
     * @return If no slot to change to, returns empty Otherwise returns true if the slot changed, false if it didn't
     */
    public Optional<Boolean> changeTool() {
        return findSlot().map(slot -> {
            int currentSlot = this.player.inventory.selectedSlot;
            if (slot == currentSlot) {
                //No need to change slot!
                return Optional.of(false);
            }

            //Loop over it since scrollinhotbar only moves one pos
            for (int i = Math.abs(currentSlot - slot); i > 0; i--){
                this.player.inventory.scrollInHotbar(currentSlot - slot);
            }
            return Optional.of(true); //Slot changed
        }).orElseGet(Optional::empty); //if nothing to change to, return empty

    }


    /**
     * @return returns true if the config allows autoswitch to happen; false otherwise.
     * Does not take into account toggle (doAS)
     */
    protected Boolean switchAllowed() {
        return ((!this.player.isCreative() || this.cfg.switchInCreative()) &&
            (switchTypeAllowed() && (!onMP || this.cfg.switchInMP())));
    }

    //Overrides

    /**
     * Populate the tool map with the right tools for that type based on subclass
     * @param stack itemstack to be checked if it is valid
     * @param i slot of stack, to be inserted into map if it is valid
     */
    abstract void populateTargetTools(ItemStack stack, int i);

    /**
     * Find the optimal tool slot. Return empty if there isn't one
     * @return Returns empty if autoswitch is not allowed or there is no slot to change to
     */
    Optional<Integer> findSlot() {
        if (!switchAllowed()) {return Optional.empty();}
        for (Map.Entry<UUID, ArrayList<Integer>> toolList : toolLists.entrySet()){ //type of tool, slots that have it
            if (!toolList.getValue().isEmpty()) {
                for (Integer i : toolList.getValue()) {
                    if (!this.toolRating.isEmpty() && i.equals(Collections.max(this.toolRating.entrySet(), Comparator.comparingDouble(Map.Entry::getValue)).getKey())) {
                        return Optional.of(i);
                    }
                }
            }
        }

        return Optional.empty();
    }

    void populateTargetToolsBase(Object tar, ItemStack stack, int i) {
        Item item = stack.getItem();

        AtomicReference<Float> counter = new AtomicReference<>((float) PlayerInventory.getHotbarSize());
        Object meh = tar instanceof AbstractBlock.AbstractBlockState ? ((AbstractBlock.AbstractBlockState) tar).getMaterial() : (tar instanceof LivingEntity ? ((LivingEntity) tar).getGroup() : ((Entity) tar).getType());
        if (this.toolTargetLists.get(meh) == null) {return;}
        this.toolTargetLists.get(meh).forEach(uuid -> {
            if (uuid == null) {return;}
            counter.updateAndGet(v -> (float) (v - 0.25));
            Pair<String, Enchantment> pair = AutoSwitch.data.enchantToolMap.get(uuid);
            String tool = pair.getLeft();
            Enchantment enchant = pair.getRight();
            int level = 0;

            if (ToolHandler.correctType(tool, item)) {
                double rating = 0;
                if (enchant == null) {
                    this.toolLists.putIfAbsent(uuid, new ArrayList<>());
                    this.toolLists.get(uuid).add(i);
                    rating += 1; //promote tool in ranking as it is the correct one
                } else if (EnchantmentHelper.getLevel(enchant, stack) > 0) {
                    level = EnchantmentHelper.getLevel(enchant, stack);
                    this.toolLists.putIfAbsent(uuid, new ArrayList<>());
                    this.toolLists.get(uuid).add(i);
                }
                rating += (tar instanceof BlockState ? stack.getMiningSpeedMultiplier((BlockState) tar) : 0) + (level) + counter.get();
                double finalRating = rating;
                this.toolRating.computeIfPresent(i, (integer, aDouble) -> Math.max(aDouble, finalRating));
                this.toolRating.putIfAbsent(i, rating);
            }
        });

    }


    /**
     * Determine config value for switching for mobs/blocks
     * @return true if that type of switch is allowed in the config
     */
    abstract Boolean switchTypeAllowed();


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
    void populateTargetTools(ItemStack stack, int i) {

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
     * @param stack itemstack to be checked if it is valid
     * @param i     slot of stack, to be inserted into map if it is valid
     */
    @Override
    void populateTargetTools(ItemStack stack, int i) {
        populateTargetToolsBase(this.entity, stack, i);

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
     * Checks against itemclass and enchantments on the stack
     * @param stack itemstack to be checked if it is valid
     * @param i slot of stack, to be inserted into map if it is valid
     */
    @Override
    void populateTargetTools(ItemStack stack, int i) {
        populateTargetToolsBase(this.bs, stack, i);

    }

    @Override
    Boolean switchTypeAllowed() {
        return this.cfg.switchForBlocks();
    }
}
