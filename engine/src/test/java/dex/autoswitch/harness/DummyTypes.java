package dex.autoswitch.harness;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import dex.autoswitch.config.codecs.SelectableTypeMarker;
import dex.autoswitch.config.data.tree.DataMap;
import dex.autoswitch.config.data.tree.ExpressionTree;
import dex.autoswitch.engine.TargetType;
import dex.autoswitch.engine.data.Match;
import dex.autoswitch.engine.data.SelectionContext;
import dex.autoswitch.engine.data.extensible.DataType;
import dex.autoswitch.engine.data.extensible.SelectableType;
import dex.autoswitch.futures.FutureSelectable;
import dex.autoswitch.futures.FutureSelectableValue;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.Nullable;

public class DummyTypes {
    public static final SelectableType<String, DummyTarget, Pattern> BLOCK_TYPE = new SelectableType<>("block") {
        @Override
        public DummyTarget lookup(String s) {
            return new DummyTarget(s, Type.BLOCK);
        }

        @Override
        public Pattern lookupGroup(String s) {
            return Pattern.compile(s);
        }

        @Override
        public boolean matches(SelectionContext context, DummyTarget v, Object selectable) {
            if (selectable instanceof DummyTarget(String id, Type t, Map<String, String> data)) {
                return id.equals(v.id) && t.equals(v.type);
            }
            return false;
        }

        @Override
        public boolean matchesGroup(SelectionContext context, Pattern pattern, Object selectable) {
            if (selectable instanceof DummyTarget(String id, Type t, Map<String, String> data)) {
                return t == Type.BLOCK && pattern.matcher(id).matches();
            }
            return false;
        }

        @Override
        public String serializeKey(String s) {
            return s;
        }

        @Override
        public String deserializeKey(String key) {
            return key;
        }

        @Override
        public TargetType targetType() {
            return TargetType.BLOCKS;
        }

        @Override
        public boolean isOf(Object o) {
            return o instanceof DummyTarget(var id, var type, Map<String, String> data) && type == Type.BLOCK;
        }

        @Override
        public double typeRating(SelectionContext context, FutureSelectable<String, DummyTarget> futureValue, Object selectable) {
            if (selectable instanceof DummyTarget(String id, Type type, Map<String, String> data)) {
                return id.length();
            }

            return 0;
        }
    };

    public static final SelectableType<String, DummyTarget, Pattern> ENTIY_TYPE = new SelectableType<>("entity") {
        @Override
        public DummyTarget lookup(String s) {
            return new DummyTarget(s, Type.ENTITY);
        }

        @Override
        public Pattern lookupGroup(String s) {
            return Pattern.compile(s);
        }

        @Override
        public boolean matches(SelectionContext context, DummyTarget v, Object selectable) {
            if (selectable instanceof DummyTarget(String id, Type t, Map<String, String> data)) {
                return id.equals(v.id) && t.equals(v.type);
            }
            return false;
        }

        @Override
        public boolean matchesGroup(SelectionContext context, Pattern pattern, Object selectable) {
            if (selectable instanceof DummyTarget(String id, Type t, Map<String, String> data)) {
                return t == Type.ENTITY && pattern.matcher(id).matches();
            }
            return false;
        }

        @Override
        public String serializeKey(String s) {
            return s;
        }

        @Override
        public String deserializeKey(String key) {
            return key;
        }

        @Override
        public TargetType targetType() {
            return TargetType.ENTITIES;
        }

        @Override
        public boolean isOf(Object o) {
            return o instanceof DummyTarget(var id, var type, Map<String, String> data) && type == Type.ENTITY;
        }

        @Override
        public double typeRating(SelectionContext context, FutureSelectable<String, DummyTarget> futureValue, Object selectable) {
            return 0;
        }
    };

    public static final SelectableType<String, DummyTool, Pattern> ITEM_TYPE = new SelectableType<>("item") {
        @Override
        public DummyTool lookup(String s) {
            return new DummyTool(s);
        }

        @Override
        public Pattern lookupGroup(String s) {
            return Pattern.compile(s);
        }

        @Override
        public boolean matches(SelectionContext context, DummyTool v, Object selectable) {
            if (selectable instanceof DummyTool(String id, Set<DummyEnchantment> enchantments)) {
                return v.id.equals(id);
            }
            return false;
        }

        @Override
        public boolean matchesGroup(SelectionContext context, Pattern pattern, Object selectable) {
            if (selectable instanceof DummyTool(String id, Set<DummyEnchantment> data)) {
                return pattern.matcher(id).matches();
            }
            return false;
        }

        @Override
        public String serializeKey(String s) {
            return s;
        }

        @Override
        public String deserializeKey(String key) {
            return key;
        }

        @Override
        public @Nullable TargetType targetType() {
            return null;
        }

        @Override
        public boolean isOf(Object o) {
            return o instanceof DummyTool(var id, var type);
        }

        @Override
        public double typeRating(SelectionContext context, FutureSelectable<String, DummyTool> futureValue, Object selectable) {
            if (selectable instanceof DummyTool(String id, Set<DummyEnchantment> data)) {
                return -id.length();
            }

            return 0;
        }
    };

    public static final SelectableType<String, DummyEnchantment, Pattern> ENCHANTMENT_TYPE = new SelectableType<>("enchantment") {
        @Override
        public DummyEnchantment lookup(String s) {
            return new DummyEnchantment(s);
        }

        @Override
        public Pattern lookupGroup(String s) {
            return Pattern.compile(s);
        }

        @Override
        public boolean matches(SelectionContext context, DummyEnchantment v, Object selectable) {
            if (selectable instanceof DummyTool(String id, Set<DummyEnchantment> enchantments)) {
                if (enchantments == null) {
                    return false;
                }
                for (DummyEnchantment enchantment : enchantments) {
                    if (v.equals(enchantment)) {
                        return true;
                    }
                }
            }
            return selectable.equals(v);
        }

        @Override
        public boolean matchesGroup(SelectionContext context, Pattern pattern, Object selectable) {
            if (selectable instanceof DummyTool(String id, Set<DummyEnchantment> data)) {
                return pattern.matcher(id).matches();
            }

            if (selectable instanceof DummyEnchantment(String id)) {
                return pattern.matcher(id).matches();
            }

            return false;
        }

        @Override
        public String serializeKey(String s) {
            return s;
        }

        @Override
        public String deserializeKey(String key) {
            return key;
        }

        @Override
        public @Nullable TargetType targetType() {
            return null;
        }

        @Override
        public boolean isOf(Object o) {
            return o instanceof DummyEnchantment;
        }

        @Override
        public double typeRating(SelectionContext context, FutureSelectable<String, DummyEnchantment> futureValue, Object selectable) {
            if (selectable instanceof DummyEnchantment(String id)) {
                return -id.length();
            }

            return 0;
        }
    };

    public static final DataType<ExpressionTree> ENCHANTMENTS = new DataType<>("enchantments", new TypeToken<@SelectableTypeMarker("enchantment") ExpressionTree>() {}) {
        @Override
        public Match matches(int baseLevel, SelectionContext context, Object selectable, ExpressionTree data) {
            return data.matches(baseLevel, context, selectable);
        }
    };

    public static final DataType<DataMap> COMPONENTS = new DataType<>("components", TypeToken.get(DataMap.class)) {
        @Override
        public Match matches(int baseLevel, SelectionContext context, Object selectable, DataMap data) {
            return new Match(false);
        }
    };

    public static DummyTool createTool(String id) {
        return new DummyTool(id);
    }

    public static FutureSelectable<String, DummyTool> createToolFuture(String id) {
        return createToolFuture(id, false);
    }

    public static FutureSelectable<String, DummyTool> createToolFuture(String id, boolean isGroup) {
        return FutureSelectableValue.getOrCreate(id, ITEM_TYPE, isGroup);
    }

    public static FutureSelectable<String, DummyTarget> createTargetFuture(String id, Type type) {
        return createTargetFuture(id, type, false);
    }

    public static FutureSelectable<String, DummyTarget> createTargetFuture(String id, Type type, boolean isGroup) {
        return switch (type) {
            case BLOCK -> FutureSelectableValue.getOrCreate(id, BLOCK_TYPE, isGroup);
            case ENTITY -> FutureSelectableValue.getOrCreate(id, ENTIY_TYPE, isGroup);
            case ITEM -> throw new IllegalArgumentException("Not a target type");
        };
    }

    public static DummyTarget createTarget(String id, Type type) {
        return new DummyTarget(id, type);
    }

    public record DummyTarget(String id, Type type, Map<String, String> data) {
        public DummyTarget(String id, Type type) {
            this(id, type, null);
        }
    }

    public record DummyTool(String id, Set<DummyEnchantment> data) {
        public DummyTool(String id) {
            this(id, null);
        }
    }

    public record DummyEnchantment(String id) {}

    public enum Type {
        BLOCK,
        ENTITY,
        ITEM,
    }
}
