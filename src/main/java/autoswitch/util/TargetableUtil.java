package autoswitch.util;

import java.util.OptionalInt;
import java.util.concurrent.atomic.AtomicReference;

import autoswitch.AutoSwitch;
import autoswitch.targetable.custom.ItemTarget;
import autoswitch.targetable.custom.TargetableGroup;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;

public class TargetableUtil {
    private static final int NONE = -1;

    public static double toolRatingChange(double oldValue, double newValue, ItemStack stack, boolean stackEnchant) {
        if (stackEnchant && AutoSwitch.featureCfg.toolEnchantmentsStack() &&
            !(stack.getItem().equals(ItemStack.EMPTY.getItem())) && !(stack.getMaxDamage() == 0)) {
            return oldValue + newValue;
        }

        return Math.max(oldValue, newValue);
    }

    public static Object getAttackTarget(Object protoTarget) {
        return getTarget(AutoSwitch.switchData.target2AttackActionToolSelectorsMap, protoTarget);
    }

    public static Object getUseTarget(Object protoTarget) {
        return getTarget(AutoSwitch.switchData.target2UseActionToolSelectorsMap, protoTarget);
    }

    /**
     * Extract target from protoTarget, given a map of targets to examine.
     *
     * @param map         map of targets to compare protoTarget to
     * @param protoTarget object to extract target data from
     *
     * @return target
     */
    private static Object getTarget(Object2ObjectOpenHashMap<Object, IntArrayList> map, Object protoTarget) {
        if (protoTarget instanceof ItemTarget) return protoTarget;

        // These methods were moved to AbstractBlockState in 20w12a,
        // so their intermediary name changed breaking compatibility
        if (protoTarget instanceof BlockState state) {
            // Block Override
            Block block = state.getBlock();
            if (map.containsKey(block)) {
                return block;
            }
            return TargetableGroup.maybeGetTarget(protoTarget)
                                  .orElse(TargetableGroup.maybeGetTarget(block).orElse(state.getMaterial()));
        }

        if (protoTarget instanceof Entity e) {
            // Entity Override
            EntityType<?> entityType = e.getType();
            if (map.containsKey(entityType)) {
                return entityType;
            }

            return TargetableGroup.maybeGetTarget(protoTarget)
                                  .orElse(TargetableGroup.maybeGetTarget(entityType)
                                                         .orElse(protoTarget instanceof LivingEntity ?
                                                                 ((LivingEntity) protoTarget).getGroup() :
                                                                 entityType));
        }

        return null;
    }

    /**
     * Generates a target rating for the given stack.
     *
     * @see net.minecraft.entity.player.PlayerEntity#attack(Entity) for Entity case's damage calculation
     */
    public static float getTargetRating(Object target, ItemStack stack) {
        if (target instanceof BlockState) { // TODO correct clamping for instabreak situations ie. swords on bamboo
            return clampToolRating(stack.getMiningSpeedMultiplier((BlockState) target));
        }

        if (target instanceof Entity) {
            if (!(stack.getItem() instanceof ToolItem)) return 0;

            float damage;
            float h = 0;
            AtomicReference<Float> baseDamage = new AtomicReference<>((float) 0);

            // Doesn't include enchants
            stack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(EntityAttributes.GENERIC_ATTACK_DAMAGE).forEach(
                    entityAttributeModifier -> baseDamage
                            .updateAndGet(v -> (float) (v + entityAttributeModifier.getValue())));

            damage = baseDamage.get();

            // Consider Enchantments
            if (AutoSwitch.featureCfg.weaponRatingIncludesEnchants()) {
                // Should group be passed? Config gives them their own entries
                if (target instanceof LivingEntity) {
                    h = EnchantmentHelper.getAttackDamage(stack, ((LivingEntity)target).getGroup());
                } else {
                    h = EnchantmentHelper.getAttackDamage(stack, EntityGroup.DEFAULT);
                }
                // Disabled as config can specify this anyway
                /*int l = EnchantmentHelper.getLevel(Enchantments.FIRE_ASPECT, stack);
                if (target instanceof LivingEntity &&
                    (((LivingEntity)target).isOnFire() && ((LivingEntity)target).isFireImmune())) {
                        damage += l;
                }*/
            }

            damage += h;

            AtomicReference<Float> attackSpeed = new AtomicReference<>((float) 0);

            // Get attack speed
            stack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(EntityAttributes.GENERIC_ATTACK_SPEED).forEach(
                    entityAttributeModifier -> attackSpeed.updateAndGet(v -> (float)
                            (v + entityAttributeModifier.getValue())));

            if (attackSpeed.get() == 0 || damage == 0) return 0;

            // Inverse as the close to 0, the faster the attack
            return (damage * (Math.abs(1/attackSpeed.get())));
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
                                  !itemStack.isDamageable()),
                                isAlmostBroken(itemStack) && AutoSwitch.featureCfg.tryPreserveDamagedTools());
        // Skip energy items that are out of power
        if (AutoSwitch.featureCfg.skipDepletedItems() &&
            AutoSwitch.switchData.damageMap.containsKey(itemStack.getItem().getClass()) && isAlmostBroken(itemStack)) {
            return true;
        }
        // First part: don't skip iff items w/o durability (non-tools) are needed
        return (!(AutoSwitch.featureCfg.useNoDurabilityItemsWhenUnspecified() && !itemStack.isDamageable()) &&
                isAlmostBroken(itemStack) && AutoSwitch.featureCfg.tryPreserveDamagedTools());

    }

    private static boolean isAlmostBroken(ItemStack stack) {
        return getDurability(stack) <= AutoSwitch.damageThreshold && getDurability(stack) != NONE;
    }

    private static int getDurability(ItemStack stack) {
        AtomicReference<Number> durability = new AtomicReference<>(NONE);

        if (!stack.isDamageable()) {
            AutoSwitch.switchData.damageMap.forEach((clazz, durabilityGetter) -> {
                if (clazz.isInstance(stack.getItem())) {
                    durability.set(durabilityGetter.getDurability(stack));
                }
            });
        } else {
            return stack.getMaxDamage() - stack.getDamage(); // Vanilla items
        }

        return durability.get().intValue();
    }

    public static OptionalInt getCachedSlot(Object target, SwitchState state, boolean isUseAction) {
        return getTargetableCache(state, isUseAction).containsKey(target) ? OptionalInt
                .of(getTargetableCache(state, isUseAction).getInt(target)) : OptionalInt.empty();
    }

    public static TargetableCache getTargetableCache(SwitchState state, boolean isUseAction) {
        if (isUseAction) return state.switchInteractCache;
        return state.switchActionCache;
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

        if (target instanceof BlockState) {
            return !((BlockState) target).isToolRequired() || stack.isSuitableFor((BlockState) target);
        }

        return true;
    }

}
