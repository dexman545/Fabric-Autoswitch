package autoswitch.util;

import autoswitch.api.AutoSwitchMap;
import autoswitch.api.DurabilityGetter;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.tag.Tag;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Store the switch state of the player and relevant maps
 */
public class SwitchDataStorage {

    /**
     * Used to process hotbar even when no target is selected.
     * For cases where users want to use nondamageable items.
     */
    public final static ReferenceArrayList<UUID> blank = new ReferenceArrayList<>();
    //public Map<UUID, Pair<String, CopyOnWriteArrayList<Enchantment>>> enchantToolMap = new ConcurrentHashMap<>();
    public Object2ObjectOpenHashMap<UUID, Pair<String, ReferenceArrayList<Enchantment>>> enchantToolMap = new Object2ObjectOpenHashMap<>();
    /**
     * Maps targets of use-action -> desired tool
     */
    //public ConcurrentHashMap<Object, CopyOnWriteArrayList<UUID>> useMap = new ConcurrentHashMap<>();
    public Object2ObjectOpenHashMap<Object, ReferenceArrayList<UUID>> useMap = new Object2ObjectOpenHashMap<>();
    //public ConcurrentHashMap<Object, CopyOnWriteArrayList<UUID>> toolTargetLists = new ConcurrentHashMap<>();
    public Object2ObjectOpenHashMap<Object, ReferenceArrayList<UUID>> toolTargetLists = new Object2ObjectOpenHashMap<>();
    //public Map<UUID, CopyOnWriteArrayList<Integer>> toolLists = Collections.synchronizedMap(new LinkedHashMap<>());
    public Object2ObjectLinkedOpenHashMap<UUID, IntArrayList> toolLists = new Object2ObjectLinkedOpenHashMap<>();

    private int prevSlot;
    private boolean hasSwitched;
    private boolean attackedEntity;

    // API Maps
    public AutoSwitchMap<String, Pair<Tag<Item>, Class<?>>> toolGroupings = new AutoSwitchMap<>();
    public AutoSwitchMap<Class<?>, DurabilityGetter> damageMap = new AutoSwitchMap<>();
    public AutoSwitchMap<String, Object> targets = new AutoSwitchMap<>();
    public AutoSwitchMap<String, String> actionConfig = new AutoSwitchMap<>();
    public AutoSwitchMap<String, String> usableConfig = new AutoSwitchMap<>();

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
