package autoswitch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

@SuppressWarnings("WeakerAccess")
public class AutoSwitchLists {
    private AutoSwitchConfig cfg;
    private AutoSwitchMaterialConfig matCfg;
    private Boolean doPopulateLists = true;

    //configuration
    public AutoSwitchLists(AutoSwitchConfig cfg, AutoSwitchMaterialConfig matCfg) {
        //check if the configs were null and don't try to do anything with them
        if (cfg != null && matCfg != null) {
            this.cfg = cfg;
            this.matCfg = matCfg;
        } else {
            doPopulateLists = false;
        }
    }

    //Lists of Material/Entity the tool targets
    private HashMap<String, ArrayList<Object>> toolTargetLists = new HashMap<>();

    //Lists of tool slots
    private LinkedHashMap<String, ArrayList<Integer>> toolLists = new LinkedHashMap<>();


    public HashMap<String, ArrayList<Object>> getToolTargetLists() {
        toolTargetLists.put("shear", new ArrayList<>());
        toolTargetLists.put("sword", new ArrayList<>());
        toolTargetLists.put("axe", new ArrayList<>());
        toolTargetLists.put("pick", new ArrayList<>());
        toolTargetLists.put("shovel", new ArrayList<>());
        toolTargetLists.put("silkAxe", new ArrayList<>());
        toolTargetLists.put("silkPick", new ArrayList<>());
        toolTargetLists.put("silkShovel", new ArrayList<>());
        toolTargetLists.put("fortPick", new ArrayList<>());
        toolTargetLists.put("fortAxe", new ArrayList<>());

        if (!doPopulateLists) {
            return toolTargetLists;
        }

        for (MaterialHandler m : this.matCfg.axeTargetMaterials()) {
            toolTargetLists.get("axe").add(m.getMat());
        }

        for (MaterialHandler m : this.matCfg.axeSilkTargetMaterials()) {
            toolTargetLists.get("silkAxe").add(m.getMat());
        }

        for (MaterialHandler m : this.matCfg.axeFortuneTargetMaterials()) {
            toolTargetLists.get("fortAxe").add(m.getMat());
        }

        for (MaterialHandler m : this.matCfg.pickTargetMaterials()) {
            toolTargetLists.get("pick").add(m.getMat());
        }

        for (MaterialHandler m : this.matCfg.pickSilkTargetMaterials()) {
            toolTargetLists.get("silkPick").add(m.getMat());
        }

        for (MaterialHandler m : this.matCfg.pickFortuneTargetMaterials()) {
            toolTargetLists.get("fortPick").add(m.getMat());
        }

        for (MaterialHandler m : this.matCfg.shearTargetMaterials()) {
            toolTargetLists.get("shear").add(m.getMat());
        }

        for (MaterialHandler m : this.matCfg.swordTargetMaterials()) {
            toolTargetLists.get("sword").add(m.getMat());
        }

        for (MaterialHandler m : this.matCfg.shovelTargetMaterials()) {
            toolTargetLists.get("shovel").add(m.getMat());
        }

        for (MaterialHandler m : this.matCfg.shovelSilkTargetMaterials()) {
            toolTargetLists.get("silkShovel").add(m.getMat());
        }

        return toolTargetLists;
    }

    public LinkedHashMap<String, ArrayList<Integer>> getToolLists() {
        if (!doPopulateLists) {
            return toolLists;
        }

        for (String type : this.cfg.toolPriorityOrder()) {
            toolLists.put(type, new ArrayList<>());
        }

        return toolLists;
    }
}
