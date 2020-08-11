package autoswitch.util;

import autoswitch.AutoSwitch;
import autoswitch.events.SwitchEvent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;

public class EventUtil {

    private static boolean hasScheduledSwitchback = false;

    public static ActionResult scheduleEvent(SwitchEvent event, boolean doSwitch, World world, PlayerEntity player,
                                             boolean doSwitchType, Object protoTarget) {
        return schedulePrimaryEvent(world, event.setPlayer(player)
                .setDoSwitch(doSwitch).setDoSwitchType(doSwitchType)
                .setProtoTarget(protoTarget));
    }

    public static ActionResult schedulePrimaryEvent(World world, SwitchEvent event) {
        return eventHandler(world, AutoSwitch.tickTime, 0, event);
    }

    public static ActionResult eventHandler(World world, int currentTime, double deltaTime, SwitchEvent event) {

        if (!world.isClient()) return ActionResult.PASS; // Make sure this is only run on client

        if (!event.handlePreSwitchTasks()) return ActionResult.FAIL;

        if (AutoSwitch.data.getHasSwitched()) deltaTime += AutoSwitch.cfg.switchDelay();

        //Fix switchback not being delayed
        if (event == SwitchEvent.SWITCHBACK) {

            // TODO improve so special case for switchback isn't needed
            if (AutoSwitch.data.getHasSwitched() && !SwitchEvent.player.handSwinging && !hasScheduledSwitchback) {
                AutoSwitch.scheduler.schedule(event.setWorld(true), AutoSwitch.cfg.switchbackDelay(), currentTime);
                hasScheduledSwitchback = true;
            }

            return ActionResult.PASS;
        }

        hasScheduledSwitchback = false;

        // Normal handing of switches
        AutoSwitch.scheduler.schedule(event.setWorld(true), deltaTime, currentTime);

        return ActionResult.PASS;
    }

}
