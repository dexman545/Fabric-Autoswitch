package autoswitch.config;

import autoswitch.config.util.CaseInsensitiveEnumConverter;
import autoswitch.config.util.Comment;
import org.aeonbits.owner.Accessible;
import org.aeonbits.owner.Config;
import org.aeonbits.owner.Mutable;
import org.aeonbits.owner.Reloadable;

@Config.HotReload(type = Config.HotReloadType.ASYNC, value = 1) //set value = X for interval of X seconds. Default: 5
@Config.Sources({"file:${configDir}"})
public interface AutoSwitchConfig extends Config, Reloadable, Accessible, Mutable {

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
    @Comment("Let AutoSwitch prevent breaking a block with an empty collision box (ie. tall grass) " +
            "when trying to attack a mob.")
    Boolean controlMowingWhenFighting();

    @DefaultValue("true")
    @Comment("Disable the swinging of the hand when trying to mow when fighting.")
    Boolean disableHandSwingWhenMowing();

    @DefaultValue("BOTH")
    @Key("switchbackWaitsForCooldown")
    @ConverterClass(CaseInsensitiveEnumConverter.class)
    @Comment("Before switching back when using the 'attack' action, wait for the attack cooldown to finish. " +
            "Fixes attacks not doing a lot of damage to mobs, and makes switchback for blocks smoother. " +
            "Acceptable values: BOTH, MOBS, BLOCKS, NONE")
    SwitchDelay switchbackWaits();

    enum SwitchDelay {
        BOTH,
        MOBS,
        BLOCKS,
        NONE
    }

    @DefaultValue("true")
    @Comment("Will ignore tools that are about to break when considering which tool to switch to.")
    Boolean tryPreserveDamagedTools();

    @DefaultValue("true")
    @Comment("Switch used tool to offhand.")
    Boolean putUseActionToolInOffHand();

    @DefaultValue("true")
    @Comment("Switch used tool to offhand if no item is there.")
    Boolean preserveOffhandItem();

    @DefaultValue("true")
    @Comment("Switch for use-action of a tool.")
    Boolean switchUseActions();

    @DefaultValue("true")
    @Comment("Prefer tool with minimum mining level.")
    Boolean preferMinimumViableTool();

    @DefaultValue("true")
    @Comment("Checks if a saddlable entity has a saddle for use action to switch. " +
            "Does not allow switching to a saddle on hotbar.")
    Boolean checkSaddlableEntitiesForSaddle();

    @DefaultValue("true")
    @Comment("Enable dumb check for tool harvestablity on the targeted block.")
    Boolean dumbMiningLevelCheck();

    @DefaultValue("true")
    @Comment("Enable switching to items with no durability when no tool is found.")
    Boolean useNoDurablityItemsWhenUnspecified();

    @DefaultValue("true")
    @Comment("Enable to allow target list to stack enchantments. Disable for old behavior. " +
            "When enabled, a target selector of 'tool;fortune, tool;mending' will prefer a tool with" +
            " both fortune and mending over one with just fortune." +
            "This differs from 'tool;fortune&mending' in that only one enchantment is required for " +
            "the tool to be selected, rather than both fortune and mending.")
    Boolean toolEnchantmentsStack();

    @DefaultValue("false")
    @Comment("This setting is meant to disable config GUI integration if in future the feature breaks," +
            "but the rest of AutoSwitch does not. Fallbacks are in place to ensure any changes to Minecraft do not break" +
            "integration, this is simply a more extreme kill switch.")
    Boolean disableModMenuConfig();

    @DefaultValue("false")
    @Comment("Tools with enchantments that increase attack damage will be rated higher. " +
            "This means an axe with sharpness can be preferred over a sword. " +
            "Set to true to enable this functionality. False ignores the enchantments.")
    Boolean weaponRatingIncludesEnchants();

    @DefaultValue("false")
    @Comment("Will force use of the toggle key in order to enable switching")
    Boolean disableSwitchingOnStartup();

    @DefaultValue("0.05") // 1 tick's time
    @Comment("Delay in seconds from end of hand swinging to perform switchback action. Resolution on the order of ticks." +
            "0.05 is 1 tick of delay.")
    Float switchbackDelay();

    @DefaultValue("0.05")
    @Comment("Delay in seconds from triggering of normal switch action on the basis that the previous switch " +
            "has not been undone via switchback. Resolution on the order of ticks." +
            "0.05 is 1 tick of delay.")
    Float switchDelay();

    @DefaultValue("true")
    @Comment("Ignore tools with 0 energy/durability")
    Boolean skipDepletedItems();

    @DefaultValue("")
    @Comment("No touchy! For checking when to regen config files!")
    String configVersion();

    @DefaultValue("false")
    @Comment("When enabled, the config files will be regenerated upon every MC startup. " +
            "This means any user-added config entries will be moved to the bottom in the 'Overrides' section. " +
            "When disabled, the files will only be rewritten when the config version does not match expected one. " +
            "Do note that the material and usable configs will not regenerate if removed with this disabled if the " +
            "main config was not also removed.")
    Boolean alwaysRewriteConfigs();

    @DefaultValue("true")
    @Comment("If enabled, AutoSwitch will attempt to avoid recalculating hotbar slots to be used on a particular target." +
            "Each time that slot is modified, it's cached data is thrown out. This can benefit performance.")
    Boolean cacheSwitchResults();
}

