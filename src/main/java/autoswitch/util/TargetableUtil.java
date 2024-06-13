package autoswitch.util;

import java.util.concurrent.atomic.AtomicReference;

import autoswitch.AutoSwitch;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.state.BlockState;

public class TargetableUtil {
    private static final int NONE = -1;

    public static double toolRatingChange(double oldValue, double newValue, ItemStack stack, boolean stackEnchant) {
        if (stackEnchant && AutoSwitch.featureCfg.toolEnchantmentsStack() &&
            !(stack.getItem().equals(ItemStack.EMPTY.getItem())) && !(stack.getMaxDamage() == 0)) {
            return oldValue + newValue;
        }

        return Math.max(oldValue, newValue);
    }

    /**
     * Generates a target rating for the given stack.
     * <p>
     * {@link net.minecraft.world.entity.player.Player#attack(Entity)} for Entity case's damage calculation
     * {@link net.minecraft.world.entity.Mob#getApproximateAttackDamageWithItem(ItemStack)} for attribute modifiers
     */
    public static float getTargetRating(Object target, ItemStack stack) {
        if (target instanceof BlockState blockState) { // TODO correct clamping for instabreak situations ie. swords on bamboo
            return clampToolRating(stack.getDestroySpeed(blockState));
        }

        if (target instanceof Entity entity) {
            if (!(stack.getItem() instanceof TieredItem)) return 0;//todo this is wrong

            float damage;
            float h = 0;
            AtomicReference<Float> baseDamage = new AtomicReference<>((float) 1);
            AtomicReference<Float> attackSpeed = new AtomicReference<>((float) 1);

            // Doesn't include enchants
            ItemAttributeModifiers itemAttributeModifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,
                                                                           ItemAttributeModifiers.EMPTY);
            itemAttributeModifiers.forEach(EquipmentSlot.MAINHAND, (holder, modifier) -> {
                if (holder.value() == Attributes.ATTACK_DAMAGE) {
                    baseDamage.accumulateAndGet((float) modifier.amount(), (t, m) -> switch(modifier.operation()) {
                        case ADD_VALUE -> t+m;
                        case ADD_MULTIPLIED_BASE -> t+(m * 1);
                        case ADD_MULTIPLIED_TOTAL -> t+(m * t);
                    });
                }

                if (holder.value() == Attributes.ATTACK_SPEED) {
                    attackSpeed.accumulateAndGet((float) modifier.amount(), (t, m) -> switch(modifier.operation()) {
                        case ADD_VALUE -> t+m;
                        case ADD_MULTIPLIED_BASE -> t+(m * 1);
                        case ADD_MULTIPLIED_TOTAL -> t+(m * t);
                    });
                }
            });

            damage = baseDamage.get();

            // Consider Enchantments
            //if (AutoSwitch.featureCfg.weaponRatingIncludesEnchants()) {
                // EnchantmentHelper.modifyDamage(); can't use as creates LootContext, no idea if it has side effects
                //h = EnchantmentHelper.getDamageBonus(stack, entity.getType());

                // Disabled as config can specify this anyway
                /*int l = EnchantmentHelper.getLevel(Enchantments.FIRE_ASPECT, stack);
                if (target instanceof LivingEntity &&
                    (((LivingEntity)target).isOnFire() && ((LivingEntity)target).isFireImmune())) {
                        damage += l;
                }*/
            //}

            damage += h;

            if (attackSpeed.get() == 0 || damage == 0) return 0;

            // Inverse as the close to 0, the faster the attack
            return (damage * (Math.abs(1 / attackSpeed.get())));
        }

        return 0;
    }

    /**
     * Function has a maximum of 1 at e; is 0 at 1; decays from e -> infinity. The (1/.16) is correction factor to have
     * maximum output be 1, instead of 0.16
     * <p>
     * Default output for getMiningSpeed is 1, same speed as the player's hand. This function clamps that to 0, as it is
     * not a viable tool under normal circumstances.
     *
     * @param original original rating
     *
     * @return clamped rating if needed
     */
    private static float clampToolRating(float original) {
        if (AutoSwitch.featureCfg.preferMinimumViableTool() && original > 0) {
            return (float) ((1 / .16) * Math.log10(original) / original);
        }

        return original;
    }

    /**
     * @param itemStack stack to evaluate
     *
     * @return whether this stack should be skipped for consideration as a tool
     */
    public static boolean skipSlot(ItemStack itemStack) {
        AutoSwitch.logger.debug("Stack: {}; First: {}; Second: {}", itemStack,
                                !(AutoSwitch.featureCfg.useNoDurabilityItemsWhenUnspecified() &&
                                  !itemStack.isDamageableItem()),
                                isAlmostBroken(itemStack) && AutoSwitch.featureCfg.tryPreserveDamagedTools());
        // Skip energy items that are out of power
        if (AutoSwitch.featureCfg.skipDepletedItems() &&
            AutoSwitch.switchData.damageMap.containsKey(itemStack.getItem().getClass()) && isAlmostBroken(itemStack)) {
            return true;
        }
        // First part: don't skip iff items w/o durability (non-tools) are needed
        return (!(AutoSwitch.featureCfg.useNoDurabilityItemsWhenUnspecified() && !itemStack.isDamageableItem()) &&
                isAlmostBroken(itemStack) && AutoSwitch.featureCfg.tryPreserveDamagedTools());

    }

    private static boolean isAlmostBroken(ItemStack stack) {
        return getDurability(stack) <= AutoSwitch.featureCfg.damageThreshold() && getDurability(stack) != NONE;
    }

    private static int getDurability(ItemStack stack) {
        AtomicReference<Number> durability = new AtomicReference<>(NONE);

        if (!stack.isDamageableItem()) {
            AutoSwitch.switchData.damageMap.forEach((clazz, durabilityGetter) -> {
                if (clazz.isInstance(stack.getItem())) {
                    durability.set(durabilityGetter.getDurability(stack));
                }
            });
        } else {
            return stack.getMaxDamage() - stack.getDamageValue(); // Vanilla items
        }

        return durability.get().intValue();
    }

    /**
     * Dumb mining level check using vanilla stuff
     *
     * @param stack  itemstack to check
     * @param target target to check
     *
     * @return if the target will obtain drops from the block, returns true for entities
     */
    public static boolean isRightTool(ItemStack stack, Object target) {
        if (!AutoSwitch.featureCfg.miningLevelCheck()) return true;

        if (AutoSwitch.featureCfg.useNoDurabilityItemsWhenUnspecified() && stack.getMaxDamage() == 0) return true;

        if (target instanceof BlockState state) {
            // Multiplier check to correct for swords on bamboo
            return !state.requiresCorrectToolForDrops() || (stack.isCorrectToolForDrops(state) || stack.getDestroySpeed(state) > 1);
        }

        return true;
    }

}
