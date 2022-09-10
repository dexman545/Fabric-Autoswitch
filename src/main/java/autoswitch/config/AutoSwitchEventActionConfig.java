package autoswitch.config;

import autoswitch.config.util.Comment;
import autoswitch.selectors.ToolSelector;

import org.aeonbits.owner.Accessible;
import org.aeonbits.owner.Config;
import org.aeonbits.owner.Mutable;
import org.aeonbits.owner.Reloadable;

@SuppressWarnings("SpellCheckingInspection")
@Config.HotReload(type = Config.HotReloadType.ASYNC, value = 1) //set value = X for interval of X seconds. Default: 5
@Config.Sources({"file:${configDirEvents}"})
public interface AutoSwitchEventActionConfig extends Config, Reloadable, Accessible, Mutable {
    @Separator(",")
    @DefaultValue("minecraft!totem_of_undying")
    @Comment("Triggered when a totem of undying is used.")
    ToolSelector[] totemUsed();
}
