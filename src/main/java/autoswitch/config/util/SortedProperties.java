package autoswitch.config.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

// Version of Properties that is sorted
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
    public Set<String> stringPropertyNames() {
        return new TreeSet<>(super.stringPropertyNames());
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
