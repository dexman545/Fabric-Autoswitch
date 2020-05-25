package autoswitch;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.aeonbits.owner.Accessible;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Environment(EnvType.CLIENT)
public class AutoSwitchLists {

    //Lists of Material/Entity the tool targets
    private final ConcurrentHashMap<Object, ArrayList<UUID>> materialTargetLists = new ConcurrentHashMap<>();

    //Lists of tool slots
    private final LinkedHashMap<UUID, ArrayList<Integer>> toolLists = new LinkedHashMap<>();

    public ConcurrentHashMap<Object, ArrayList<UUID>> getToolTargetLists() {

        populateMap(this.materialTargetLists, AutoSwitch.matCfg);
        populateMap(AutoSwitch.data.useMap, AutoSwitch.usableCfg);

        return this.materialTargetLists;

    }

    private void populateMap(ConcurrentHashMap<Object, ArrayList<UUID>> map, Accessible cfg) {
        for (String key : cfg.propertyNames()) {
            String raw = cfg.getProperty(key);
            String[] split = raw.split(",");

            ArrayList<UUID> list = new ArrayList<>();
            for (String input : split) {
                //Handle normal operation where input is tool and enchantment
                UUID x = (new ToolHandler(input, 0)).getId();
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

    public LinkedHashMap<UUID, ArrayList<Integer>> getToolLists() {

        if (AutoSwitch.cfg.toolPriorityOrder() == null) {
            return toolLists;
        }

        for (String type : AutoSwitch.cfg.toolPriorityOrder()) {
            toolLists.put((new ToolHandler(type, 0).getId()), new ArrayList<>());
        }

        return toolLists;
    }
}
