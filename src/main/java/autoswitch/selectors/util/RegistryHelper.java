package autoswitch.selectors.util;

import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;

public final class RegistryHelper {
    private RegistryHelper() {
    }


    public static <T> boolean isDefaultEntry(Registry<T> registry, T entry) {
        if (entry == null) return false;
        if (registry instanceof DefaultedRegistry<T> defaultedRegistry) {
            return registry.get(defaultedRegistry.getDefaultId()).equals(entry);
        }

        return false;
    }

}
