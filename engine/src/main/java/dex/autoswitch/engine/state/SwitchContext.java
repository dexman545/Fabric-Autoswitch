package dex.autoswitch.engine.state;

import dex.autoswitch.config.AutoSwitchConfig;
import dex.autoswitch.engine.Action;
import dex.autoswitch.engine.data.extensible.PlayerInventory;
import dex.autoswitch.engine.events.Scheduler;

import java.util.Optional;

public record SwitchContext(PlayerInventory<?> player, AutoSwitchConfig config,
                            Action action, Object target,
                            SwitchState switchState, Scheduler scheduler) {
    public Optional<Boolean> findSlot() {
        var maybeSlot = config.getEngine().findSlot(player, action, target);

        if (maybeSlot.isPresent()) {
            return Optional.of(switchSlot(maybeSlot.getAsInt()));
        }

        return Optional.empty();
    }

    public boolean switchSlot(int slot) {
        var switched = slot != player.currentSelectedSlot();
        player.selectSlot(slot);

        return switched;
    }

    public SwitchContext withTarget(Object o) {
        return new SwitchContext(player, config, action, o, switchState, scheduler);
    }
}
