package dex.autoswitch.config.codecs;

import dex.autoswitch.config.ConfigHandler;
import dex.autoswitch.config.data.tree.*;
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

public class ExpressionTreeCodec implements TypeSerializer<ExpressionTree> {
    public static final ExpressionTreeCodec INSTANCE = new ExpressionTreeCodec();
    private static final String TYPE = "type";
    private static final String CHILD = "child";
    private static final TypeToken<Set<ExpressionTree>> EXPRESSION_SET = new TypeToken<>() {
    };

    private ExpressionTreeCodec() {
    }

    @Override
    public ExpressionTree deserialize(@NotNull Type type, ConfigurationNode node) throws SerializationException {
        if (node.hasChild(CHILD)) {
            var typeNode = node.node(TYPE);
            var childrenNode = node.node(CHILD);

            if (typeNode.virtual()) {
                ConfigHandler.LOGGER.warning(createLogMessage(node, "Failed to find type"));
                return null;
            }

            if (childrenNode.virtual()) {
                ConfigHandler.LOGGER.warning(createLogMessage(node, "Failed to find children"));
                return null;
            }

            var op = typeNode.get(OpType.class, OpType.AND);
            var children = childrenNode.get(EXPRESSION_SET);
            if (children == null) {
                ConfigHandler.LOGGER.warning(createLogMessage(node, "Failed to read children"));
                return null;
            }

            children.removeIf(Objects::isNull);

            return switch (op) {
                case XOR -> new DisjunctiveUnion(children);
                case AND -> new Union(children);
                case NOT -> {
                    if (children.size() == 1) {
                        for (ExpressionTree child : children) {
                            if (child == null) {
                                yield null;
                            }
                            yield new Invert(child);
                        }
                    }
                    ConfigHandler.LOGGER.warning(createLogMessage(node, "Inversions cannot have more than one child"));
                    yield null;
                }
                case OR -> new Intersection(children);
                case null -> throw new SerializationException("Could not find operator");
            };
        }

        return IdSelectorCodec.INSTANCE.deserialize(type, node);
    }

    @Override
    public void serialize(@NotNull Type type, @Nullable ExpressionTree obj, @NotNull ConfigurationNode node) throws SerializationException {
        switch (obj) {
            case DisjunctiveUnion disjunctiveUnion -> {
                node.node(TYPE).set(OpType.XOR);
                node.node(CHILD).set(disjunctiveUnion.children());
            }
            case IdSelector idSelector -> {
                node.set(idSelector);
            }
            case Intersection intersection -> {
                node.node(TYPE).set(OpType.OR);
                node.node(CHILD).set(intersection.children());
            }
            case Invert invert -> {
                node.node(TYPE).set(OpType.NOT);
                node.node(CHILD).set(invert.child());
            }
            case Union union -> {
                node.node(TYPE).set(OpType.AND);
                node.node(CHILD).set(union.children());
            }
            case null -> node.raw(null);
        }
    }

    private static String createLogMessage(ConfigurationNode node, String message) {
        return "Error reading config at: " + node.path() + "\nError: " + message;
    }

    private ConfigurationNode nonVirtualNode(final ConfigurationNode source, final Object... path) throws SerializationException {
        if (!source.hasChild(path)) {
            throw new SerializationException("Required field " + Arrays.toString(path) + " was not present in node");
        }
        return source.node(path);
    }

    private enum OpType {
        XOR,
        AND,
        NOT,
        OR,
    }
}
