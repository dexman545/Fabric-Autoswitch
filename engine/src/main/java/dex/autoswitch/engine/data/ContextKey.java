package dex.autoswitch.engine.data;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import dex.autoswitch.engine.state.SwitchContext;

/**
 * A type-safe key for accessing values within a {@link SelectionContext} or {@link SwitchContext}.
 * <p>
 * Keys are interned by their {@link #id()}. This ensures that multiple components can access the same 
 * context data by using the same string ID, while maintaining type safety through the {@link #type()} class.
 *
 * @param <T>  the type of value associated with this key
 * @param id   the unique identifier for this key
 * @param type the class representing the type of the value
 */
public record ContextKey<T>(String id, Class<T> type) {
    private static final ConcurrentMap<String, ContextKey<?>> INTERNER = new ConcurrentHashMap<>();

    /**
     * Creates or retrieves an interned type-safe key.
     *
     * @param id   the unique identifier for this key
     * @param type the class type of the value
     * @param <T>  the type of the value
     * @return the interned ContextKey instance
     * @throws IllegalArgumentException if a key with the same ID but a different type already exists
     */
    @SuppressWarnings("unchecked")
    public static <T> ContextKey<T> create(String id, Class<T> type) {
        var key = (ContextKey<T>) INTERNER.computeIfAbsent(id, k -> new ContextKey<>(k, type));
        if (key.type != type) {
            throw new IllegalArgumentException("Type mismatch for key " + id);
        }
        return key;
    }

    /**
     * Creates or retrieves an interned type-safe key.
     *
     * @param id   the unique identifier for this key
     * @param type the class type of the value
     * @param <T>  the type of the value
     * @return the interned ContextKey instance
     * @throws IllegalArgumentException if a key with the same ID but a different type already exists
     */
    public static <T> ContextKey<T> get(String id, Class<T> type) {
        return create(id, type);
    }
}
