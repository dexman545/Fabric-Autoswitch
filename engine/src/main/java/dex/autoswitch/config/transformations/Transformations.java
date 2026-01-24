package dex.autoswitch.config.transformations;

import static org.spongepowered.configurate.NodePath.path;

import java.util.Set;
import java.util.logging.Logger;

import dex.autoswitch.engine.Action;
import io.leangen.geantyref.TypeToken;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.transformation.ConfigurationTransformation;

public final class Transformations {
    private static final Logger LOGGER = Logger.getLogger("AutoSwitch Config");
    private static final int VERSION_LATEST = 3;

    private Transformations() {}

    /**
     * Create a new builder for versioned configurations. This builder uses a
     * field in the node (by default {@code schema-version}) to determine the
     * current schema version (using -1 for no version present).
     *
     * @return versioned transformation
     */
    public static ConfigurationTransformation.Versioned create() {
        return ConfigurationTransformation.versionedBuilder()
                .versionKey("config-version")
                //.addVersion(VERSION_LATEST, oneToTwo())
                .addVersion(2, transformSwitchAwayFromTools())
                .addVersion(3, transformSkipDepletedItems())
                .build();
    }

    /**
     * Apply the transformations to a node.
     *
     * <p>This method also prints information about the version update that
     * occurred</p>
     *
     * @param node the node to transform
     * @param <N>  node type
     * @return provided node, after transformation
     */
    public static <N extends ConfigurationNode> N updateNode(final N node) throws ConfigurateException {
        if (!node.virtual()) { // we only want to migrate existing data
            final ConfigurationTransformation.Versioned trans = create();
            final int startVersion = trans.version(node);
            trans.apply(node);
            final int endVersion = trans.version(node);
            if (startVersion != endVersion) { // we might not have made any changes
                LOGGER.info("Updated config schema from " + startVersion + " to " + endVersion);
            }
        }
        return node;
    }

    /**
     * Migrates {@code feature-config.switch-away-from-tools} from a {@code boolean} to a {@code Set<Action>}.
     */
    public static ConfigurationTransformation transformSwitchAwayFromTools() {
        return ConfigurationTransformation.builder()
                .addAction(path("feature-config", "switch-away-from-tools"), ((path, value) -> {
                    if (value.getBoolean(true)) {
                        value.set(new TypeToken<Set<Action>>(){}, Set.<Action>of(Action.ATTACK));
                    } else {
                        value.set(new TypeToken<Set<Action>>(){}, Set.<Action>of());
                    }

                    return null; // Don't move the value to a new path
                }))
                .build();
    }

    /**
     * Migrates {@code feature-config.skipDepletedItems} into two options.
     */
    public static ConfigurationTransformation transformSkipDepletedItems() {
        return ConfigurationTransformation.builder()
                .addAction(path("feature-config"), ((path, value) -> {
                    var old = value.node("skip-depleted-items").getBoolean(true);
                    var n = value.node("preserve-damaged-tools");
                    if (n.virtual()) {
                        n.set(old);
                        if (n instanceof CommentedConfigurationNode commentedConfigurationNode) {
                            commentedConfigurationNode.commentIfAbsent("If true, skip tools that are about to break.");
                        }
                    }
                    return null; // Don't move the value to a new path
                }))
                .build();
    }

}
