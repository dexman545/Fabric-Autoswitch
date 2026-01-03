package dex.autoswitch.config;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.NodePath;
import org.spongepowered.configurate.reactive.Subscriber;
import org.spongepowered.configurate.reference.ConfigurationReference;
import org.spongepowered.configurate.reference.ValueReference;
import org.spongepowered.configurate.reference.WatchServiceListener;

// See https://github.com/SpongePowered/Configurate/wiki/Transformations-and-Visitors for an updating config
public class AutoSwitchConfigReference {
    private static final Logger LOGGER = Logger.getLogger("AutoSwitch Config");
    private final Path path;
    private final WatchServiceListener listener;
    private final ConfigurationReference<@NotNull CommentedConfigurationNode> base;
    private final ValueReference<AutoSwitchConfig, @NotNull CommentedConfigurationNode> config;

    public AutoSwitchConfigReference(Path path, URL defaultConfig) throws IOException {
        this.path = path;
        this.listener = WatchServiceListener.create();
        this.base = listener.listenToConfiguration(ConfigHandler::createLoader, path);

        base.updates().subscribe($ -> LOGGER.info("Configuration automatically reloaded"));
        base.errors().subscribe(e -> {
            final Throwable thr = e.getValue();
            LOGGER.severe("Encountered error reading config, using current values or defaults!");
            LOGGER.severe("Unable to " + e.getKey() + " the configuration: " + thr.getMessage());
            if (thr.getCause() != null) {
                LOGGER.log(Level.SEVERE, "Cause", thr);
            }
        });

        // Load default options, merging them into the config
        base.get().mergeFrom(ConfigHandler.createLoader(defaultConfig).load());

        this.config = base.referenceTo(AutoSwitchConfig.class, NodePath.path(), new AutoSwitchConfig());

        base.save();
    }

    public AutoSwitchConfig config() {
        return config.get();
    }

    public void addListener(Subscriber<AutoSwitchConfig> subscriber) {
        config.subscribe(subscriber);
    }

    public void updateConfig(Function<AutoSwitchConfig, AutoSwitchConfig> function) throws ConfigurateException {
        config.update(function);
        base.save();
    }

    public void save() throws ConfigurateException {
        base.save();
    }
}
