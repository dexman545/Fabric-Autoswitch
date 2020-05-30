package autoswitch.util;

import autoswitch.AutoSwitch;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;

import java.util.concurrent.atomic.AtomicReference;

public class TargetableUtil {

    public static double toolRatingChange(double oldValue, double newValue, ItemStack stack) {
        if (AutoSwitch.cfg.toolEnchantmentsStack() && !(stack.getItem().equals(ItemStack.EMPTY.getItem())) && !(stack.getMaxDamage() == 0)) {
            return oldValue + newValue;
        }

        return Math.max(oldValue, newValue);
    }

    public static Object getTarget(Object protoTarget) {

        if (protoTarget instanceof AbstractBlock.AbstractBlockState) {
            // Block Override
            if (AutoSwitch.data.toolTargetLists.containsKey(((AbstractBlock.AbstractBlockState) protoTarget).getBlock())) {
                return ((AbstractBlock.AbstractBlockState) protoTarget).getBlock();
            }
            return ((AbstractBlock.AbstractBlockState) protoTarget).getMaterial();
        }

        // Entity Override
        if (AutoSwitch.data.toolTargetLists.containsKey(((Entity) protoTarget).getType())) {
            return ((Entity) protoTarget).getType();
        }

        if (protoTarget instanceof LivingEntity) {
            return ((LivingEntity) protoTarget).getGroup();
        }

        return ((Entity) protoTarget).getType();

    }

    /**
     * Function has a maximum of 1 at e
     * is 0 at 1; decays from e -> infinity
     * the (1/.16) is correction factor to have maximum output be 1, instead of 0.16
     *
     * @param original original rating
     * @return clamped rating if needed
     */
    public static float clampToolRating(float original) {
        if (AutoSwitch.cfg.preferMinimumViableTool()) {
            return (float) ((1/.16) * Math.log10(original) * (1/original));
        }

        return original;
    }

    public static float getTargetRating(Object target, ItemStack stack) {
        if (target instanceof BlockState) {
            return clampToolRating(stack.getMiningSpeedMultiplier((BlockState) target));
        }

        if (target instanceof Entity) {
            AtomicReference<Float> x = new AtomicReference<>((float) 0);
            stack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(EntityAttributes.GENERIC_ATTACK_DAMAGE).forEach(entityAttributeModifier ->
                    x.updateAndGet(v -> (float) (v + entityAttributeModifier.getValue()))
            );
            return x.get();
        }

        return 0;
    }

    public static Object getUseTarget(Object protoTarget) {
        if (protoTarget instanceof Block) {
            return protoTarget;
        }

        return ((Entity) protoTarget).getType();
    }

    /**
     * Dumb mining level check using vanilla stuff
     *
     * @param stack  itemstack to check
     * @param target target to check
     * @return if the target will obtain drops from the block, returns true for entities
     */
    public static boolean isRightTool(ItemStack stack, Object target) {
        if (!AutoSwitch.cfg.dumbMiningLevelCheck()) return true;

        if (AutoSwitch.cfg.useNoDurablityItemsWhenUnspecified() && stack.getMaxDamage() == 0) return true;

        if (target instanceof BlockState) { //TODO add mining level check here
            return !((BlockState) target).method_29291() || stack.isEffectiveOn((BlockState) target);
        }

        return true;
    }

}
