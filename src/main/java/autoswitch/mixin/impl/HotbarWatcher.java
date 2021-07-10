package autoswitch.mixin.impl;

import java.util.List;
import java.util.function.IntConsumer;

import autoswitch.AutoSwitch;
import autoswitch.util.TargetableCache;

import it.unimi.dsi.fastutil.ints.IntArrayList;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

public class HotbarWatcher {

    public static void handleSlotChange(List<ItemStack> prev, List<ItemStack> current) {
        updateCaches(compareHotbars(prev, current));

    }

    public static void updateCaches(IntArrayList changedSlots) {
        changedSlots.forEach((IntConsumer) slot -> {
            updateCache(AutoSwitch.switchState.switchActionCache, slot);
            updateCache(AutoSwitch.switchState.switchInteractCache, slot);
        });
    }

    public static IntArrayList compareHotbars(List<ItemStack> prev, List<ItemStack> current) {
        IntArrayList changedHotbarSlots = new IntArrayList(PlayerInventory.getHotbarSize());
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
        if (ItemStack.areEqual(o, n)) return false;

        return !ItemStack.areNbtEqual(o, n) || !ItemStack.areItemsEqual(o, n);

    }

}
