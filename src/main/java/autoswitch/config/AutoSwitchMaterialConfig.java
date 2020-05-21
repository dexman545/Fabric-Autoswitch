package autoswitch.config;

import autoswitch.ToolHandler;
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
    @DefaultValue("shears, axe")
    ToolHandler[] plant();

    @Separator(",")
    @DefaultValue("shears")
    ToolHandler[] underwater_plant();

    @Separator(",")
    @DefaultValue("shears, axe")
    ToolHandler[] replaceable_plant();

    @Separator(",")
    @DefaultValue("shears")
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
    @DefaultValue("shovel;minecraft-silk_touch,shovel")
    ToolHandler[] snow_layer();

    @Separator(",")
    @DefaultValue("")
    ToolHandler[] fire();

    @Separator(",")
    @DefaultValue("")
    ToolHandler[] supported();

    @Separator(",")
    @DefaultValue("shears, sword")
    ToolHandler[] cobweb();

    @Separator(",")
    @DefaultValue("pickaxe")
    ToolHandler[] redstone_lamp();

    @Separator(",")
    @DefaultValue("shovel;minecraft-silk_touch,shovel")
    ToolHandler[] organic_product();

    @Separator(",")
    @DefaultValue("shovel;minecraft-silk_touch,shovel")
    ToolHandler[] soil();

    @Separator(",")
    @DefaultValue("shovel;minecraft-silk_touch,shovel,hoe")
    ToolHandler[] solid_organic();

    @Separator(",")
    @DefaultValue("pickaxe;minecraft-silk_touch,any;minecraft-silk_touch,pickaxe")
    ToolHandler[] dense_ice();

    @Separator(",")
    @DefaultValue("shovel")
    ToolHandler[] aggregate();

    @Separator(",")
    @DefaultValue("hoe")
    ToolHandler[] sponge();

    @Separator(",")
    @DefaultValue("pickaxe")
    ToolHandler[] shulker_box();

    @Separator(",")
    @DefaultValue("axe;minecraft-fortune,axe;minecraft-silk_touch,axe")
    ToolHandler[] wood();

    @Separator(",")
    @DefaultValue("axe")
    ToolHandler[] nether_wood();

    @Separator(",")
    @DefaultValue("sword")
    ToolHandler[] bamboo_sapling();

    @Separator(",")
    @DefaultValue("sword, axe")
    ToolHandler[] bamboo();

    @Separator(",")
    @DefaultValue("shears")
    ToolHandler[] wool();

    @Separator(",")
    @DefaultValue("")
    ToolHandler[] tnt();

    @Separator(",")
    @DefaultValue("shears, hoe, sword")
    ToolHandler[] leaves();

    @Separator(",")
    @DefaultValue("pickaxe;minecraft-silk_touch,any;minecraft-silk_touch,pickaxe;minecraft-fortune,pickaxe")
    ToolHandler[] glass();

    @Separator(",")
    @DefaultValue("pickaxe;minecraft-silk_touch,any;minecraft-silk_touch,pickaxe")
    ToolHandler[] ice();

    @Separator(",")
    @DefaultValue("")
    ToolHandler[] cactus();

    @Separator(",")
    @DefaultValue("pickaxe;minecraft-fortune,pickaxe;minecraft-silk_touch,pickaxe")
    ToolHandler[] stone();

    @Separator(",")
    @DefaultValue("pickaxe,pickaxe;minecraft-fortune")
    ToolHandler[] metal();

    @Separator(",")
    @DefaultValue("shovel;minecraft-silk_touch,shovel;minecraft-fortune,shovel")
    ToolHandler[] snow_block();

    @Separator(",")
    @DefaultValue("pickaxe")
    ToolHandler[] repair_station();

    @Separator(",")
    @DefaultValue("")
    ToolHandler[] barrier();

    @Separator(",")
    @DefaultValue("pickaxe")
    ToolHandler[] piston();

    @Separator(",")
    @DefaultValue("shears")
    ToolHandler[] unused_plant();

    @Separator(",")
    @DefaultValue("axe, sword")
    ToolHandler[] gourd();

    @Separator(",")
    @DefaultValue("any;minecraft-silk_touch")
    ToolHandler[] egg();

    @Separator(",")
    @DefaultValue("")
    ToolHandler[] cake();

    @Separator(",")
    @DefaultValue("trident;minecraft:impaling,any;minecraft-sharpness,sword,axe")
    ToolHandler[] aquaticEntity();

    @Separator(",")
    @DefaultValue("any;minecraft:bane_of_arthropods,any;minecraft-sharpness,sword,axe")
    ToolHandler[] arthropod();

    @Separator(",")
    @DefaultValue("any;minecraft-sharpness,sword,axe")
    ToolHandler[] defaultEntity();

    @Separator(",")
    @DefaultValue("any;minecraft-sharpness,sword,axe")
    ToolHandler[] illager();

    @Separator(",")
    @DefaultValue("any;minecraft-smite,any;minecraft-sharpness,sword,axe")
    ToolHandler[] undead();

    @Separator(",")
    @DefaultValue("axe,sword")
    ToolHandler[] boat();

    @Separator(",")
    @DefaultValue("minecraft-pig;minecraft-carrot_on_a_stick,minecraft-strider;minecraft-warped_fungus_on_a_stick,minecraft:creeper;minecraft:flint_and_steel")
    ToolHandler[] useTool(); //Item to use when right-clicking on something



}
