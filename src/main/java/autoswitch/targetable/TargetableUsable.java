package autoswitch.targetable;

import autoswitch.AutoSwitch;
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
     */
    public TargetableUsable(PlayerEntity player, Object target) {
        super(player);
        this.protoTarget = target;
        populateToolLists(player);
    }

    @Override
    protected void populateToolSelection(ItemStack stack, int slot) {
        processToolSelectors(stack, slot, AutoSwitch.data.target2UseActionToolSelectorsMap,
                TargetableUtil::getUseTarget, TargetableUtil::isCorrectUseType);
    }

    @Override
    protected boolean checkSpecialCase(Object target) {
        //Don't switch if the target isn't saddled. Assumes only use for saddleable entity would be to ride it
        return AutoSwitch.featureCfg.checkSaddlableEntitiesForSaddle() &&
                this.protoTarget instanceof Saddleable && !((Saddleable) protoTarget).isSaddled();
    }

    @Override
    protected boolean isUse() {
        return true;
    }

    @Override
    Boolean switchTypeAllowed() {
        return AutoSwitch.featureCfg.switchUseActions();
    }

}
