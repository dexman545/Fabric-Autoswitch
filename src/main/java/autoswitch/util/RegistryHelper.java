package autoswitch.util;

import autoswitch.AutoSwitch;
import autoswitch.actions.Action;
import autoswitch.selectors.futures.FutureRegistryEntry;
import autoswitch.selectors.futures.IdentifiedTag;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

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
    public static <T> T getEntry(Registry<T> registry, ResourceLocation id) {
        if (registry != null) {
            var entry = registry.get(id);
            if (!isDefaultEntry(registry, entry, id)) {
                return entry;
            }
        }

        return null;
    }

    public static <T> boolean isDefaultEntry(Registry<T> registry, T entry, ResourceLocation id) {
        if (entry == null) return false;
        if (registry instanceof DefaultedRegistry<T> defaultedRegistry) {
            if (defaultedRegistry.getDefaultKey().equals(id)) return false;
            return registry.get(defaultedRegistry.getDefaultKey()).equals(entry);
        }

        return false;
    }

}
