package autoswitch.util;

import java.util.function.Predicate;

import autoswitch.api.AutoSwitchMap;
import autoswitch.api.DurabilityGetter;
import autoswitch.selectors.ItemTarget;
import autoswitch.selectors.ToolSelector;
import autoswitch.selectors.futures.FutureRegistryEntry;

import com.google.common.primitives.Ints;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;

public class SwitchData {
    /**
     * Used to process hotbar even when no target is selected. For cases where users want to use nondamageable items.
     */
    public final static IntArrayList blank = new IntArrayList();

    /**
     * Map of toolSelector input from the config -> it's int id. Used to ensure uniqueness of toolSelectors and avoid
     * processing duplicates.
     */
    public final Object2IntOpenHashMap<String> toolSelectorKeys = new Object2IntOpenHashMap<>();

    /**
     * Map of toolSelector id -> ToolSelector object.
     */
    public final Int2ObjectOpenHashMap<ToolSelector> toolSelectors = new Int2ObjectOpenHashMap<>();

    // API Maps

    /**
     * API Map - this map is passed to interfacing mods for them to add to it.
     * <p>
     * Map of tool grouping's key (eg. "pickaxe") -> the predicate accepting Item to match the item against for
     * determining if the item is in the toolGrouping.
     */
    public final AutoSwitchMap<String, Predicate<Object>> toolPredicates = new AutoSwitchMap<>();

    /**
     * API Map - this map is passed to interfacing mods for them to add to it.
     * <p>
     * Map of Class that and item extends -> the method reference that takes in the ItemStack and returns the amount of
     * durability remaining.
     */
    public final AutoSwitchMap<Class<?>, DurabilityGetter> damageMap = new AutoSwitchMap<>();

    /**
     * API Map - this map is passed to interfacing mods for them to add to it.
     * <p>
     * Map of target's key from the config -> the Object it represents.
     */
    public final AutoSwitchMap<String, Object> targets = new AutoSwitchMap<>();

    public SwitchData() {
        // Add a dummy entry for when the list of toolSelector IDs does not contain a target,
        // allowing for the implementation of using non-tools for an action.
        blank.add(Ints.fromByteArray("__BLANK__".getBytes()));
    }

}