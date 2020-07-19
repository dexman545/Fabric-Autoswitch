package autoswitch.util;

import autoswitch.AutoSwitch;
import autoswitch.api.AutoSwitchApi;
import net.fabricmc.loader.api.FabricLoader;

public class ApiGenUtil {

    public static void pullHookedMods() {
        FabricLoader.getInstance().getEntrypointContainers("autoswitch", AutoSwitchApi.class).forEach(entrypoint -> {
            AutoSwitchApi api = entrypoint.getEntrypoint();
            api.customDamageSystems(AutoSwitch.data.damageMap);
            api.moddedToolGroups(AutoSwitch.data.toolGroupings);
            api.moddedTargets(AutoSwitch.data.targets, AutoSwitch.data.actionConfig, AutoSwitch.data.usableConfig);
            AutoSwitch.logger.info("AutoSwitch has interfaced with the {} mod!",
                    entrypoint.getProvider().getMetadata().getName());
        });
    }

}
