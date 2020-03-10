package autoswitch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.UUID;

@SuppressWarnings("WeakerAccess")
public class AutoSwitchLists {
    private AutoSwitchConfig cfg = AutoSwitch.cfg;
    private AutoSwitchMaterialConfig matCfg = AutoSwitch.matCfg;
    private Boolean doPopulateLists = true;

    //Lists of Material/Entity the tool targets
    private HashMap<String, ArrayList<Object>> toolTargetLists = new HashMap<>();

    private HashMap<Object, ArrayList<UUID>> materialTargetLists = new HashMap<>();

    //Lists of tool slots
    private LinkedHashMap<UUID, ArrayList<Integer>> toolLists = new LinkedHashMap<>();

    public HashMap<Object, ArrayList<UUID>> getToolTargetLists() {
        if (!doPopulateLists) {
            return this.materialTargetLists;
        }

        for (String key : this.matCfg.propertyNames()) {
            String raw = this.matCfg.getProperty(key);
            String[] split = raw.split(",");
            ArrayList<UUID> list = new ArrayList<>();
            for (String input : split) {
                UUID x = (new ToolHandler(input)).getId();
                if (x != null) {
                    list.add(x);
                }
            }

            if (!list.isEmpty()) {
                this.materialTargetLists.put((new MaterialHandler(key)).getMat(), list);
            }

        }

        return this.materialTargetLists;

    }

    public LinkedHashMap<UUID, ArrayList<Integer>> getToolLists() {
        if (!doPopulateLists) {
            return toolLists;
        }

        for (String type : this.cfg.toolPriorityOrder()) {
            toolLists.put((new ToolHandler(type).getId()), new ArrayList<>());
        }

        return toolLists;
    }
}
