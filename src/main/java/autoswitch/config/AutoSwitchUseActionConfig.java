package autoswitch.config;

import autoswitch.config.util.Comment;
import autoswitch.selectors.ToolSelector;

import org.aeonbits.owner.Accessible;
import org.aeonbits.owner.Config;
import org.aeonbits.owner.Mutable;
import org.aeonbits.owner.Reloadable;

@Config.HotReload(type = Config.HotReloadType.ASYNC, value = 1) //set value = X for interval of X seconds. Default: 5
@Config.Sources({"file:${configUsable}"})
public interface AutoSwitchUseActionConfig extends Config, Reloadable, Accessible, Mutable {

    @Separator(",")
    @DefaultValue("minecraft:flint_and_steel")
    @Key("minecraft!creeper")
    @Comment("Using flint & steel on a creeper triggers it to immediately explode.")
    ToolSelector[] creeper();

    @Separator(",")
    @DefaultValue("minecraft:bowl, minecraft:bucket")
    @Key("minecraft!mooshroom")
    @Comment("Milk mooshrooms with a bowl to get suspicious stew, or a bucket to get milk. Shears can turn them into " +
             "a cow.")
    ToolSelector[] mooshroom();

    @Separator(",")
    @DefaultValue("minecraft:bucket")
    @Key("minecraft!cow")
    @Comment("Milk a cow to get milk.")
    ToolSelector[] cow();

    @Separator(",")
    @DefaultValue("minecraft:bucket")
    @Key("minecraft!goat")
    @Comment("Milk a goat to get milk.")
    ToolSelector[] goat();

    @Separator(",")
    @DefaultValue("minecraft:iron_ingot")
    @Key("minecraft!iron_golem")
    @Comment("Heal an iron golem")
    ToolSelector[] ironGolem();

    @Separator(",")
    @DefaultValue("minecraft:shears")
    @Key("minecraft!sheep")
    @Comment("Shear a sheep.")
    ToolSelector[] sheep();

    @Separator(",")
    @DefaultValue("")
    @Key("minecraft!beehive[honey_level+5]")
    @Comment("Get honeycomb or honey from the hive with shears or a bottle, respectively.")
    ToolSelector[] beehive();

    @Separator(",")
    @DefaultValue("")
    @Key("minecraft!bee_nest[honey_level+5]")
    @Comment("Get honeycomb or honey from the hive with shears or a bottle, respectively.")
    ToolSelector[] beeNest();

    @Separator(",")
    @DefaultValue("minecraft:water_bucket")
    @Key("bucketable_swimmer")
    @Comment("Catch an aquatic creature in a bucket.")
    ToolSelector[] bucketableSwimmer();

    @Separator(",")
    @DefaultValue("")
    @Comment("For 'item use' action, ie right clicking an item into the empty air, such as for a bow.")
    ToolSelector[] bow_action();


}
