package dex.autoswitch.mixin.impl;

import dex.autoswitch.Constants;

public class ConnectionHandler {
    /**
     * Reset switch state and toggle state to their values as if they were newly inited.
     */
    public static void reset() {
        resetSwitchState();
    }

    private static void resetSwitchState() {
        Constants.reset();
    }
}
