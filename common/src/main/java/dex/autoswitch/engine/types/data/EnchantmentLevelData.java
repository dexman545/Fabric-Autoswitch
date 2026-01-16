package dex.autoswitch.engine.types.data;

import dex.autoswitch.config.data.tree.ValueCondition;
import dex.autoswitch.engine.data.Match;
import dex.autoswitch.engine.data.SelectionContext;
import dex.autoswitch.engine.data.extensible.DataType;
import dex.autoswitch.futures.FutureSelectable;
import dex.autoswitch.platform.Services;
import io.leangen.geantyref.TypeToken;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public class EnchantmentLevelData extends DataType<ValueCondition<Integer>> {
    public static final EnchantmentLevelData INSTANCE = new EnchantmentLevelData();

    private EnchantmentLevelData() {
        //noinspection Convert2Diamond
        super("level", new TypeToken<ValueCondition<Integer>>() {});
    }

    @Override
    public Match matches(int baseLevel, SelectionContext context, Object selectable, ValueCondition<Integer> condition) {
        if (context.target() instanceof FutureSelectable<?, ?> enchantmentSelector) {
            if (selectable instanceof ItemStack stack) {
                var enchantmentData = Services.PLATFORM.getItemEnchantments(stack);
                var enchantments = enchantmentData.keySet();
                for (Holder<Enchantment> enchantment : enchantments) {
                    if (enchantmentSelector.matches(null, enchantment.value())) {
                        return new Match(condition.matches(enchantmentData.getLevel(enchantment)));
                    }
                }
            }
        }

        return new Match(false);
    }
}
