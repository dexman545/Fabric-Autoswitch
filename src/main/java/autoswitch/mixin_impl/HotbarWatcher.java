package autoswitch.mixin_impl;

import autoswitch.AutoSwitch;
import autoswitch.util.TargetableCache;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.function.IntConsumer;

public class HotbarWatcher {

    public static IntArrayList compareHotbars(List<ItemStack> prev, List<ItemStack> current) {
        IntArrayList changedHotbarSlots = new IntArrayList(9);
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

    private static boolean stacksSufficientlyDiffer(ItemStack o, ItemStack n) {
        if (o.isEmpty() || n.isEmpty()) return true;
        if (ItemStack.areEqual(o, n)) return false;

        return !ItemStack.areTagsEqual(o, n) || !ItemStack.areItemsEqual(o, n);

    }

    public static void handleSlotChange(int slot, List<ItemStack> prev, List<ItemStack> current) {
        if (!PlayerInventory.isValidHotbarIndex(slot)) return;
        updateCaches(compareHotbars(prev, current));

    }

    public static void updateCaches(IntArrayList changedSlots) {
        changedSlots.forEach((IntConsumer) slot -> {
            updateCache(AutoSwitch.switchState.switchActionCache, slot);
            updateCache(AutoSwitch.switchState.switchInteractCache, slot);
        });
    }

    private static void updateCache(final TargetableCache cache, int slot) {
        if (!cache.containsValue(slot)) return;

        // Previously this was #removePairOnValue to avoid needless recalculation.
        // This was changed to fix issues with moving a tool to the hotbar but the cache not resetting.
        // Conveniently, it also handles the case of "blank"/non-tool switches.
        // Alternatively, use a smarter method of removing old cache values.
        cache.clear();
    }

}
