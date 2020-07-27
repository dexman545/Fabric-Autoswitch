package autoswitch.config.io;

import autoswitch.config.AutoSwitchMaterialConfig;
import autoswitch.config.AutoSwitchUsableConfig;
import autoswitch.config.util.SortedProperties;
import autoswitch.config.util.ConfigReflection;
import autoswitch.config.util.ConfigTemplates;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.aeonbits.owner.Accessible;
import org.aeonbits.owner.Config;

import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import static autoswitch.config.util.ConfigReflection.comments;
import static autoswitch.config.util.ConfigReflection.defaults;

public class GenerateConfigTemplate {

    public static <T extends Config & Accessible> String initConfig(T cfg, Object2ObjectOpenHashMap<String, Set<String>> moddedEntries, String header) {
        if (header == null) header = "";
        header = header.replace("\n", "\n# ");
        header = "# " + header; // Prepend comment symbol
        StringBuilder config = new StringBuilder(!header.equals("") ? header + "\n\n" : "");

        SortedProperties comments = new SortedProperties(new Properties());
        SortedProperties defaults = new SortedProperties(new Properties());

        defaults(defaults, Objects.requireNonNull(ConfigReflection.getClass(cfg)));
        comments(comments, Objects.requireNonNull(ConfigReflection.getClass(cfg)));
        sanitize(defaults);
        sanitize(comments);

        if (cfg instanceof AutoSwitchUsableConfig || cfg instanceof AutoSwitchMaterialConfig) {
            config.append(ConfigTemplates.toolGroupings());
            config.append("\n");
        }

        // Generate default config values
        for (String propertyName : defaults.stringPropertyNames()) {
            String entry = ConfigTemplates.configEntry(propertyName.replaceAll("(?<!\\\\)(?:\\\\{2})*:",
                    "\\\\:"), cfg.getProperty(propertyName),
                    comments.getProperty(propertyName), defaults.getProperty(propertyName)) + "\n";
            config.append(entry);
        }

        if (moddedEntries != null) {
            moddedEntries.forEach((mod, keys) -> {
                config.append(ConfigTemplates.modCategory(mod)).append("\n\n");

                for (String key : keys) {
                    config.append(ConfigTemplates.configEntry(key.replaceAll("(?<!\\\\)(?:\\\\{2})*:",
                            "\\\\:"), cfg.getProperty(key), null, null))
                            .append("\n");
                }
            });
        }

        Properties cfgProp = new Properties();
        cfg.fill(cfgProp);

        Set<Object> userKeys = diffMaps(defaults, cfgProp);
        if (userKeys != null) {
            config.append("\n# Overrides").append("\n").append(ConfigTemplates.border).append("\n\n");
            for (Object key : userKeys) {
                config.append(ConfigTemplates.configEntry(((String) key).replaceAll("(?<!\\\\)(?:\\\\{2})*:",
                        "\\\\:"), cfg.getProperty((String) key), null, null)).append("\n");
            }
        }

        return config.toString();


    }

    // Remove colons as it breaks when reading if not escaped
    private static void sanitize(Properties prop) {
        for (String propertyName : prop.stringPropertyNames()) {
            String newKey = propertyName.replaceAll("(?<!\\\\)(?:\\\\{2})*:", "\\:");
            if (!propertyName.equals(newKey)) {
                prop.put(newKey, prop.get(propertyName));
                prop.remove(propertyName);
            }
        }
    }

    private static Set<Object> diffMaps(final Properties base, final Properties modded) {
        if (!base.equals(modded)) {
            MapDifference<Object, Object> diff = Maps.difference(base, modded);
            return diff.entriesOnlyOnRight().keySet();
        }

        return null;
    }

}
