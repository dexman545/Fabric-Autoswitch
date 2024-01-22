package autoswitch.selectors.futures;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Set;

import autoswitch.AutoSwitch;
import autoswitch.util.RegistryHelper;

import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import net.minecraft.core.IdMap;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;

@SuppressWarnings("unchecked")
public class FutureRegistryEntry extends FutureStateHolder implements Representable {
    private static final LinkedList<RegistryHolder<?, ?>> REGISTRY_HOLDERS = new LinkedList<>();
    private static final ObjectOpenHashSet<FutureRegistryEntry> INSTANCES = new ObjectOpenHashSet<>();

    static {
        REGISTRY_HOLDERS.add(new RegistryHolder<>(BuiltInRegistries.BLOCK, Block.class, RegistryType.BLOCK));
        REGISTRY_HOLDERS.add(
                new RegistryHolder<>(BuiltInRegistries.ENTITY_TYPE, (Class<EntityType<?>>) (Class<?>) EntityType.class,
                                     RegistryType.ENTITY));
        REGISTRY_HOLDERS.add(new RegistryHolder<>(BuiltInRegistries.ITEM, Item.class, RegistryType.ITEM));
        REGISTRY_HOLDERS.add(new RegistryHolder<>(BuiltInRegistries.ENCHANTMENT, Enchantment.class, RegistryType.ENCHANTMENT));
    }

    private final ResourceLocation id;
    private Object entry;
    private RegistryHolder<?, ?> holder;
    private RegistryType type;
    private boolean typeLocked = false;

    protected FutureRegistryEntry(RegistryType type, ResourceLocation id) {
        this.id = id;
        this.type = type;
        entry = null;
        holder = null;
        INSTANCES.add(this);
    }

    public static void prependRegistryHolder(RegistryHolder<?, ?> registryHolder) {
        REGISTRY_HOLDERS.addFirst(registryHolder);
    }

    public static FutureRegistryEntry getOrCreate(RegistryType type, ResourceLocation id) {
        return INSTANCES.addOrGet(new FutureRegistryEntry(type, id));
    }

    public static void forceRevalidateEntries() {
        INSTANCES.forEach(f -> f.validateEntry(true));
    }

    public RegistryType getType() {
        return type;
    }

    public Object getEntry() {
        return entry;
    }

    @Override
    public void validateEntry(boolean force) {
        if (!force && state != FutureState.AWAITING_VALIDATION) return;
        // Reset type to undetermined
        if (!typeLocked && RegistryType.BLOCK_OR_ENTITY.matches(type)) type = RegistryType.BLOCK_OR_ENTITY;
        // Find entry
        for (RegistryHolder<?, ?> registryHolder : REGISTRY_HOLDERS) {//todo flag for multiple registry match
            if (type.matches(registryHolder.type)) {
                entry = registryHolder.id2EntryFunction.apply(id);
                if (entry != null) {
                    type = registryHolder.type;
                    state = FutureState.VALID;
                    holder = registryHolder;
                    return;
                }
            }
        }

        state = FutureState.INVALID;
        AutoSwitch.logger.warn(
                String.format("Could not find entry in registries of type: %s for id: %s", type, id.toString()));
    }

    public boolean matches(Object comparator) {
        validateEntry();

        if (entry == null || state == FutureState.INVALID) {
            state = FutureState.INVALID;
            return false;
        }

        return entry.equals(comparator);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (state != FutureState.INVALID) return matches(o);
        if (getClass() != o.getClass()) return false;

        FutureRegistryEntry that = (FutureRegistryEntry) o;

        if (!Objects.equals(type, that.type)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        if (isValid()) return entry.hashCode();
        int result = holder != null ? holder.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }

    public void setTypeLocked(boolean typeLocked) {
        this.typeLocked = typeLocked;
    }

    @Override
    public Set<FutureRegistryEntry> getRepresentable() {
        validateEntry();
        return Collections.singleton(this);
    }

    public static class TargetHashingStrategy implements Hash.Strategy<Object> {
        public TargetHashingStrategy() {
        }

        @Override
        public int hashCode(Object o) {
            if (o != null) {
                if (o instanceof FutureRegistryEntry targetEntry) {
                    if (!targetEntry.isValid()) {
                        targetEntry.validateEntry();
                    }
                    return targetEntry.hashCode();
                }
                return o.hashCode();
            }

            return 0;
        }

        @Override
        public boolean equals(Object a, Object b) {
            if (a instanceof FutureRegistryEntry targetEntry) {
                return targetEntry.equals(b);
            }

            if (b instanceof FutureRegistryEntry targetEntry) {
                return targetEntry.equals(a);
            }

            if (a != null) {
                return a.equals(b);
            }

            return false;
        }

    }

    public record RegistryHolder<T extends IdMap<U>, U>(T registry, Class<U> clazz, RegistryType type,
                                                                  Function<ResourceLocation, U> id2EntryFunction) {
        public RegistryHolder(Registry<U> registry, Class<U> clazz, RegistryType type) {
            this((T) registry, clazz, type, id -> RegistryHelper.getEntry(registry, ((ResourceLocation) id)));
        }

        public boolean canHold(Object o) {
            return clazz.isInstance(o);
        }

    }

}
