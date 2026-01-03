package dex.autoswitch.engine.types.data;

import static dex.autoswitch.engine.ContextKeys.BLOCK_POS;
import static dex.autoswitch.engine.ContextKeys.PLAYER;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import dex.autoswitch.Constants;
import dex.autoswitch.config.data.tree.DataMap;
import dex.autoswitch.engine.data.Match;
import dex.autoswitch.engine.data.SelectionContext;
import dex.autoswitch.engine.data.extensible.DataType;
import io.leangen.geantyref.TypeToken;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class PlayerData extends DataType<DataMap> {
    public static final PlayerData INSTANCE = new PlayerData();
    private final Map<DataMap, Set<ValueCondition<?>>> CONDITION_CACHE = new HashMap<>();
    private static final Map<String, Value<?>> VALUES = Map.ofEntries(
            Map.entry("isFlying".toLowerCase(Locale.ENGLISH), new Value<>(Boolean.class, PlayerData::isFlying)),
            Map.entry("isCrouching".toLowerCase(Locale.ENGLISH), new Value<>(Boolean.class, PlayerData::isCrouching)),
            Map.entry("isPassenger".toLowerCase(Locale.ENGLISH), new Value<>(Boolean.class, PlayerData::isPassenger)),
            Map.entry("isOnGround".toLowerCase(Locale.ENGLISH), new Value<>(Boolean.class, PlayerData::isOnGround)),
            Map.entry("isSprinting".toLowerCase(Locale.ENGLISH), new Value<>(Boolean.class, PlayerData::isSprinting)),
            Map.entry("distance".toLowerCase(Locale.ENGLISH), new Value<>(Double.class, PlayerData::getDistanceFromPlayer)),
            Map.entry("fallDistance".toLowerCase(Locale.ENGLISH), new Value<>(Double.class, PlayerData::getFallDistance))
    );

    private PlayerData() {
        //noinspection Convert2Diamond
        super("player", new TypeToken<DataMap>() {});
    }

    @Override
    public Match matches(int baseLevel, SelectionContext context, Object selectable, DataMap data) {
        if (context.get(PLAYER) instanceof Player player) {
            try {
                var condition = CONDITION_CACHE.computeIfAbsent(data, this::process);

                for (ValueCondition<?> valueCondition : condition) {
                    if (valueCondition.comp() == ValueComparison.INVALID || !valueCondition.matches(player, context, selectable)) {
                        return new Match(false);
                    }
                }
            } catch (Exception e) {
                Constants.LOG.error("Failed to process player attachments", e);
            }

            return new Match(true);
        }

        return new Match(false);
    }

    private Set<ValueCondition<?>> process(DataMap dataMap) {
        return process(dataMap, null);
    }

    private Set<ValueCondition<?>> process(DataMap dataMap, Value<?> value) {
        var properties = new HashSet<ValueCondition<?>>();
        switch (dataMap) {
            case DataMap.Map map -> {
                for (DataMap entry : map.entries()) {
                    properties.addAll(process(entry, value));
                }
            }
            case DataMap.Pair(var key, DataMap.Value(var val)) when value == null -> {
                var valueExtractor = VALUES.get(key.toLowerCase(Locale.ENGLISH));
                if (valueExtractor == null) {
                    Constants.LOG.error("Unknown value in pair: {}", key);
                    properties.add(ValueCondition.of(null, ValueComparison.INVALID, val));
                } else {
                    properties.add(ValueCondition.of(valueExtractor, ValueComparison.EQUALS, val));
                }
            }
            case DataMap.Pair(var key, DataMap.Value(var val)) -> {
                ValueComparison comp;
                try {
                    comp = ValueComparison.valueOf(key.toUpperCase(Locale.ENGLISH));
                } catch (IllegalArgumentException e) {
                    Constants.LOG.error("Unknown comparator: {}", dataMap);
                    comp = ValueComparison.INVALID;
                }
                properties.add(ValueCondition.of(value, comp, val));
            }
            case DataMap.Value(var val) -> {
                properties.add(ValueCondition.of(value, ValueComparison.EQUALS, val));
            }
            case DataMap.Pair(var key, var val) -> {
                var valueExtractor = VALUES.get(key.toLowerCase(Locale.ENGLISH));
                if (valueExtractor == null) {
                    Constants.LOG.error("Unknown value: {}", key);
                }
                properties.addAll(process(val, valueExtractor));
            }
        }

        return properties;
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

    private record ValueCondition<T extends Comparable<T>>(Value<T> value, ValueComparison comp, T val) {
        @SuppressWarnings("unchecked")
        public static <T extends Comparable<T>> ValueCondition<T> of(Value<T> value, ValueComparison comp, String s) {
            if (value == null) {
                return new ValueCondition<>(null, ValueComparison.INVALID, null);
            }
            return switch (value.clazz) {
                case Class<?> c when c == Boolean.class -> {
                    yield (ValueCondition<T>) new ValueCondition<Boolean>((Value<Boolean>) value, comp, Boolean.valueOf(s));
                }
                case Class<?> c when c == Double.class -> {
                    try {
                        yield (ValueCondition<T>) new ValueCondition<Double>((Value<Double>) value, comp, Double.parseDouble(s));
                    } catch (NumberFormatException e) {
                        Constants.LOG.error("Invalid number format for Double value: {}", s, e);
                        yield (ValueCondition<T>) new ValueCondition<>(null, ValueComparison.INVALID, null);
                    }
                }
                default -> {
                    Constants.LOG.error("Unknown value type for PlayerData comparison: {}", value.clazz);
                    yield new ValueCondition<>(null, ValueComparison.INVALID, null);
                }
            };
        }

        boolean matches(Player player, SelectionContext context, Object selectable) {
            if (value == null) {
                return false;
            }

            T val2 = value.extractor.extract(player, context, selectable);
            return switch (comp) {
                case INVALID -> false;
                case EQUALS -> this.val.compareTo(val2) == 0;
                case LESSER -> this.val.compareTo(val2) > 0;
                case GREATER -> this.val.compareTo(val2) < 0;
                case LESSER_EQUAL -> this.val.compareTo(val2) >= 0;
                case GREATER_EQUAL -> this.val.compareTo(val2) <= 0;
            };
        }
    }

    private enum ValueComparison {
        INVALID,
        EQUALS,
        LESSER,
        GREATER,
        LESSER_EQUAL,
        GREATER_EQUAL,
    }

    private record Value<T extends Comparable<T>>(Class<T> clazz, ValueExtractor<T> extractor) {}

    @FunctionalInterface
    private interface ValueExtractor<T extends Comparable<T>> {
        T extract(Player player, SelectionContext context, Object selectable);
    }
}
