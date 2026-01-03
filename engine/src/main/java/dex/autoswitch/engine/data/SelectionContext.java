package dex.autoswitch.engine.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import dex.autoswitch.engine.Action;
import dex.autoswitch.engine.Matcher;
import dex.autoswitch.engine.SelectionEngine;

/**
 * Provides the environmental context for a selection matching operation.
 * <p>
 * This context is passed through the {@link SelectionEngine} to all {@link Matcher}s.
 * It carries the primary {@link Action} and target, as well as a map of type-safe {@link #attachments()}
 * for carrying arbitrary data like positions, entities, or players without coupling the engine to those types.
 *
 * @param action      The action currently being performed
 * @param target      The current target of the {@code action}
 * @param attachments Extra data to provide further context in which the selection is occurring
 */
public record SelectionContext(Action action, Object target, Map<ContextKey<?>, Object> attachments) {
    /**
     * Constructs a new {@link SelectionContext} with the given action, target, and attachment entries.
     *
     * @param action            the primary {@link Action} being performed
     * @param target            the target object associated with the selection context
     * @param attachmentEntries the variable-length array of key-value pairs, each represented as a
     *                          {@link Map.Entry}, to be attached to the context
     */
    @SafeVarargs
    public SelectionContext(Action action, Object target, Map.Entry<ContextKey<?>, Object>... attachmentEntries) {
        this(action, target, Map.ofEntries(attachmentEntries));
    }

    /**
     * Retrieves an attachment wrapped in an {@link Optional}.
     */
    public <T> Optional<T> getOptional(ContextKey<T> key) {
        return Optional.ofNullable(get(key));
    }

    /**
     * Retrieves an attachment.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(ContextKey<T> key) {
        return (T) attachments.get(key);
    }

    /**
     * Returns a new {@link SelectionContext} instance by merging the current attachments with the provided additional data.
     *
     * @param extraData a map of additional attachments to be included in the new context
     * @return a new {@link SelectionContext} with the merged attachments
     */
    public SelectionContext with(Map<ContextKey<?>, Object> extraData) {
        var newAttachments = new HashMap<>(attachments);
        newAttachments.putAll(extraData);
        return new SelectionContext(action, target, Collections.unmodifiableMap(newAttachments));
    }

    /**
     * Returns a new {@link SelectionContext} instance by adding a single key-value attachment to the current context.
     *
     * @param <T>   the type of the value being added to the context
     * @param key   the {@link ContextKey} representing the key for the value
     * @param value the value to associate with the provided key
     * @return a new {@link SelectionContext} containing all current attachments with the addition of the specified key-value pair
     */
    public <T> SelectionContext with(ContextKey<T> key, T value) {
        var newAttachments = new HashMap<>(attachments);
        newAttachments.put(key, value);
        return new SelectionContext(action, target, Map.copyOf(newAttachments));
    }
}
