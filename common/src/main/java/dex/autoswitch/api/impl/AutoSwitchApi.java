package dex.autoswitch.api.impl;

import dex.autoswitch.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

/**
 * For Fabric:
 *           Each API entry is added to {@code ObjectShare} with the given id and a {@link Collection} of entries.
 *           Add new entries to the collection as needed.
 * <p>
 * For NeoForge:
 *          Each API entry is processed via InterModComms.
 */
@ApiStatus.Internal
public final class AutoSwitchApi {
    public static AutoSwitchApi INSTANCE = new AutoSwitchApi();
    private final HashMap<ResourceLocation, ApiEntry<?>> entryMap = new HashMap<>();
    /**
     * A predicate that shall return true if and only if the {@link ItemStack} is unable
     * to perform its job as a tool, such as a drill that is out of energy.
     */
    public ApiEntry<Predicate<ItemStack>> DEPLETED = register("depletion");

    private AutoSwitchApi() {
    }

    public Collection<ApiEntry<?>> getEntries() {
        return entryMap.values();
    }

    public HashMap<ResourceLocation, ApiEntry<?>> getEntryMap() {
        return entryMap;
    }

    private <T> ApiEntry<T> register(String id) {
        ApiEntry<T> entry = new ApiEntry<>(id);
        entryMap.put(entry.id(), entry);
        return entry;
    }

    public record ApiEntry<T>(ResourceLocation id, Set<T> entries) implements Iterable<T> {
        public ApiEntry(String id) {
            this(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, id), Collections.synchronizedSet(new ApiSet<>()));
        }

        public void addEntry(T t) {
            entries.add(Objects.requireNonNull(t));
        }

        public void addUnknown(Object o) throws ClassCastException {
            //noinspection unchecked
            entries.add((T) Objects.requireNonNull(o));
        }

        @Override
        public @NotNull Iterator<T> iterator() {
            return entries().iterator();
        }
    }

    private static class ApiSet<T> extends HashSet<T> {
        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException("Cannot remove entries from autoswitch api!");
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException("Cannot remove entries from autoswitch api!");
        }

        @Override
        public @NotNull Iterator<T> iterator() {
            return new Iterator<>() {
                private final Iterator<T> i = ApiSet.super.iterator();

                @Override
                public boolean hasNext() {
                    return i.hasNext();
                }

                @Override
                public T next() {
                    return i.next();
                }
            };
        }

        @Override
        public boolean removeIf(@NotNull Predicate<? super T> filter) {
            throw new UnsupportedOperationException("Cannot remove entries from autoswitch api!");
        }

        @Override
        public boolean retainAll(@NotNull Collection<?> c) {
            throw new UnsupportedOperationException("Cannot remove entries from autoswitch api!");
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException("Cannot remove entries from autoswitch api!");
        }

        @Override
        public boolean add(T t) {
            return super.add(Objects.requireNonNull(t));
        }
    }
}
