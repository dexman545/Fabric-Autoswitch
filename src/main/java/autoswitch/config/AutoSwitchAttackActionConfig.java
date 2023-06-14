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

    @Separator(",")
    @DefaultValue("any;minecraft:sharpness, sword;minecraft:mending, axe;minecraft:mending, " + "sword,axe")
    @Key("minecraft!player")
    @Comment("A default-provided override for players.")
    ToolSelector[] playerSpecial();

    @Separator(",")
    @DefaultValue("sword;minecraft:mending, sword")
    @Key("block@autoswitch!sword_efficient")
    @Comment("A tag for blocks broken more quickly by a sword.")
    ToolSelector[] swordTargets();

    @Separator(",")
    @DefaultValue("axe;minecraft:efficiency, axe;minecraft:mending, axe")
    @Key("block@minecraft!mineable/axe")
    @Comment("A tag for blocks broken more quickly by a sword.")
    ToolSelector[] axeTargets();

    @Separator(",")
    @DefaultValue("pickaxe;minecraft:mending, pickaxe;minecraft:efficiency, pickaxe")
    @Key("block@minecraft!mineable/pickaxe")
    @Comment("A tag for blocks broken more quickly by a sword.")
    ToolSelector[] pickaxeTargets();

    @Separator(",")
    @DefaultValue("hoe;minecraft:silk_touch, hoe;minecraft:efficiency, hoe;minecraft:mending, hoe")
    @Key("block@minecraft!mineable/hoe")
    @Comment("A tag for blocks broken more quickly by a sword.")
    ToolSelector[] hoeTargets();

    @Separator(",")
    @DefaultValue("shovel")
    @Key("block@minecraft!mineable/shovel")
    @Comment("A tag for blocks broken more quickly by a sword.")
    ToolSelector[] shovelTargets();

    @Separator(",")
    @DefaultValue("shears;minecraft:mending, shears")
    @Key("block@autoswitch!shears_efficient")
    @Comment("A tag for blocks broken more quickly by shears.")
    ToolSelector[] shearsTargets();

    @Separator(",")
    @DefaultValue("")
    @Key("minecraft!sugar_cane")
    @Comment("A default-provided override for sugar cane so that tools are not used.")
    ToolSelector[] reedsSpecial();

    @Separator(",")
    @DefaultValue("pickaxe;minecraft:silk_touch, pickaxe;minecraft:mending, pickaxe;minecraft:efficiency, pickaxe")
    @Key("minecraft!ender_chest")
    @Comment("A default-provided override for ender chests to prefer silk touch.")
    ToolSelector[] enderChestSpecial();

    @Separator(",")
    @DefaultValue("any;minecraft:silk_touch")
    @Key("block@c!glass_blocks")
    @Comment("A tag for glass.")
    ToolSelector[] glass();

    @Separator(",")
    @DefaultValue("pickaxe;minecraft:silk_touch, pickaxe;minecraft:efficiency, pickaxe")
    @Key("block@minecraft!ice")
    @Comment("A tag for ice.")
    ToolSelector[] ice();

}
