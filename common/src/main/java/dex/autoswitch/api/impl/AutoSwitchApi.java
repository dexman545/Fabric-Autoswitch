package dex.autoswitch.api.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import dex.autoswitch.Constants;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * For Fabric:
 * Each API entry is added to {@code ObjectShare} with the given id and a {@link Collection} of entries.
 * Add new entries to the collection as needed.
 * <p>
 * For NeoForge:
 * Each API entry is processed via InterModComms.
 */
@ApiStatus.Internal
public final class AutoSwitchApi {
    public static final AutoSwitchApi INSTANCE = new AutoSwitchApi();
    private final HashMap<ResourceLocation, ApiEntry<?>> entryMap = new HashMap<>();

    /**
     * Registry of predicates that determine whether an {@link ItemStack} is considered "depleted" by AutoSwitch.
     * <p>
     * A {@code true} result means the item should be skipped when selecting tools. Examples include:
     * <ul>
     *   <li>A drill or tool that has zero energy left.</li>
     *   <li>A tool with durability whose next damage would break it.</li>
     * </ul>
     * <p>
     * Expected type: {@link Predicate}{@code <}{@link ItemStack}{@code >}
     * <p>
     * Platform integration guidance:
     * <ul>
     *   <li>Fabric: Obtain the shared {@link Collection} via {@code ObjectShare} and
     *       add the {@code Predicate<ItemStack>} to the collection.</li>
     *   <li>NeoForge: Send IMC using {@code method = DEPLETED.id().getPath()} and payload of type
     *       {@code Predicate<ItemStack>}.</li>
     * </ul>
     */
    public final ApiEntry<Predicate<ItemStack>> DEPLETED = register("depletion");

    private AutoSwitchApi() {
    }

    /**
     * Returns a view of all registered API entries.
     * <p>
     * Useful on Fabric to publish entries through the {@code ObjectShare}.
     */
    public Collection<ApiEntry<?>> getEntries() {
        return entryMap.values();
    }

    /**
     * Returns the mutable internal map of API ids to their entries.
     * <p>
     * Intended for platform bootstrap to route incoming contributions to the
     * appropriate {@link ApiEntry}.
     */
    public HashMap<ResourceLocation, ApiEntry<?>> getEntryMap() {
        return entryMap;
    }

    /**
     * Registers a new {@link ApiEntry} under the AutoSwitch namespace using the provided id path.
     * <p>
     * Example: {@code register("depletion")} becomes {@code autoswitch:depletion}.
     */
    private <T> ApiEntry<T> register(String id) {
        ApiEntry<T> entry = new ApiEntry<>(id);
        entryMap.put(entry.id(), entry);
        return entry;
    }

    /**
     * A typed collection of contributed API objects. Integrators should add, but not remove, values.
     *
     * @param id      The fully qualified id of the entry (e.g. {@code autoswitch:depletion}).
     * @param entries The synchronized, non-removable set backing this API entry.
     * @param <T>     The expected type of contributed objects (e.g. {@code Predicate<ItemStack>}).
     */
    public record ApiEntry<T>(ResourceLocation id, Set<T> entries) implements Iterable<T> {
        /**
         * Creates a namespaced API entry from a simple path component.
         *
         * @param id The path component to register under the {@link Constants#MOD_ID} namespace.
         */
        public ApiEntry(String id) {
            this(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, id), Collections.synchronizedSet(new ApiSet<>()));
        }

        /**
         * Adds a new contribution to this API entry.
         *
         * @param t The object to add; must be non-null and of the correct type {@code T}.
         * @throws NullPointerException If {@code t} is null.
         */
        public void addEntry(T t) {
            entries.add(Objects.requireNonNull(t));
        }

        /**
         * Adds a contribution coming from a dynamically typed source (e.g., IMC payload).
         *
         * @param o The object to cast and add.
         * @throws ClassCastException   If {@code o} is not of the expected type {@code T}.
         * @throws NullPointerException If {@code o} is null.
         */
        public void addUnknown(Object o) throws ClassCastException {
            //noinspection unchecked
            entries.add((T) Objects.requireNonNull(o));
        }

        @Override
        public @NotNull Iterator<T> iterator() {
            return entries().iterator();
        }
    }

    /**
     * Backing set for API entries that enforces "add-only" semantics.
     * <p>
     * All removal operations are unsupported to ensure the stability of registered integrations for the
     * lifetime of the client session.
     */
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
