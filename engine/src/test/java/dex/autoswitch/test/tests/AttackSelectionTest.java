package dex.autoswitch.test.tests;

import dex.autoswitch.config.AutoSwitchConfig;
import dex.autoswitch.engine.Action;
import dex.autoswitch.engine.SelectionEngine;
import dex.autoswitch.harness.DummyInventory;
import dex.autoswitch.harness.DummyTypes;
import dex.autoswitch.test.util.AbstractSelectionTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AttackSelectionTest extends AbstractSelectionTest {
    private final AutoSwitchConfig autoSwitchConfig = loadConfig("attackSelector");

    @Override
    public DummyInventory createInventory() {
        return new DummyInventory("pickaxe", "shovel", "ax", "pickaxe", "shears", "spear");
    }

    @Override
    public SelectionEngine getEngine() {
        return autoSwitchConfig.getEngine();
    }

    @Test
    void testPickaxeSelection() {
        inventory.selectSlot(2);
        engine.select(inventory, Action.ATTACK, DummyTypes.createTarget("stone", DummyTypes.Type.BLOCK));
        assertEquals(0, inventory.currentSelectedSlot());
        assertTrue(inventory.slotChanged());
    }

    @Test
    void testWoodSelection() {
        engine.select(inventory, Action.ATTACK, DummyTypes.createTarget("wood", DummyTypes.Type.BLOCK));
        assertEquals(2, inventory.currentSelectedSlot());
        assertTrue(inventory.slotChanged());
    }

    @Test
    void testNoSelection() {
        var slot = engine.findSlot(inventory, Action.ATTACK, DummyTypes.createTarget("mehNoSelection", DummyTypes.Type.BLOCK));
        slot.ifPresent(inventory::selectSlot);
        assertTrue(slot.isEmpty());
        assertEquals(0, inventory.currentSelectedSlot());
        assertFalse(inventory.slotChanged());
    }

    @Test
    void testRatingLevel1() {
        engine.select(inventory, Action.ATTACK, DummyTypes.createTarget("specialStone", DummyTypes.Type.BLOCK));
        assertEquals(2, inventory.currentSelectedSlot());
        assertTrue(inventory.slotChanged());
    }

    @Test
    void testCurrentSlotPreference() {
        engine.select(inventory, Action.ATTACK, DummyTypes.createTarget("stone", DummyTypes.Type.BLOCK));
        assertEquals(0, inventory.currentSelectedSlot());
        inventory.selectSlot(3);
        inventory.reset();
        engine.select(inventory, Action.ATTACK, DummyTypes.createTarget("stone", DummyTypes.Type.BLOCK));
        assertEquals(3, inventory.currentSelectedSlot());
        assertFalse(inventory.slotChanged());
    }

    @Test
    void targetPrioritySelection() {
        engine.select(inventory, Action.INTERACT, DummyTypes.createTarget("wool", DummyTypes.Type.BLOCK));
        assertEquals(4, inventory.currentSelectedSlot());
        engine.select(inventory, Action.INTERACT, DummyTypes.createTarget("brownWool", DummyTypes.Type.BLOCK));
        assertEquals(5, inventory.currentSelectedSlot());
        engine.select(inventory, Action.INTERACT, DummyTypes.createTarget("bob", DummyTypes.Type.BLOCK));
        assertEquals(0, inventory.currentSelectedSlot());
    }
}
