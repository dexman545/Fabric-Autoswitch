package dex.autoswitch.config.codecs;

import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.util.HashSet;
import java.util.Locale;

import dex.autoswitch.config.ConfigHandler;
import dex.autoswitch.config.data.tree.ValueCondition;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

public final class ValueConditionCodec<T extends Comparable<T>> implements TypeSerializer.Annotated<ValueCondition<T>> {
    @SuppressWarnings("rawtypes")
    public static final ValueConditionCodec INSTANCE = new ValueConditionCodec();

    private ValueConditionCodec() {}

    @Override
    public ValueCondition<T> deserialize(@NonNull AnnotatedType type, @NonNull ConfigurationNode node) throws SerializationException {
        // Extract the T from ValueCondition<T>
        if (!(type instanceof AnnotatedParameterizedType parameterizedType)) {
            throw new SerializationException("Type " + type + " is not a parameterized type");
        }
        var elementType = parameterizedType.getAnnotatedActualTypeArguments()[0];

        var conditions = new HashSet<ValueCondition.Condition<T>>();

        if (node.isMap()) {
            for (var entry : node.childrenMap().entrySet()) {
                var k = entry.getKey();
                var v = entry.getValue();
                if (k instanceof String name) {
                    try {
                        var c = ValueCondition.Comparator.valueOf(name.toUpperCase(Locale.ENGLISH));
                        @SuppressWarnings("unchecked")
                        var val = (T) v.get(elementType);
                        if (val != null) {
                            conditions.add(new ValueCondition.Condition<>(c, val));
                        }
                    } catch (IllegalArgumentException e) {
                        ConfigHandler.LOGGER.warning("Invalid comparison operator '" + name + "' at: " + v.path());
                    }
                }
            }
        } else if (!node.virtual()) { // Handle the case where it's just a raw value (EQUALS shorthand)
            @SuppressWarnings("unchecked")
            var val = (T) node.get(elementType);
            if (val != null) {
                conditions.add(new ValueCondition.Condition<>(ValueCondition.Comparator.EQUALS, val));
            }
        }

        return new ValueCondition<>(conditions);
    }

    @Override
    public void serialize(@NonNull AnnotatedType type, @Nullable ValueCondition<T> valueCondition, @NonNull ConfigurationNode node) throws SerializationException {
        if (valueCondition == null) {
            node.raw(null);
            return;
        }

        if (valueCondition.conditions().size() == 1) {
            var condition = valueCondition.conditions().toArray(ValueCondition.Condition[]::new)[0];
            if (condition.comparator() == ValueCondition.Comparator.EQUALS) {
                node.set(condition.value());
                return;
            }
        }

        for (ValueCondition.Condition<?> condition : valueCondition.conditions()) {
            if (condition.comparator() != ValueCondition.Comparator.INVALID) {
                node.node(condition.comparator()).set(condition.value());
            }
        }
    }
}
