package autoswitch.targetable;

import autoswitch.AutoSwitch;
import autoswitch.util.SwitchDataStorage;
import autoswitch.util.TargetableUtil;
import it.unimi.dsi.fastutil.ints.Int2DoubleArrayMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.IntConsumer;

/**
 * Parent class for Targetable type. Used to establish shared functions and parameters that are used for manipulating
 * the player's selected slot.
 */
@Environment(EnvType.CLIENT)
public abstract class AbstractTargetable {
    //Rating for tool effectiveness - ie. speed for blocks or enchantment level based on user config
    private final Int2DoubleArrayMap slot2ToolRating = new Int2DoubleArrayMap();
    PlayerEntity player;
    Object protoTarget = null;


    /**
     * Base constructor for Targetable, initializes the class parameters and
     * fetches the target map and initial tool map based on configs passed to it
     *
     * @param player player this will effect
     */
    AbstractTargetable(PlayerEntity player) {
        this.player = player;
    }

    /**
     * Switch logic for 'use' action
     *
     * @return returns the correct AbstractTargetable subclass to handle the operation
     */
    public static AbstractTargetable use(Object protoTarget, PlayerEntity player) {
        return new TargetableUsable(player, protoTarget);
    }

    /**
     * Switch logic for 'switchback' action
     *
     * @return returns the correct AbstractTargetable subclass to handle the operation
     */
    public static AbstractTargetable switchback(int prevSlot, PlayerEntity player) {
        return new TargetableNone(prevSlot, player);
    }

    /**
     * Switch logic for 'attack' action
     *
     * @return returns the correct AbstractTargetable subclass to handle the operation
     */
    public static AbstractTargetable attack(Object protoTarget, PlayerEntity player) {
        return new TargetableAttack(protoTarget, player);
    }


    /**
     * Pulls the list of ItemStacks from the player's hotbar and send the stack and slot number
     * to populate the tool map. Sends an air item if the slot is empty.
     *
     * @param player player whose inventory will be checked
     */
    void populateToolLists(PlayerEntity player) {
        List<ItemStack> hotbar = player.inventory.main.subList(0, PlayerInventory.getHotbarSize());
        for (int slot = 0; slot < PlayerInventory.getHotbarSize(); slot++) {
            if (TargetableUtil.skipSlot(hotbar.get(slot))) {
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
     * @see AbstractTargetable#findSlot()
     */
    public Optional<Boolean> changeTool() {
        return findSlot().map(slot -> {
            int currentSlot = this.player.inventory.selectedSlot;
            if (slot == currentSlot) {
                //No need to change slot!
                return Optional.of(false);
            }

            this.player.inventory.selectedSlot = slot;

            return Optional.of(true); //Slot changed
        }).orElseGet(Optional::empty); //if nothing to change to, return empty

    }


    /**
     * @return returns true if the config allows autoswitch to happen; false otherwise.
     * Does not take into account toggle (AutoSwitch#doAS)
     */
    private Boolean switchAllowed() {
        return ((!this.player.isCreative() || AutoSwitch.featureCfg.switchInCreative()) &&
                (switchTypeAllowed() && (MinecraftClient.getInstance().isInSingleplayer() || AutoSwitch.featureCfg.switchInMP())));
    }

    /**
     * Find the optimal tool slot. Return empty if there isn't one
     *
     * @return Returns empty if autoswitch is not allowed or there is no slot to change to
     */
    Optional<Integer> findSlot() {
        if (!switchAllowed() || this.slot2ToolRating.isEmpty()) {
            return Optional.empty();
        }

        AutoSwitch.logger.debug(slot2ToolRating);
        if (!this.slot2ToolRating.isEmpty()) {
            return Optional.of(Collections.max(this.slot2ToolRating.int2DoubleEntrySet(),
                    Comparator.comparingDouble(Map.Entry::getValue)).getIntKey());
        }


        return Optional.empty();
    }

    /**
     * Populate the tool map with the right tools for that type based on subclass
     *
     * @param stack ItemStack to be checked if it is valid
     * @param slot  slot of stack, to be inserted into map if it is valid
     */
    abstract void populateToolSelection(ItemStack stack, int slot);

    /**
     * Add tools to map that can handle this target
     *
     * @param stack           item in hotbar slot to check for usage
     * @param slot            hotbar slot number
     * @param targetGetter    lookup protoTarget in the correct map
     * @param toolSelector    check ToolType for correct case
     * @param toolSelectorMap ToolSelectors relevant to the case
     */
    void processToolSelectors(ItemStack stack, int slot,
                              Object2ObjectOpenHashMap<Object, IntArrayList> toolSelectorMap,
                              TargetGetter targetGetter, ToolSelector toolSelector) {
        if (!switchAllowed()) return; // Short-circuit to not evaluate tools when cannot switch

        Item item = stack.getItem();

        // Establish base value to add to the tool rating,
        // promoting higher priority tools from the config in the selection
        AtomicReference<Float> counter = new AtomicReference<>((float) PlayerInventory.getHotbarSize() * 10);

        Object target = targetGetter.getTarget(protoTarget);

        if (target == null || checkSpecialCase(target)) return;

        toolSelectorMap.getOrDefault(target, SwitchDataStorage.blank).forEach((IntConsumer) id -> {
            if (id == 0) return; // Check if no ID was assigned to the toolSelector.

            counter.updateAndGet(v -> (float) (v - 0.75)); //tools later in the config list are not preferred
            String tool;
            ReferenceArrayList<Enchantment> enchants;

            if (id != SwitchDataStorage.blank.getInt(0)) {
                Pair<String, ReferenceArrayList<Enchantment>> pair = AutoSwitch.data.toolSelectors.get(id);
                tool = pair.getLeft();
                enchants = pair.getRight();
            } else { // Handle case of no target but user desires fallback to items
                tool = "blank";
                enchants = null;
            }

            if (toolSelector.correctType(tool, item) && (isUse() || TargetableUtil.isRightTool(stack, protoTarget))) {
                updateToolListsAndRatings(stack, tool, enchants, slot, counter);
            }

        });

    }

    /**
     * Moves some core switch logic out of the lambda to increase clarity
     */
    private void updateToolListsAndRatings(ItemStack stack, String tool, ReferenceArrayList<Enchantment> enchants,
                                           int slot, AtomicReference<Float> counter) {
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

        if (!isUse()) {
            if (AutoSwitch.featureCfg.preferMinimumViableTool() && rating != 0D) {
                rating += -1 * Math.log10(rating); // reverse and clamp tool
            }
            rating += TargetableUtil.getTargetRating(protoTarget, stack) + counter.get();

            if (!tool.equals("blank") && ((stack.getItem().getMaxDamage() == 0))) { // Fix ignore overrides
                rating = 0.1;
            }
        }

        // Prefer current slot. Has outcome of making undamageable item fallback not switch if it can help it
        if (this.player.inventory.selectedSlot == slot) {
            rating += 0.1;
        }
        double finalRating = rating;
        boolean finalStackEnchants = stackEnchants;
        AutoSwitch.logger.debug("Rating: {}; Slot: {}", rating, slot);

        this.slot2ToolRating.computeIfPresent(slot, (iSlot, oldRating) ->
                TargetableUtil.toolRatingChange(oldRating, finalRating, stack, finalStackEnchants));
        this.slot2ToolRating.putIfAbsent(slot, rating);

    }

    boolean isUse() {
        return false;
    }

    boolean checkSpecialCase(Object target) {
        return false;
    }

    /**
     * Determine config value for switching for mobs/blocks
     *
     * @return true if that type of switch is allowed in the config
     */
    abstract Boolean switchTypeAllowed();

    @FunctionalInterface
    interface TargetGetter {
        Object getTarget(Object protoTarget);
    }


    @FunctionalInterface
    interface ToolSelector {
        boolean correctType(String tool, Item item);
    }

}

