package dex.autoswitch.config.codecs;

import dex.autoswitch.config.data.tree.DataMap;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.*;

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
                    System.out.println("Unknown key type in DataMap" + entry.getKey());
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

        switch (dataMap) {
            case DataMap.Map(var entries) -> node.set(entries);
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
