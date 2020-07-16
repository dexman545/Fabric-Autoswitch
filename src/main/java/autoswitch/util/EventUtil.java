package autoswitch.util;

import autoswitch.AutoSwitch;
import autoswitch.events.SwitchEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;

public class EventUtil {

    public static ActionResult eventHandler(World world, int currentTime, double deltaTime, SwitchEvent event) {

        if (!world.isClient()) return ActionResult.PASS; // Make sure this is only run on client

        if (AutoSwitch.data.getHasSwitched()) deltaTime += AutoSwitch.switchDelay;

        AutoSwitch.scheduler.schedule(event.setWorld(true), deltaTime, currentTime);

        return ActionResult.PASS;
    }
}
