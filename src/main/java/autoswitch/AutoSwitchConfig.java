package autoswitch;

import org.aeonbits.owner.Accessible;
import org.aeonbits.owner.Config;
import org.aeonbits.owner.Reloadable;

@Config.HotReload(type= Config.HotReloadType.ASYNC) //set value = X for interval of X seconds. Default: 5
@Config.Sources({"file:${configDir}"})
public interface AutoSwitchConfig extends Config, Reloadable, Accessible {

    @DefaultValue("true")
    //Display toggle message above hotbar. Set to false for normal chat message
    Boolean toggleMsgOverHotbar();

    @DefaultValue("true")
    //Display toggle message, set to false to remove it
    Boolean displayToggleMsg();

    @DefaultValue("false")
    //AutoSwitch functionality in creative mode
    Boolean switchInCreative();

    @DefaultValue("true")
    //AutoSwitch for mining
    Boolean switchForBlocks();

    @DefaultValue("true")
    //AutoSwitch for PvE
    Boolean switchForMobs();

    @DefaultValue("true")
    //AutoSwitch when in multiplayer
    Boolean switchInMP();

    @DefaultValue("true")
    //Return to previous slot when not attacking a block
    Boolean switchbackBlocks();

    @DefaultValue("true")
    //Return to previous slot after attacking a mob
    Boolean switchbackMobs();

    /*
    @DefaultValue("true")
    //AutoSwitch when in singleplayer
    Boolean switchInSP();

    @DefaultValue("1")
    int $configVersion();
     */

}

