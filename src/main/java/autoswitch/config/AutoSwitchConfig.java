package autoswitch.config;

import autoswitch.config.util.CaseInsensitiveEnumConverter;
import autoswitch.config.util.Comment;
import autoswitch.config.util.Permission;

import org.aeonbits.owner.Accessible;
import org.aeonbits.owner.Config;
import org.aeonbits.owner.Mutable;
import org.aeonbits.owner.Reloadable;

@Config.HotReload(type = Config.HotReloadType.ASYNC, value = 1) //set value = X for interval of X seconds. Default: 5
@Config.Sources({"file:${configDir}"})
public interface AutoSwitchConfig extends Config, Reloadable, Accessible, Mutable {

    @DefaultValue("DEFAULT")
    @ConverterClass(CaseInsensitiveEnumConverter.class)
    @Comment("Controls where and if the keybinding toggle message should be displayed. DEFAULT is above the hotbar," +
             " like with bed messages. CHAT is in the chat bar, like a normal chat message. Set to OFF to disable " +
             "the message entirely." + "\nAcceptable values: DEFAULT, CHAT, OFF")
    DisplayControl toggleMessageControl();

    @DefaultValue("false")
    @Comment("AutoSwitch functionality in creative mode.")
    Boolean switchInCreative();

    @DefaultValue("BOTH")
    @Key("switchAllowedFor")
    @ConverterClass(CaseInsensitiveEnumConverter.class)
    @Comment("Allow switching on the specified type, eg. only switch for blocks by specifying 'BLOCKS'. Set to 'NONE'" +
             " to disable this behavior entirely." + "\nAcceptable values: BOTH, MOBS, BLOCKS, NONE")
    TargetType switchAllowed();

    @DefaultValue("true")
    @Comment("Allow AutoSwitch when in multiplayer.")
    Boolean switchInMP();

    @DefaultValue("BOTH")
    @ConverterClass(CaseInsensitiveEnumConverter.class)
    @Key("switchbackAllowedFor")
    @Comment("Return to the previous slot when no longer performing the action on the specified type. Set to 'NONE'" +
             " to disable this behavior entirely." + "\nAcceptable values: BOTH, MOBS, BLOCKS, NONE")
    TargetType switchbackAllowed();

    @DefaultValue("MOBS")
    @Key("switchbackWaitsForCooldown")
    @ConverterClass(CaseInsensitiveEnumConverter.class)
    @Comment("Before switching back when using the 'attack' action, wait for the attack cooldown to finish. " +
             "Fixes attacks not doing a lot of damage to mobs, and makes switchback for blocks visually smoother. " +
             "\nAcceptable values: BOTH, MOBS, BLOCKS, NONE")
    TargetType switchbackWaits();

    @DefaultValue("true")
    @Comment("Will ignore tools that are about to break when considering which tool to switch to.")
    Boolean tryPreserveDamagedTools();

    @DefaultValue("SADDLE")
    @ConverterClass(CaseInsensitiveEnumConverter.class)
    @Comment("Switch used tool to offhand for the specified type." +
             "Use 'ALL' to move all items to the offhand. 'SADDLE' will only move the item to the offhand for " +
             "saddleable targets." + "\nAcceptable values: ALL, SADDLE, OFF.")
    OffhandType putUseActionToolInOffHand();

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
    @Comment("Enable check for tool's ability to harvest the targeted block via comparison of mining levels.")
    Boolean miningLevelCheck();

    @DefaultValue("true")
    @Comment("Enable switching to items with no durability when no tool is found.")
    Boolean useNoDurabilityItemsWhenUnspecified();

    @DefaultValue("true")
    @Comment("Enable to allow target list to stack enchantments. Disable for old behavior. " +
             "When enabled, a target selector of 'tool;fortune, tool;mending' will prefer a tool with " +
             "both fortune and mending over one with just fortune. " +
             "This differs from 'tool;fortune&mending' in that only one enchantment is required for " +
             "the tool to be selected, rather than both fortune and mending.")
    Boolean toolEnchantmentsStack();

    @DefaultValue("false")
    @Comment("This setting is meant to disable config GUI integration if in future the feature breaks, " +
             "but the rest of AutoSwitch does not. Fallbacks are in place to ensure any changes to Minecraft do not " +
             "break " + "integration, this is simply a more extreme kill switch.")
    Boolean disableModMenuConfig();

    @DefaultValue("false")
    @Comment("Tools with enchantments that increase attack damage will be rated higher. " +
             "This means an axe with sharpness can be preferred over a sword. " +
             "Set to true to enable this functionality. False ignores the enchantments.")
    Boolean weaponRatingIncludesEnchants();

    @DefaultValue("false")
    @Comment("Will force use of the toggle key in order to enable switching.")
    Boolean disableSwitchingOnStartup();

    @DefaultValue("0.05") // 1 tick's time
    @Comment("Delay in seconds from end of hand swinging to perform switchback action. Resolution on the order of " +
             "ticks. " + "0.05 is 1 tick of delay.")
    Float switchbackDelay();

    @DefaultValue("0.05")
    @Comment("Delay in seconds from triggering of normal switch action on the basis that the previous switch " +
             "has not been undone via switchback. Resolution on the order of ticks. " + "0.05 is 1 tick of delay.")
    Float switchDelay();

    @DefaultValue("true")
    @Comment("Ignore tools with 0 energy/durability.")
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
    @Comment("If enabled, AutoSwitch will attempt to avoid recalculating hotbar slots to be used on a particular " +
             "target. " +
             "Each time that slot is modified, it's cached data is thrown out. This can benefit performance.")
    Boolean cacheSwitchResults();

    @DefaultValue("0.25")
    @Comment("Prevents switching for 'attack' action on a block for the specified number of seconds after attacking " +
             "an entity. Resolution on the order of ticks. " + "0.05 is 1 tick of delay.")
    Float preventBlockSwitchAfterEntityAttack();

    @DefaultValue("true")
    @Comment("Enable client-sided commands to modify options in this file, as well to toggle AS on/off during " +
             "gameplay. Only effective on restart.")
    Boolean enableConfigCommands();

    @DefaultValue("false")
    @Comment("When enabled, switching is disabled while the player is crouching/sneaking.")
    Boolean disableSwitchingWhenCrouching();

    @DefaultValue("3")
    @Comment("The amount of remaining durability needed to trigger tool preservation.")
    Integer damageThreshold();

    enum TargetType implements Permission {
        BOTH, MOBS, BLOCKS,
        NONE {
            @Override
            public boolean allowed() {
                return false;
            }
        }
    }

    enum DisplayControl implements Permission {
        DEFAULT, CHAT,
        OFF {
            @Override
            public boolean allowed() {
                return false;
            }
        }
    }

    enum OffhandType implements Permission {
        SADDLE, ALL,
        OFF {
            @Override
            public boolean allowed() {
                return false;
            }
        }
    }

}

