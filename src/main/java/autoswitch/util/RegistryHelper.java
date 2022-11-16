package autoswitch.util;

import autoswitch.AutoSwitch;
import autoswitch.actions.Action;
import autoswitch.selectors.futures.FutureRegistryEntry;
import autoswitch.selectors.futures.IdentifiedTag;

import org.jetbrains.annotations.Nullable;

import net.minecraft.util.Identifier;
import net.minecraft.registry.DefaultedRegistry;
import net.minecraft.registry.Registry;

public final class RegistryHelper {
    private RegistryHelper() {
    }

    public static void revalidateFutureEntries() {
        //todo don't revalidate them, just make them all invalid to be handled as they are checked?
        FutureRegistryEntry.forceRevalidateEntries();
        IdentifiedTag.refreshIdentifiers();
        // Rehashes the maps to allow for the new entries to behave well
        for (Action action : Action.values()) {
            action.getTarget2ToolSelectorsMap().trim();
        }
    }

    @Nullable
    public static <T> T getEntry(Registry<T> registry, Identifier id) {
        var entry = registry.get(id);
        if (!isDefaultEntry(registry, entry, id)) {
            return entry;
        }

        return null;
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
