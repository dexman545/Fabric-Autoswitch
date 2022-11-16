package autoswitch.selectors;

import java.util.function.Predicate;

import autoswitch.selectors.futures.IdentifiedTag;
import autoswitch.selectors.futures.RegistryType;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class EnchantmentSelector implements Selector<Enchantment> {
    private final Predicate<Object> predicate;
    private final String entryName;

    public EnchantmentSelector(Identifier id) {
        predicate = makeFutureRegistryEntryPredicate(RegistryType.ENCHANTMENT, id);
        entryName = id.toString();
    }

    public EnchantmentSelector(TagKey<Enchantment> tagKey) {
        this.predicate = IdentifiedTag.makeEnchantmentPredicate(tagKey);
        this.entryName = "enchant@" + tagKey.id();
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
        if (stack.hasEnchantments()) {
            var enchantments = EnchantmentHelper.get(stack).keySet();
            for (Enchantment enchantment : enchantments) {
                if (matches(enchantment)) {
                    enchantmentRating += 1.1 * EnchantmentHelper.getLevel(enchantment, stack);
                }
            }
        }

        return enchantmentRating;
    }

    public boolean matches(ItemStack stack) {
        var matches = false;
        if (stack.hasEnchantments()) {
            var enchantments = EnchantmentHelper.get(stack).keySet();
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
