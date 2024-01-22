package autoswitch.selectors;

import java.util.function.Predicate;

import autoswitch.selectors.futures.IdentifiedTag;
import autoswitch.selectors.futures.RegistryType;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class EnchantmentSelector implements Selector<Enchantment> {
    private final Predicate<Object> predicate;
    private final String entryName;

    public EnchantmentSelector(ResourceLocation id) {
        predicate = makeFutureRegistryEntryPredicate(RegistryType.ENCHANTMENT, id);
        entryName = id.toString();
    }

    public EnchantmentSelector(TagKey<Enchantment> tagKey) {
        this.predicate = IdentifiedTag.makeEnchantmentPredicate(tagKey);
        this.entryName = "enchant@" + tagKey.location();
    }

    /*public EnchantmentSelector(Enchantment enchantment) {
        this(enchantment::equals);
    }*/

    public EnchantmentSelector(Predicate<Object> predicate, String entryName) {
        this.predicate = predicate;
        this.entryName = entryName;
    }

    public double getRating(ItemStack stack) {
        var enchantmentRating = 0d;
        if (stack.isEnchanted()) {
            var enchantments = EnchantmentHelper.getEnchantments(stack).keySet();
            for (Enchantment enchantment : enchantments) {
                if (matches(enchantment)) {
                    enchantmentRating += 1.1 * EnchantmentHelper.getItemEnchantmentLevel(enchantment, stack);
                }
            }
        }

        return enchantmentRating;
    }

    public boolean matches(ItemStack stack) {
        var matches = false;
        if (stack.isEnchanted()) {
            var enchantments = EnchantmentHelper.getEnchantments(stack).keySet();
            for (Enchantment enchantment : enchantments) {
                if (matches(enchantment)) {
                    matches = true;
                    break;
                }
            }
        }

        return matches;
    }

    @Override
    public boolean matches(Enchantment compare) {
        return predicate.test(compare);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EnchantmentSelector that = (EnchantmentSelector) o;

        return predicate.equals(that.predicate);
    }

    @Override
    public int hashCode() {
        return predicate.hashCode();
    }

    @Override
    public String toString() {
        return "EnchantmentSelector{" + configEntry() + '}';
    }

    @Override
    public String configEntry() {
        return entryName;
    }

    @Override
    public String separator() {
        return "&";
    }

    @Override
    public boolean chainable() {
        return true;
    }

}
