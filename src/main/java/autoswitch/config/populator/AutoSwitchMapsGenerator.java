package autoswitch.config.populator;

import autoswitch.AutoSwitch;
import autoswitch.config.io.MaterialHandler;
import autoswitch.config.io.ToolHandler;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.aeonbits.owner.Accessible;

@Environment(EnvType.CLIENT)
public class AutoSwitchMapsGenerator {

    /**
     * Populates maps AutoSwitch uses for switching.
     * Maps populated: ToolSelectors, ToolLists, UseMap, and ToolTargetLists
     */
    public static void populateAutoSwitchMaps() {
        populateToolTargetMaps();

        // Trim the maps
        AutoSwitch.data.toolSelectors.trim();
        AutoSwitch.data.target2UseActionToolSelectorsMap.trim();
        AutoSwitch.data.target2AttackActionToolSelectorsMap.trim();
    }

    /**
     * Populate Target maps (toolTargetLists and useMap).
     */
    private static void populateToolTargetMaps() {
        populateMap(AutoSwitch.data.target2AttackActionToolSelectorsMap, AutoSwitch.attackActionCfg);
        populateMap(AutoSwitch.data.target2UseActionToolSelectorsMap, AutoSwitch.useActionCfg);

    }

    /**
     * Populates the provided map from the config file, parsing the input via ToolHandler and MaterialHandler
     * into something usable.
     *
     * @param map Map to populate.
     * @param cfg Config to pull data from.
     */
    private static void populateMap(Object2ObjectOpenHashMap<Object, IntArrayList> map, Accessible cfg) {
        for (String key : cfg.propertyNames()) {
            String raw = cfg.getProperty(key);
            String[] split = raw.split(",");

            IntArrayList list = new IntArrayList();
            for (String input : split) {
                //Handle normal operation where input is tool and enchantment
                int x = (new ToolHandler(input)).getId();
                if (x != 0) {
                    list.add(x);
                }
            }

            AutoSwitch.data.targets.computeIfAbsent(key, k -> (new MaterialHandler(k)).getMat());

            //Populate target map with the list
            if (!list.isEmpty() && AutoSwitch.data.targets.containsKey(key)) {
                map.put(AutoSwitch.data.targets.get(key), list);
            }

        }
    }

}
