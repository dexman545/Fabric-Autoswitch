package dex.autoswitch.engine.state;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import dex.autoswitch.config.AutoSwitchConfig;
import dex.autoswitch.engine.Action;
import dex.autoswitch.engine.data.ContextKey;
import dex.autoswitch.engine.data.SelectionContext;
import dex.autoswitch.engine.data.extensible.PlayerInventory;
import dex.autoswitch.engine.events.Scheduler;

public record SwitchContext(PlayerInventory<?> player, AutoSwitchConfig config,
                            Action action, Object target,
                            SwitchState switchState, Scheduler scheduler,
                            Map<ContextKey<?>, Object> attachments) {

    @SafeVarargs
    public SwitchContext(PlayerInventory<?> player, AutoSwitchConfig config,
                         Action action, Object target,
                         SwitchState switchState, Scheduler scheduler, Map.Entry<ContextKey<?>, Object>... attachmentEntries) {
        this(player, config, action, target, switchState, scheduler, Map.ofEntries(attachmentEntries));
    }

    public Optional<Boolean> findSlot() {
        var maybeSlot = config.getEngine().findSlot(player, new SelectionContext(action, target, attachments));

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
        return new SwitchContext(player, config, action, o, switchState, scheduler, attachments);
    }

    public <T> SwitchContext with(ContextKey<T> key, T value) {
        var newAttachments = new HashMap<>(attachments);
        newAttachments.put(key, value);
        return new SwitchContext(player, config, action, target, switchState, scheduler, Collections.unmodifiableMap(newAttachments));
    }
}
