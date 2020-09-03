package autoswitch.mixin_impl;

import autoswitch.util.SwitchState;

import static autoswitch.AutoSwitch.*;

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
        mowing = false;
    }

    private static void resetSwitchState() {
        switchState = new SwitchState();
    }

}
