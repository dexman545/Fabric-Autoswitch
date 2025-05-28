package dex.autoswitch.test.tests;

import dex.autoswitch.config.AutoSwitchConfig;
import dex.autoswitch.engine.Action;
import dex.autoswitch.engine.SelectionEngine;
import dex.autoswitch.harness.DummyInventory;
import dex.autoswitch.harness.DummyTypes;
import dex.autoswitch.test.util.AbstractSelectionTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SelectionTest extends AbstractSelectionTest {
    private final AutoSwitchConfig autoSwitchConfig = loadConfig("emptySelector");

    @Override
    public DummyInventory createInventory() {
        return new DummyInventory("air", "pickaxe", "ax", "shears", "shovel", "stone", "cobweb", "spear");
    }

    @Override
    public SelectionEngine getEngine() {
        return autoSwitchConfig.getEngine();
    }

    /**
     * Test that block overrides work
     */
    @Test
    void emptySelector() {
        var stone = DummyTypes.createTarget("stone", DummyTypes.Type.BLOCK);
        var blackStone = DummyTypes.createTarget("black_stone", DummyTypes.Type.BLOCK);

        inventory.selectSlot(6);
        engine.select(inventory, Action.ATTACK, stone);
        assertEquals(6, inventory.currentSelectedSlot());

        engine.select(inventory, Action.ATTACK, blackStone);
        assertEquals(1, inventory.currentSelectedSlot());
    }

    /**
     * Test that selecting an entity of the same id does not confuse with the block
     */
    @Test
    void entitySelection() {
        var stone = DummyTypes.createTarget("stone", DummyTypes.Type.ENTITY);

        engine.select(inventory, Action.ATTACK, stone);
        assertEquals(2, inventory.currentSelectedSlot());
    }

    /**
     * Test if item tags work
     */
    @Test
    void itemTags() {
        // First see if it will select shovels
        var deer = DummyTypes.createTarget("deer", DummyTypes.Type.ENTITY);
        engine.select(inventory, Action.INTERACT, deer);
        assertEquals(7, inventory.currentSelectedSlot());

        // Manually select spear (which is valid), then see if it will select stay on the current tool
        inventory.selectSlot(7);
        engine.select(inventory, Action.INTERACT, deer);
        assertEquals(7, inventory.currentSelectedSlot());

        inventory.selectSlot(6);
        engine.select(inventory, Action.INTERACT, deer);
        assertEquals(7, inventory.currentSelectedSlot());
    }

    /**
     * Test that 2 matching groups, tool selector priority is interleaved
     */
    @Test
    void mixedGroups() {
        var stone = DummyTypes.createTarget("stone", DummyTypes.Type.BLOCK);
        var rock = DummyTypes.createTarget("rock", DummyTypes.Type.BLOCK);

        engine.select(inventory, Action.INTERACT, stone);
        assertEquals(1, inventory.currentSelectedSlot());

        engine.select(inventory, Action.INTERACT, rock);
        assertEquals(7, inventory.currentSelectedSlot());
    }

    @Test
    void testNonToolSelection() {
        var torak = DummyTypes.createTarget("torak", DummyTypes.Type.ENTITY);

        engine.select(inventory, Action.ATTACK, torak);
        assertEquals(0, inventory.currentSelectedSlot());

        inventory.selectSlot(1);
        engine.select(inventory, Action.ATTACK, torak);
        assertEquals(0, inventory.currentSelectedSlot());

        autoSwitchConfig.featureConfig.switchAwayFromTools = false;
        autoSwitchConfig.resetConfiguration();
        engine = autoSwitchConfig.getEngine();
        inventory.selectSlot(1);
        engine.select(inventory, Action.ATTACK, torak);
        assertEquals(1, inventory.currentSelectedSlot());
        autoSwitchConfig.featureConfig.switchAwayFromTools = true;
        autoSwitchConfig.resetConfiguration();
    }
}
