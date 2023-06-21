package autoswitch.config.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Set;

import autoswitch.AutoSwitch;
import autoswitch.actions.Action;
import autoswitch.api.AutoSwitchMap;
import autoswitch.compat.autoswitch_api.impl.ApiGenUtil;
import autoswitch.config.AutoSwitchAttackActionConfig;
import autoswitch.config.AutoSwitchConfig;
import autoswitch.config.AutoSwitchEventActionConfig;
import autoswitch.config.AutoSwitchUseActionConfig;
import autoswitch.config.populator.AutoSwitchMapsGenerator;
import autoswitch.config.util.ConfigHeaders;
import autoswitch.util.SwitchUtil;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.aeonbits.owner.Accessible;
import org.aeonbits.owner.Config;
import org.aeonbits.owner.ConfigFactory;
import org.aeonbits.owner.Mutable;
import org.apache.commons.io.FileUtils;

import net.fabricmc.loader.api.FabricLoader;

public final class ConfigEstablishment {
    private static final Path configDir = FabricLoader.getInstance().getConfigDir();
    private static final Path featurePath = configDir.resolve("autoswitch.cfg");
    private static final String configFeature = featurePath.toString();
    private static final Path useActionPath = configDir.resolve("autoswitchUseAction.cfg");
    private static final String configUseAction = useActionPath.toString();
    private static final Path attackActionPath = configDir.resolve("autoswitchAttackAction.cfg");
    private static final String configAttackAction = attackActionPath.toString();
    private static final Path eventActionPath = configDir.resolve("autoswitchEventAction.cfg");
    private static final String configEventAction = eventActionPath.toString();

    // AutoSwitch has 3 config files - basic, material, and usable.
    // Each can be represented by a map of key -> value pairs.
    // The basic config consists of a set of keys that is non-expandable and known at and before compile-time.
    // Its values are booleans, numerics, and one text input.

    // Material and usable configs share the same format: a minimum set of keys that are known at and before compile-
    // time. These keys are always present. Their set of keys is not known at compile-time, as it can be expanded during
    // user-time. There are no limits to how much a user can expand this set of keys outside of practical ones. The
    // values of these keys share similar issues:

    // Each value is an Optional List of ToolSelector, where a ToolSelector is a Tool + Optional List of Enchant,
    // where Tool is either a known ToolGroup (a String key in a map of key -> (Optional Tag<Item> and Optional Class))
    // or the item's Identifier, and Enchant is the enchantment's Identifier. Neither the ToolSelector nor Enchant lists
    // are bounded outside of practical limitations.

    public static void establishConfigs() {
        // Update old config files
        updateOldConfigFiles();

        // Replace : with ! when reading to prevent configs from being messed up
        ConfigFactory.registerLoader(new LenientPropertiesLoader());
        ConfigFactory.setProperty("configDir", configFeature);
        ConfigFactory.setProperty("configDirMats", configAttackAction);
        ConfigFactory.setProperty("configDirEvents", configEventAction);
        ConfigFactory.setProperty("configUsable", configUseAction);
        AutoSwitch.featureCfg = ConfigFactory.create(AutoSwitchConfig.class);
        AutoSwitch.attackActionCfg = ConfigFactory.create(AutoSwitchAttackActionConfig.class);
        AutoSwitch.useActionCfg = ConfigFactory.create(AutoSwitchUseActionConfig.class);
        AutoSwitch.eventActionConfig = ConfigFactory.create(AutoSwitchEventActionConfig.class);

        for (Action action : Action.values()) {
            mergeConfigs(action.getConfigMap(), action.getActionConfig());
        }

        // Generate config file; removes incorrect values from existing one as well
        try {
            // Pull mod version
            String currentVersion = SwitchUtil.getAutoSwitchVersion();
            String configVersion = AutoSwitch.featureCfg.configVersion();

            // Check if the configs need to be rewritten
            if (AutoSwitch.featureCfg.alwaysRewriteConfigs() || !configVersion.equals(currentVersion)) {
                AutoSwitch.featureCfg.setProperty("configVersion", currentVersion); // Update version before writing

                writeConfigFiles();

            }

        } catch (IOException e) {
            AutoSwitch.logger.error("AutoSwitch failed to obtain the configs during writing", e);
        }

        // Clear data and recreate it based on new config

        AutoSwitch.attackActionCfg.addReloadListener(event -> {
            Action.ATTACK.clearSelectors();
            AutoSwitchMapsGenerator.populateAutoSwitchMaps();
            AutoSwitch.logger.info("Attack Config Reloaded");
        });

        AutoSwitch.useActionCfg.addReloadListener(event -> {
            Action.ATTACK.clearSelectors();
            AutoSwitchMapsGenerator.populateAutoSwitchMaps();
            AutoSwitch.logger.info("Interact Config Reloaded");
        });

        AutoSwitch.eventActionConfig.addReloadListener(event -> {
            Action.EVENT.clearSelectors();
            AutoSwitchMapsGenerator.populateAutoSwitchMaps();
            AutoSwitch.logger.info("Event Config Reloaded");
        });

        AutoSwitch.featureCfg.addReloadListener(event -> {
            Action.resetAllActionStates();
            AutoSwitch.logger.info("Feature Config Reloaded");
        });
    }

    private static void updateOldConfigFiles() {
        try {
            //updateOldConfigFormat(attackActionPath.toFile());
            //updateOldConfigFormat(useActionPath.toFile());
            updateOldConfigFormat(featurePath.toFile());
        } catch (IOException e) {
            AutoSwitch.logger.debug("Error migrating config files", e);
        }
    }

    // Add API added config values
    private static <T extends Mutable & Accessible> void mergeConfigs(AutoSwitchMap<String, String> api, T cfg) {
        api.forEach((k, v) -> {
            if (cfg.getProperty(k) == null) {
                cfg.setProperty(k, v);
            }
        });
    }

    /**
     * Write all config values to file with pretty-print.
     * <p>
     * Used for saving changes made by the user in the config GUI.
     *
     * @throws IOException when configs fail to write
     */
    public static void writeConfigFiles() throws IOException {
        genFile(configFeature, AutoSwitch.featureCfg, ConfigHeaders.basicConfig, null);
        genFile(configAttackAction, AutoSwitch.attackActionCfg, ConfigHeaders.attackConfig,
                ApiGenUtil.modActionConfigs);
        genFile(configUseAction, AutoSwitch.useActionCfg, ConfigHeaders.usableConfig, ApiGenUtil.modUseConfigs);
        genFile(configEventAction, AutoSwitch.eventActionConfig, ConfigHeaders.eventConfig, null);
    }

    private static void updateOldConfigFormat(File file) throws IOException {
        String s = FileUtils.readFileToString(file, StandardCharsets.UTF_8).replaceAll("BOTH", "BLOCKS, MOBS");
        s = s.replaceAll("NONE", "");
        FileUtils.writeStringToFile(file, s, StandardCharsets.UTF_8, false);
    }

    // Write file
    private static <T extends Accessible & Config> void genFile(String path, T config, String header,
                                                                Object2ObjectOpenHashMap<String, Set<String>> moddedEntries)
            throws IOException {
        FileOutputStream basicConfig = new FileOutputStream(path);
        basicConfig.write(GenerateConfigTemplate.initConfig(config, moddedEntries, header).getBytes());
        basicConfig.close();
    }

}
