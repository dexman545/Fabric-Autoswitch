package autoswitch.config.io;

import java.util.regex.Pattern;

import autoswitch.selectors.EnchantmentSelector;
import autoswitch.selectors.ItemSelector;
import autoswitch.selectors.TargetableGroup;
import autoswitch.selectors.futures.IdentifiedTag;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public class TagTargetHandler {
    private static final Pattern tagGroupPattern = Pattern.compile("(\\w+@\\w+:\\w+)");

    private static boolean isTagGroup(String str) {
        return tagGroupPattern.matcher(str).matches();
    }

    public static TargetableGroup<?> getTargetableTagGroup(String str) {
        if (isTagGroup(str)) {
            var tagType = TagType.getType(str);
            if (tagType != null) {
                return tagType.handler.makeGroup(str);
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public static ItemSelector getItemSelector(String str) {
        if (isTagGroup(str)) {
            var tagType = TagType.getType(str);
            if (tagType != null) {
                if (tagType == TagType.ITEM) {
                    return new ItemSelector((TagKey<Item>) tagType.handler.makeTag(str));
                }
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public static EnchantmentSelector getEnchantmentSelector(String str) {
        if (isTagGroup(str)) {
            var tagType = TagType.getType(str);
            if (tagType != null) {
                if (tagType == TagType.ENCHANTMENT) {
                    return new EnchantmentSelector((TagKey<Enchantment>) tagType.handler.makeTag(str));
                }
            }
        }

        return null;
    }

    public static TagKey<?> getTag(String str) {
        if (isTagGroup(str)) {
            var tagType = TagType.getType(str);
            if (tagType != null) {
                return tagType.handler.makeTag(str);
            }
        }

        return null;
    }

    public enum TagType {
        BLOCK(new TagTypeHandler<Block>() {
            @Override
            protected RegistryKey<? extends Registry<Block>> getRegistry() {
                return RegistryKeys.BLOCK;
            }

            @Override
            public TargetableGroup<?> makeGroup(String tagGroup) {
                var ss = tagGroup.split("@");
                var tagId = Identifier.tryParse(ss[1]);

                if (tagId == null) return null;

                var tagKey = makeTagKey(tagId);

                return new TargetableGroup<>(tagGroup,
                                             new TargetableGroup.TargetPredicate("Matches tag group:" + tagGroup,
                                                                                 IdentifiedTag.makeBlockPredicate(
                                                                                         tagKey)));
            }
        }),
        ENTITY_TYPE(new TagTypeHandler<EntityType<?>>() {
            @Override
            protected RegistryKey<? extends Registry<EntityType<?>>> getRegistry() {
                return RegistryKeys.ENTITY_TYPE;
            }

            @Override
            public TargetableGroup<?> makeGroup(String tagGroup) {
                var ss = tagGroup.split("@");
                var tagId = Identifier.tryParse(ss[1]);

                if (tagId == null) return null;

                var tagKey = makeTagKey(tagId);

                return new TargetableGroup<>(tagGroup,
                                             new TargetableGroup
                                                     .TargetPredicate("Matches tag group:" + tagGroup,
                                                                      IdentifiedTag.makeEntityPredicate(tagKey)));
            }
        }),
        ITEM(new TagTypeHandler<Item>() {
            @Override
            protected RegistryKey<? extends Registry<Item>> getRegistry() {
                return RegistryKeys.ITEM;
            }

            @Override
            public TargetableGroup<?> makeGroup(String tagGroup) {
                return null;
            }
        }),
        ENCHANTMENT(new TagTypeHandler<Enchantment>() {
            @Override
            protected RegistryKey<? extends Registry<Enchantment>> getRegistry() {
                return RegistryKeys.ENCHANTMENT;
            }

            @Override
            public TargetableGroup<?> makeGroup(String tagGroup) {
                return null;
            }
        });

        private final TagTypeHandler<?> handler;

        TagType(TagTypeHandler<?> handler) {
            this.handler = handler;
        }

        public static TagType getType(String tagGroup) {
            if (tagGroup.startsWith("entity")) {
                return ENTITY_TYPE;
            } else if (tagGroup.startsWith("item")) {
                return ITEM;
            } else if (tagGroup.startsWith("block")) {
                return BLOCK;
            } else if (tagGroup.startsWith("enchantment") || tagGroup.startsWith("enchant")) {
                return ENCHANTMENT;
            }

            return null;
        }
    }

    private abstract static class TagTypeHandler<T> {
        protected abstract RegistryKey<? extends Registry<T>> getRegistry();

        public abstract TargetableGroup<?> makeGroup(String tagGroup);

        public TagKey<T> makeTag(String tagGroup) {
            var ss = tagGroup.split("@");
            var tagId = Identifier.tryParse(ss[1]);

            if (tagId == null) return null;

            return makeTagKey(tagId);
        }

        protected TagKey<T> makeTagKey(Identifier tagId) {
            return TagKey.of(getRegistry(), tagId);
        }

    }

}
