package autoswitch.config;

import autoswitch.config.io.ToolHandler;
import autoswitch.config.util.Comment;
import org.aeonbits.owner.Accessible;
import org.aeonbits.owner.Config;
import org.aeonbits.owner.Mutable;
import org.aeonbits.owner.Reloadable;

@Config.HotReload(type = Config.HotReloadType.ASYNC, value = 1) //set value = X for interval of X seconds. Default: 5
@Config.Sources({"file:${configUsable}"})
public interface AutoSwitchUsableConfig extends Config, Reloadable, Accessible, Mutable {

    @Separator(",")
    @DefaultValue("minecraft:flint_and_steel")
    @Key("minecraft-creeper")
    @Comment("Using flint & steel on a creeper triggers it to immediately explode.")
    ToolHandler[] creeper();

    @Separator(",")
    @DefaultValue("minecraft-carrot_on_a_stick")
    @Key("minecraft:pig")
    @Comment("Switch when getting on a pig so you are ready to steer it.")
    ToolHandler[] pig();

    @Separator(",")
    @DefaultValue("minecraft-warped_fungus_on_a_stick")
    @Key("minecraft-strider")
    @Comment("Switch when getting on a strider so you are ready to steer it.")
    ToolHandler[] strider();

    @Separator("'")
    @DefaultValue("")
    @Comment("For 'item use' action, ie right clicking an item into the empty air, such as for a bow.")
    ToolHandler[] bow_action();


}
