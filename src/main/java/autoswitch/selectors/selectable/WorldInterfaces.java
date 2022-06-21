package autoswitch.selectors.selectable;

import java.util.LinkedList;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class WorldInterfaces {//todo use
    private static LinkedList<WorldInterface<?, ?, ?, ?, ?>> INTERFACES = new LinkedList<>();

    public static void registerVanilla() {
        register(new WorldInterface<BlockState, World, BlockPos, Entity, EntityType<?>>() {
            @Override
            public @Nullable BlockState getState(World world, BlockPos pos) {
                return world.getBlockState(pos);
            }

            @Override
            public @Nullable EntityType<?> getSpecialEntityType(Entity entity) {
                // Vanilla has no special entity type
                return null;
            }
        });
    }

    public static void register(WorldInterface<?, ?, ?, ?, ?> worldInterface) {
        INTERFACES.addFirst(worldInterface);
    }
}
