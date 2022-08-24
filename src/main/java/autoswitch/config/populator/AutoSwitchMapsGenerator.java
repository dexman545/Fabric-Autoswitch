package autoswitch.config.populator;

import java.util.Map;

import autoswitch.AutoSwitch;
import autoswitch.actions.Action;
import autoswitch.config.io.TargetHandler;
import autoswitch.selectors.ToolSelector;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.aeonbits.owner.Accessible;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class AutoSwitchMapsGenerator {

    /**
     * Populates maps AutoSwitch uses for switching. Maps populated: ToolSelectors, ToolLists, UseMap, and
     * ToolTargetLists
     */
    public static void populateAutoSwitchMaps() {
        populateTarget2ToolSelectorMap();

        // Trim the maps
        AutoSwitch.switchData.toolSelectors.trim();

        for (Action action : Action.values()) {
            action.getTarget2ToolSelectorsMap().trim();
        }
    }

    /**
     * Populate tool selector maps for each action.
     */
    private static void populateTarget2ToolSelectorMap() {
        for (Action action : Action.values()) {
            populateMap(action.getTarget2ToolSelectorsMap(), action.getActionConfig());
        }

    }

    /**
     * Populates the provided map from the config file, parsing the input via ToolHandler and MaterialHandler into
     * something usable.
     *
     * @param map Map to populate.
     * @param cfg Config to pull data from.
     */
    private static void populateMap(Map<Object, IntArrayList> map, Accessible cfg) {
        for (String key : cfg.propertyNames()) {
            String raw = cfg.getProperty(key);
            String[] split = raw.split(",");

            IntArrayList toolIdList = new IntArrayList();
            for (String input : split) {
                if ("".equals(input)) continue;
                // Handle normal operation where input is tool and enchantment
                int x = (new ToolSelector(input)).getId();
                if (x != 0) {
                    toolIdList.add(x);
                }
            }

            AutoSwitch.switchData.targets.computeIfAbsent(key, TargetHandler::getTarget);

            // Populate target map with the toolIdList
            if (AutoSwitch.switchData.targets.containsKey(key)) {
                map.put(AutoSwitch.switchData.targets.get(key), toolIdList);
            }

        }
    }

}
