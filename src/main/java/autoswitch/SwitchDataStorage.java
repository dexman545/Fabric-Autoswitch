package autoswitch;

import net.minecraft.enchantment.Enchantment;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Store the switch state of the player and relevant maps
 */
public class SwitchDataStorage {

    /**
     * Used to process hotbar even when no target is selected.
     * For cases where users want to use nondamageable items.
     */
    public final static ArrayList<UUID> blank = new ArrayList<>();
    public Map<UUID, Pair<String, Enchantment>> enchantToolMap = new ConcurrentHashMap<>();
    /**
     * Maps targets of use-action -> desired tool
     */
    public ConcurrentHashMap<Object, ArrayList<UUID>> useMap = new ConcurrentHashMap<>();
    public ConcurrentHashMap<Object, ArrayList<UUID>> toolTargetLists;
    public LinkedHashMap<UUID, ArrayList<Integer>> toolLists;
    private int prevSlot;
    private boolean hasSwitched;
    private boolean attackedEntity;

    public SwitchDataStorage() {
        prevSlot = -1;
        hasSwitched = false;
        attackedEntity = false;
        blank.add(UUID.randomUUID());
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
