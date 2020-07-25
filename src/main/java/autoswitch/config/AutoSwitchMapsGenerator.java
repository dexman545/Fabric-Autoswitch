package autoswitch.config;

import autoswitch.AutoSwitch;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.aeonbits.owner.Accessible;

import java.util.Map;
import java.util.UUID;
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

    private void populateMap(Object2ObjectOpenHashMap<Object, ReferenceArrayList<UUID>> map, Accessible cfg) {
        for (String key : cfg.propertyNames()) {
            String raw = cfg.getProperty(key);
            String[] split = raw.split(",");

            ReferenceArrayList<UUID> list = new ReferenceArrayList<>();
            for (String input : split) {
                //Handle normal operation where input is tool and enchantment
                UUID x = (new ToolHandler(input)).getId();
                if (x != null) {
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

    private void populateToolListMap(Map<UUID, IntArrayList> toolLists) {

        if (AutoSwitch.cfg.toolPriorityOrder() == null) {
            return;
        }

        for (String type : AutoSwitch.cfg.toolPriorityOrder()) {
            toolLists.put((new ToolHandler(type).getId()), new IntArrayList());
        }

    }
}
