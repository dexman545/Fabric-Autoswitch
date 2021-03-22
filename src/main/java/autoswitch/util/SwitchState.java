package autoswitch.util;

/**
 * Store the switch state of the player and relevant maps
 */
public class SwitchState {

    public static boolean preventBlockAttack;
    public TargetableCache switchActionCache;
    public TargetableCache switchInteractCache;
    private int prevSlot;
    private boolean hasSwitched;
    private boolean attackedEntity;

    public SwitchState() {
        prevSlot = -1;
        hasSwitched = false;
        attackedEntity = false;
        int maxCacheSize = 128;
        switchActionCache = new TargetableCache(maxCacheSize);
        switchInteractCache = new TargetableCache(maxCacheSize);
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
