package dex.autoswitch.engine.types.data;

import static dex.autoswitch.engine.ContextKeys.BLOCK_POS;
import static dex.autoswitch.engine.ContextKeys.PLAYER;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dex.autoswitch.config.data.tree.ExtensibleData;
import dex.autoswitch.config.data.tree.ValueCondition;
import dex.autoswitch.engine.data.Match;
import dex.autoswitch.engine.data.SelectionContext;
import dex.autoswitch.engine.data.extensible.DataType;
import io.leangen.geantyref.TypeToken;
import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class PlayerData extends DataType<PlayerData.State> {
    public static final PlayerData INSTANCE = new PlayerData();

    private PlayerData() {
        //noinspection Convert2Diamond
        super("player", new TypeToken<State>() {});
    }

    @Override
    public Match matches(int baseLevel, SelectionContext context, Object selectable, State state) {
        if (context.get(PLAYER) instanceof Player player && state != null) {
            return new Match(state.matches(player, context, selectable));
        }

        return new Match(false);
    }

    private static double getDistanceFromPlayer(Player player, SelectionContext context, Object selectable) {
        return switch (selectable) {
            case BlockState $ when context.get(BLOCK_POS) != null -> {
                var blockPos = context.get(BLOCK_POS);
                yield Math.sqrt(player.distanceToSqr(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
            }
            case Block $ when context.get(BLOCK_POS) != null -> {
                var blockPos = context.get(BLOCK_POS);
                yield Math.sqrt(player.distanceToSqr(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
            }
            case Entity entity -> player.distanceTo(entity);
            default -> Double.NaN;
        };
    }

    private static double getFallDistance(Player player, SelectionContext selectionContext, Object selectable) {
        return player.fallDistance;
    }

    private static boolean isFlying(Player player, SelectionContext context, Object selectable) {
        return player.isFallFlying();
    }

    private static boolean isCrouching(Player player, SelectionContext context, Object selectable) {
        return player.isCrouching();
    }

    private static boolean isSprinting(Player player, SelectionContext context, Object selectable) {
        return player.isSprinting();
    }

    private static boolean isPassenger(Player player, SelectionContext context, Object selectable) {
        return player.isPassenger();
    }

    private static boolean isOnGround(Player player, SelectionContext context, Object selectable) {
        return player.onGround();
    }

    @ConfigSerializable
    public record State( // Need to use @Setting to avoid camelCase becoming kebab-case in config
            @Setting("isFlying")
            @Nullable ValueCondition<Boolean> isFlying,
            @Setting("isCrouching")
            @Nullable ValueCondition<Boolean> isCrouching,
            @Setting("isPassenger")
            @Nullable ValueCondition<Boolean> isPassenger,
            @Setting("isOnGround")
            @Nullable ValueCondition<Boolean> isOnGround,
            @Setting("isSprinting")
            @Nullable ValueCondition<Boolean> isSprinting,
            @Setting("fallDistance")
            @Nullable ValueCondition<Double> fallDistance,
            @Nullable ValueCondition<Double> distance
    ) implements ExtensibleData {
        @Override
        public String prettyPrint(int level) {
            var indent = " ".repeat(level);
            return Stream.of(
                            isFlying != null ? "isFlying: " + isFlying : null,
                            isCrouching != null ? "isCrouching: " + isCrouching : null,
                            isPassenger != null ? "isPassenger: " + isPassenger : null,
                            isOnGround != null ? "isOnGround: " + isOnGround : null,
                            isSprinting != null ? "isSprinting: " + isSprinting : null,
                            fallDistance != null ? "fallDistance: " + fallDistance : null,
                            distance != null ? "distance: " + distance : null
                    )
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining("\n" + indent));
        }

        public boolean matches(Player player, SelectionContext context, Object selectable) {
            if (isFlying != null) {
                if (!isFlying.matches(PlayerData.isFlying(player, context, selectable))) {
                    return false;
                }
            }

            if (isCrouching != null) {
                if (!isCrouching.matches(PlayerData.isCrouching(player, context, selectable))) {
                    return false;
                }
            }

            if (isPassenger != null) {
                if (!isPassenger.matches(PlayerData.isPassenger(player, context, selectable))) {
                    return false;
                }
            }

            if (isOnGround != null) {
                if (!isOnGround.matches(PlayerData.isOnGround(player, context, selectable))) {
                    return false;
                }
            }

            if (isSprinting != null) {
                if (!isSprinting.matches(PlayerData.isSprinting(player, context, selectable))) {
                    return false;
                }
            }

            if (fallDistance != null) {
                if (!fallDistance.matches(PlayerData.getFallDistance(player, context, selectable))) {
                    return false;
                }
            }

            if (distance != null) {
                //noinspection RedundantIfStatement
                if (!distance.matches(PlayerData.getDistanceFromPlayer(player, context, selectable))) {
                    return false;
                }
            }

            return true;
        }
    }
}
