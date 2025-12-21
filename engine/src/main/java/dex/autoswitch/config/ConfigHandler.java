package dex.autoswitch.config;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.logging.Logger;

import dex.autoswitch.config.codecs.DataMapCodec;
import dex.autoswitch.config.codecs.ExpressionTreeCodec;
import dex.autoswitch.config.codecs.IdSelectorCodec;
import dex.autoswitch.config.data.tree.DataMap;
import dex.autoswitch.config.data.tree.ExpressionTree;
import dex.autoswitch.config.data.tree.IdSelector;
import dex.autoswitch.config.transformations.Transformations;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;

public class ConfigHandler {
    public static final Logger LOGGER = Logger.getLogger("AutoSwitch-Config");

    public static HoconConfigurationLoader createLoader(Path path) {
        return HoconConfigurationLoader.builder()
                .path(path)
                .prettyPrinting(true)
                .emitComments(true)
                .defaultOptions(opts -> opts.serializers(build -> {
                    build.register(IdSelector.class, IdSelectorCodec.INSTANCE);
                    build.register(ExpressionTree.class, ExpressionTreeCodec.INSTANCE);
                    build.register(DataMap.class, DataMapCodec.INSTANCE);
                }))
                .build();
    }

    public static HoconConfigurationLoader createLoader(URL uri) {
        return HoconConfigurationLoader.builder()
                .url(uri)
                .prettyPrinting(true)
                .emitComments(true)
                .defaultOptions(opts -> opts.serializers(build -> {
                    build.register(IdSelector.class, IdSelectorCodec.INSTANCE);
                    build.register(ExpressionTree.class, ExpressionTreeCodec.INSTANCE);
                    build.register(DataMap.class, DataMapCodec.INSTANCE);
                }))
                .build();
    }

    public static AutoSwitchConfig readConfiguration(Path path) throws ConfigurateException {
        var loader = createLoader(path);

        var root = Transformations.updateNode(loader.load());
        return root.get(AutoSwitchConfig.class);
    }

    public static AutoSwitchConfig readConfiguration(URL url) throws ConfigurateException {
        var loader = createLoader(url);

        var root = Transformations.updateNode(loader.load());
        return root.get(AutoSwitchConfig.class);
    }

    public static AutoSwitchConfigReference readDynamicConfiguration(Path path, URL ref) throws IOException {
        updateConfig(path);
        return new AutoSwitchConfigReference(path, ref);
    }

    private static void updateConfig(Path path) throws ConfigurateException {
        var loader = createLoader(path);

        var root = Transformations.updateNode(loader.load());
        loader.save(root);
    }
}
