package autoswitch.targetable;

import java.util.Optional;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

/**
 * Implementation of Targetable when there is no target. Intended for switchback feature.
 */
class TargetableNone extends AbstractTargetable {
    private final int prevSlot;

    public TargetableNone(int prevSlot, PlayerEntity player) {
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

}
