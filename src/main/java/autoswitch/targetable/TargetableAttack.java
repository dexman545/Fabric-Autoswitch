package autoswitch.targetable;

import autoswitch.AutoSwitch;
import autoswitch.actions.Action;
import autoswitch.config.AutoSwitchConfig;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

/**
 * Targetable instance for attacking a block or entity
 */
class TargetableAttack extends Targetable {

    public TargetableAttack(Object protoTarget, PlayerEntity player) {
        super(player);
        this.player = player;
        this.protoTarget = protoTarget;
        populateToolLists();
    }

    @Override
    void populateToolSelection(ItemStack stack, int slot) {
        processToolSelectors(stack, slot);
    }

    @Override
    Action getAction() {
        return Action.ATTACK;
    }

    @Override
    Boolean switchTypeAllowed() {
        if (AutoSwitch.featureCfg.switchAllowed().containsAll(AutoSwitchConfig.TargetType.MOB_BLOCK)) {
            return true;
        }

        if (this.protoTarget instanceof BlockState) {
            return AutoSwitch.featureCfg.switchAllowed().contains(AutoSwitchConfig.TargetType.BLOCKS);
        }
        if (this.protoTarget instanceof Entity) {
            return AutoSwitch.featureCfg.switchAllowed().contains(AutoSwitchConfig.TargetType.MOBS);
        }

        AutoSwitch.logger.error("Something strange tried to trigger a switch...");
        return false;
    }

    @Override
    protected boolean stopProcessingSlot(Object target, int slot) {
        return !AutoSwitch.featureCfg.useNoDurabilityItemsWhenUnspecified() &&
               getAction().getTarget2ToolSelectorsMap().get(target) == null;
    }

}
