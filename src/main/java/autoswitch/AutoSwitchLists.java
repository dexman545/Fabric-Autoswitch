package autoswitch;

import net.fabricmc.loader.api.FabricLoader;
import org.aeonbits.owner.ConfigFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class AutoSwitchLists {
    private AutoSwitchConfig cfg;
    private AutoSwitchMaterialConfig matCfg;

    //configuration
    public AutoSwitchLists() {
        String config = FabricLoader.getInstance().getConfigDirectory().toString() + "/autoswitch.cfg";
        String configMats = FabricLoader.getInstance().getConfigDirectory().toString() + "/autoswitchMaterials.cfg";
        ConfigFactory.setProperty("configDir", config);
        ConfigFactory.setProperty("configDirMats", configMats);
        this.cfg = ConfigFactory.create(AutoSwitchConfig.class);
        this.matCfg = ConfigFactory.create(AutoSwitchMaterialConfig.class);
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

        for (MaterialHandler m : matCfg.axeTargetMaterials()) {
            toolTargetLists.get("axe").add(m.getMat());
        }

        for (MaterialHandler m : matCfg.axeSilkTargetMaterials()) {
            toolTargetLists.get("silkAxe").add(m.getMat());
        }

        for (MaterialHandler m : matCfg.axeFortuneTargetMaterials()) {
            toolTargetLists.get("fortAxe").add(m.getMat());
        }

        for (MaterialHandler m : matCfg.pickTargetMaterials()) {
            toolTargetLists.get("pick").add(m.getMat());
        }

        for (MaterialHandler m : matCfg.pickSilkTargetMaterials()) {
            toolTargetLists.get("silkPick").add(m.getMat());
        }

        for (MaterialHandler m : matCfg.pickFortuneTargetMaterials()) {
            toolTargetLists.get("fortPick").add(m.getMat());
        }

        for (MaterialHandler m : matCfg.shearTargetMaterials()) {
            toolTargetLists.get("shear").add(m.getMat());
        }

        for (MaterialHandler m : matCfg.swordTargetMaterials()) {
            toolTargetLists.get("sword").add(m.getMat());
        }

        for (MaterialHandler m : matCfg.shovelTargetMaterials()) {
            toolTargetLists.get("shovel").add(m.getMat());
        }

        for (MaterialHandler m : matCfg.shovelSilkTargetMaterials()) {
            toolTargetLists.get("silkShovel").add(m.getMat());
        }

        return toolTargetLists;
    }

    public LinkedHashMap<String, ArrayList<Integer>> getToolLists() {
        for (String type : this.cfg.toolPriorityOrder()) {
            toolLists.put(type, new ArrayList<>());
        }

        return toolLists;
    }
}
