package dex.autoswitch.engine.types.selectable;

import dex.autoswitch.engine.TargetType;
import dex.autoswitch.engine.data.SelectionContext;
import dex.autoswitch.futures.FutureSelectable;
import dex.autoswitch.futures.FutureSelectableGroup;
import dex.autoswitch.futures.FutureSelectableValue;
import dex.autoswitch.platform.Services;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public class EnchantmentSelectableType extends SelectableResource<Enchantment> {
    public static final EnchantmentSelectableType INSTANCE = new EnchantmentSelectableType();

    private EnchantmentSelectableType() {
        super("enchantment");
    }

    @Override
    public Holder<Enchantment> lookup(ResourceLocation resourceLocation) {
        var maybeReg = Services.PLATFORM.getRegistryAccess().lookup(Registries.ENCHANTMENT);
        if (maybeReg.isPresent()) {
            var entry = maybeReg.get().get(ResourceKey.create(Registries.ENCHANTMENT, resourceLocation));
            if (entry.isPresent()) {
                return entry.get();
            }
        }

        return null;
    }

    @Override
    public TagKey<Enchantment> lookupGroup(ResourceLocation resourceLocation) {
        return TagKey.create(Registries.ENCHANTMENT, resourceLocation);
    }

    @Override
    public boolean matches(SelectionContext context, Holder<Enchantment> v, Object selectable) {
        if (selectable instanceof ItemStack stack) {
            if (stack.isEnchanted()) {
                for (Holder<Enchantment> holder : Services.PLATFORM.getItemEnchantments(stack).keySet()) {
                    //noinspection deprecation
                    if (holder.is(v)) {
                        return true;
                    }
                }
            }
        }

        if (selectable instanceof Enchantment enchantment) {
            return enchantment.equals(v.value());
        }

        return false;
    }

    @Override
    public boolean matchesGroup(SelectionContext context, TagKey<Enchantment> enchantmentTagKey, Object selectable) {
        if (selectable instanceof ItemStack stack) {
            if (stack.isEnchanted()) {
                for (Holder<Enchantment> holder : Services.PLATFORM.getItemEnchantments(stack).keySet()) {
                    if (Services.PLATFORM.isInTag(enchantmentTagKey, holder.value())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public @Nullable TargetType targetType() {
        return null;
    }

    @Override
    public boolean isOf(Object o) {
        return o instanceof Enchantment || (o instanceof Holder<?> h && h.value() instanceof Enchantment);
    }

    @Override
    public double typeRating(SelectionContext context, FutureSelectable<ResourceLocation, Holder<Enchantment>> futureValue, Object selectable) {
        if (selectable instanceof ItemStack stack) {
            if (stack.isEnchanted()) {
                var enchantments = Services.PLATFORM.getItemEnchantments(stack);
                return switch (futureValue) {
                    case FutureSelectableGroup<ResourceLocation, Holder<Enchantment>, ?> v -> {
                        var d = 0D;
                        var c = 0;
                        for (Holder<Enchantment> holder : enchantments.keySet()) {
                            //noinspection unchecked
                            if (Services.PLATFORM.isInTag((TagKey<Enchantment>) v.getGroup(), holder.value())) {
                                d += (double) enchantments.getLevel(holder) / holder.value().getMaxLevel();
                                c++;
                            }
                        }

                        yield c == 0 ? 0 : d / c;
                    }
                    case FutureSelectableValue<ResourceLocation, Holder<Enchantment>> enchantment -> {
                        yield (double) enchantments.getLevel(enchantment.getValue()) / enchantment.getValue().value().getMaxLevel();
                    }
                };
            }
        }

        return 0;
    }
}
