package autoswitch.util;

import java.util.OptionalInt;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import autoswitch.AutoSwitch;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class TargetableUtil {

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

        if (protoTarget instanceof AbstractBlock.AbstractBlockState) {
            // Block Override
            if (map.containsKey(((AbstractBlock.AbstractBlockState) protoTarget).getBlock())) {
                return ((AbstractBlock.AbstractBlockState) protoTarget).getBlock();
            }
            return ((AbstractBlock.AbstractBlockState) protoTarget).getMaterial();
        }

        if (protoTarget instanceof Entity) {
            // Entity Override
            if (map.containsKey(((Entity) protoTarget).getType())) {
                return ((Entity) protoTarget).getType();
            }

            if (protoTarget instanceof LivingEntity) {
                return ((LivingEntity) protoTarget).getGroup();
            }

            return ((Entity) protoTarget).getType();
        }

        return null;
    }

    public static Object getUseTarget(Object protoTarget) {
        return getTarget(AutoSwitch.switchData.target2UseActionToolSelectorsMap, protoTarget);
    }

    public static float getTargetRating(Object target, ItemStack stack) {
        if (target instanceof BlockState) { //TODO correct clamping for instabreak situations ie. swords on bamboo
            return clampToolRating(stack.getMiningSpeedMultiplier((BlockState) target));
        }

        if (target instanceof Entity) {
            if (!(stack.getItem() instanceof ToolItem)) return 0;

            AtomicReference<Float> x = new AtomicReference<>((float) 0);
            AtomicReference<Float> y = new AtomicReference<>((float) 0);

            // Get attack speed
            stack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(EntityAttributes.GENERIC_ATTACK_SPEED).forEach(
                    entityAttributeModifier -> y.updateAndGet(v -> (float) (v - entityAttributeModifier.getValue())));

            if (AutoSwitch.featureCfg.weaponRatingIncludesEnchants()) { //Evaluate attack damage based on enchantments
                stack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(EntityAttributes.GENERIC_ATTACK_DAMAGE).forEach(
                        entityAttributeModifier -> x
                                .updateAndGet(v -> (float) (v + entityAttributeModifier.getValue())));

                return x.get() * (3 - y.get());
            } else { // No care for enchantments
                return ((3 - y.get()) * ((ToolItem) stack.getItem()).getMaterial().getAttackDamage());
            }

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
     * @return whether or not this stack should be skipped for consideration as a tool
     */
    public static boolean skipSlot(ItemStack itemStack) {
        AutoSwitch.logger.debug("Stack: {}; First: {}; Second: {}", itemStack,
                                !(AutoSwitch.featureCfg.useNoDurablityItemsWhenUnspecified() &&
                                  !itemStack.isDamageable()),
                                isAlmostBroken(itemStack) && AutoSwitch.featureCfg.tryPreserveDamagedTools());
        // Skip energy items that are out of power
        if (AutoSwitch.featureCfg.skipDepletedItems() &&
            AutoSwitch.switchData.damageMap.containsKey(itemStack.getClass()) && isAlmostBroken(itemStack)) {
            return true;
        }
        // First part: don't skip iff undamagable items are needed
        return (!(AutoSwitch.featureCfg.useNoDurablityItemsWhenUnspecified() && !itemStack.isDamageable()) &&
                isAlmostBroken(itemStack) && AutoSwitch.featureCfg.tryPreserveDamagedTools());

    }

    private static boolean isAlmostBroken(ItemStack stack) {
        return getDurability(stack) <= 3 && getDurability(stack) != -1;
    }

    private static int getDurability(ItemStack stack) {
        AtomicReference<Number> durability = new AtomicReference<>(-1);

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

        if (AutoSwitch.featureCfg.useNoDurablityItemsWhenUnspecified() && stack.getMaxDamage() == 0) return true;

        if (target instanceof BlockState) {
            return !((BlockState) target).isToolRequired() || stack.isSuitableFor((BlockState) target);
        }

        return true;
    }

    public static boolean isCorrectAttackType(String tool, Item item) {
        return (AutoSwitch.featureCfg.useNoDurablityItemsWhenUnspecified() && item.getMaxDamage() == 0) ||
               isCorrectTool(tool, item);
    }

    /**
     * Checks if the tool is of the correct type or not
     *
     * @param tool tool name from config
     * @param item item from hotbar
     *
     * @return true if tool name and item match
     */
    private static boolean isCorrectTool(String tool, Item item) {
        AtomicBoolean matches = new AtomicBoolean(false);

        AutoSwitch.switchData.toolGroupings.forEach((toolKey, tagClassPair) -> {
            if (tool.equals(toolKey) || tool.equals("any")) {
                if (checkTagAndClass(tagClassPair.getLeft(), tagClassPair.getRight(), item)) {
                    matches.set(true);
                }
            }
        });

        return matches.get() || (Registry.ITEM.getId(item).equals(Identifier.tryParse(tool)));
    }

    private static boolean checkTagAndClass(Tag<Item> tag, Class<?> clazz, Item item) {
        return (tag != null && tag.contains(item)) || (clazz != null && clazz.isInstance(item));
    }

    public static boolean isCorrectUseType(String tool, Item item) {
        return isCorrectTool(tool, item);
    }

}
