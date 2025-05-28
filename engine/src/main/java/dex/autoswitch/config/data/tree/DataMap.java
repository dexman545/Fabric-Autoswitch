package dex.autoswitch.config.data.tree;

import java.util.Set;

/**
 * Represents a map. Implicit AND operation for all entries in the map.
 */
//todo if in future we want more operations, implement as new types on Data
public sealed interface DataMap extends Data {
    record Pair(String key, DataMap value) implements DataMap {}
    record Value(String value) implements DataMap {}
    record Map(Set<? extends DataMap> entries) implements DataMap {}
}
