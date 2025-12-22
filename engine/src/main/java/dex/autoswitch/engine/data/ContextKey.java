package dex.autoswitch.engine.data;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A type-safe key for accessing attachments within a {@link SelectionContext}.
 */
public record ContextKey<T>(String id, Class<T> type) {
    private static final ConcurrentMap<String, ContextKey<?>> INTERNER = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> ContextKey<T> create(String id, Class<T> type) {
        var key = (ContextKey<T>) INTERNER.computeIfAbsent(id, k -> new ContextKey<>(k, type));
        if (key.type != type) {
            throw new IllegalArgumentException("Type mismatch for key " + id);
        }
        return key;
    }

    public static <T> ContextKey<T> get(String id, Class<T> type) {
        return create(id, type);
    }
}
