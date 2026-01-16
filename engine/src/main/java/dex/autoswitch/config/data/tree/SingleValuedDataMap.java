package dex.autoswitch.config.data.tree;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public record SingleValuedDataMap<K, V>(@Setting(nodeFromParent = true) Map<K, V> map) implements Data {
    public SingleValuedDataMap {
        map = Collections.unmodifiableMap(map);
    }

    public String prettyPrint(int level) {
        return map.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining("\n" + " ".repeat(level)));
    }
}
