package autoswitch.selectors.selectable;

import org.jetbrains.annotations.Nullable;

public interface WorldInterface<BlockType, BlockHolder, BlockLocation, Entity, EntityType> {
    @Nullable
    BlockType getStateInternal(BlockHolder world, BlockLocation pos);

    @Nullable
    EntityType getSpecialEntityTypeInternal(Entity entity);

    @Nullable
    BlockHolder isOfBlockHolder(Object o);

    @Nullable
    BlockLocation isOfBlockLocation(Object o);

    @Nullable
    Entity isOfEntity(Object o);

    @Nullable
    @SuppressWarnings("unchecked")
    default BlockType getState(Object world, Object pos) {
        if (isOfBlockHolder(world) != null && isOfBlockLocation(pos) != null) {
            return getStateInternal((BlockHolder) world, (BlockLocation) pos);
        }

        return null;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    default EntityType getSpecialEntityType(Object entity) {
        if (isOfEntity(entity) != null) {
            return getSpecialEntityTypeInternal((Entity) entity);
        }

        return null;
    }
}
