package autoswitch.util;

import autoswitch.actions.Action;

/**
 * Store the switch state of the player and relevant maps
 */
public class SwitchState {

    public static boolean preventBlockAttack;
    private int prevSlot;
    private boolean hasSwitched;
    private boolean attackedEntity;

    public SwitchState() {
        prevSlot = -1;
        hasSwitched = false;
        attackedEntity = false;
        Action.resetAllActionStates();
        preventBlockAttack = false;
    }

    public boolean getHasSwitched() {
        return hasSwitched;
    }

    public void setHasSwitched(boolean hasSwitched) {
        this.hasSwitched = hasSwitched;
    }

    public int getPrevSlot() {
        return prevSlot;
    }

    public void setPrevSlot(int prevSlot) {
        this.prevSlot = prevSlot;
    }

    /**
     * Used for check if switchback is desired for mobs, short-circuits if false to perform switchback
     *
     * @return if the player attacked an entity
     */
    public boolean hasAttackedEntity() {
        return attackedEntity;
    }

    public void setAttackedEntity(boolean attackedEntity) {
        this.attackedEntity = attackedEntity;
    }


}
