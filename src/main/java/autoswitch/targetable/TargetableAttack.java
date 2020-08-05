package autoswitch.targetable;

import autoswitch.AutoSwitch;
import autoswitch.config.io.ToolHandler;
import autoswitch.util.TargetableUtil;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

/**
 * Targetable instance for attacking a block or entity
 */
class TargetableAttack extends AbstractTargetable {

    public TargetableAttack(Object protoTarget, PlayerEntity player, Boolean onMP) {
        super(player, onMP);
        this.player = player;
        this.protoTarget = protoTarget;
        populateToolLists(player);

    }

    @Override
    Boolean switchTypeAllowed() {
        if (this.protoTarget instanceof BlockState) return this.cfg.switchForBlocks();
        if (this.protoTarget instanceof Entity) return this.cfg.switchbackMobs();

        AutoSwitch.logger.error("Something strange tried to trigger a switch...");
        return false;
    }

    @Override
    void populateToolSelection(ItemStack stack, int slot) {
        processToolSelectors(stack, slot, AutoSwitch.data.toolTargetLists,
                TargetableUtil::getAttackTarget, ToolHandler::isCorrectType);
    }

    @Override
    protected boolean isUse() {
        return false;
    }

    @Override
    protected boolean checkSpecialCase(Object target) {
        return !AutoSwitch.cfg.useNoDurablityItemsWhenUnspecified()
                && this.toolTargetLists.get(target) == null;
    }
}
