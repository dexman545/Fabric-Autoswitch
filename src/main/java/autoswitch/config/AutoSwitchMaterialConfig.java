package autoswitch.config;

import autoswitch.config.io.ToolHandler;
import autoswitch.config.util.Comment;
import org.aeonbits.owner.Accessible;
import org.aeonbits.owner.Config;
import org.aeonbits.owner.Mutable;
import org.aeonbits.owner.Reloadable;

@Config.HotReload(type = Config.HotReloadType.ASYNC, value = 1) //set value = X for interval of X seconds. Default: 5
@Config.Sources({"file:${configDirMats}"})
public interface AutoSwitchMaterialConfig extends Config, Reloadable, Accessible, Mutable {
    @Separator(",")
    @DefaultValue("")
    @Comment("A Material")
    ToolHandler[] air();

    @Separator(",")
    @DefaultValue("")
    @Comment("A Material")
    ToolHandler[] structure_void();

    @Separator(",")
    @DefaultValue("")
    @Comment("A Material")
    ToolHandler[] portal();

    @Separator(",")
    @DefaultValue("")
    @Comment("A Material")
    ToolHandler[] carpet();

    @Separator(",")
    @DefaultValue("shears;minecraft-mending, axe;minecraft-mending, shears, axe")
    @Comment("A Material for plants such as flowers and crops")
    ToolHandler[] plant();

    @Separator(",")
    @DefaultValue("shears;minecraft-mending,shears")
    @Comment("A Material for underwater plants such as seagrass")
    ToolHandler[] underwater_plant();

    @Separator(",")
    @DefaultValue("shears;minecraft-mending, axe;minecraft-mending, shears, axe")
    @Comment("A Material for plants such as tall grass that can have a block placed in them, thus 'replacing' it.")
    ToolHandler[] replaceable_plant();

    @Separator(",")
    @DefaultValue("shears;minecraft-mending, shears")
    @Comment("A Material for underwater plants such as seagrass that can have a block placed in them, thus 'replacing' it.")
    ToolHandler[] replaceable_underwater_plant();

    @Separator(",")
    @DefaultValue("")
    @Comment("A Material")
    ToolHandler[] water();

    @Separator(",")
    @DefaultValue("")
    @Comment("A Material")
    ToolHandler[] bubble_column();

    @Separator(",")
    @DefaultValue("")
    @Comment("A Material")
    ToolHandler[] lava();

    @Separator(",")
    @DefaultValue("shovel;minecraft-silk_touch, shovel;minecraft-mending, shovel;minecraft-efficiency, shovel")
    @Comment("A Material")
    ToolHandler[] snow_layer();

    @Separator(",")
    @DefaultValue("")
    @Comment("A Material")
    ToolHandler[] fire();

    @Separator(",")
    @DefaultValue("")
    @Comment("A Material for scaffolding, rails, flowerpots, skulls, and similar. Wiki calls it 'Decoration.'")
    ToolHandler[] sub_block();

    @Separator(",")
    @DefaultValue("shears;minecraft-mending, sword;minecraft-mending, shears, sword")
    @Comment("A Material")
    ToolHandler[] cobweb();

    @Separator(",")
    @DefaultValue("pickaxe;minecraft-mending, pickaxe;minecraft-efficiency, pickaxe")
    @Comment("A Material")
    ToolHandler[] redstone_lamp();

    @Separator(",")
    @DefaultValue("shovel;minecraft-silk_touch, shovel;minecraft-mending, shovel;minecraft-efficiency, shovel")
    @Comment("A Material for blocks that come from mobs such as honey, slime, or infested blocks. Includes clay but not bone blocks.")
    ToolHandler[] organic_product();

    @Separator(",")
    @DefaultValue("shovel;minecraft-silk_touch, shovel;minecraft-efficiency, shovel;minecraft-mending, shovel")
    @Comment("A Material for the topsoil. Path, dirt, podzol, soul soil, farmland and similar.")
    ToolHandler[] soil();

    @Separator(",")
    @DefaultValue("shovel;minecraft-silk_touch,shovel,hoe")
    @Comment("A Material for organic blocks that are solid, including hay, target, and grass blocks.")
    ToolHandler[] solid_organic();

    @Separator(",")
    @DefaultValue("pickaxe;minecraft-silk_touch, any;minecraft-silk_touch, pickaxe;minecraft-efficiency, pickaxe;minecraft-mending, pickaxe")
    @Comment("A Material for ice blocks that do not melt such as packed ice.")
    ToolHandler[] dense_ice();

    @Separator(",")
    @DefaultValue("shovel;minecraft-efficiency, shovel;minecraft-mending, shovel")
    @Comment("A Material formed from a loosely compacted mass of fragments or particles, such as sand or gravel.")
    ToolHandler[] aggregate();

    @Separator(",")
    @DefaultValue("hoe;minecraft-efficiency, hoe;minecraft-mending, hoe")
    @Comment("A Material")
    ToolHandler[] sponge();

    @Separator(",")
    @DefaultValue("pickaxe;minecraft-efficiency, pickaxe;minecraft-mending, pickaxe")
    @Comment("A Material")
    ToolHandler[] shulker_box();

    @Separator(",")
    @DefaultValue("axe;minecraft-fortune, axe;minecraft-silk_touch, axe;minecraft-efficiency, axe;minecraft-mending, axe")
    @Comment("A Material for wood logs, and things crafted from them.")
    ToolHandler[] wood();

    @Separator(",")
    @DefaultValue("axe;minecraft-efficiency, axe;minecraft-mending, axe")
    @Comment("A Material")
    ToolHandler[] nether_wood();

    @Separator(",")
    @DefaultValue("sword")
    @Comment("A Material for blocks crafted from Nether stems and hyphae.")
    ToolHandler[] bamboo_sapling();

    @Separator(",")
    @DefaultValue("sword, axe;minecraft-mending, axe")
    @Comment("A Material")
    ToolHandler[] bamboo();

    @Separator(",")
    @DefaultValue("shears;minecraft-mending, shears")
    @Comment("A Material for wool and bed blocks.")
    ToolHandler[] wool();

    @Separator(",")
    @DefaultValue("")
    @Comment("A Material")
    ToolHandler[] tnt();

    @Separator(",")
    @DefaultValue("shears;minecraft-mending, shears, hoe;minecraft-silk_touch, hoe;minecraft-mending, hoe, sword;minecraft-mending, sword")
    @Comment("A Material")
    ToolHandler[] leaves();

    @Separator(",")
    @DefaultValue("pickaxe;minecraft-silk_touch, any;minecraft-silk_touch, pickaxe;minecraft-fortune, pickaxe;minecraft-mending, pickaxe")
    @Comment("A Material for glass and glass-like blocks (includes sea lanterns and conduits).")
    ToolHandler[] glass();

    @Separator(",")
    @DefaultValue("pickaxe;minecraft-silk_touch,any;minecraft-silk_touch, pickaxe;minecraft-mending, pickaxe")
    @Comment("A Material for ice that can melt.")
    ToolHandler[] ice();

    @Separator(",")
    @DefaultValue("")
    @Comment("A Material")
    ToolHandler[] cactus();

    @Separator(",")
    @DefaultValue("pickaxe;minecraft-fortune,pickaxe;minecraft-silk_touch, pickaxe;minecraft-mending, pickaxe;minecraft-efficiency, pickaxe")
    @Comment("A Material for blocks that are stone or made from it, and generally prefer to be broken by a pickaxe, such as ores.")
    ToolHandler[] stone();

    @Separator(",")
    @DefaultValue("pickaxe;minecraft-fortune, pickaxe;minecraft-mending, pickaxe")
    @Comment("A Material")
    ToolHandler[] metal();

    @Separator(",")
    @DefaultValue("shovel;minecraft-silk_touch, shovel;minecraft-fortune, shovel;minecraft-mending, shovel")
    @Comment("A Material for full sized snow blocks.")
    ToolHandler[] snow_block();

    @Separator(",")
    @DefaultValue("pickaxe;minecraft-mending, pickaxe")
    @Comment("A Material for anvils and grindstones")
    ToolHandler[] repair_station();

    @Separator(",")
    @DefaultValue("")
    @Comment("A Material")
    ToolHandler[] barrier();

    @Separator(",")
    @DefaultValue("pickaxe;minecraft-efficiency, pickaxe;minecraft-mending, pickaxe")
    @Comment("A Material")
    ToolHandler[] piston();

    @Separator(",")
    @DefaultValue("shears;minecraft-mending, shears")
    @Comment("A Material Not in use by vanilla, but has the foliage color.")
    ToolHandler[] unused_plant();

    @Separator(",")
    @DefaultValue("axe;minecraft-efficiency, axe;minecraft-mending, sword;minecraft-mending, axe, sword")
    @Comment("A Material for gourds. Includes the carved pumpkin and jack o' lantern.")
    ToolHandler[] gourd();

    @Separator(",")
    @DefaultValue("any;minecraft-silk_touch")
    @Comment("A Material for egg blocks, such as dragon and turtle eggs.")
    ToolHandler[] egg();

    @Separator(",")
    @DefaultValue("")
    @Comment("A Material")
    ToolHandler[] cake();

    @Separator(",")
    @DefaultValue("trident;minecraft:impaling, any;minecraft-sharpness, sword;minecraft-mending, axe;minecraft-mending, sword, axe")
    @Comment("An EntityGroup for gaurdians, turtles, fish, squids, dolphins, and the like.")
    ToolHandler[] aquaticEntity();

    @Separator(",")
    @DefaultValue("any;minecraft:bane_of_arthropods, any;minecraft-sharpness, sword;minecraft-mending, axe;minecraft-mending, sword, axe")
    @Comment("An EntityGroup for spiders, bees, silverfish, and the like.")
    ToolHandler[] arthropod();

    @Separator(",")
    @DefaultValue("any;minecraft-sharpness, sword;minecraft-mending, axe;minecraft-mending, sword, axe")
    @Comment("An EntityGroup for mobs that were not assigned a different one, such as pigs.")
    ToolHandler[] defaultEntity();

    @Separator(",")
    @DefaultValue("any;minecraft-sharpness, sword;minecraft-mending, axe;minecraft-mending, sword, axe")
    @Comment("An EntityGroup for evokers, pillagers, illagers, vindicators, illusioners, and ther like.")
    ToolHandler[] illager();

    @Separator(",")
    @DefaultValue("any;minecraft-smite, any;minecraft-sharpness, sword;minecraft-mending, axe;minecraft-mending, sword,axe")
    @Comment("An EntityGroup for mobs that take extra damage from smite, such as zombies, withers, and zoglins")
    ToolHandler[] undead();

    @Separator(",")
    @DefaultValue("axe;minecraft-mending, sword;minecraft-mending, axe, sword")
    @Key("minecraft-boat")
    @Comment("An Entity, specifically a boat. This is here so that an axe will be used to break it so that any mobs in the boat aren't accidently hit.")
    ToolHandler[] boat();

    // Hoe's special breaking since solid_organic will like enchants more than hoes

    @Separator(",")
    @DefaultValue("hoe;minecraft-efficiency, hoe;minecraft-mending, hoe")
    @Key("minecraft-nether_wart_block")
    @Comment("A default-provided override for hoes to function properly.")
    ToolHandler[] hoeSpecial1();

    @Separator(",")
    @DefaultValue("hoe;minecraft-efficiency, hoe;minecraft-mending, hoe")
    @Key("minecraft-warped_wart_block")
    @Comment("A default-provided override for hoes to function properly.")
    ToolHandler[] hoeSpecial2();

    @Separator(",")
    @DefaultValue("hoe;minecraft-efficiency, hoe;minecraft-mending, hoe")
    @Key("minecraft-shroomlight")
    @Comment("A default-provided override for hoes to function properly.")
    ToolHandler[] hoeSpecial3();

    @Separator(",")
    @DefaultValue("hoe;minecraft-efficiency, hoe;minecraft-mending, hoe")
    @Key("minecraft-hay_block")
    @Comment("A default-provided override for hoes to function properly.")
    ToolHandler[] hoeSpecial4();

    @Separator(",")
    @DefaultValue("hoe;minecraft-efficiency, hoe;minecraft-mending, hoe")
    @Key("minecraft-target")
    @Comment("A default-provided override for hoes to function properly.")
    ToolHandler[] hoeSpecial5();

    @Separator(",")
    @DefaultValue("hoe;minecraft-efficiency, hoe;minecraft-mending, hoe")
    @Key("minecraft-dried_kelp_block")
    @Comment("A default-provided override for hoes to function properly.")
    ToolHandler[] hoeSpecial6();


}
