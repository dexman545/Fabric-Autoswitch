package autoswitch.selectors.util;

import autoswitch.AutoSwitch;
import autoswitch.selectors.futures.FutureRegistryEntry;
import autoswitch.selectors.futures.FutureTargetEntry;

import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;

public final class RegistryHelper {
    private RegistryHelper() {
    }

    public static void revalidateFutureEntries() {
        //todo don't revalidate them, just make them all invalid?
        // would likely work better with polymer
        FutureRegistryEntry.forceRevalidateEntries();
        FutureTargetEntry.forceRevalidateEntries();
        // Rehashes the maps to allow for the new entries to behave well
        AutoSwitch.switchData.target2AttackActionToolSelectorsMap.trim();
        AutoSwitch.switchData.target2UseActionToolSelectorsMap.trim();
    }

    public static <T> boolean isInTag(Registry<T> registry, TagKey<T> tagKey, T entry) {
        var maybeKey = registry.getKey(entry);
        return maybeKey.filter(registryKey -> registry.entryOf(registryKey).isIn(tagKey))
                       .isPresent();
    }

    public static <T> boolean isDefaultEntry(Registry<T> registry, T entry, Identifier id) {
        if (entry == null) return false;
        if (registry instanceof DefaultedRegistry<T> defaultedRegistry) {
            if (defaultedRegistry.getDefaultId().equals(id)) return false;
            return registry.get(defaultedRegistry.getDefaultId()).equals(entry);
        }

        return false;
    }

}
