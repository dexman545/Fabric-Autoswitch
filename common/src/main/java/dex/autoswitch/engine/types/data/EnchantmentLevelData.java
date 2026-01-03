package dex.autoswitch.engine.types.data;

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
import dex.autoswitch.futures.FutureSelectable;
import dex.autoswitch.platform.Services;
import io.leangen.geantyref.TypeToken;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public class EnchantmentLevelData extends DataType<DataMap> {
    public static final EnchantmentLevelData INSTANCE = new EnchantmentLevelData();
    private final Map<DataMap, Set<LevelCondition>> CONDITION_CACHE = new HashMap<>();

    private EnchantmentLevelData() {
        //noinspection Convert2Diamond
        super("level", new TypeToken<DataMap>() {});
    }

    @Override
    public Match matches(int baseLevel, SelectionContext context, Object selectable, DataMap data) {
        if (context.target() instanceof FutureSelectable<?, ?> enchantmentSelector) {
            if (selectable instanceof ItemStack stack) {
                var enchantmentData = Services.PLATFORM.getItemEnchantments(stack);
                var enchantments = enchantmentData.keySet();
                for (Holder<Enchantment> enchantment : enchantments) {
                    if (enchantmentSelector.matches(null, enchantment.value())) {
                        var condition = CONDITION_CACHE.computeIfAbsent(data, this::process);

                        for (LevelCondition levelCondition : condition) {
                            if (!levelCondition.matches(enchantmentData.getLevel(enchantment))) {
                                return new Match(false);
                            }
                        }

                        return new Match(true);
                    }
                }
            }
        }

        return new Match(false);
    }

    private Set<LevelCondition> process(DataMap dataMap) {
        var properties = new HashSet<LevelCondition>();
        switch (dataMap) {
            case DataMap.Map map -> {
                for (DataMap entry : map.entries()) {
                    properties.addAll(process(entry));
                }
            }
            case DataMap.Pair(var key, DataMap.Value(var val)) -> {
                LevelComparison comp;
                try {
                    comp = LevelComparison.valueOf(key.toUpperCase(Locale.ENGLISH));
                } catch (IllegalArgumentException e) {
                    Constants.LOG.error("Unknown comparator: {}", dataMap);
                    comp = LevelComparison.INVALID;
                }
                properties.add(LevelCondition.of(comp, val));
            }
            case DataMap.Value(var val) -> {
                properties.add(LevelCondition.of(LevelComparison.EQUALS, val));
            }
            case DataMap.Pair pair -> {
            }
        }

        return properties;
    }

    private record LevelCondition(LevelComparison comp, Integer level) {
        public static LevelCondition of(LevelComparison comp, String s) {
            int level = 0;
            try {
                level = Integer.parseInt(s);
            } catch (NumberFormatException e) {
                Constants.LOG.error("Could not convert enchantment level to integer {}", s);
                comp = LevelComparison.INVALID;
            }
            return new LevelCondition(comp, level);
        }

        boolean matches(int level) {
            return switch (comp) {
                case INVALID -> false;
                case EQUALS -> level == this.level;
                case LESSER -> level < this.level;
                case GREATER -> level > this.level;
                case LESSER_EQUAL -> level <= this.level;
                case GREATER_EQUAL -> level >= this.level;
            };
        }
    }

    private enum LevelComparison {
        INVALID,
        EQUALS,
        LESSER,
        GREATER,
        LESSER_EQUAL,
        GREATER_EQUAL,
    }
}
