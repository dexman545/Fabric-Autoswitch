package autoswitch.mixin.impl;

import java.util.List;
import java.util.function.IntConsumer;

import autoswitch.actions.Action;
import autoswitch.util.TargetableCache;

import it.unimi.dsi.fastutil.ints.IntArrayList;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class HotbarWatcher {

    public static void handleSlotChange(List<ItemStack> prev, List<ItemStack> current) {
        updateCaches(compareHotbars(prev, current));

    }

    public static void updateCaches(IntArrayList changedSlots) {
        for (Action action : Action.values()) {
            changedSlots.forEach((IntConsumer) slot -> updateCache(action.getActionCache(), slot));
        }
    }

    public static IntArrayList compareHotbars(List<ItemStack> prev, List<ItemStack> current) {
        IntArrayList changedHotbarSlots = new IntArrayList(Inventory.getSelectionSize());
        if (prev == null || current == null) return changedHotbarSlots;
        for (int slot = 0; slot < prev.size(); slot++) {
            final ItemStack oStack = prev.get(slot);
            final ItemStack nStack = current.get(slot);
            if (stacksSufficientlyDiffer(oStack, nStack)) {
                changedHotbarSlots.add(slot);
            }
        }

        return changedHotbarSlots;
    }

    private static void updateCache(final TargetableCache cache, int slot) {
        // Disabled as adding items to hotbar doesn't properly fix the cache,
        // see hoe on grass then adding shovel to hotbar
        //if (!cache.containsValue(slot)) return;

        // Previously this was #removePairOnValue to avoid needless recalculation.
        // This was changed to fix issues with moving a tool to the hotbar but the cache not resetting.
        // Conveniently, it also handles the case of "blank"/non-tool switches.
        // Alternatively, use a smarter method of removing old cache values.
        //TODO smart cache update
        cache.clear();
    }

    private static boolean stacksSufficientlyDiffer(ItemStack o, ItemStack n) {
        if (o.isEmpty() || n.isEmpty()) return true;
        if (ItemStack.matches(o, n)) return false;

        return !ItemStack.isSameItemSameTags(o, n) || !ItemStack.isSameItem(o, n);

    }

}
