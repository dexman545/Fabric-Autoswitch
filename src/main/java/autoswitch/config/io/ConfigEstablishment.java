package autoswitch.config.io;

import autoswitch.AutoSwitch;
import autoswitch.api.AutoSwitchMap;
import autoswitch.config.AutoSwitchConfig;
import autoswitch.config.AutoSwitchMaterialConfig;
import autoswitch.config.AutoSwitchUsableConfig;
import autoswitch.config.populator.AutoSwitchMapsGenerator;
import autoswitch.config.util.ConfigHeaders;
import autoswitch.compat.autoswitch_api.impl.ApiGenUtil;
import autoswitch.util.SwitchUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.loader.api.FabricLoader;
import org.aeonbits.owner.Accessible;
import org.aeonbits.owner.Config;
import org.aeonbits.owner.ConfigFactory;
import org.aeonbits.owner.Mutable;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;

public final class ConfigEstablishment {

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
        String config = FabricLoader.getInstance().getConfigDir().resolve("autoswitch.cfg").toString();
        String configMats = FabricLoader.getInstance().getConfigDir().resolve("autoswitchMaterials.cfg").toString();
        String configUsable = FabricLoader.getInstance().getConfigDir().resolve("autoswitchUsable.cfg").toString();
        ConfigFactory.setProperty("configDir", config);
        ConfigFactory.setProperty("configDirMats", configMats);
        ConfigFactory.setProperty("configUsable", configUsable);
        AutoSwitch.featureCfg = ConfigFactory.create(AutoSwitchConfig.class);
        AutoSwitch.attackActionCfg = ConfigFactory.create(AutoSwitchMaterialConfig.class);
        AutoSwitch.useActionCfg = ConfigFactory.create(AutoSwitchUsableConfig.class);

        mergeConfigs(AutoSwitch.switchData.attackConfig, AutoSwitch.attackActionCfg);
        mergeConfigs(AutoSwitch.switchData.usableConfig, AutoSwitch.useActionCfg);

        //generate config file; removes incorrect values from existing one as well
        try {
            // Pull mod version
            String currentVersion = SwitchUtil.getAutoSwitchVersion();
            String configVersion = AutoSwitch.featureCfg.configVersion();

            // Check if the configs need to be rewritten
            if (AutoSwitch.featureCfg.alwaysRewriteConfigs() || !configVersion.equals(currentVersion)) {
                AutoSwitch.featureCfg.setProperty("configVersion", currentVersion); // Update version before writing

                genFile(config, AutoSwitch.featureCfg, ConfigHeaders.basicConfig, null);
                genFile(configMats, AutoSwitch.attackActionCfg, ConfigHeaders.materialConfig, ApiGenUtil.modActionConfigs);
                genFile(configUsable, AutoSwitch.useActionCfg, ConfigHeaders.usableConfig, ApiGenUtil.modUseConfigs);
            }

        } catch (IOException e) {
            AutoSwitch.logger.error("AutoSwitch failed to obtain the configs during writing!");
            AutoSwitch.logger.error(e);
        }

        // Clear data and recreate it based on new config

        AutoSwitch.attackActionCfg.addReloadListener(event -> {
            AutoSwitch.switchState.switchActionCache.clear();
            AutoSwitch.switchData.target2AttackActionToolSelectorsMap.clear();
            AutoSwitchMapsGenerator.populateAutoSwitchMaps();
            AutoSwitch.logger.info("Attack Config Reloaded");
        });

        AutoSwitch.useActionCfg.addReloadListener(event -> {
            AutoSwitch.switchState.switchInteractCache.clear();
            AutoSwitch.switchData.target2UseActionToolSelectorsMap.clear();
            AutoSwitchMapsGenerator.populateAutoSwitchMaps();
            AutoSwitch.logger.info("Interact Config Reloaded");
        });

        AutoSwitch.featureCfg.addReloadListener(event -> {
            AutoSwitch.switchState.switchInteractCache.clear();
            AutoSwitch.switchState.switchActionCache.clear();
            AutoSwitch.logger.info("Feature Config Reloaded");
        });
    }

    // Write file
    private static <T extends Accessible & Config> void
    genFile(String path, T config, String header, Object2ObjectOpenHashMap<String, Set<String>> moddedEntries)
            throws IOException {
        FileOutputStream basicConfig = new FileOutputStream(path);
        basicConfig.write(GenerateConfigTemplate.initConfig(config, moddedEntries, header).getBytes());
        basicConfig.close();
    }

    // Add API added config values
    private static <T extends Mutable & Accessible> void mergeConfigs(AutoSwitchMap<String, String> api, T cfg) {
        api.forEach((k, v) -> {
            if (cfg.getProperty(k) == null) {
                cfg.setProperty(k, v);
            }
        });
    }

}
