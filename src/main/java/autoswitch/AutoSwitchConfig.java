package autoswitch;

import org.aeonbits.owner.Accessible;
import org.aeonbits.owner.Config;
import org.aeonbits.owner.Reloadable;

@Config.HotReload(type = Config.HotReloadType.ASYNC) //set value = X for interval of X seconds. Default: 5
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

    @DefaultValue("true")
    //Let AutoSwitch control breaking a block with an empty collision box (ie. tall grass) when trying to attack a mob
    Boolean controlMowingWhenFighting();

    @DefaultValue("true")
    //Disable the swinging of the hand when trying to mow when fighting
    Boolean disableHandSwingWhenMowing();

    @TokenizerClass(StringInputVerifier.class)
    @DefaultValue("shears > silkPicks > fortPicks > picks > silkAxes > fortAxes > axes > swords > silkShovels > shovels > banes > smites > sharps > tridents > impalingTridents")
    //Order for tool priorities. Shared between blocks and mobs. Also set in StringInputVerifier
    //Some orders don't matter for entities, eg. impalingTridents will always be used if it can on the correct mob
    //Ex. ThisIsBefore > ThisIsAfter > Etc.
    String[] toolPriorityOrder();

    @DefaultValue("true")
    @Key("switchbackWaitsForCooldownWhenAttackingMobs")
    //before switching back when fighting a mob, wait for the attack cooldown to finish. Fixes attacks not doing a lot of damage
    Boolean switchbackWaits();

    @DefaultValue("true")
    //Will ignore tools that are about to break when considering which tool to switch to
    Boolean tryPreserveDamagedTools();

    /*
    @DefaultValue("true")
    //AutoSwitch when in singleplayer
    Boolean switchInSP();

    @DefaultValue("1")
    int $configVersion();
     */

}

