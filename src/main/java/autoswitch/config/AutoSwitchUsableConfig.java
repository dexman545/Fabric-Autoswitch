package autoswitch.config;

import autoswitch.ToolHandler;
import org.aeonbits.owner.Accessible;
import org.aeonbits.owner.Config;
import org.aeonbits.owner.Reloadable;

@Config.HotReload(type = Config.HotReloadType.ASYNC, value = 1) //set value = X for interval of X seconds. Default: 5
@Config.Sources({"file:${configUsable}"})
public interface AutoSwitchUsableConfig extends Config, Reloadable, Accessible {

    @Separator(",")
    @DefaultValue("minecraft:flint_and_steel")
    @Key("minecraft-creeper")
    ToolHandler[] creeper();

    @Separator(",")
    @DefaultValue("minecraft-carrot_on_a_stick")
    @Key("minecraft:pig")
    ToolHandler[] pig();

    @Separator(",")
    @DefaultValue("minecraft-warped_fungus_on_a_stick")
    @Key("minecraft-strider")
    ToolHandler[] strider();


}
