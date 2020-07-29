package autoswitch.targetable;

import autoswitch.AutoSwitch;
import autoswitch.config.io.ToolHandler;
import autoswitch.util.TargetableUtil;
import net.minecraft.entity.Saddleable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

class TargetableUsable extends AbstractTargetable {

    /**
     * Base constructor for Targetable, initializes the class parameters and
     * fetches the target map and initial tool map based on configs passed to it
     *
     * @param player player this will effect
     * @param onMP   whether the player is on a remote server. If given null, will assume that AutoSwitch is allowed
     */
    public TargetableUsable(PlayerEntity player, Boolean onMP, Object target) {
        super(player, onMP);
        this.protoTarget = target;
        populateToolLists(player);
    }

    @Override
    protected void populateToolSelection(ItemStack stack, int slot) {
        processToolSelectors(stack, slot, AutoSwitch.data.useMap,
                TargetableUtil::getUseTarget, ToolHandler::isCorrectUseType);
    }

    @Override
    protected boolean checkSpecialCase(Object target) {
        //Don't switch if the target isn't saddled. Assumes only use for saddleable entity would be to ride it
        return AutoSwitch.cfg.checkSaddlableEntitiesForSaddle() &&
                this.protoTarget instanceof Saddleable && !((Saddleable) protoTarget).isSaddled();
    }

    @Override
    protected boolean isUse() {
        return true;
    }

    @Override
    Boolean switchTypeAllowed() {
        return this.cfg.switchUseActions();
    }

}
