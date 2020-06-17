package autoswitch.config;

import org.aeonbits.owner.Accessible;
import org.aeonbits.owner.Config;
import org.aeonbits.owner.Reloadable;

@Config.HotReload(type = Config.HotReloadType.ASYNC, value = 1) //set value = X for interval of X seconds. Default: 5
@Config.Sources({"file:${configDirMats}"})
public interface AutoSwitchMaterialConfig extends Config, Reloadable, Accessible {
    @Separator(",")
    @DefaultValue("")
    ToolHandler[] air();

    @Separator(",")
    @DefaultValue("")
    ToolHandler[] structure_void();

    @Separator(",")
    @DefaultValue("")
    ToolHandler[] portal();

    @Separator(",")
    @DefaultValue("")
    ToolHandler[] carpet();

    @Separator(",")
    @DefaultValue("shears;minecraft-mending, axe;minecraft-mending, shears, axe")
    ToolHandler[] plant();

    @Separator(",")
    @DefaultValue("shears;minecraft-mending,shears")
    ToolHandler[] underwater_plant();

    @Separator(",")
    @DefaultValue("shears;minecraft-mending, axe;minecraft-mending, shears, axe")
    ToolHandler[] replaceable_plant();

    @Separator(",")
    @DefaultValue("shears;minecraft-mending, shears")
    ToolHandler[] replaceable_underwater_plant();

    @Separator(",")
    @DefaultValue("")
    ToolHandler[] water();

    @Separator(",")
    @DefaultValue("")
    ToolHandler[] bubble_column();

    @Separator(",")
    @DefaultValue("")
    ToolHandler[] lava();

    @Separator(",")
    @DefaultValue("shovel;minecraft-silk_touch, shovel;minecraft-mending, shovel;minecraft-efficiency, shovel")
    ToolHandler[] snow_layer();

    @Separator(",")
    @DefaultValue("")
    ToolHandler[] fire();

    @Separator(",")
    @DefaultValue("")
    ToolHandler[] supported();

    @Separator(",")
    @DefaultValue("shears;minecraft-mending, sword;minecraft-mending, shears, sword")
    ToolHandler[] cobweb();

    @Separator(",")
    @DefaultValue("pickaxe;minecraft-mending, pickaxe;minecraft-efficiency, pickaxe")
    ToolHandler[] redstone_lamp();

    @Separator(",")
    @DefaultValue("shovel;minecraft-silk_touch, shovel;minecraft-mending, shovel;minecraft-efficiency, shovel")
    ToolHandler[] organic_product();

    @Separator(",")
    @DefaultValue("shovel;minecraft-silk_touch, shovel;minecraft-efficiency, shovel;minecraft-mending, shovel")
    ToolHandler[] soil();

    @Separator(",")
    @DefaultValue("shovel;minecraft-silk_touch,shovel,hoe")
    ToolHandler[] solid_organic();

    @Separator(",")
    @DefaultValue("pickaxe;minecraft-silk_touch, any;minecraft-silk_touch, pickaxe;minecraft-efficiency, pickaxe;minecraft-mending, pickaxe")
    ToolHandler[] dense_ice();

    @Separator(",")
    @DefaultValue("shovel;minecraft-efficiency, shovel;minecraft-mending, shovel")
    ToolHandler[] aggregate();

    @Separator(",")
    @DefaultValue("hoe;minecraft-efficiency, hoe;minecraft-mending, hoe")
    ToolHandler[] sponge();

    @Separator(",")
    @DefaultValue("pickaxe;minecraft-efficiency, pickaxe;minecraft-mending, pickaxe")
    ToolHandler[] shulker_box();

    @Separator(",")
    @DefaultValue("axe;minecraft-fortune, axe;minecraft-silk_touch, axe;minecraft-efficiency, axe;minecraft-mending, axe")
    ToolHandler[] wood();

    @Separator(",")
    @DefaultValue("axe;minecraft-efficiency, axe;minecraft-mending, axe")
    ToolHandler[] nether_wood();

    @Separator(",")
    @DefaultValue("sword")
    ToolHandler[] bamboo_sapling();

    @Separator(",")
    @DefaultValue("sword, axe;minecraft-mending, axe")
    ToolHandler[] bamboo();

    @Separator(",")
    @DefaultValue("shears;minecraft-mending, shears")
    ToolHandler[] wool();

    @Separator(",")
    @DefaultValue("")
    ToolHandler[] tnt();

    @Separator(",")
    @DefaultValue("shears;minecraft-mending, shears,  hoe;minecraft-mending, hoe, sword;minecraft-mending, sword")
    ToolHandler[] leaves();

    @Separator(",")
    @DefaultValue("pickaxe;minecraft-silk_touch, any;minecraft-silk_touch, pickaxe;minecraft-fortune, pickaxe;minecraft-mending, pickaxe")
    ToolHandler[] glass();

    @Separator(",")
    @DefaultValue("pickaxe;minecraft-silk_touch,any;minecraft-silk_touch, pickaxe;minecraft-mending, pickaxe")
    ToolHandler[] ice();

    @Separator(",")
    @DefaultValue("")
    ToolHandler[] cactus();

    @Separator(",")
    @DefaultValue("pickaxe;minecraft-fortune,pickaxe;minecraft-silk_touch, pickaxe;minecraft-mending, pickaxe")
    ToolHandler[] stone();

    @Separator(",")
    @DefaultValue("pickaxe;minecraft-fortune, pickaxe;minecraft-mending, pickaxe")
    ToolHandler[] metal();

    @Separator(",")
    @DefaultValue("shovel;minecraft-silk_touch, shovel;minecraft-fortune, shovel;minecraft-mending, shovel")
    ToolHandler[] snow_block();

    @Separator(",")
    @DefaultValue("pickaxe;minecraft-mending, pickaxe")
    ToolHandler[] repair_station();

    @Separator(",")
    @DefaultValue("")
    ToolHandler[] barrier();

    @Separator(",")
    @DefaultValue("pickaxe;minecraft-efficiency, pickaxe;minecraft-mending, pickaxe")
    ToolHandler[] piston();

    @Separator(",")
    @DefaultValue("shears;minecraft-mending, shears")
    ToolHandler[] unused_plant();

    @Separator(",")
    @DefaultValue("axe;minecraft-efficiency, axe;minecraft-mending, sword;minecraft-mending, axe, sword")
    ToolHandler[] gourd();

    @Separator(",")
    @DefaultValue("any;minecraft-silk_touch")
    ToolHandler[] egg();

    @Separator(",")
    @DefaultValue("")
    ToolHandler[] cake();

    @Separator(",")
    @DefaultValue("trident;minecraft:impaling, any;minecraft-sharpness, sword;minecraft-mending, axe;minecraft-mending, sword, axe")
    ToolHandler[] aquaticEntity();

    @Separator(",")
    @DefaultValue("any;minecraft:bane_of_arthropods, any;minecraft-sharpness, sword;minecraft-mending, axe;minecraft-mending, sword, axe")
    ToolHandler[] arthropod();

    @Separator(",")
    @DefaultValue("any;minecraft-sharpness, sword;minecraft-mending, axe;minecraft-mending, sword, axe")
    ToolHandler[] defaultEntity();

    @Separator(",")
    @DefaultValue("any;minecraft-sharpness, sword;minecraft-mending, axe;minecraft-mending, sword, axe")
    ToolHandler[] illager();

    @Separator(",")
    @DefaultValue("any;minecraft-smite, any;minecraft-sharpness, sword;minecraft-mending, axe;minecraft-mending, sword,axe")
    ToolHandler[] undead();

    @Separator(",")
    @DefaultValue("axe;minecraft-mending, sword;minecraft-mending, axe, sword")
    @Key("minecraft-boat")
    ToolHandler[] boat();


}
