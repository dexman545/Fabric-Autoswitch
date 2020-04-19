package autoswitch;

import net.minecraft.enchantment.Enchantment;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

/**
 * Store the switch state of the player and relevant maps
 */
public class SwitchDataStorage {

    private int prevSlot;
    private boolean hasSwitched;
    private boolean attackedEntity;

    public Map<UUID, Pair<String , Enchantment>> enchantToolMap = new HashMap<>();

    /**
     * Maps targets of use-action -> desired tool
     */
    public Map<Object, String> useMap = new HashMap<>();

    public HashMap<Object, ArrayList<UUID>> toolTargetLists;
    public LinkedHashMap<UUID, ArrayList<Integer>> toolLists;

    public SwitchDataStorage() {
        prevSlot = -1;
        hasSwitched = false;
        attackedEntity = false;
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
