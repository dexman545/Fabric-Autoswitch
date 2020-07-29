package autoswitch.config.util;

import java.util.*;

public final class SortedProperties extends Properties {
    private static final Comparator<Map.Entry<Object, Object>> KEY_COMPARATOR =
            Comparator.comparing(entry -> entry.getKey().toString());

    public SortedProperties(final Properties delegate) {
        this.putAll(delegate);
    }

    public SortedProperties() {
        super();
    }

    @Override
    public synchronized Enumeration<Object> keys() {
        return Collections.enumeration(new TreeSet<>(super.keySet()));
    }

    @Override
    public Set<Object> keySet() {
        return new TreeSet<>(super.keySet());
    }

    @Override
    public Set<Map.Entry<Object, Object>> entrySet() {
        final Set<Map.Entry<Object, Object>> sorted = new TreeSet<>(KEY_COMPARATOR);
        sorted.addAll(super.entrySet());
        return sorted;
    }
}
