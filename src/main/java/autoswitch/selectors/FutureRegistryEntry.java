package autoswitch.selectors;

import autoswitch.AutoSwitch;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;

import java.util.Objects;

public class FutureRegistryEntry<T> {
    private final Registry<T> registry;
    private T entry;
    private final Identifier id;
    private final Class<T> clazz;

    public FutureRegistryEntry(Registry<T> registry, Identifier id, Class<T> clazz) {
        this.registry = registry;
        this.id = id;
        this.clazz = clazz;
        entry = null;
    }

    public FutureRegistryEntry(Registry<T> registry, T entry, Class<T> clazz) {
        this.registry = registry;
        this.entry = entry;
        this.clazz = clazz;
        this.id = registry.getId(entry);
    }

    public boolean matches(T comparator) {
        if (entry == null) {
            if (registry.containsId(id)) {
                var e = registry.get(id);
                if (!isDefaultEntry(e)) entry = e;
            }
        }

        if (entry == null) return false;
        //todo store failed lookup and skip subsequent checks until world
        // reload/registry modification

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

    private boolean isDefaultEntry(T entry) {
        if (entry != null) return false;
        if (registry instanceof DefaultedRegistry<T> defaultedRegistry) {
            return registry.get(id).equals(registry.get(defaultedRegistry.getDefaultId()));
        }

        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (clazz.isInstance(o)) return matches(clazz.cast(o));
        if (getClass() != o.getClass()) return false;

        FutureRegistryEntry<?> that = (FutureRegistryEntry<?>) o;

        if (!Objects.equals(registry, that.registry)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        int result = registry != null ? registry.hashCode() : 0;
        result = 31 * result + (entry != null ? entry.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }
}
