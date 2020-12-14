package autoswitch.targetable;

import autoswitch.AutoSwitch;
import autoswitch.config.AutoSwitchConfig;
import autoswitch.util.TargetableUtil;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

/**
 * Targetable instance for attacking a block or entity
 */
class TargetableAttack extends AbstractTargetable {

    public TargetableAttack(Object protoTarget, PlayerEntity player) {
        super(player);
        this.player = player;
        this.protoTarget = protoTarget;
        populateToolLists(player);
    }

    @Override
    Boolean switchTypeAllowed() {
        if (AutoSwitch.featureCfg.switchbackAllowed() == AutoSwitchConfig.SwitchType.BOTH) return true;

        if (this.protoTarget instanceof BlockState) {
            return AutoSwitch.featureCfg.switchbackAllowed() == AutoSwitchConfig.SwitchType.BLOCKS;
        }
        if (this.protoTarget instanceof Entity) {
            return AutoSwitch.featureCfg.switchbackAllowed() == AutoSwitchConfig.SwitchType.MOBS;
        }

        AutoSwitch.logger.error("Something strange tried to trigger a switch...");
        return false;
    }

    @Override
    void populateToolSelection(ItemStack stack, int slot) {
        processToolSelectors(stack, slot, AutoSwitch.switchData.target2AttackActionToolSelectorsMap,
                TargetableUtil::getAttackTarget, TargetableUtil::isCorrectAttackType);
    }

    @Override
    protected boolean checkSpecialCase(Object target) {
        return !AutoSwitch.featureCfg.useNoDurablityItemsWhenUnspecified()
                && AutoSwitch.switchData.target2AttackActionToolSelectorsMap.get(target) == null;
    }
}
