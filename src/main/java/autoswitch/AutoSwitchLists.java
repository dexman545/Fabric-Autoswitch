package autoswitch;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

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

        for (String key : AutoSwitch.matCfg.propertyNames()) {
            String raw = AutoSwitch.matCfg.getProperty(key);
            String[] split = raw.split(",");

            ArrayList<UUID> list = new ArrayList<>();
            for (String input : split) {

                //Handle special case of useTool that takes in targets and tool to use
                if (key.equals("useTool")) {
                    ToolHandler v = (new ToolHandler(input, 1));
                    MaterialHandler c = (new MaterialHandler(v.getTag()));
                    AutoSwitch.data.useMap.put(c.getMat(), v.getEnchTag());

                    continue;
                }

                //Handle normal operation where input is tool and enchantment
                UUID x = (new ToolHandler(input, 0)).getId();
                if (x != null) {
                    list.add(x);
                }
            }

            //Populate target map with the list
            if (!list.isEmpty() && (new MaterialHandler(key)).getMat() != null) {
                this.materialTargetLists.put((new MaterialHandler(key)).getMat(), list);
            }

        }

        return this.materialTargetLists;

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
