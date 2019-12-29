package autoswitch;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Material;
import org.aeonbits.owner.ConfigFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class AutoSwitchLists {

    //Lists of Material/Entity the tool targets
    HashMap<String, ArrayList<Object>> toolTargetLists = new HashMap<>();

    //Lists of tool slots
    LinkedHashMap<String, ArrayList<Integer>> toolLists = new LinkedHashMap<>();


    public HashMap<String, ArrayList<Object>> getToolTargetLists() {
        toolTargetLists.put("shear", new ArrayList<>(Arrays.asList(
                Material.PLANT, Material.UNUSED_PLANT, Material.UNDERWATER_PLANT, Material.REPLACEABLE_PLANT,
                Material.LEAVES, Material.COBWEB, Material.WOOL
        )));
        toolTargetLists.put("sword", new ArrayList<>(Arrays.asList(
                Material.BAMBOO, Material.BAMBOO_SAPLING, Material.PUMPKIN, Material.COBWEB, Material.LEAVES
        )));
        toolTargetLists.put("axe", new ArrayList<>(Arrays.asList(
                Material.WOOD, Material.PLANT, Material.REPLACEABLE_PLANT, Material.PUMPKIN
        )));
        toolTargetLists.put("pick", new ArrayList<>(Arrays.asList(
                Material.ICE, Material.PACKED_ICE, Material.METAL, Material.ANVIL, Material.SHULKER_BOX,
                Material.STONE, Material.REDSTONE_LAMP
        )));
        toolTargetLists.put("shovel", new ArrayList<>(Arrays.asList(
                Material.EARTH, Material.ORGANIC, Material.SNOW, Material.SNOW_BLOCK, Material.CLAY,
                Material.SAND
        )));
        toolTargetLists.put("silkAxe", new ArrayList<>(Arrays.asList(
                Material.EGG, Material.GLASS, Material.ICE, Material.PACKED_ICE, Material.GLASS, Material.WOOD, Material.PUMPKIN
        )));
        toolTargetLists.put("silkPick", new ArrayList<>(Arrays.asList(
                Material.EGG, Material.GLASS, Material.ICE, Material.PACKED_ICE, Material.STONE
        )));
        toolTargetLists.put("silkShovel", new ArrayList<>(Arrays.asList(
                Material.EGG, Material.GLASS, Material.ICE, Material.PACKED_ICE, Material.GLASS, Material.EARTH,
                Material.ORGANIC, Material.SAND, Material.SNOW_BLOCK, Material.SNOW
        )));
        toolTargetLists.put("fortPick", new ArrayList<>(Arrays.asList(
                Material.STONE, Material.GLASS, Material.METAL
        )));
        toolTargetLists.put("fortAxe", new ArrayList<>(Arrays.asList(
                Material.WOOD, Material.PLANT, Material.REPLACEABLE_PLANT, Material.PUMPKIN
        )));

        return toolTargetLists;
    }

    public LinkedHashMap<String, ArrayList<Integer>> getToolLists() {
        //configuration
        String config = FabricLoader.getInstance().getConfigDirectory().toString() + "/autoswitch.cfg";
        ConfigFactory.setProperty("configDir", config);
        AutoSwitchConfig cfg = ConfigFactory.create(AutoSwitchConfig.class);

        for (String type : cfg.toolPriorityOrder()) {
            toolLists.put(type, new ArrayList<>());
        }

        return toolLists;
    }
}
