package dex.autoswitch.engine.types.data;

import dex.autoswitch.Constants;
import dex.autoswitch.config.data.tree.Data;
import dex.autoswitch.config.data.tree.DataMap;
import dex.autoswitch.engine.data.Match;
import dex.autoswitch.engine.data.SelectionContext;
import dex.autoswitch.engine.data.extensible.DataType;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class EnchantmentLevelData extends DataType<DataMap> {
    public static final EnchantmentLevelData INSTANCE = new EnchantmentLevelData();
    private final Map<DataMap, Set<LevelCondition>> CONDITION_CACHE = new HashMap<>();

    private EnchantmentLevelData() {
        super("level", DataMap.class);
    }

    @Override
    public Match matches(int baseLevel, SelectionContext context, Object selectable, Data data) {
        if (selectable instanceof ItemStack stack) {
            if (data instanceof DataMap dataMap) {
                var condition = CONDITION_CACHE.computeIfAbsent(dataMap, this::process);

                return new Match(true);
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
