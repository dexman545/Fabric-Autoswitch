package autoswitch.util;

import autoswitch.AutoSwitch;
import autoswitch.api.AutoSwitchApi;
import autoswitch.api.AutoSwitchMap;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.lang3.SerializationUtils;

import java.util.Set;

public class ApiGenUtil {
    public static final Object2ObjectOpenHashMap<String, Set<String>> modActionConfigs = new Object2ObjectOpenHashMap<>();
    public static final Object2ObjectOpenHashMap<String, Set<String>> modUseConfigs = new Object2ObjectOpenHashMap<>();

    public static void pullHookedMods() {
        FabricLoader.getInstance().getEntrypointContainers("autoswitch", AutoSwitchApi.class).forEach(entrypoint -> {
            AutoSwitchApi api = entrypoint.getEntrypoint();
            api.customDamageSystems(AutoSwitch.data.damageMap);
            api.moddedToolGroups(AutoSwitch.data.toolGroupings);
            final AutoSwitchMap<String, String> baseAction = duplicateMap(AutoSwitch.data.actionConfig);
            final AutoSwitchMap<String, String> baseUseAction = duplicateMap(AutoSwitch.data.usableConfig);
            api.moddedTargets(AutoSwitch.data.targets, AutoSwitch.data.actionConfig, AutoSwitch.data.usableConfig);

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
        processMapDiff(mod, base, AutoSwitch.data.actionConfig, modActionConfigs);
    }

    private static void processUseDif(String mod, AutoSwitchMap<String, String> base) {
        processMapDiff(mod, base, AutoSwitch.data.usableConfig, modUseConfigs);
    }

    private static void processMapDiff(String mod, AutoSwitchMap<String, String> base,
                                       AutoSwitchMap<String, String> config,
                                       Object2ObjectOpenHashMap<String, Set<String>> populationTarget) {
        Set<String> diff = diffMaps(base, config);

        if (diff != null) {
            populationTarget.put(mod, diff);
        }
    }

    private static Set<String> diffMaps(final AutoSwitchMap<String, String> base, final AutoSwitchMap<String, String> modded) {
        if (!base.equals(modded)) {
            MapDifference<String, String> diff = Maps.difference(base, modded);
            return diff.entriesOnlyOnRight().keySet();
        }

        return null;
    }

}
