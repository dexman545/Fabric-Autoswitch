package autoswitch.selectors.util;

import net.minecraft.tag.TagKey;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;

public final class RegistryHelper {
    private RegistryHelper() {
    }

    public static <T> boolean isInTag(Registry<T> registry, TagKey<T> tagKey, T entry) {
        var maybeKey = registry.getKey(entry);
        return maybeKey.filter(registryKey -> registry.entryOf(registryKey).isIn(tagKey))
                       .isPresent();
    }

    public static <T> boolean isDefaultEntry(Registry<T> registry, T entry) {
        if (entry == null) return false;
        if (registry instanceof DefaultedRegistry<T> defaultedRegistry) {
            return registry.get(defaultedRegistry.getDefaultId()).equals(entry);
        }

        return false;
    }

}
