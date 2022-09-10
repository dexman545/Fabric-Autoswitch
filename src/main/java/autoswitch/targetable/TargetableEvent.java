package autoswitch.targetable;

import autoswitch.actions.Action;

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
        processToolSelectors(stack, slot);//todo is this correct?
    }

    @Override
    Action getAction() {
        return Action.EVENT;
    }

    @Override
    Boolean switchTypeAllowed() {
        return true;//todo check config
    }

}
