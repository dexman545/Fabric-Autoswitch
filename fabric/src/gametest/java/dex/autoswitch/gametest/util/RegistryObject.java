package dex.autoswitch.gametest.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class RegistryObject {
    public static ItemStack stack(GameTestHelper helper, Item item, Enchant... enchantments) {
        var stack = item.getDefaultInstance();
        if (EnchantmentHelper.canStoreEnchantments(stack)) {
            for (var enchantment : enchantments) {
                if (enchantment != null) {
                    var e = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                            .getOrThrow(enchantment.key);
                    stack.enchant(e, enchantment.level);
                }
            }
        }

        return stack;
    }

    public static BlockState block(Block block, State<?, ?>... states) {
        var state = block.defaultBlockState();
        for (@SuppressWarnings("rawtypes") State s : states) {
            //noinspection unchecked
            state = state.setValue(s.property, s.value);
        }

        return state;
    }

    public static <T extends Entity> T entity(GameTestHelper helper, EntityType<T> type) {
        return helper.spawn(type, BlockPos.ZERO);
    }

    public record Enchant(ResourceKey<Enchantment> key, int level) {
        public static Enchant of(ResourceKey<Enchantment> key, int level) {
            return new Enchant(key, level);
        }
    }

    public record State<T extends Comparable<T>, V extends T>(Property<T> property, V value) {
        public static <T extends Comparable<T>, V extends T> State<T, V> of(Property<T> property, V value) {
            return new State<>(property, value);
        }
    }
}
