package dex.autoswitch.harness;

import java.util.List;

import dex.autoswitch.engine.data.extensible.PlayerInventory;
import dex.autoswitch.engine.state.SwitchContext;

import net.minecraft.world.item.ItemStack;

public class DummyInventory implements PlayerInventory<ItemStack> {
    private final List<ItemStack> inventory;
    private int s = 0;
    private boolean slotChanged;

    public DummyInventory(List<ItemStack> stacks) {
        inventory = stacks;
    }

    @Override
    public void selectSlot(int slot) {
        slotChanged = s != slot;
        s = slot;
    }

    @Override
    public int currentSelectedSlot() {
        return s;
    }

    @Override
    public int slotCount() {
        return inventory.size();
    }

    @Override
    public ItemStack getTool(int slot) {
        return inventory.get(slot);
    }

    public void reset() {
        slotChanged = false;
    }

    public boolean slotChanged() {
        return slotChanged;
    }

    @Override
    public boolean canSwitchBack(SwitchContext ctx) {
        return false;
    }

    @Override
    public void moveOffhand() {

    }
}
