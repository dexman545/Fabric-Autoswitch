package autoswitch.util;

import autoswitch.AutoSwitch;
import autoswitch.events.SwitchEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;

public class EventUtil {

    public static ActionResult eventHandler(World world, int currentTime, double deltaTime, SwitchEvent event) {

        if (!world.isClient()) return ActionResult.PASS; // Make sure this is only run on client

        AutoSwitch.logger.error(event.getProtoTarget());

        if (AutoSwitch.data.getHasSwitched() && !AutoSwitch.scheduler.isCurrentlyTargeted(event.getProtoTarget())) {
            deltaTime += AutoSwitch.switchDelay;

        } //todo fix this being fired twice as tool switching triggers it a second time

        AutoSwitch.scheduler.schedule(event.setWorld(true), deltaTime, currentTime);

        return ActionResult.PASS;
    }
}
