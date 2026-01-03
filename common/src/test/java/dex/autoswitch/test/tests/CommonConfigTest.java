package dex.autoswitch.test.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;

import dex.autoswitch.Constants;
import dex.autoswitch.config.AutoSwitchConfig;
import dex.autoswitch.config.ConfigHandler;
import dex.autoswitch.config.data.tree.ExpressionTree;
import dex.autoswitch.config.data.tree.IdSelector;
import dex.autoswitch.config.data.tree.Intersection;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.ConfigurateException;

public class CommonConfigTest {
    @Test
    void roundTripTest() throws IOException, URISyntaxException {
        var config = loadDefaultConfig();
        var p = Path.of("configs", "roundTrip.conf");
        Files.deleteIfExists(p);
        writeConfig(config, p);
        var newConfig = getConfig(p);

        assertThat(newConfig)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(config);

        // Remove some entries to ensure the recursive comparison works
        assertTrue(config.interactAction.removeIf(t -> t.target instanceof Intersection));
        assertThat(config)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isNotEqualTo(newConfig);
    }

    @Test
    void testExplodedData() throws IOException, URISyntaxException {
        var ref = loadConfig("explodedDataRef");
        var exploded = loadConfig("explodedDataTest");
        assertThat(exploded).usingRecursiveComparison().ignoringCollectionOrder().isEqualTo(ref);
    }

    @Test
    // Tests if implicitly defined types for IdSelector work
    void elidedTypeSpecification() throws IOException, URISyntaxException {
        var ref = loadConfig("elidedTypeSpecificationRef");
        var elided = loadConfig("elidedTypeSpecification");
        assertThat(elided).usingRecursiveComparison().ignoringCollectionOrder().isEqualTo(ref);

        assertThat(ref.featureConfig.offhandSelectors)
                .areExactly(1, new Condition<>(sel -> {
                    if (sel.tool instanceof Intersection(Set<ExpressionTree> children)) {
                        for (ExpressionTree child : children) {
                            if (child instanceof IdSelector idSelector) {
                                if ("BLOCK".equals(idSelector.selectable().getSelectorType().id())) {
                                    return true;
                                }
                            }
                        }
                    }
                    return false;
                }, "with overridden type of `BLOCK`"));
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
