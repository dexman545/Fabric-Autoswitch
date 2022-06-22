package autoswitch.selectors.selectable;

import java.security.InvalidParameterException;
import java.util.LinkedList;

import autoswitch.AutoSwitch;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class WorldInterfaces {
    private static final LinkedList<WorldInterface<?, ?, ?, ?, ?>> INTERFACES = new LinkedList<>();

    public static void registerVanilla() {
        register(new WorldInterface<BlockState, World, BlockPos, Entity, EntityType<?>>() {
            @Override
            public @Nullable BlockState getStateInternal(World world, BlockPos pos) {
                return world.getBlockState(pos);
            }

            @Override
            public @Nullable EntityType<?> getSpecialEntityTypeInternal(Entity entity) {
                // Vanilla has no special entity type
                return null;
            }

            @Override
            public @Nullable World isOfBlockHolder(Object o) {
                if (o instanceof World w) return w;
                return null;
            }

            @Override
            public @Nullable BlockPos isOfBlockLocation(Object o) {
                if (o instanceof BlockPos w) return w;
                return null;
            }

            @Override
            public @Nullable Entity isOfEntity(Object o) {
                if (o instanceof Entity w) return w;
                return null;
            }
        });
    }

    public static Object getState(Object world, Object pos) {
        for (WorldInterface<?, ?, ?, ?, ?> worldInterface : INTERFACES) {
            var state = worldInterface.getState(world, pos);
            if (state != null) {
                return state;
            }
        }
        AutoSwitch.logger.info("Failed to find a valid WorldInterface for the given loc {} {}", world, pos);
        throw new InvalidParameterException("Missing WorldInterface!");
    }

    public static Object getEntityObject(Object entity) {
        for (WorldInterface<?, ?, ?, ?, ?> worldInterface : INTERFACES) {
            var state = worldInterface.getSpecialEntityType(entity);
            if (state != null) {
                return state;
            }
        }

        return entity;
    }

    public static void register(WorldInterface<?, ?, ?, ?, ?> worldInterface) {
        INTERFACES.addFirst(worldInterface);
    }
}
