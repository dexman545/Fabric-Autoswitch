package autoswitch.util;

import autoswitch.AutoSwitch;
import autoswitch.events.SwitchEvent;

import net.minecraft.world.entity.player.Player;

public class EventUtil {

    public static void scheduleEvent(SwitchEvent event, boolean doSwitch, Player player, boolean doSwitchType,
                                     Object protoTarget) {
        schedulePrimaryEvent(event.setPlayer(player).setDoSwitch(doSwitch).setDoSwitchType(doSwitchType)
                                  .setProtoTarget(protoTarget));
    }

    public static void schedulePrimaryEvent(SwitchEvent event) {
        eventHandler(AutoSwitch.tickTime, 0, event);
    }

    /**
     * Add event to schedule if its pre-switch tasks are completed. Schedules switchback with proper delay.
     * <p>
     * Only run on client world/tick, not server.
     *
     * @param currentTime current tick time
     * @param deltaTime   time till switch execution in seconds
     * @param event       event to add to queue
     */
    public static void eventHandler(int currentTime, double deltaTime, SwitchEvent event) {

        // Only schedule events when they can execute
        if (!event.handlePreSwitchTasks() || !AutoSwitch.doAS || event.canNotSwitch()) return;

        if (AutoSwitch.switchState.getHasSwitched()) deltaTime += AutoSwitch.featureCfg.switchDelay();

        // Normal handing of switches
        AutoSwitch.scheduler.schedule(event, deltaTime, currentTime);

    }

}
