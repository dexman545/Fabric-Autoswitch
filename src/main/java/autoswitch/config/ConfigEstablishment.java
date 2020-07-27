package autoswitch.config;

import autoswitch.AutoSwitch;
import autoswitch.api.AutoSwitchMap;
import autoswitch.api.DurabilityGetter;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.*;
import net.minecraft.tag.Tag;
import org.aeonbits.owner.Accessible;
import org.aeonbits.owner.ConfigFactory;
import org.aeonbits.owner.Mutable;
import org.apache.commons.lang3.tuple.Pair;

import java.io.FileOutputStream;
import java.io.IOException;

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
            //TODO manually write config files with the following requirements: (will be hard to keep updated)
            // 1. ordered (so the mixin can be removed)
            // 2. write in the comments
            // 3. do some DFU shizzle to update old configs to new defaults?
            // 4. make sure OWNER will still read from the file
            AutoSwitch.cfg.store(new FileOutputStream(config), ConfigHeaders.basicConfig);
            AutoSwitch.matCfg.store(new FileOutputStream(configMats), ConfigHeaders.materialConfig);
            AutoSwitch.usableCfg.store(new FileOutputStream(configUsable), ConfigHeaders.usableConfig);
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

    // Add API added config values
    private  <T extends Mutable & Accessible> void mergeConfigs(AutoSwitchMap<String, String> api, T cfg) {
        System.out.println("API " + api.toString());
        System.out.println("CFG " + cfg.toString());
        api.forEach((k, v) -> {
            if (cfg.getProperty(k).isEmpty()) {
                cfg.setProperty(k, v);
            }
        });
    }

}
