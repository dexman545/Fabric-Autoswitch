package autoswitch.util;

import autoswitch.api.AutoSwitchMap;
import autoswitch.api.DurabilityGetter;
import com.google.common.primitives.Ints;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.tag.Tag;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Store the switch state of the player and relevant maps
 */
public class SwitchDataStorage {

    /**
     * Used to process hotbar even when no target is selected.
     * For cases where users want to use nondamageable items.
     */
    public final static IntArrayList blank = new IntArrayList();

    /**
     * Maps targets of use-action -> desired tool
     */
    public final Object2ObjectOpenHashMap<Object, IntArrayList> useMap = new Object2ObjectOpenHashMap<>();
    public final Object2ObjectOpenHashMap<Object, IntArrayList> toolTargetLists = new Object2ObjectOpenHashMap<>();
    public final Int2ObjectLinkedOpenHashMap<IntArrayList> toolLists = new Int2ObjectLinkedOpenHashMap<>();

    public final Object2IntOpenHashMap<String> toolSelectorKeys = new Object2IntOpenHashMap<>();
    public final Int2ObjectOpenHashMap<Pair<String, ReferenceArrayList<Enchantment>>> toolSelectors = new Int2ObjectOpenHashMap<>();

    // API Maps
    public final AutoSwitchMap<String, Pair<Tag<Item>, Class<?>>> toolGroupings = new AutoSwitchMap<>();
    public final AutoSwitchMap<Class<?>, DurabilityGetter> damageMap = new AutoSwitchMap<>();
    public final AutoSwitchMap<String, Object> targets = new AutoSwitchMap<>();
    public final AutoSwitchMap<String, String> actionConfig = new AutoSwitchMap<>();
    public final AutoSwitchMap<String, String> usableConfig = new AutoSwitchMap<>();

    private int prevSlot;
    private boolean hasSwitched;
    private boolean attackedEntity;

    public SwitchDataStorage() {
        prevSlot = -1;
        hasSwitched = false;
        attackedEntity = false;
        blank.add(Ints.fromByteArray("__BLANK__".getBytes()));
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
