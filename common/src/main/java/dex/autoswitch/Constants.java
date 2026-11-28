package dex.autoswitch;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.UnaryOperator;

import dex.autoswitch.config.AutoSwitchConfig;
import dex.autoswitch.config.AutoSwitchConfigReference;
import dex.autoswitch.config.ConfigHandler;
import dex.autoswitch.engine.events.Scheduler;
import dex.autoswitch.engine.state.SwitchState;
import dex.autoswitch.platform.Services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.ConfigurateException;

public class Constants {
    public static final String MOD_ID = "autoswitch";
    public static final String MOD_NAME = "AutoSwitch";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);
    private static final Path CONFIG_PATH = Services.PLATFORM.getConfigDir().resolve("autoswitch.conf");
    private static AutoSwitchConfigReference CONFIG_REF;
    public static AutoSwitchConfig CONFIG = loadConfig();
    public static final Scheduler SCHEDULER = new Scheduler();
    public static final SwitchState SWITCH_STATE = new SwitchState();
    public static boolean performSwitch = !CONFIG.featureConfig.disableOnStartup;

    private static AutoSwitchConfig loadConfig() {
        try {
            var defaultConfig = Objects.requireNonNull(Constants.class.getResource("/default.conf")).toURI().toURL();
            var ref = ConfigHandler.readDynamicConfiguration(CONFIG_PATH, defaultConfig);
            ref.addListener(c -> {
                CONFIG = c;
                LOG.info("Updated config reference");
            });
            CONFIG_REF = ref;
            return ref.config();
        } catch (IOException e) {
            LOG.error("Could not load autoswitch.conf", e);
        } catch (URISyntaxException e) {
            LOG.error("Could not load default.conf", e);
        }

        return new AutoSwitchConfig();
    }

    public static void reset() {
        performSwitch = !CONFIG.featureConfig.disableOnStartup;
        SWITCH_STATE.reset();
        SCHEDULER.reset();
    }

    /**
     * Mutate and persist the config to disk, for use by config GUIs (e.g. the ModMenu screen).
     * The supplied function receives the current config, mutates it in place (or returns a
     * replacement), and the result is written back to {@code autoswitch.conf}.
     */
    public static void updateConfig(UnaryOperator<AutoSwitchConfig> function) {
        if (CONFIG_REF == null) {
            LOG.error("Cannot update config: config reference was never initialized");
            return;
        }

        try {
            CONFIG_REF.updateConfig(function);
        } catch (ConfigurateException e) {
            LOG.error("Failed to save autoswitch.conf", e);
        }
    }
}
