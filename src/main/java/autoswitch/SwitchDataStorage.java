package autoswitch;

import net.minecraft.enchantment.Enchantment;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

/**
 * Store the switch state of the player in a safe place for use with the switchback feature
 */
@SuppressWarnings("WeakerAccess")
public class SwitchDataStorage {

    private int prevSlot;
    private boolean hasSwitched;
    private boolean attackedEntity;

    public Map<UUID, Pair<String , Enchantment>> enchantToolMap = new HashMap<>();

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

    public boolean isAttackedEntity() {
        return attackedEntity;
    }

    public void setAttackedEntity(boolean attackedEntity) {
        this.attackedEntity = attackedEntity;
    }
}
