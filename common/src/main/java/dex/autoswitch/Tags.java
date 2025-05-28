package dex.autoswitch;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Contains all custom autoswitch tag definitions, for us in datagen for Fabric client tags or
 * manual tag lookup on Neoforge
 */
public final class Tags {
    private static final Tags INSTANCE = new Tags();
    private final Map<TagKey<?>, Group<?>> tagMap = new HashMap<>();

    private Tags() {
        tagMap.put(tagKey(Registries.BLOCK, "shears_efficient"), new Group.CustomTag<>(
                Set.of(
                        Blocks.COBWEB,
                        Blocks.SHORT_GRASS,
                        Blocks.FERN,
                        Blocks.LARGE_FERN,
                        Blocks.DEAD_BUSH,
                        Blocks.HANGING_ROOTS,
                        Blocks.VINE,
                        Blocks.TRIPWIRE,
                        Blocks.GLOW_LICHEN,
                        Blocks.BUSH,
                        Blocks.SEAGRASS,
                        Blocks.SHORT_DRY_GRASS,
                        Blocks.TALL_DRY_GRASS,
                        Blocks.TALL_GRASS,
                        Blocks.TALL_SEAGRASS,
                        Blocks.NETHER_SPROUTS,
                        Blocks.WEEPING_VINES,
                        Blocks.WEEPING_VINES_PLANT,
                        Blocks.TWISTING_VINES,
                        Blocks.TWISTING_VINES_PLANT,
                        Blocks.PALE_HANGING_MOSS
                ),
                Set.of(
                        mcTag(Registries.BLOCK, "mineable/shears"),
                        BlockTags.WOOL,
                        BlockTags.LEAVES
                )
        ));
        /*tagMap.put(tagKey(Registries.BLOCK, ""), new Group.CustomTag<>(
                Set.of(),
                Set.of()
        ));*/
        tagMap.put(tagKey(Registries.BLOCK, "any"), new Group.CustomPredicate<Block>($ -> true));
        tagMap.put(tagKey(Registries.ENTITY_TYPE, "any"), new Group.CustomPredicate<EntityType<?>>($ -> true));
        tagMap.put(tagKey(Registries.ENCHANTMENT, "any"), new Group.CustomPredicate<Enchantment>($ -> true));
        tagMap.put(tagKey(Registries.ITEM, "any"), new Group.CustomPredicate<Item>($ -> true));
    }

    public static <T> Group<T> getTag(TagKey<T> tagKey) {
        //noinspection unchecked
        return (Group<T>) INSTANCE.tagMap.get(tagKey);
    }

    private static <T> TagKey<T> tagKey(ResourceKey<? extends Registry<T>> registry, String id) {
        return TagKey.create(registry, ResourceLocation.fromNamespaceAndPath("autoswitch", id));
    }

    private static <T> TagKey<T> mcTag(ResourceKey<? extends Registry<T>> registry, String id) {
        return TagKey.create(registry, ResourceLocation.fromNamespaceAndPath("minecraft", id));
    }

    public sealed interface Group<T> {
        record CustomTag<T>(Set<T> entries, Set<TagKey<T>> includedTags) implements Group<T> {}
        record CustomPredicate<T>(Predicate<T> predicate) implements Group<T> {}
    }
}
