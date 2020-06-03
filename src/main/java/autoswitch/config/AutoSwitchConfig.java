package autoswitch.config;

import org.aeonbits.owner.Accessible;
import org.aeonbits.owner.Config;
import org.aeonbits.owner.Reloadable;

@Config.HotReload(type = Config.HotReloadType.ASYNC, value = 1) //set value = X for interval of X seconds. Default: 5
@Config.Sources({"file:${configDir}"})
public interface AutoSwitchConfig extends Config, Reloadable, Accessible {

    @DefaultValue("true")
    @Comment("Display toggle message above hotbar. Set to false for normal chat message.")
    Boolean toggleMsgOverHotbar();

    @DefaultValue("true")
    @Comment("Display toggle message, set to false to remove it.")
    Boolean displayToggleMsg();

    @DefaultValue("false")
    @Comment("AutoSwitch functionality in creative mode.")
    Boolean switchInCreative();

    @DefaultValue("true")
    @Comment("AutoSwitch for mining.")
    Boolean switchForBlocks();

    @DefaultValue("true")
    @Comment("AutoSwitch for attacking mobs.")
    Boolean switchForMobs();

    @DefaultValue("true")
    @Comment("Allow AutoSwitch when in multiplayer.")
    Boolean switchInMP();

    @DefaultValue("true")
    @Comment("Return to previous slot when not attacking a block.")
    Boolean switchbackBlocks();

    @DefaultValue("true")
    @Comment("Return to previous slot after attacking a mob.")
    Boolean switchbackMobs();

    @DefaultValue("true")
    @Comment("Let AutoSwitch prevent breaking a block with an empty collision box (ie. tall grass) when trying to attack a mob.")
    Boolean controlMowingWhenFighting();

    @DefaultValue("true")
    @Comment("Disable the swinging of the hand when trying to mow when fighting.")
    Boolean disableHandSwingWhenMowing();

    @Separator(">")
    @DefaultValue("sword > axe > pickaxe")
        //Order for tool priorities. Shared between blocks and mobs.
        //Values must match their appearance in the material config
        //Ex. ThisIsBefore > ThisIsAfter > Etc.
    String[] toolPriorityOrder();

    @DefaultValue("true")
    @Key("switchbackWaitsForCooldownWhenAttackingMobs")
    @Comment("Before switching back when fighting a mob, wait for the attack cooldown to finish. Fixes attacks not doing a lot of damage.")
    Boolean switchbackWaits();

    @DefaultValue("true")
    @Comment("Will ignore tools that are about to break when considering which tool to switch to.")
    Boolean tryPreserveDamagedTools();

    @DefaultValue("true")
    @Comment("Switch used tool to offhand.")
    Boolean putUseActionToolInOffHand();

    @DefaultValue("true")
    @Comment("Switch for use-action of a tool.")
    Boolean switchUseActions();

    @DefaultValue("true")
    @Comment("Prefer tool with minimum mining level.")
    Boolean preferMinimumViableTool();

    @DefaultValue("true")
    @Comment("Checks if a saddlable entity has a saddle for use action to switch. Does not allow switching to a saddle on hotbar.")
    Boolean checkSaddlableEntitiesForSaddle();

    @DefaultValue("true")
    @Comment("Enable dumb check for tool harvestablity on the targeted block.")
    Boolean dumbMiningLevelCheck();

    @DefaultValue("true")
    @Comment("Enable switching to items with no durability when no tool is found.")
    Boolean useNoDurablityItemsWhenUnspecified();

    @DefaultValue("true")
    @Comment("Enable to allow target list to stack enchantments. Disable for old behavior.")
    Boolean toolEnchantmentsStack();

    @DefaultValue("false")
        // This setting is meant to disable config GUI integration if in future the feature breaks,
        // but the rest of AutoSwitch does not. Fallbacks are in place to ensure any changes to Minecraft do not break
        // integration, this is simply a more extreme kill switch.
    Boolean disableModMenuConfig();

    @DefaultValue("false")
    @Comment("Tools with enchantments that increase attack damage will be rated higher. " +
            "This means an axe with sharpness can be preferred over a sword." +
            "Set to true to enable this functionality. False ignores the enchantments.")
    Boolean weaponRatingIncludesEnchants();

}

