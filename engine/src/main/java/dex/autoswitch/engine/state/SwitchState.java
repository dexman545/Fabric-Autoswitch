package dex.autoswitch.engine.state;

public class SwitchState {
    private int prevSlot;
    private boolean awaitingSwitchback;
    private boolean preventBlockAttack;
    private boolean doOffhandSwitch;

    public void reset() {
        prevSlot = 0;
        awaitingSwitchback = false;
        preventBlockAttack = false;
        doOffhandSwitch = false;
    }

    public int getPrevSlot() {
        return prevSlot;
    }

    public void setPrevSlot(int prevSlot) {
        if (!awaitingSwitchback) {
            awaitingSwitchback = true;
            this.prevSlot = prevSlot;
        }
    }

    public boolean awaitingSwitchback() {
        return awaitingSwitchback;
    }

    public void setAwaitingSwitchback(boolean awaitingSwitchback) {
        this.awaitingSwitchback = awaitingSwitchback;
    }

    public boolean preventBlockAttack() {
        return preventBlockAttack;
    }

    public void setPreventBlockAttack(boolean preventBlockAttack) {
        this.preventBlockAttack = preventBlockAttack;
    }

    public boolean doOffhandSwitch() {
        return doOffhandSwitch;
    }

    public void setDoOffhandSwitch(boolean doOffhandSwitch) {
        this.doOffhandSwitch = doOffhandSwitch;
    }
}
