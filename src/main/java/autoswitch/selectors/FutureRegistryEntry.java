package autoswitch.selectors;

import java.util.Objects;

import autoswitch.AutoSwitch;
import autoswitch.selectors.util.RegistryHelper;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class FutureRegistryEntry<T> {
    private final Registry<T> registry;
    private T entry;
    private final Identifier id;
    private final Class<T> clazz;

    public static final ObjectOpenHashSet<FutureRegistryEntry<?>> INSTANCES = new ObjectOpenHashSet<>();

    protected FutureRegistryEntry(Registry<T> registry, Identifier id, Class<T> clazz) {
        this.registry = registry;
        this.id = id;
        this.clazz = clazz;
        entry = null;
    }

    protected FutureRegistryEntry(Registry<T> registry, T entry, Class<T> clazz) {
        this.registry = registry;
        this.entry = entry;
        this.clazz = clazz;
        this.id = registry.getId(entry);
    }

    @SuppressWarnings("unchecked")
    public static <T> FutureRegistryEntry<T> getOrCreateEntry(Registry<T> registry, Identifier id, Class<T> clazz) {
        return (FutureRegistryEntry<T>) INSTANCES.addOrGet(new FutureRegistryEntry<>(registry, id, clazz));
    }

    @SuppressWarnings("unchecked")
    public static <T> FutureRegistryEntry<T> getOrCreateEntry(Registry<T> registry, T entry, Class<T> clazz) {
        return (FutureRegistryEntry<T>) INSTANCES.addOrGet(new FutureRegistryEntry<>(registry, entry, clazz));
    }

    public boolean matches(T comparator) {
        if (entry == null) {
            if (!RegistryHelper.isDefaultEntry(registry, comparator) && id.equals(registry.getId(comparator))) {
                entry = comparator;
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
        if (RegistryHelper.isDefaultEntry(registry, registry.get(id))) {
            AutoSwitch.logger.warn(String.format("Could not find entry in registry: %s for id: %s",
                                                 registry, id.toString()));
        } else {
            if (!registry.containsId(id)) {
                AutoSwitch.logger.warn(String.format("Could not find entry in registry: %s for id: %s",
                                                     registry, id.toString()));
            } else {
                entry = registry.get(id);
            }
        }
    }

    public static void validateEntries() {
        INSTANCES.forEach(FutureRegistryEntry::validateEntry);
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
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }
}
