package dex.autoswitch.config.codecs;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import dex.autoswitch.config.ConfigHandler;
import dex.autoswitch.config.data.tree.IdSelector;
import dex.autoswitch.config.data.tree.TypedData;
import dex.autoswitch.engine.data.SwitchRegistry;
import dex.autoswitch.engine.data.extensible.SelectableType;
import dex.autoswitch.futures.FutureSelectable;
import dex.autoswitch.futures.FutureSelectableGroup;
import dex.autoswitch.futures.FutureSelectableValue;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

public final class IdSelectorCodec implements TypeSerializer<IdSelector> {
    public static final IdSelectorCodec INSTANCE = new IdSelectorCodec();
    private static final String TYPE = "type";
    private static final String ID = "id";
    private static final String TAG = "tag";
    private static final String DATA = "data";
    private static final Set<String> KEYS = Set.of(TYPE, ID, TAG, DATA);

    private IdSelectorCodec() {
    }

    @Override
    public IdSelector deserialize(@NotNull Type type, @NotNull ConfigurationNode node) throws SerializationException {
        var idNode = node.node(ID);
        var tagNode = node.node(TAG);
        var dataNode = node.node(DATA);

        if (idNode.virtual()) {
            ConfigHandler.LOGGER.warning(createLogMessage(node, "Failed to find ID"));
            return null;
        }

        SelectableType<Object, Object, Object> selType;
        try {
            selType = getSelectorType(node);
        } catch (SerializationException e) {
            ConfigHandler.LOGGER.warning(createLogMessage(node, "Failed to find type"));
            return null;
        } catch (IllegalArgumentException e) {
            ConfigHandler.LOGGER.warning(createLogMessage(node, "Encountered unknown type: " + node.node(TYPE).getString()));
            return null;
        }

        var idStr = idNode.getString();

        if (idStr == null) {
            throw new SerializationException("Null string id");
        }

        var hasPrefix = idStr.startsWith("#");
        if (hasPrefix) {
            idStr = idStr.substring(1);
        }

        var isTag = false;
        if (tagNode.virtual()) {
            isTag = hasPrefix;
        } else {
            // Calling #getBoolean make #virtual return false
            isTag = tagNode.getBoolean();
        }

        Object k;
        try {
            k = selType.deserializeKey(idStr);
        } catch (Exception e) {
            ConfigHandler.LOGGER.warning(createLogMessage(node, "Invalid id: " + idStr));
            return null;
        }

        Set<TypedData<?>> data = new HashSet<>();

        // If data {} exists and exploded data is present, read only from the explicitly marked data node
        if (dataNode.isMap()) {
            if (node.isMap()) {
                // If both data{} and exploded keys are present, warn the user.
                for (Map.Entry<Object, ? extends ConfigurationNode> entry : node.childrenMap().entrySet()) {
                    if (entry.getKey() instanceof String kName && !KEYS.contains(kName)) {
                        ConfigHandler.LOGGER.warning(createLogMessage(node, """
                                Both 'data' block and exploded data keys present; \
                                'data' block will take precedence. Found exploded key:\s""" + kName));
                        break;
                    }
                }
            }

            var children = dataNode.childrenMap();
            for (Map.Entry<Object, ? extends ConfigurationNode> entry : children.entrySet()) {
                if (entry.getKey() instanceof String key) {
                    try {
                        var dataType = SwitchRegistry.INSTANCE.getDataType(key);
                        data.add(new TypedData<>(dataType, entry.getValue().get(dataType.getSupportedData())));
                    } catch (IllegalArgumentException e) {
                        // Unknown data type id inside data{}, skip
                        ConfigHandler.LOGGER.warning(createLogMessage(node,
                                "Unknown data key in 'data' block: " + key + " - skipping."));
                    } catch (Exception e) {
                        throw new SerializationException(e);
                    }
                } else {
                    throw new SerializationException("Unknown key type " + entry.getKey());
                }
            }
        } else {
            // Exploded data - data not in a data{} object
            if (node.isMap()) {
                var children = node.childrenMap();
                for (Map.Entry<Object, ? extends ConfigurationNode> entry : children.entrySet()) {
                    if (entry.getKey() instanceof String key) {
                        if (KEYS.contains(key)) {
                            continue; // Skip IdSelector-specific keys
                        }
                        try {
                            var dataType = SwitchRegistry.INSTANCE.getDataType(key);
                            data.add(new TypedData<>(dataType, entry.getValue().get(dataType.getSupportedData())));
                        } catch (IllegalArgumentException e) {
                            // Unknown data type id in exploded data, skip
                            ConfigHandler.LOGGER.warning(createLogMessage(node,
                                    "Unknown data key in exploded 'data': " + key + " - skipping."));
                        } catch (Exception e) {
                            throw new SerializationException(e);
                        }
                    } else {
                        throw new SerializationException("Unknown key type " + entry.getKey());
                    }
                }
            }
        }

        return new IdSelector(FutureSelectable.getOrCreate(k, selType, isTag), data);
    }

    @Override
    public void serialize(@NotNull Type type, @Nullable IdSelector selector, @NotNull ConfigurationNode node) throws SerializationException {
        if (selector == null) {
            node.raw(null);
            return;
        }

        var selType = getSelectorType(selector);
        node.node(TYPE).set(selector.selectable().getSelectorType().id());
        node.node(ID).set(selType.serializeKey(selector.selectable().getKey()));

        switch (selector.selectable()) {
            case FutureSelectableGroup<?, ?, ?> v -> {
                node.node(TAG).set(true);
            }
            case FutureSelectableValue<?, ?> v -> {
            }
        }

        if (selector.data() != null && !selector.data().isEmpty()) {
            for (TypedData<?> typedData : selector.data()) {
                node.node(DATA).node(typedData.type().id().toLowerCase(Locale.ENGLISH)).set(typedData.data());
            }
        }
    }

    private <K, V, G> SelectableType<K, V, G> getSelectorType(ConfigurationNode source) throws SerializationException {
        var typeNode = source.node(TYPE);
        if (!typeNode.virtual()) {
            return getSelectorType(typeNode.getString());
        }

        // Type not specified, try to guess from the configuration path
        if (!source.virtual()) {
            ConfigurationNode p = source;
            while (p != null && !p.virtual()) {
                switch (p.key()) {
                    case String s -> {
                        switch (s) {
                            case "tools" -> {
                                return getSelectorType("item");
                            }
                            case "enchantments" -> {
                                return getSelectorType("enchantment");
                            }
                        }
                    }
                    case null, default -> {}
                }

                p = p.parent();
            }
        }

        throw new SerializationException("Required field " + TYPE + " was not present in node");
    }

    private <K, V, G> SelectableType<K, V, G> getSelectorType(IdSelector selector) {
        //noinspection unchecked
        return (SelectableType<K, V, G>) selector.selectable().getSelectorType();
    }

    private <K, V, G> SelectableType<K, V, G> getSelectorType(String id) {
        //noinspection unchecked
        return (SelectableType<K, V, G>) SwitchRegistry.INSTANCE.getSelectableType(id);
    }

    private ConfigurationNode nonVirtualNode(final ConfigurationNode source, final Object... path) throws SerializationException {
        if (!source.hasChild(path)) {
            throw new SerializationException("Required field " + Arrays.toString(path) + " was not present in node");
        }
        return source.node(path);
    }

    private static String createLogMessage(ConfigurationNode node, String message) {
        return "Error reading config at: " + node.path() + "\nError: " + message;
    }
}
