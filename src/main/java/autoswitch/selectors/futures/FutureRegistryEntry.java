package autoswitch.selectors.futures;

import java.util.Objects;

import autoswitch.AutoSwitch;
import autoswitch.selectors.util.RegistryHelper;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class FutureRegistryEntry<T> extends FutureStateHolder {
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
        state = FutureStateHolder.FutureState.VALID;
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
        validateEntry();

        if (entry == null || state == FutureStateHolder.FutureState.INVALID) {
            state = FutureStateHolder.FutureState.INVALID;
            return false;
        }

        return entry.equals(comparator);
    }

    //todo store instances somewhere and validate these on config/world load
    @Override
    public void validateEntry() {
        validateEntry(false);
    }

    public void validateEntry(boolean force) {
        if (!force && state != FutureStateHolder.FutureState.AWAITING_VALIDATION) return;
        if (RegistryHelper.isDefaultEntry(registry, registry.get(id), id)) {
            state = FutureStateHolder.FutureState.INVALID;
            AutoSwitch.logger.warn(String.format("Could not find entry in registry: %s for id: %s",
                                                 registry, id.toString()));
        } else {
            if (!registry.containsId(id)) {
                state = FutureStateHolder.FutureState.INVALID;
                AutoSwitch.logger.warn(String.format("Could not find entry in registry: %s for id: %s",
                                                     registry, id.toString()));
            } else {
                state = FutureStateHolder.FutureState.VALID;
                entry = registry.get(id);
            }
        }
    }

    public static void forceRevalidateEntries() {
        INSTANCES.forEach(f -> f.validateEntry(true));
    }

    public boolean isOfType(Object o) {
        return clazz.isInstance(o);
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
        if (isValid()) return entry.hashCode();
        int result = registry != null ? registry.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }

    public static class NoneEntry extends FutureRegistryEntry<Void> {
        public static final FutureRegistryEntry<?> NULL = new NoneEntry();

        private NoneEntry() {
            this(null, null, null);
            state = FutureState.VALID;
        }

        private NoneEntry(Registry<Void> registry, Identifier id, Class<Void> clazz) {
            super(registry, id, clazz);
        }

        @Override
        public boolean matches(Void comparator) {
            return false;
        }

        @Override
        public void validateEntry() {
            // NO-OP
        }

        @Override
        public boolean isOfType(Object o) {
            return false;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public boolean equals(Object o) {
            return false;
        }

        @Override
        public int hashCode() {
            return 0;
        }
    }
}
