package dex.autoswitch.engine.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import dex.autoswitch.engine.Action;

public record SelectionContext(
    Action action, 
    Object target, 
    Map<ContextKey<?>, Object> attachments
) {
    @SafeVarargs
    public SelectionContext(Action action, Object target, Map.Entry<ContextKey<?>, Object>... attachmentEntries) {
        this(action, target, Map.ofEntries(attachmentEntries));
    }

    public <T> Optional<T> getOptional(ContextKey<T> key) {
        return Optional.ofNullable(get(key));
    }

    @SuppressWarnings("unchecked")
    public <T> T get(ContextKey<T> key) {
        return (T) attachments.get(key);
    }

    public SelectionContext with(Map<ContextKey<?>, Object> extraData) {
        var newAttachments = new HashMap<>(attachments);
        newAttachments.putAll(extraData);
        return new SelectionContext(action, target, Collections.unmodifiableMap(newAttachments));
    }

    public <T> SelectionContext with(ContextKey<T> key, T value) {
        var newAttachments = new HashMap<>(attachments);
        newAttachments.put(key, value);
        return new SelectionContext(action, target, Map.copyOf(newAttachments));
    }
}
