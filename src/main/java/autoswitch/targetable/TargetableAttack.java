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
class TargetableAttack extends Targetable {

    public TargetableAttack(Object protoTarget, PlayerEntity player) {
        super(player);
        this.player = player;
        this.protoTarget = protoTarget;
        populateToolLists();
    }

    @Override
    void populateToolSelection(ItemStack stack, int slot) {
        processToolSelectors(stack, slot, AutoSwitch.switchData.target2AttackActionToolSelectorsMap,
                             TargetableUtil::getAttackTarget, TargetableUtil::isCorrectAttackType);
    }

    @Override
    Boolean switchTypeAllowed() {
        if (AutoSwitch.featureCfg.switchAllowed() == AutoSwitchConfig.TargetType.BOTH) return true;

        if (this.protoTarget instanceof BlockState) {
            return AutoSwitch.featureCfg.switchAllowed() == AutoSwitchConfig.TargetType.BLOCKS;
        }
        if (this.protoTarget instanceof Entity) {
            return AutoSwitch.featureCfg.switchAllowed() == AutoSwitchConfig.TargetType.MOBS;
        }

        AutoSwitch.logger.error("Something strange tried to trigger a switch...");
        return false;
    }

    @Override
    protected boolean checkSpecialCase(Object target) {
        return !AutoSwitch.featureCfg.useNoDurabilityItemsWhenUnspecified() &&
               AutoSwitch.switchData.target2AttackActionToolSelectorsMap.get(target) == null;
    }

}
