package dex.autoswitch.futures;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import dex.autoswitch.engine.data.SelectionContext;
import dex.autoswitch.engine.data.extensible.SelectableType;

/**
 * Represents an Object that may not yet be instantiated, or a group of Objects.
 * @param <KEY> The type of {@code key} used to look up these Objects.
 * @param <TYPE> The type of the Object that may be selectable
 */
public sealed abstract class FutureSelectable<KEY, TYPE> permits FutureSelectableValue, FutureSelectableGroup {
    protected static final Map<Key<?>, FutureSelectable<?, ?>> INSTANCES = new HashMap<>();
    protected final KEY key;
    protected final SelectableType<KEY, TYPE, ?> selectableType;
    protected Status status = Status.UNVERIFIED;

    public FutureSelectable(KEY key, SelectableType<KEY, TYPE, ?> type) {
        this.key = Objects.requireNonNull(key, "key must not be null");
        this.selectableType = Objects.requireNonNull(type, "type must not be null");
    }

    public static void invalidateValues() {
        INSTANCES.forEach((i, future) -> future.status = Status.UNVERIFIED);
    }

    @SuppressWarnings("unchecked")
    public static <K, T, G> FutureSelectable<K, T> getOrCreate(K key, SelectableType<K, T, G> type, boolean isGroup) {
        var k = new Key<>(key, type, isGroup);

        if (isGroup) {
            INSTANCES.putIfAbsent(k, new FutureSelectableGroup<>(key, type));
            return (FutureSelectableGroup<K, T, G>) INSTANCES.get(k);
        } else {
            INSTANCES.putIfAbsent(k, new FutureSelectableValue<>(key, type));
            return (FutureSelectableValue<K, T>) INSTANCES.get(k);
        }
    }

    public abstract boolean matches(SelectionContext context, Object o);

    public abstract void validate();

    public double rating(SelectionContext context, Object selectable) {
        return selectableType.typeRating(context, this, selectable);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof FutureSelectable<?, ?> that)) {
            return false;
        }

        if (!getClass().equals(o.getClass())) {
            return false;
        }

        return key.equals(that.key) && selectableType.equals(that.selectableType);
    }

    public KEY getKey() {
        return key;
    }

    public SelectableType<KEY, TYPE, ?> getSelectorType() {
        return selectableType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), key, selectableType);
    }

    protected enum Status {
        UNVERIFIED,
        VALID,
        INVALID
    }

    public record Key<K>(K key, SelectableType<K, ?, ?> type, boolean isGroup) {}
}
