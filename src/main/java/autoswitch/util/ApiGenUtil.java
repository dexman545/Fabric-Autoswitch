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

            processActionDif(entrypoint.getProvider().getMetadata().getName(), baseAction);
            processUseDif(entrypoint.getProvider().getMetadata().getName(), baseUseAction);

            AutoSwitch.logger.info("AutoSwitch has interfaced with the {} mod!",
                    entrypoint.getProvider().getMetadata().getName());
        });
    }

    // Deep copy the map via (de)serializing it. All types within must be serializable
    private static <K, V> AutoSwitchMap<K, V> duplicateMap(AutoSwitchMap<K, V> map) {
        return SerializationUtils.clone(map);
    }

    private static Set<String> diffMaps(final AutoSwitchMap<String, String> base, final AutoSwitchMap<String, String> modded) {
        if (!base.equals(modded)) {
            MapDifference<String, String> diff = Maps.difference(base, modded);
            return diff.entriesOnlyOnRight().keySet();
        }

        return null;
    }

    private static void processActionDif(String mod, AutoSwitchMap<String, String> base) {
        Set<String> diff = diffMaps(base, AutoSwitch.data.actionConfig);

        if (diff != null) {
            modActionConfigs.put(mod, diff);
        }

    }

    private static void processUseDif(String mod, AutoSwitchMap<String, String> base) {
        Set<String> diff = diffMaps(base, AutoSwitch.data.usableConfig);

        if (diff != null) {
            modUseConfigs.put(mod, diff);
        }

    }

}
