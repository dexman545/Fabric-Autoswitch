package autoswitch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.UUID;

@SuppressWarnings("WeakerAccess")
public class AutoSwitchLists {
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

        for (String key : AutoSwitch.matCfg.propertyNames()) {
            String raw = AutoSwitch.matCfg.getProperty(key);
            String[] split = raw.split(",");

            ArrayList<UUID> list = new ArrayList<>();
            for (String input : split) {

                if (key.equals("useTool")) {
                    ToolHandler v = (new ToolHandler(input, 1));
                    MaterialHandler c = (new MaterialHandler(v.getTag()));
                    AutoSwitch.data.useMap.put(c.getMat(), v.getEnchTag());

                    continue;
                }

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

        if (AutoSwitch.cfg.toolPriorityOrder() == null) {
            return toolLists;
        }

        for (String type : AutoSwitch.cfg.toolPriorityOrder()) {
            toolLists.put((new ToolHandler(type).getId()), new ArrayList<>());
        }

        return toolLists;
    }
}
