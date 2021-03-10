package autoswitch.mixin.impl;

import static autoswitch.AutoSwitch.doAS;
import static autoswitch.AutoSwitch.featureCfg;
import static autoswitch.AutoSwitch.switchState;

import autoswitch.AutoSwitch;
import autoswitch.util.SwitchState;

public class DisconnectHandler {

    /**
     * Reset switch state and toggle state to their values as if they were newly inited.
     */
    public static void reset() {
        resetKeybindingToggleState();
        resetSwitchState();
    }

    private static void resetKeybindingToggleState() {
        doAS = !featureCfg.disableSwitchingOnStartup();
    }

    private static void resetSwitchState() {
        switchState = new SwitchState();
        AutoSwitch.scheduler.resetSchedule();
    }

}
