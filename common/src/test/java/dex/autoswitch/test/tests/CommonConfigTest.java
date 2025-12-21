package dex.autoswitch.test.tests;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import dex.autoswitch.Constants;
import dex.autoswitch.config.AutoSwitchConfig;
import dex.autoswitch.config.ConfigHandler;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.ConfigurateException;

public class CommonConfigTest {
    @Test
    @Disabled("Broken due to classloading on these old versions")
    void roundTripTest() throws IOException, URISyntaxException {
        var config = loadDefaultConfig();
        var p = Path.of("configs", "roundTrip.conf");
        Files.deleteIfExists(p);
        writeConfig(config, p);
        var newConfig = getConfig(p);

        assertThat(newConfig).usingRecursiveComparison().isEqualTo(config);
    }

    @Test
    @Disabled("Broken due to classloading on these old versions")
    void testExplodedData() throws IOException, URISyntaxException {
        var ref = loadConfig("explodedDataRef");
        var exploded = loadConfig("explodedDataTest");
        assertThat(exploded).usingRecursiveComparison().isEqualTo(ref);
    }

    protected AutoSwitchConfig loadDefaultConfig() throws URISyntaxException, MalformedURLException {
        return getConfig(Objects.requireNonNull(Constants.class.getResource("/default.conf")).toURI().toURL());
    }

    protected AutoSwitchConfig loadConfig(String file) throws URISyntaxException, MalformedURLException {
        return getConfig(Objects.requireNonNull(Constants.class.getResource("/configs/" + file + ".conf")).toURI().toURL());
    }

    private static AutoSwitchConfig getConfig(Path path) {
        try {
            return ConfigHandler.readConfiguration(path);
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }

    private static AutoSwitchConfig getConfig(URL url) {
        try {
            return ConfigHandler.readConfiguration(url);
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeConfig(AutoSwitchConfig config, Path path) throws ConfigurateException {
        var loader = ConfigHandler.createLoader(path);
        var root = loader.load().set(AutoSwitchConfig.class, config);
        loader.save(root);
    }
}
