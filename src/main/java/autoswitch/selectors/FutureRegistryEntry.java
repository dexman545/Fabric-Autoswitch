package autoswitch.selectors;

import autoswitch.AutoSwitch;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;

public class FutureRegistryEntry<T> {
    private final Registry<T> registry;
    private T entry;
    private final Identifier id;

    public FutureRegistryEntry(Registry<T> registry, Identifier id) {
        this.registry = registry;
        this.id = id;
        entry = null;
    }

    public FutureRegistryEntry(Registry<T> registry, T entry) {
        this.registry = registry;
        this.entry = entry;
        this.id = registry.getId(entry);
    }

    public boolean matches(T comparator) {
        if (entry == null) {
            if (registry.containsId(id)) {//todo defaultEntry check
                entry = registry.get(id);
                return entry != null && entry.equals(comparator);
            }
        }

        return entry.equals(comparator);
    }

    //todo store instances somewhere and validate these on config/world load
    public void validateEntry() {
        if (entry != null) return;
        if (registry instanceof DefaultedRegistry<T> defaultedRegistry) {
            if (registry.get(id).equals(registry.get(defaultedRegistry.getDefaultId()))) {
                AutoSwitch.logger.warn(String.format("Could not find entry in registry: %s for id: %s",
                                                     registry, id.toString()));
            }
        } else {
            if (!registry.containsId(id)) {
                AutoSwitch.logger.warn(String.format("Could not find entry in registry: %s for id: %s",
                                                     registry, id.toString()));
            } else {
                entry = registry.get(id);
            }
        }
    }

}
