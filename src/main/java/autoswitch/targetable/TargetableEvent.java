package autoswitch.targetable;

import autoswitch.AutoSwitch;
import autoswitch.actions.Action;
import autoswitch.config.AutoSwitchConfig;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class TargetableEvent extends Targetable {

    protected TargetableEvent(Object protoTarget, PlayerEntity player) {
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
        return Action.EVENT;
    }

    @Override
    Boolean switchTypeAllowed() {
        return AutoSwitch.featureCfg.switchAllowed().contains(AutoSwitchConfig.TargetType.EVENTS);
    }

    @Override
    boolean stopProcessingSlot(Object target, int slot) {
        // Stat increases before the item is removed
        if (player.getInventory().selectedSlot == slot) {
            var stack = player.getMainHandStack();
            return (stack.isDamageable() && stack.getDamage() + 1 >= stack.getMaxDamage()) ||
                   !stack.isDamageable();
        }

        return false;
    }

}
