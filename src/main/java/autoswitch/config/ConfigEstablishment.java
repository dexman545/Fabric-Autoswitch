package autoswitch.config;

import autoswitch.AutoSwitch;
import net.fabricmc.loader.api.FabricLoader;
import org.aeonbits.owner.ConfigFactory;

import java.io.FileOutputStream;
import java.io.IOException;

public final class ConfigEstablishment {

    public ConfigEstablishment() {
        String config = FabricLoader.getInstance().getConfigDirectory().toString() + "/autoswitch.cfg";
        String configMats = FabricLoader.getInstance().getConfigDirectory().toString() + "/autoswitchMaterials.cfg";
        String configUsable = FabricLoader.getInstance().getConfigDirectory().toString() + "/autoswitchUsable.cfg";
        ConfigFactory.setProperty("configDir", config);
        ConfigFactory.setProperty("configDirMats", configMats);
        ConfigFactory.setProperty("configUsable", configUsable);
        AutoSwitch.cfg = ConfigFactory.create(AutoSwitchConfig.class);
        AutoSwitch.matCfg = ConfigFactory.create(AutoSwitchMaterialConfig.class);
        AutoSwitch.usableCfg = ConfigFactory.create(AutoSwitchUsableConfig.class);

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
            AutoSwitch.data.enchantToolMap.clear();
            new AutoSwitchMapsGenerator();
        });

        AutoSwitch.cfg.addReloadListener(event -> {
            AutoSwitch.data.toolLists.clear();
            new AutoSwitchMapsGenerator();
        });

        AutoSwitch.usableCfg.addReloadListener(event -> {
            AutoSwitch.data.enchantToolMap.clear();
            AutoSwitch.data.useMap.clear();
            new AutoSwitchMapsGenerator();
        });
    }

}
