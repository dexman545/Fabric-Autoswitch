package autoswitch.util;

import autoswitch.AutoSwitch;
import autoswitch.events.SwitchEvent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class EventUtil {

    private static boolean hasScheduledSwitchback = false;

    public static void scheduleEvent(SwitchEvent event, boolean doSwitch, World world, PlayerEntity player,
                                     boolean doSwitchType, Object protoTarget) {
        schedulePrimaryEvent(world, event.setPlayer(player)
                .setDoSwitch(doSwitch).setDoSwitchType(doSwitchType)
                .setProtoTarget(protoTarget));
    }

    public static void schedulePrimaryEvent(World world, SwitchEvent event) {
        eventHandler(world, AutoSwitch.tickTime, 0, event);
    }

    public static void eventHandler(World world, int currentTime, double deltaTime, SwitchEvent event) {

        if (!world.isClient()) return; // Make sure this is only run on client

        if (!event.handlePreSwitchTasks()) return;

        if (AutoSwitch.data.getHasSwitched()) deltaTime += AutoSwitch.featureCfg.switchDelay();

        //Fix switchback not being delayed
        if (event == SwitchEvent.SWITCHBACK) {

            // TODO improve so special case for switchback isn't needed
            if (AutoSwitch.data.getHasSwitched() && !SwitchEvent.player.handSwinging && !hasScheduledSwitchback) {
                AutoSwitch.scheduler.schedule(event.setWorld(true), AutoSwitch.featureCfg.switchbackDelay(), currentTime);
                hasScheduledSwitchback = true;
            }

            return;
        }

        hasScheduledSwitchback = false;

        // Normal handing of switches
        AutoSwitch.scheduler.schedule(event.setWorld(true), deltaTime, currentTime);

    }

}
