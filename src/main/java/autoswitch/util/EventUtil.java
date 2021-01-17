package autoswitch.util;

import autoswitch.AutoSwitch;
import autoswitch.events.SwitchEvent;

import net.minecraft.entity.player.PlayerEntity;

public class EventUtil {

    private static boolean hasScheduledSwitchback = false;

    public static void scheduleEvent(SwitchEvent event, boolean doSwitch, PlayerEntity player, boolean doSwitchType,
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

        if (!event.handlePreSwitchTasks()) return;

        if (AutoSwitch.switchState.getHasSwitched()) deltaTime += AutoSwitch.featureCfg.switchDelay();

        //Fix switchback not being delayed
        if (event == SwitchEvent.SWITCHBACK) {

            // TODO improve so special case for switchback isn't needed
            if (AutoSwitch.switchState.getHasSwitched() && !SwitchEvent.player.handSwinging &&
                !hasScheduledSwitchback) {
                AutoSwitch.scheduler.schedule(event, AutoSwitch.featureCfg.switchbackDelay(), currentTime);
                hasScheduledSwitchback = true;
            }

            return;
        }

        hasScheduledSwitchback = false;

        // Normal handing of switches
        AutoSwitch.scheduler.schedule(event, deltaTime, currentTime);

    }

}
