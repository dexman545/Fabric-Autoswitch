package autoswitch.config.populator;

import autoswitch.AutoSwitch;
import autoswitch.config.io.MaterialHandler;
import autoswitch.config.io.ToolHandler;
import it.unimi.dsi.fastutil.ints.AbstractInt2ObjectMap;
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
    public AutoSwitchMapsGenerator() {
        populateToolTargetMaps();
        populateToolListMap(AutoSwitch.data.toolLists);

        // Trim the maps
        AutoSwitch.data.toolSelectors.trim();
        AutoSwitch.data.toolLists.trim();
        AutoSwitch.data.useMap.trim();
        AutoSwitch.data.toolTargetLists.trim();
    }

    /**
     * Populate Target maps (toolTargetLists and useMap).
     */
    private void populateToolTargetMaps() {
        populateMap(AutoSwitch.data.toolTargetLists, AutoSwitch.matCfg);
        populateMap(AutoSwitch.data.useMap, AutoSwitch.usableCfg);

    }

    /**
     * Populates the provided map from the config file, parsing the input via ToolHandler and MaterialHandler
     * into something usable.
     *
     * @param map Map to populate.
     * @param cfg Config to pull data from.
     */
    private void populateMap(Object2ObjectOpenHashMap<Object, IntArrayList> map, Accessible cfg) {
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

    /**
     * Populate tool lists with order from the primary config. This allows for global overriding of tool selection
     * preferences.
     *
     * @param toolLists list to populate from primary config.
     */
    private void populateToolListMap(AbstractInt2ObjectMap<IntArrayList> toolLists) {

        if (AutoSwitch.cfg.toolPriorityOrder() != null) {
            for (String type : AutoSwitch.cfg.toolPriorityOrder()) {
                toolLists.put((new ToolHandler(type).getId()), new IntArrayList());
            }
        }

    }
}
