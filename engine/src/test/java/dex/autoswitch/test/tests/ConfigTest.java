package dex.autoswitch.test.tests;

import dex.autoswitch.config.AutoSwitchConfig;
import dex.autoswitch.config.ConfigHandler;
import dex.autoswitch.config.data.tree.IdSelector;
import dex.autoswitch.config.data.tree.Intersection;
import dex.autoswitch.config.data.tree.TypedData;
import dex.autoswitch.engine.Action;
import dex.autoswitch.engine.Selector;
import dex.autoswitch.engine.data.extensible.SelectableType;
import dex.autoswitch.futures.FutureSelectable;
import dex.autoswitch.harness.DummyTypes;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.ConfigurateException;

import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConfigTest {
    @Test
    @Disabled
    public void more() throws ConfigurateException {
        var loader = ConfigHandler.createLoader(Path.of("meh.conf"));
        var node = loader.load();

        var config = node.get(AutoSwitchConfig.class);
        //node.set(config);

        loader.save(node);

        //System.out.println(config.getConfiguration());

        //System.out.println(config.attackAction);
    }

    @Test
    void readConfigTypes() {
        var config = loadConfig("configTypes");
        var map = config.getConfiguration();
        var attack = map.get(Action.ATTACK);

        //System.out.println(attack);

        var woolBlockSelector = new Selector(10, makeIdSelector("wool", DummyTypes.BLOCK_TYPE, true));
        var woolTools = attack.get(woolBlockSelector);
        assertNotNull(woolTools);
        assertTrue(woolTools.contains(
                new Selector(0,
                        makeIdSelector("pickaxe", DummyTypes.ITEM_TYPE, false,
                                new TypedData(DummyTypes.ENCHANTMENTS, makeIdSelector("bob", DummyTypes.ENCHANTMENT_TYPE, false))))));
        assertTrue(woolTools.contains(
                new Selector(-1, makeIdSelector("shears", DummyTypes.ITEM_TYPE, false,
                        new TypedData(DummyTypes.ENCHANTMENTS, new Intersection(Set.of(
                                makeIdSelector("bob", DummyTypes.ENCHANTMENT_TYPE, true),
                                makeIdSelector("bobby", DummyTypes.ENCHANTMENT_TYPE, false)
                        )))))));
    }

    protected AutoSwitchConfig loadConfig(String file) {
        try {
            return ConfigHandler.readConfiguration(Path.of("src", "test", "resources", "configs", file + ".conf"));
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }

    protected <T> IdSelector makeIdSelector(T id, SelectableType<T, ?, ?> type, boolean isGroup, TypedData... data) {
        return new IdSelector(FutureSelectable.getOrCreate(id, type, isGroup), Set.of(data));
    }
}
