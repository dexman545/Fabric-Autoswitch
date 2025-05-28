package dex.autoswitch.test.util;

import dex.autoswitch.config.AutoSwitchConfig;
import dex.autoswitch.config.ConfigHandler;
import dex.autoswitch.engine.SelectionEngine;
import dex.autoswitch.harness.DummyInventory;
import org.junit.jupiter.api.BeforeEach;
import org.spongepowered.configurate.ConfigurateException;

import java.nio.file.Path;

public abstract class AbstractSelectionTest {
    protected DummyInventory inventory;
    protected SelectionEngine engine;

    public abstract DummyInventory createInventory();

    public abstract SelectionEngine getEngine();

    @BeforeEach
    void setup() {
        inventory = createInventory();
        engine = getEngine();
    }

    protected AutoSwitchConfig loadConfig(String file) {
        try {
            return ConfigHandler.readConfiguration(Path.of("src", "test", "resources", "configs", file + ".conf"));
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }
}
