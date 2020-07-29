package autoswitch.config.io;

import autoswitch.AutoSwitch;
import autoswitch.api.AutoSwitchMap;
import autoswitch.config.*;
import autoswitch.config.populator.AutoSwitchMapsGenerator;
import autoswitch.config.util.ConfigHeaders;
import autoswitch.util.ApiGenUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.loader.api.FabricLoader;
import org.aeonbits.owner.Accessible;
import org.aeonbits.owner.Config;
import org.aeonbits.owner.ConfigFactory;
import org.aeonbits.owner.Mutable;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public final class ConfigEstablishment {

    public ConfigEstablishment() {
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
            AtomicReference<String> currentVersion = new AtomicReference<>();
            String configVersion = AutoSwitch.cfg.configVersion();

            FabricLoader.getInstance().getModContainer("autoswitch").ifPresent(modContainer -> {
                currentVersion.set(modContainer.getMetadata().getVersion().getFriendlyString());
            });

            // Check if the configs need to be rewritten
            if (AutoSwitch.cfg.alwaysRewriteConfigs() || !configVersion.equals(currentVersion.get())) {
                AutoSwitch.cfg.setProperty("configVersion", currentVersion.get()); // Update version before writing

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
            new AutoSwitchMapsGenerator();
        });

        AutoSwitch.cfg.addReloadListener(event -> {
            AutoSwitch.data.toolLists.clear();
            new AutoSwitchMapsGenerator();
        });

        AutoSwitch.usableCfg.addReloadListener(event -> {
            AutoSwitch.data.useMap.clear();
            new AutoSwitchMapsGenerator();
        });
    }

    private <T extends Accessible & Config> void
    genFile(String path, T config, String header, Object2ObjectOpenHashMap<String, Set<String>> moddedEntries)
            throws IOException {
        FileOutputStream basicConfig = new FileOutputStream(path);
        basicConfig.write(GenerateConfigTemplate.initConfig(config, moddedEntries, header).getBytes());
        basicConfig.close();
    }

    // Add API added config values
    private  <T extends Mutable & Accessible> void mergeConfigs(AutoSwitchMap<String, String> api, T cfg) {
        api.forEach((k, v) -> {
            if (cfg.getProperty(k) == null) {
                cfg.setProperty(k, v);
            }
        });
    }

}
