package autoswitch.targetable;

import java.util.Optional;

import autoswitch.actions.Action;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Implementation of Targetable when there is no target. Intended for switchback feature.
 */
class TargetableNone extends Targetable {
    private final int prevSlot;

    public TargetableNone(int prevSlot, Player player) {
        super(player);
        this.prevSlot = prevSlot;
    }

    @Override
    protected void populateToolSelection(ItemStack stack, int slot) {
        // No need to process anything
    }

    @Override
    Optional<Integer> findSlot() {
        return Optional.of(this.prevSlot);
    }

    @Override
    Boolean switchTypeAllowed() {
        return true;
    }

    @Override
    Action getAction() {
        return null;
    }

}
