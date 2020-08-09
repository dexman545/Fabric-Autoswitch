package autoswitch.config.io;

import autoswitch.AutoSwitch;
import autoswitch.api.AutoSwitchMap;
import autoswitch.config.AutoSwitchConfig;
import autoswitch.config.AutoSwitchMaterialConfig;
import autoswitch.config.AutoSwitchUsableConfig;
import autoswitch.config.populator.AutoSwitchMapsGenerator;
import autoswitch.config.util.ConfigHeaders;
import autoswitch.util.ApiGenUtil;
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
        String config = FabricLoader.getInstance().getConfigDir().toString() + "/autoswitch.cfg";
        String configMats = FabricLoader.getInstance().getConfigDir().toString() + "/autoswitchMaterials.cfg";
        String configUsable = FabricLoader.getInstance().getConfigDir().toString() + "/autoswitchUsable.cfg";
        ConfigFactory.setProperty("configDir", config);
        ConfigFactory.setProperty("configDirMats", configMats);
        ConfigFactory.setProperty("configUsable", configUsable);
        AutoSwitch.cfg = ConfigFactory.create(AutoSwitchConfig.class);
        AutoSwitch.matCfg = ConfigFactory.create(AutoSwitchMaterialConfig.class);
        AutoSwitch.usableCfg = ConfigFactory.create(AutoSwitchUsableConfig.class);

        mergeConfigs(AutoSwitch.data.actionConfig, AutoSwitch.matCfg);
        mergeConfigs(AutoSwitch.data.usableConfig, AutoSwitch.usableCfg);

        //generate config file; removes incorrect values from existing one as well
        try {
            // Pull mod version
            String currentVersion = SwitchUtil.getAutoSwitchVersion();
            String configVersion = AutoSwitch.cfg.configVersion();

            // Check if the configs need to be rewritten
            if (AutoSwitch.cfg.alwaysRewriteConfigs() || !configVersion.equals(currentVersion)) {
                AutoSwitch.cfg.setProperty("configVersion", currentVersion); // Update version before writing

                genFile(config, AutoSwitch.cfg, ConfigHeaders.basicConfig, null);
                genFile(configMats, AutoSwitch.matCfg, ConfigHeaders.materialConfig, ApiGenUtil.modActionConfigs);
                genFile(configUsable, AutoSwitch.usableCfg, ConfigHeaders.usableConfig, ApiGenUtil.modUseConfigs);
            }

        } catch (IOException e) {
            AutoSwitch.logger.error("AutoSwitch failed to obtain the configs during writing!");
            AutoSwitch.logger.error(e);
        }

        // Clear data and recreate it based on new config

        AutoSwitch.matCfg.addReloadListener(event -> {
            AutoSwitch.data.toolTargetLists.clear();
            AutoSwitchMapsGenerator.populateAutoSwitchMaps();
        });

        AutoSwitch.cfg.addReloadListener(event -> {
            AutoSwitch.data.toolLists.clear();
            AutoSwitchMapsGenerator.populateAutoSwitchMaps();
        });

        AutoSwitch.usableCfg.addReloadListener(event -> {
            AutoSwitch.data.useMap.clear();
            AutoSwitchMapsGenerator.populateAutoSwitchMaps();
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
