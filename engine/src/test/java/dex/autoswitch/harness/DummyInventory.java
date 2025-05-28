package dex.autoswitch.harness;

import dex.autoswitch.engine.data.extensible.PlayerInventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DummyInventory implements PlayerInventory<DummyTypes.DummyTool> {
    private final List<DummyTypes.DummyTool> inventory;
    private int s = 0;
    private boolean slotChanged;

    public DummyInventory(String... ids) {
        inventory = new ArrayList<>(ids.length);
        for (String id : ids) {
            inventory.add(DummyTypes.createTool(id));
        }
    }

    public DummyInventory(DummyTypes.DummyTool... tools) {
        inventory = new ArrayList<>(tools.length);
        inventory.addAll(Arrays.asList(tools));
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
    public DummyTypes.DummyTool getTool(int slot) {
        return inventory.get(slot);
    }

    public void reset() {
        slotChanged = false;
    }

    public boolean slotChanged() {
        return slotChanged;
    }

    @Override
    public boolean canSwitchBack() {
        return false;
    }

    @Override
    public void moveOffhand() {

    }
}
