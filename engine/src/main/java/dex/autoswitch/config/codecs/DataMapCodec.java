package dex.autoswitch.config.codecs;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import dex.autoswitch.config.ConfigHandler;
import dex.autoswitch.config.data.tree.DataMap;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

public final class DataMapCodec implements TypeSerializer<DataMap> {
    public static final DataMapCodec INSTANCE = new DataMapCodec();

    private DataMapCodec() {
    }

    @Override
    public DataMap deserialize(@NotNull Type type, ConfigurationNode node) throws SerializationException {
        if (node.isMap()) {
            var entries = new HashSet<DataMap.Pair>();
            for (Map.Entry<Object, ? extends ConfigurationNode> entry : node.childrenMap().entrySet()) {
                if (entry.getKey() instanceof String key) {
                    entries.add(new DataMap.Pair(key, entry.getValue().get(DataMap.class)));
                } else {
                    ConfigHandler.LOGGER.warning("Unknown key type in DataMap: " + entry.getKey());
                }
            }

            if (entries.size() == 1) {
                for (DataMap.Pair entry : entries) {
                    return entry;
                }
            } else {
                return new DataMap.Map(entries);
            }
        }

        if (node.isList()) {
            return new DataMap.Map(Set.copyOf(Objects.requireNonNull(node.getList(DataMap.class))));
        }

        return new DataMap.Value(node.getString());
    }

    @Override
    public void serialize(@NotNull Type type, @Nullable DataMap dataMap, @NotNull ConfigurationNode node) throws SerializationException {
        if (dataMap == null) {
            node.raw(null);
            return;
        }

        serialize(dataMap, node);
    }

    private void serialize(@NotNull DataMap dataMap, @NotNull ConfigurationNode node) throws SerializationException {
        switch (dataMap) {
            case DataMap.Map(var entries) -> {
                for (DataMap entry : entries) {
                    serialize(entry, node);
                }
            }
            case DataMap.Pair(var key, var val) -> node.node(key).set(val);
            case DataMap.Value(var val) -> node.set(String.class, val);
        }
    }

    private ConfigurationNode nonVirtualNode(final ConfigurationNode source, final Object... path) throws SerializationException {
        if (!source.hasChild(path)) {
            throw new SerializationException("Required field " + Arrays.toString(path) + " was not present in node");
        }
        return source.node(path);
    }
}
