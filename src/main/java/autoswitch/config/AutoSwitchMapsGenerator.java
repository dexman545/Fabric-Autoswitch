package autoswitch.config;

import autoswitch.AutoSwitch;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.aeonbits.owner.Accessible;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Environment(EnvType.CLIENT)
public class AutoSwitchMapsGenerator {

    public AutoSwitchMapsGenerator() {
        populateToolTargetMaps();
        populateToolListMap(AutoSwitch.data.toolLists);
    }

    private void populateToolTargetMaps() {
        populateMap(AutoSwitch.data.toolTargetLists, AutoSwitch.matCfg);
        populateMap(AutoSwitch.data.useMap, AutoSwitch.usableCfg);

    }

    private void populateMap(ConcurrentHashMap<Object, CopyOnWriteArrayList<UUID>> map, Accessible cfg) {
        for (String key : cfg.propertyNames()) {
            String raw = cfg.getProperty(key);
            String[] split = raw.split(",");

            CopyOnWriteArrayList<UUID> list = new CopyOnWriteArrayList<>();
            for (String input : split) {
                //Handle normal operation where input is tool and enchantment
                UUID x = (new ToolHandler(input)).getId();
                if (x != null) {
                    list.add(x);
                }
            }

            //Populate target map with the list
            if (!list.isEmpty() && (new MaterialHandler(key)).getMat() != null) {
                map.put((new MaterialHandler(key)).getMat(), list);
            }

        }
    }

    private void populateToolListMap(Map<UUID, CopyOnWriteArrayList<Integer>> toolLists) {

        if (AutoSwitch.cfg.toolPriorityOrder() == null) {
            return;
        }

        for (String type : AutoSwitch.cfg.toolPriorityOrder()) {
            toolLists.put((new ToolHandler(type).getId()), new CopyOnWriteArrayList<>());
        }

    }
}
