package autoswitch.selectors;

import java.util.Objects;
import java.util.function.Predicate;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class EnchantmentSelector implements Selector<Enchantment> {
    private final Predicate<Enchantment> predicate;

    public EnchantmentSelector(Identifier id) {
        predicate = makeFutureRegistryEntryPredicate(id);
    }

    public EnchantmentSelector(TagKey<Enchantment> tagKey) {
        this(enchantment -> {
            var maybeKey = Registry.ENCHANTMENT.getKey(enchantment);//todo cache entry?
            return maybeKey.filter(enchantmentRegistryKey ->
                                           Registry.ENCHANTMENT.entryOf(enchantmentRegistryKey).isIn(tagKey))
                           .isPresent();
        });
    }

    public EnchantmentSelector(Enchantment enchantment) {
        this(enchantment::equals);
    }

    public EnchantmentSelector(Predicate<Enchantment> predicate) {
        this.predicate = predicate;
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
    public Registry<Enchantment> getRegistry() {
        return Registry.ENCHANTMENT;
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
        return "EnchantmentSelector{" + "predicate=" + predicate + '}';
    }

}
