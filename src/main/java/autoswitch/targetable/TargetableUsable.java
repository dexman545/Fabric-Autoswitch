package autoswitch.targetable;

import autoswitch.AutoSwitch;
import autoswitch.actions.Action;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PlayerRideable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

class TargetableUsable extends Targetable {

    /**
     * Base constructor for Targetable, initializes the class parameters and fetches the target map and initial tool map
     * based on configs passed to it
     *
     * @param player player this will effect
     */
    public TargetableUsable(Player player, Object target) {
        super(player);
        this.protoTarget = target;
        populateToolLists();
    }

    @Override
    protected void populateToolSelection(ItemStack stack, int slot) {
        processToolSelectors(stack, slot);
    }

    @Override
    Action getAction() {
        return Action.INTERACT;
    }

    @Override
    Boolean switchTypeAllowed() {
        return AutoSwitch.featureCfg.switchUseActions();
    }

    @Override
    protected boolean stopProcessingSlot(Object target, int slot) {
        // Don't switch if the target isn't saddled. Assumes only use for saddleable entity would be to ride it
        return AutoSwitch.featureCfg.checkSaddlableEntitiesForSaddle() && this.protoTarget instanceof PlayerRideable &&
               (protoTarget instanceof Mob) && !((Mob) protoTarget).isSaddled();
    }

}
