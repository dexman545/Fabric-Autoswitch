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
    @Key("stat@used!totem_of_undying")
    @Comment("Triggered when a totem of undying is used.")
    ToolSelector[] totemUsed();

    @Separator(",")
    @DefaultValue("minecraft!milk_bucket")
    @Key("stat@custom!sleep_in_bed")
    @Comment("Have some milk to help you sleep well.")
    ToolSelector[] easterEgg();

    @Separator(",")
    @DefaultValue("minecraft:carrot_on_a_stick")
    @Key("stat@custom!pig_one_cm")
    @Comment("Switch when getting on a pig so you are ready to steer it.")
    ToolSelector[] pig();

    @Separator(",")
    @DefaultValue("minecraft:warped_fungus_on_a_stick")
    @Key("stat@custom!strider_one_cm")
    @Comment("Switch when getting on a strider so you are ready to steer it.")
    ToolSelector[] strider();
}
