package autoswitch.selectors.futures;

import autoswitch.AutoSwitch;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class FutureTargetEntry extends FutureStateHolder {
    public static final ObjectOpenHashSet<FutureTargetEntry> INSTANCES = new ObjectOpenHashSet<>();
    private final Identifier id;
    private final FutureRegistryEntry<EntityType<?>> entityTarget;
    private final FutureRegistryEntry<Block> blockTarget;
    private TargetType type;

    @SuppressWarnings("unchecked")
    private FutureTargetEntry(Identifier id) {
        this.id = id;
        entityTarget = FutureRegistryEntry.getOrCreateEntry(Registry.ENTITY_TYPE, id,
                                                 (Class<EntityType<?>>)(Class<?>)EntityType.class);
        blockTarget = FutureRegistryEntry.getOrCreateEntry(Registry.BLOCK, id, Block.class);
        type = TargetType.UNDETERMINED;
        INSTANCES.add(this);
    }

    @SuppressWarnings("unchecked")
    private FutureTargetEntry(Block block) {
        this.id = Registry.BLOCK.getId(block);
        blockTarget = FutureRegistryEntry.getOrCreateEntry(Registry.BLOCK, block, Block.class);
        entityTarget = (FutureRegistryEntry<EntityType<?>>) FutureRegistryEntry.NoneEntry.NULL;
        type = TargetType.BLOCK;
        INSTANCES.add(this);
        state = FutureState.VALID;
    }

    @SuppressWarnings("unchecked")
    private FutureTargetEntry(EntityType<?> entityType) {
        this.id = Registry.ENTITY_TYPE.getId(entityType);
        entityTarget = FutureRegistryEntry.getOrCreateEntry(Registry.ENTITY_TYPE, entityType,
                                                 (Class<EntityType<?>>)(Class<?>)EntityType.class);
        blockTarget = (FutureRegistryEntry<Block>) FutureRegistryEntry.NoneEntry.NULL;
        type = TargetType.ENTITY;
        INSTANCES.add(this);
        state = FutureState.VALID;
    }

    public static FutureTargetEntry getOrCreate(Identifier id) {
        return INSTANCES.addOrGet(new FutureTargetEntry(id));
    }

    public boolean isValid() {
        return type != TargetType.UNDETERMINED && state == FutureState.VALID;
    }

    public static void forceRevalidateEntries() {
        INSTANCES.forEach(f -> f.validateEntry(true));
    }

    @Override
    public void validateEntry() {
        validateEntry(false);
    }

    public void validateEntry(boolean force) {
        if (!force && isValid()) return;
        var hasMatch = false;
        entityTarget.validateEntry();
        if (entityTarget.isValid()) {
            type = TargetType.ENTITY;
            hasMatch = true;
            state = FutureState.VALID;
        }

        blockTarget.validateEntry();
        if (blockTarget.isValid()) {
            type = TargetType.BLOCK;
            state = FutureState.VALID;
            if (hasMatch) {
                AutoSwitch.logger.warn("A target ({}) matched in both the entity type and block " +
                                       "registries, preferring block.", id);
            }
            hasMatch = true;
        }

        if (!hasMatch) {
            AutoSwitch.logger.warn("A target ({}) was not found that matches the given ID.", id);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (state == FutureState.INVALID) return false;

        if (entityTarget.isOfType(o) || blockTarget.isOfType(o)) {
            switch (type) {
                case UNDETERMINED -> {
                    var hasMatch = false;
                    if (entityTarget.equals(o)) {
                        type = TargetType.ENTITY;
                        hasMatch = true;
                    }

                    if (blockTarget.equals(o)) {
                        type = TargetType.BLOCK;
                        if (hasMatch) {
                            AutoSwitch.logger.warn("A target ({}) matched in both the entity type and block " +
                                                   "registries, preferring block.", id);
                        }
                        hasMatch = true;
                    }
                    if (hasMatch) {
                        state = FutureState.VALID;
                    } else {
                        state = FutureState.INVALID;
                    }
                    return hasMatch;
                }
                case BLOCK -> {
                    return blockTarget.equals(o);
                }
                case ENTITY -> {
                    return entityTarget.equals(o);
                }
            }
        }

        if (o == null || getClass() != o.getClass()) return false;

        FutureTargetEntry that = (FutureTargetEntry) o;

        if (!id.equals(that.id)) return false;
        return type == that.type;
    }

    @Override
    public int hashCode() {
        return switch (type) {
            case UNDETERMINED -> {
                if (blockTarget.isValid()) yield blockTarget.hashCode();
                if (entityTarget.isValid()) yield entityTarget.hashCode();
                yield id.hashCode();
            }
            case BLOCK -> blockTarget.hashCode();
            case ENTITY -> entityTarget.hashCode();
        };
    }

    public enum TargetType {
        UNDETERMINED,
        BLOCK,
        ENTITY;
    }

    public static class TargetHashingStrategy implements Hash.Strategy<Object> {
        public TargetHashingStrategy() {
            //todo this is fine, but doesn't help as when adding entry it is hashed - need to
            // rebuild map when registry changes. trim() seems to rehash it?
        }

        @Override
        public int hashCode(Object o) {
            if (o != null) {
                if (o instanceof FutureTargetEntry targetEntry) {
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
            if (a instanceof FutureTargetEntry targetEntry) {
                return targetEntry.equals(b);
            }

            if (b instanceof FutureTargetEntry targetEntry) {
                return targetEntry.equals(a);
            }

            if (a != null) {
                return a.equals(b);
            }

            return false;
        }

    }

}
