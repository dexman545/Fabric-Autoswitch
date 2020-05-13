package autoswitch;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public class Util {

    public static Object getTarget(Object protoTarget) {
        if (protoTarget instanceof AbstractBlock.AbstractBlockState) {
            return ((AbstractBlock.AbstractBlockState) protoTarget).getMaterial();
        }
        if (protoTarget instanceof LivingEntity) {
            return ((LivingEntity) protoTarget).getGroup();
        }
        if (protoTarget instanceof Entity) {
            return ((Entity) protoTarget).getType();
        }

        AutoSwitch.logger.error("AutoSwitch tried to parse something that wasn't a BlockState, Entity, or Living Entity!");
        return null;
    }

    public static float getTargetRating(Object target, ItemStack stack) {
        if (target instanceof BlockState) { //TODO add mining level check here
            return stack.getMiningSpeedMultiplier((BlockState) target);
        }

        return 0;
    }

    public static Object getUseTarget(Object protoTarget) {
        if (protoTarget instanceof Block) {
            return protoTarget;
        }
        if (protoTarget instanceof Entity) {
            return ((Entity) protoTarget).getType();
        }

        AutoSwitch.logger.error("AutoSwitch tried to parse something that wasn't a Block or Entity for a use action!");
        return null;
    }

    /**
     * Dumb mining level check using vanilla stuff
     *
     * @param stack itemstack to check
     * @param target target to check
     * @return if the target will obtain drops from the block, returns true for entities
     */
    public static boolean isRightTool(ItemStack stack, Object target) {
        if (!AutoSwitch.cfg.dumbMiningLevelCheck()) return true;

        if (target instanceof BlockState) { //TODO add mining level check here
            return ((BlockState) target).getMaterial().canBreakByHand() || stack.isEffectiveOn((BlockState) target);
        }

        return true;
    }

}
