package autoswitch.selectors.futures;

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;

import net.fabricmc.fabric.api.tag.client.v1.ClientTags;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;

public record IdentifiedTag<T>(TagKey<T> tagKey, Class<T> clazz, RegistryType type, Predicate<Object> defaultIsIn) implements Representable {
    private static final Object2ObjectOpenHashMap<TagKey<?>, IdentifiedTag<?>> IDENTIFIED_TAGS =
            new Object2ObjectOpenHashMap<>();
    private static final Object2ObjectOpenHashMap<IdentifiedTag<?>, ObjectOpenCustomHashSet<FutureRegistryEntry>>
            FALLBACK_ENTRIES = new Object2ObjectOpenHashMap<>();

    public IdentifiedTag(TagKey<T> tagKey, Class<T> clazz, RegistryType type, Predicate<Object> defaultIsIn) {
        this.tagKey = tagKey;
        this.clazz = clazz;
        this.type = type;
        this.defaultIsIn = defaultIsIn;

        // Canonical constructor to get these to run properly
        IDENTIFIED_TAGS.put(tagKey, this);
        FALLBACK_ENTRIES.computeIfAbsent(this,
                                         $ -> new ObjectOpenCustomHashSet<>(new FutureRegistryEntry.TargetHashingStrategy()));
    }

    @SuppressWarnings("unchecked")
    public static <U> IdentifiedTag<U> getOrCreate(TagKey<U> tagKey, Class<U> clazz, RegistryType type,
                                                   Predicate<Object> defaultIsIn) {
        return (IdentifiedTag<U>) IDENTIFIED_TAGS.computeIfAbsent(tagKey, t -> new IdentifiedTag<>(tagKey, clazz, type, defaultIsIn));
    }

    public static IdentifiedTag<Item> getOrCreateItem(TagKey<Item> tagKey) {
        return getOrCreate(tagKey, Item.class, RegistryType.ITEM, o -> {
            if (o instanceof Item item) {
                return ClientTags.isInWithLocalFallback(tagKey, item);
            } else if (o instanceof ItemStack stack) {
                return ClientTags.isInWithLocalFallback(tagKey, stack.getItem());
            }
            return false;
        });
    }

    public static IdentifiedTag<Block> getOrCreateBlock(TagKey<Block> tagKey) {
        return IdentifiedTag.getOrCreate(tagKey, Block.class, RegistryType.BLOCK, o -> {
            if (o instanceof BlockState state) {
                return ClientTags.isInWithLocalFallback(tagKey, state.getBlock());
            } else if (o instanceof Block block) {
                return ClientTags.isInWithLocalFallback(tagKey, block);
            }
            return false;
        });
    }

    @SuppressWarnings("unchecked")
    public static IdentifiedTag<EntityType<?>> getOrCreateEntity(TagKey<EntityType<?>> tagKey) {
        return IdentifiedTag.getOrCreate(tagKey, (Class<EntityType<?>>) (Class<?>) EntityType.class,
                                         RegistryType.ENTITY, o -> {
            if (o instanceof Entity e) {
                return ClientTags.isInWithLocalFallback(tagKey, e.getType());
            } else if (o instanceof EntityType<?> type) {
                return ClientTags.isInWithLocalFallback(tagKey, type);
            }
            return false;
        });
    }

    public static IdentifiedTag<Enchantment> getOrCreateEnchantment(TagKey<Enchantment> tagKey) {
        return getOrCreate(tagKey, Enchantment.class, RegistryType.ENCHANTMENT, o -> {
            if (o instanceof Enchantment enchantment) {
                return ClientTags.isInWithLocalFallback(tagKey, enchantment);
            }
            return false;
        });
    }

    public static <U> Predicate<U> makeItemPredicate(TagKey<Item> tagKey) {
        return getOrCreateItem(tagKey)::contains;
    }

    public static <U> Predicate<U> makeBlockPredicate(TagKey<Block> tagKey) {
        return getOrCreateBlock(tagKey)::contains;
    }

    public static <U> Predicate<U> makeEntityPredicate(TagKey<EntityType<?>> tagKey) {
        return getOrCreateEntity(tagKey)::contains;
    }

    public static <U> Predicate<U> makeEnchantmentPredicate(TagKey<Enchantment> tagKey) {
        return getOrCreateEnchantment(tagKey)::contains;
    }

    // To be called only after FREs are refreshed
    public static void refreshIdentifiers() {
        IDENTIFIED_TAGS.values().forEach(IdentifiedTag::refreshFutureEntries);
    }

    public void refreshFutureEntries() {
        FALLBACK_ENTRIES.get(this).trim();
    }

    public void addEntries(Collection<Identifier> ids) {
        for (Identifier id : ids) {
            FALLBACK_ENTRIES.get(this).add(FutureRegistryEntry.getOrCreate(type, id));
        }
    }

    public void buildFutureEntries() {
        addEntries(ClientTags.getOrCreateLocalTag(tagKey));
    }

    public boolean contains(Object o) {
        if (defaultIsIn.test(o)) {
            return true;
        }

        return FALLBACK_ENTRIES.get(this).contains(o);
    }

    @Override
    public Set<FutureRegistryEntry> getRepresentable() {
        buildFutureEntries();
        FALLBACK_ENTRIES.get(this).trim();
        return FALLBACK_ENTRIES.get(this);
    }

}
