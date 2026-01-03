package dex.autoswitch.test.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import dex.autoswitch.config.AutoSwitchConfig;
import dex.autoswitch.config.data.tree.IdSelector;
import dex.autoswitch.engine.Action;
import dex.autoswitch.engine.SelectionEngine;
import dex.autoswitch.engine.Selector;
import dex.autoswitch.futures.FutureSelectable;
import dex.autoswitch.harness.DummyInventory;
import dex.autoswitch.harness.DummyTypes;
import dex.autoswitch.test.util.AbstractSelectionTest;
import org.junit.jupiter.api.Test;

public class DataTest extends AbstractSelectionTest {
    private final AutoSwitchConfig autoSwitchConfig = loadConfig("configTypes");

    @Override
    public DummyInventory createInventory() {
        return new DummyInventory(
                new DummyTypes.DummyTool("pickaxe"),
                new DummyTypes.DummyTool("pickaxe", Set.of(
                        new DummyTypes.DummyEnchantment("bob")
                )),
                new DummyTypes.DummyTool("ax"),
                new DummyTypes.DummyTool("ax", Set.of(
                )),
                new DummyTypes.DummyTool("shovel", Set.of(
                        new DummyTypes.DummyEnchantment("meh")
                )),
                new DummyTypes.DummyTool("pickaxe"),
                new DummyTypes.DummyTool("pickaxe", Set.of(
                        new DummyTypes.DummyEnchantment("robert")
                ))
        );
    }

    @Override
    public SelectionEngine getEngine() {
        return autoSwitchConfig.getEngine();
    }

    /**
     * Tests if configuration that specifies data will not select matching items with missing data
     */
    @Test
    void componentsPreventSelection() {
        var stone = DummyTypes.createTarget("stone", DummyTypes.Type.BLOCK);
        var wool = DummyTypes.createTarget("wool", DummyTypes.Type.BLOCK);

        var slot = engine.findSlot(inventory, Action.ATTACK, stone);
        slot.ifPresent(inventory::selectSlot);
        assertTrue(slot.isEmpty());
        assertEquals(0, inventory.currentSelectedSlot());

        engine.select(inventory, Action.ATTACK, wool);
        assertEquals(1, inventory.currentSelectedSlot());

        engine.select(inventory, Action.INTERACT, wool);
        assertEquals(4, inventory.currentSelectedSlot());

        engine.select(inventory, Action.STAT_CHANGE, wool);
        assertEquals(6, inventory.currentSelectedSlot());

        var inv = new DummyInventory(
                new DummyTypes.DummyTool("shear"),
                new DummyTypes.DummyTool("shears", Set.of(
                        new DummyTypes.DummyEnchantment("bobby")
                )),
                new DummyTypes.DummyTool("ax"),
                new DummyTypes.DummyTool("ax", Set.of(
                )),
                new DummyTypes.DummyTool("shovel", Set.of(
                        new DummyTypes.DummyEnchantment("meh")
                )),
                new DummyTypes.DummyTool("pickaxe"),
                new DummyTypes.DummyTool("pickaxe", Set.of(
                        new DummyTypes.DummyEnchantment("robert")
                ))
        );

        engine.select(inv, Action.ATTACK, wool);
        assertEquals(1, inv.currentSelectedSlot());
    }

    /**
     * Test if a # prefix will define a group
     */
    @Test
    void hashPrefix() {
        var woolTag = FutureSelectable.getOrCreate("wool", DummyTypes.BLOCK_TYPE, true);
        var interactions = autoSwitchConfig.getConfiguration().get(Action.INTERACT);
        assertTrue(interactions.containsKey(new Selector(10, new IdSelector(woolTag, null))));
    }
}
