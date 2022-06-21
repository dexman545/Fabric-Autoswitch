package autoswitch.selectors.selectable;

import org.jetbrains.annotations.Nullable;

public interface WorldInterface<BlockType, BlockHolder, BlockLocation, Entity, EntityType> {
    @Nullable
    BlockType getState(BlockHolder world, BlockLocation pos);

    @Nullable EntityType getSpecialEntityType(Entity entity);
}
