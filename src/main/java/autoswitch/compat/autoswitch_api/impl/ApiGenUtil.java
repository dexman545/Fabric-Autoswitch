package autoswitch.compat.autoswitch_api.impl;

import java.util.Set;

import autoswitch.AutoSwitch;
import autoswitch.api.AutoSwitchApi;
import autoswitch.api.AutoSwitchMap;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.apache.commons.lang3.SerializationUtils;

import net.fabricmc.loader.api.FabricLoader;

public class ApiGenUtil {
    public static final Object2ObjectOpenHashMap<String, Set<String>> modActionConfigs =
            new Object2ObjectOpenHashMap<>();
    public static final Object2ObjectOpenHashMap<String, Set<String>> modUseConfigs = new Object2ObjectOpenHashMap<>();

    public static void pullHookedMods() {
        FabricLoader.getInstance().getEntrypointContainers("autoswitch", AutoSwitchApi.class).forEach(entrypoint -> {
            AutoSwitchApi api = entrypoint.getEntrypoint();
            api.customDamageSystems(AutoSwitch.switchData.damageMap);
            api.moddedToolGroups(AutoSwitch.switchData.toolGroupings);
            final AutoSwitchMap<String, String> baseAction = duplicateMap(AutoSwitch.switchData.attackConfig);
            final AutoSwitchMap<String, String> baseUseAction = duplicateMap(AutoSwitch.switchData.usableConfig);
            api.moddedTargets(AutoSwitch.switchData.targets, AutoSwitch.switchData.attackConfig,
                              AutoSwitch.switchData.usableConfig);

            final String name = entrypoint.getProvider().getMetadata().getName();

            processActionDif(name, baseAction);
            processUseDif(name, baseUseAction);

            AutoSwitch.logger.info("AutoSwitch has interfaced with the {} mod!", name);
        });
    }

    // Deep copy the map via (de)serializing it. All types within must be serializable
    private static <K, V> AutoSwitchMap<K, V> duplicateMap(AutoSwitchMap<K, V> map) {
        return SerializationUtils.clone(map);
    }

    private static void processActionDif(String mod, AutoSwitchMap<String, String> base) {
        processMapDiff(mod, base, AutoSwitch.switchData.attackConfig, modActionConfigs);
    }

    private static void processUseDif(String mod, AutoSwitchMap<String, String> base) {
        processMapDiff(mod, base, AutoSwitch.switchData.usableConfig, modUseConfigs);
    }

    private static void processMapDiff(String mod, AutoSwitchMap<String, String> base,
                                       AutoSwitchMap<String, String> config,
                                       Object2ObjectOpenHashMap<String, Set<String>> populationTarget) {
        Set<String> diff = diffMaps(base, config);

        if (diff != null) {
            populationTarget.put(mod, diff);
        }
    }

    private static Set<String> diffMaps(final AutoSwitchMap<String, String> base,
                                        final AutoSwitchMap<String, String> modded) {
        if (!base.equals(modded)) {
            MapDifference<String, String> diff = Maps.difference(base, modded);
            return diff.entriesOnlyOnRight().keySet();
        }

        return null;
    }

}
