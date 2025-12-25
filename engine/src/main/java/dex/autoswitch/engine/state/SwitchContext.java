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

/**
 * A heavy context object used during the event execution phase of a switch.
 * <p>
 * Unlike {@link SelectionContext}, which is optimized for the selection engine, {@link SwitchContext}
 * carries the full state required to perform side effects, such as interacting with the {@link Scheduler},
 * modifying {@link SwitchState}, or manipulating the {@link PlayerInventory}.
 * @param player The inventory of the player to consider for selection
 * @param config The current configuration
 * @param action The current {@link Action} being performed
 * @param target The current target of the {@code action}
 * @param switchState The current {@link SwitchState}, which carries information such as the previous selected slot
 * @param scheduler The {@link Scheduler} to schedule switch actions
 * @param attachments Extra context provided for this switch
 */
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
