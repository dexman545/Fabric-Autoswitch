package autoswitch.config;

import autoswitch.config.util.Comment;
import autoswitch.selectors.ToolSelector;

import org.aeonbits.owner.Accessible;
import org.aeonbits.owner.Config;
import org.aeonbits.owner.Mutable;
import org.aeonbits.owner.Reloadable;

@SuppressWarnings("SpellCheckingInspection")
@Config.HotReload(type = Config.HotReloadType.ASYNC, value = 1) //set value = X for interval of X seconds. Default: 5
@Config.Sources({"file:${configDirMats}"})
// Note: Material/EntityGroups need to be added to the map to be recognized
public interface AutoSwitchAttackActionConfig extends Config, Reloadable, Accessible, Mutable {
    @Separator(",")
    @DefaultValue("")
    @Comment("A Material")
    ToolSelector[] air();

    @Separator(",")
    @DefaultValue("pickaxe;minecraft:silk_touch, pickaxe;minecraft:mending, pickaxe;minecraft:efficiency, pickaxe")
    @Comment("A Material")
    ToolSelector[] amethyst();

    @Separator(",")
    @DefaultValue("")
    @Comment("A Material for powdered snow.")
    ToolSelector[] passable_snow_block();

    @Separator(",")
    @DefaultValue("")
    @Comment("A Material")
    ToolSelector[] structure_void();

    @Separator(",")
    @DefaultValue("")
    @Comment("A Material")
    ToolSelector[] portal();

    @Separator(",")
    @DefaultValue("")
    @Comment("A Material for carpets.")
    ToolSelector[] carpet();

    @Separator(",")
    @DefaultValue("shears;minecraft:mending, axe;minecraft:mending, shears, axe")
    @Comment("A Material for plants such as flowers and crops")
    ToolSelector[] plant();

    @Separator(",")
    @DefaultValue("shears;minecraft:mending,shears")
    @Comment("A Material for underwater plants such as seagrass")
    ToolSelector[] underwater_plant();

    @Separator(",")
    @DefaultValue("shears;minecraft:mending, axe;minecraft:mending, shears, axe")
    @Comment("A Material for plants such as tall grass that can have a block placed in them, thus 'replacing' it.")
    ToolSelector[] replaceable_plant();

    @Separator(",")
    @DefaultValue("shears;minecraft:mending, shears")
    @Comment("A Material for underwater plants such as seagrass that can have a block placed in them, thus " +
             "'replacing' it.")
    ToolSelector[] replaceable_underwater_plant();

    @Separator(",")
    @DefaultValue("")
    @Comment("A Material for water.")
    ToolSelector[] water();

    @Separator(",")
    @DefaultValue("")
    @Comment("A Material bubble columns.")
    ToolSelector[] bubble_column();

    @Separator(",")
    @DefaultValue("")
    @Comment("A Material lava.")
    ToolSelector[] lava();

    @Separator(",")
    @DefaultValue("shovel;minecraft:silk_touch, shovel;minecraft:mending, shovel;minecraft:efficiency, shovel")
    @Comment("A Material for snow layers.")
    ToolSelector[] snow_layer();

    @Separator(",")
    @DefaultValue("")
    @Comment("A Material for fire.")
    ToolSelector[] fire();

    @Separator(",")
    @DefaultValue("")
    @Comment("A Material for scaffolding, rails, flowerpots, skulls, and similar. Wiki calls it 'Decoration.'")
    ToolSelector[] sub_block();

    @Separator(",")
    @DefaultValue("shears;minecraft:mending, sword;minecraft:mending, shears, sword")
    @Comment("A Material for cobwebs.")
    ToolSelector[] cobweb();

    @Separator(",")
    @DefaultValue("pickaxe;minecraft:mending, pickaxe;minecraft:efficiency, pickaxe")
    @Comment("A Material redstone lamps.")
    ToolSelector[] redstone_lamp();

    @Separator(",")
    @DefaultValue("shovel;minecraft:silk_touch, shovel;minecraft:mending, shovel;minecraft:efficiency, shovel")
    @Comment("A Material for blocks that come from mobs such as honey, slime, or infested blocks. Includes clay but " +
             "not bone blocks.")
    ToolSelector[] organic_product();

    @Separator(",")
    @DefaultValue("shovel;minecraft:silk_touch, shovel;minecraft:efficiency, shovel;minecraft:mending, shovel")
    @Comment("A Material for the topsoil. Path, dirt, podzol, soul soil, farmland and similar.")
    ToolSelector[] soil();

    @Separator(",")
    @DefaultValue("shovel;minecraft:silk_touch,shovel,hoe")
    @Comment("A Material for organic blocks that are solid, including hay, target, and grass blocks.")
    ToolSelector[] solid_organic();

    @Separator(",")
    @DefaultValue("pickaxe;minecraft:silk_touch, any;minecraft:silk_touch, pickaxe;minecraft:efficiency, pickaxe;" +
                  "minecraft:mending, pickaxe")
    @Comment("A Material for ice blocks that do not melt such as packed ice.")
    ToolSelector[] dense_ice();

    @Separator(",")
    @DefaultValue("shovel;minecraft:efficiency, shovel;minecraft:mending, shovel")
    @Comment("A Material formed from a loosely compacted mass of fragments or particles, such as sand or gravel.")
    ToolSelector[] aggregate();

    @Separator(",")
    @DefaultValue("hoe;minecraft:efficiency, hoe;minecraft:mending, hoe")
    @Comment("A Material for sponges.")
    ToolSelector[] sponge();

    @Separator(",")
    @DefaultValue("pickaxe;minecraft:efficiency, pickaxe;minecraft:mending, pickaxe")
    @Comment("A Material for shulker boxes.")
    ToolSelector[] shulker_box();

    @Separator(",")
    @DefaultValue("axe;minecraft:fortune, axe;minecraft:silk_touch, axe;minecraft:efficiency, axe;minecraft:mending, " +
                  "axe")
    @Comment("A Material for wood logs, and things crafted from them.")
    ToolSelector[] wood();

    @Separator(",")
    @DefaultValue("axe;minecraft:efficiency, axe;minecraft:mending, axe")
    @Comment("A Material for blocks crafted from Nether stems and hyphae.")
    ToolSelector[] nether_wood();

    @Separator(",")
    @DefaultValue("sword")
    @Comment("A Material bamboo saplings.")
    ToolSelector[] bamboo_sapling();

    @Separator(",")
    @DefaultValue("sword, axe;minecraft:mending, axe")
    @Comment("A Material for grown bamboo.")
    ToolSelector[] bamboo();

    @Separator(",")
    @DefaultValue("shears;minecraft:mending, shears")
    @Comment("A Material for wool and bed blocks.")
    ToolSelector[] wool();

    @Separator(",")
    @DefaultValue("")
    @Comment("A Material for TNT.")
    ToolSelector[] tnt();

    @Separator(",")
    @DefaultValue("shears;minecraft:mending, shears, hoe;minecraft:silk_touch, hoe;minecraft:mending, hoe, sword;" +
                  "minecraft:mending, sword")
    @Comment("A Material for leaves.")
    ToolSelector[] leaves();

    @Separator(",")
    @DefaultValue("pickaxe;minecraft:silk_touch, any;minecraft:silk_touch, pickaxe;minecraft:fortune, pickaxe;" +
                  "minecraft:mending, pickaxe")
    @Comment("A Material for glass and glass-like blocks (includes sea lanterns and conduits).")
    ToolSelector[] glass();

    @Separator(",")
    @DefaultValue("pickaxe;minecraft:silk_touch,any;minecraft:silk_touch, pickaxe;minecraft:mending, pickaxe")
    @Comment("A Material for ice that can melt.")
    ToolSelector[] ice();

    @Separator(",")
    @DefaultValue("")
    @Comment("A Material for cactus.")
    ToolSelector[] cactus();

    @Separator(",")
    @DefaultValue("pickaxe;minecraft:fortune,pickaxe;minecraft:silk_touch, pickaxe;minecraft:mending, pickaxe;" +
                  "minecraft:efficiency, pickaxe")
    @Comment("A Material for blocks that are stone or made from it, and generally prefer to be broken by a pickaxe, " +
             "such as ores.")
    ToolSelector[] stone();

    @Separator(",")
    @DefaultValue("pickaxe;minecraft:fortune, pickaxe;minecraft:mending, pickaxe")
    @Comment("A Material for metal blocks and compressed ore blocks, such as lapis, diamond, and redstone blocks, " +
             "chains, iron (trap)doors, and cauldrons.")
    ToolSelector[] metal();

    @Separator(",")
    @DefaultValue("shovel;minecraft:silk_touch, shovel;minecraft:fortune, shovel;minecraft:mending, shovel")
    @Comment("A Material for full sized snow blocks.")
    ToolSelector[] snow_block();

    @Separator(",")
    @DefaultValue("pickaxe;minecraft:efficiency, pickaxe;minecraft:mending, pickaxe")
    @Comment("A Material for anvils and grindstones")
    ToolSelector[] repair_station();

    @Separator(",")
    @DefaultValue("")
    @Comment("A Material barrier blocks.")
    ToolSelector[] barrier();

    @Separator(",")
    @DefaultValue("pickaxe;minecraft:efficiency, pickaxe;minecraft:mending, pickaxe")
    @Comment("A Material")
    ToolSelector[] piston();

    @Separator(",")
    @DefaultValue("hoe;minecraft:efficiency, hoe;minecraft:mending, hoe")
    @Comment("A Material for full sized moss blocks")
    ToolSelector[] moss_block();

    @Separator(",")
    @DefaultValue("axe;minecraft:efficiency, axe;minecraft:mending, sword;minecraft:mending, axe, sword")
    @Comment("A Material for gourds. Includes the carved pumpkin and jack o' lantern.")
    ToolSelector[] gourd();

    @Separator(",")
    @DefaultValue("any;minecraft:silk_touch")
    @Comment("A Material for egg blocks, such as dragon and turtle eggs.")
    ToolSelector[] egg();

    @Separator(",")
    @DefaultValue("")
    @Comment("A Material for cake.")
    ToolSelector[] cake();

    @Separator(",")
    @DefaultValue("trident;minecraft:impaling, any;minecraft:sharpness, sword;minecraft:mending, axe;" +
                  "minecraft:mending, sword, axe")
    @Comment("An EntityGroup for guardians, turtles, fish, squids, dolphins, and the like.")
    ToolSelector[] aquaticEntity();

    @Separator(",")
    @DefaultValue("any;minecraft:bane_of_arthropods, any;minecraft:sharpness, sword;minecraft:mending, axe;" +
                  "minecraft:mending, sword, axe")
    @Comment("An EntityGroup for spiders, bees, silverfish, and the like.")
    ToolSelector[] arthropod();

    @Separator(",")
    @DefaultValue("any;minecraft:sharpness, sword;minecraft:mending, axe;minecraft:mending, sword, axe")
    @Comment("An EntityGroup for mobs that were not assigned a different one, such as pigs.")
    ToolSelector[] defaultEntity();

    @Separator(",")
    @DefaultValue("any;minecraft:sharpness, sword;minecraft:mending, axe;minecraft:mending, sword, axe")
    @Comment("The Ender Dragon")
    ToolSelector[] ender_dragon();

    @Separator(",")
    @DefaultValue("any;minecraft:sharpness, sword;minecraft:mending, axe;minecraft:mending, sword, axe")
    @Comment("An EntityGroup for evokers, pillagers, illagers, vindicators, illusioners, and the like.")
    ToolSelector[] illager();

    @Separator(",")
    @DefaultValue("any;minecraft:smite, any;minecraft:sharpness, sword;minecraft:mending, axe;minecraft:mending, " +
                  "sword,axe")
    @Comment("An EntityGroup for mobs that take extra damage from smite, such as zombies, withers, and zoglins")
    ToolSelector[] undead();

    @Separator(",")
    @DefaultValue("axe;minecraft:mending, sword;minecraft:mending, axe, sword")
    @Comment("An Entity, specifically boats. This is here so that an axe will be used to break it so that any mobs " +
             "in the boat aren't accidentally hit.")
    ToolSelector[] boats();

    @Separator(",")
    @DefaultValue("axe;minecraft:mending, sword;minecraft:mending, axe, sword")
    @Comment("All minecarts. This is here so that an axe will be used to break it so that any " +
             "mobs in the minecart aren't accidentally hit.")
    ToolSelector[] minecart();

    // Hoe's special breaking since solid_organic will like enchants more than hoes

    @Separator(",")
    @DefaultValue("hoe;minecraft:efficiency, hoe;minecraft:mending, hoe")
    @Key("minecraft!nether_wart_block")
    @Comment("A default-provided override for hoes to function properly.")
    ToolSelector[] hoeSpecial1();

    @Separator(",")
    @DefaultValue("hoe;minecraft:efficiency, hoe;minecraft:mending, hoe")
    @Key("minecraft!warped_wart_block")
    @Comment("A default-provided override for hoes to function properly.")
    ToolSelector[] hoeSpecial2();

    @Separator(",")
    @DefaultValue("hoe;minecraft:efficiency, hoe;minecraft:mending, hoe")
    @Key("minecraft!shroomlight")
    @Comment("A default-provided override for hoes to function properly.")
    ToolSelector[] hoeSpecial3();

    @Separator(",")
    @DefaultValue("hoe;minecraft:efficiency, hoe;minecraft:mending, hoe")
    @Key("minecraft!hay_block")
    @Comment("A default-provided override for hoes to function properly.")
    ToolSelector[] hoeSpecial4();

    @Separator(",")
    @DefaultValue("hoe;minecraft:efficiency, hoe;minecraft:mending, hoe")
    @Key("minecraft!target")
    @Comment("A default-provided override for hoes to function properly.")
    ToolSelector[] hoeSpecial5();

    @Separator(",")
    @DefaultValue("hoe;minecraft:efficiency, hoe;minecraft:mending, hoe")
    @Key("minecraft!dried_kelp_block")
    @Comment("A default-provided override for hoes to function properly.")
    ToolSelector[] hoeSpecial6();

    @Separator(",")
    @DefaultValue("hoe;minecraft:silk_touch, hoe;minecraft:efficiency, hoe;minecraft:mending, hoe")
    @Comment("A Material for sculk blocks.")
    ToolSelector[] sculk();

    @Separator(",")
    @DefaultValue("")
    @Comment("A Material for frog spawn.")
    ToolSelector[] frogspawn();

    @Separator(",")
    @DefaultValue("pickaxe")
    @Comment("A Material for froglights.")
    ToolSelector[] froglight();

    @Separator(",")
    @DefaultValue("shears;minecraft:mending, shears")
    @Key("minecraft!nether_sprouts")
    @Comment("A default-provided override for nether sprouts, which require shears to drop as an item.")
    ToolSelector[] netherSprouts();

    @Separator(",")
    @DefaultValue("")
    @Comment("A material for crimson and warped roots, as well as nether sprouts")
    ToolSelector[] nether_shoots();

    @Separator(",")
    @DefaultValue("")
    @Comment("A default-provided override to make it clear to users that budding amethyst cannot be obtained by " +
             "mining.")
    @Key("minecraft!budding_amethyst")
    ToolSelector[] budSpecial();

    @Separator(",")
    @DefaultValue("pickaxe;minecraft:silk_touch, pickaxe;minecraft:mending, pickaxe;minecraft:efficiency, pickaxe")
    @Key("minecraft!ender_chest")
    @Comment("A default-provided override for ender chests to prefer silk touch.")
    ToolSelector[] enderChestSpecial();

    @Separator(",")
    @DefaultValue("any;minecraft:sharpness, sword;minecraft:mending, axe;minecraft:mending, " + "sword,axe")
    @Key("minecraft!player")
    @Comment("A default-provided override for players.")
    ToolSelector[] playerSpecial();

    @Separator(",")
    @DefaultValue("axe;minecraft:efficiency, axe;minecraft:mending, axe")
    @Key("minecraft!cocoa")
    @Comment("A default-provided override for cocoa beans.")
    ToolSelector[] cocoaSpecial();

    @Separator(",")
    @DefaultValue("")
    @Key("minecraft!sugar_cane")
    @Comment("A default-provided override for sugar cane.")
    ToolSelector[] reedsSpecial();

    @Separator(",")
    @DefaultValue("shears, axe")
    @Key("minecraft!glow_lichen")
    @Comment("A default-provided override for glow lichen.")
    ToolSelector[] lichenSpecial();

}
