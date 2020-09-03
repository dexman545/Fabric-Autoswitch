package autoswitch.util;

import autoswitch.api.AutoSwitchMap;
import autoswitch.api.DurabilityGetter;
import com.google.common.primitives.Ints;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.tag.Tag;
import org.apache.commons.lang3.tuple.Pair;

public class SwitchData {
    /**
     * Target object for use with `bow_action`
     */
    public final static ItemTarget itemTarget = new ItemTarget();

    /**
     * Used to process hotbar even when no target is selected.
     * For cases where users want to use nondamageable items.
     */
    public final static IntArrayList blank = new IntArrayList();

    /**
     * Map of target object to list of toolSelector IDs for the 'use' action.
     */
    public final Object2ObjectOpenHashMap<Object, IntArrayList> target2UseActionToolSelectorsMap =
            new Object2ObjectOpenHashMap<>();
    /**
     * Map of target object to list of toolSelector IDs for the 'attack' action.
     */
    public final Object2ObjectOpenHashMap<Object, IntArrayList> target2AttackActionToolSelectorsMap =
            new Object2ObjectOpenHashMap<>();

    /**
     * Map of toolSelector input from the config -> it's int id.
     * Used o ensure uniqueness of toolSelectors and avoid processing duplicates.
     */
    public final Object2IntOpenHashMap<String> toolSelectorKeys = new Object2IntOpenHashMap<>();

    /**
     * Map of toolSelector id -> toolSelector object (string toolId, list of Enchantments).
     */
    public final Int2ObjectOpenHashMap<Pair<String, ReferenceArrayList<Enchantment>>> toolSelectors =
            new Int2ObjectOpenHashMap<>();

    // API Maps
    /**
     * API Map - this map is passed to interfacing mods for them to add to it.
     * <p>
     * Map of tool grouping's key (eg. "pickaxe") -> the Tag and/or Class to match the item against for
     * determining if the item is in the toolGrouping.
     */
    public final AutoSwitchMap<String, Pair<Tag<Item>, Class<?>>> toolGroupings = new AutoSwitchMap<>();

    /**
     * API Map - this map is passed to interfacing mods for them to add to it.
     * <p>
     * Map of Class that and item extends -> the method reference that takes in the ItemStack and
     * returns the amount of durability remaining.
     */
    public final AutoSwitchMap<Class<?>, DurabilityGetter> damageMap = new AutoSwitchMap<>();

    /**
     * API Map - this map is passed to interfacing mods for them to add to it.
     * <p>
     * Map of target's key from the config -> the Object it represents.
     */
    public final AutoSwitchMap<String, Object> targets = new AutoSwitchMap<>();

    /**
     * API Map - this map is passed to interfacing mods for them to add to it.
     * <p>
     * A map of config key -> config value for the "attack" action.
     */
    public final AutoSwitchMap<String, String> attackConfig = new AutoSwitchMap<>();

    /**
     * API Map - this map is passed to interfacing mods for them to add to it.
     * <p>
     * A map of config key -> config value for the "use" action.
     */
    public final AutoSwitchMap<String, String> usableConfig = new AutoSwitchMap<>();

    public SwitchData() {
        // Add a dummy entry for when the list of toolSelector IDs does not contain a target,
        // allowing for the implementation of using non-tools for an action.
        blank.add(Ints.fromByteArray("__BLANK__".getBytes()));
    }
}