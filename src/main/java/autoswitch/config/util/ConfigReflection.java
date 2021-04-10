package autoswitch.config.util;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Properties;

import autoswitch.AutoSwitch;
import autoswitch.config.AutoSwitchAttackActionConfig;
import autoswitch.config.AutoSwitchConfig;
import autoswitch.config.AutoSwitchUseActionConfig;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.aeonbits.owner.Config;

public class ConfigReflection {


    /**
     * Map of config classes for check and casting as asking for the class on the config object directly returns some
     * random sun object.
     */
    private static final ObjectArrayList<Class<? extends Config>> configClasses = new ObjectArrayList<>();

    static {
        configClasses.add(AutoSwitchAttackActionConfig.class);
        configClasses.add(AutoSwitchConfig.class);
        configClasses.add(AutoSwitchUseActionConfig.class);
    }

    // Fix returning wrong class
    public static Class<? extends Config> getClass(Object o) {
        for (Class<? extends Config> configClass : ConfigReflection.configClasses) {
            if (configClass.isInstance(o)) return configClass;
        }

        AutoSwitch.logger.error("Attempted to gen. config template for an unknown config class!");
        return null;
    }

    // Populate provided map object with the default values for the config
    public static void defaults(Map<String, String> properties, Class<? extends Config> clazz) {
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            String key = key(method);
            String value = defaultValue(method);
            if (value != null) {
                properties.put(key, value);
            }
        }
    }

    // Get the config entry's key
    public static String key(Method method) {
        Config.Key key = method.getAnnotation(Config.Key.class);
        return (key == null) ? method.getName() : key.value().replaceAll("(?<!\\\\)(?:\\\\{2})*:", "\\:");
    }

    // Get the config entry's value
    private static String defaultValue(Method method) {
        Config.DefaultValue defaultValue = method.getAnnotation(Config.DefaultValue.class);
        return defaultValue != null ? defaultValue.value() : null;
    }

    // Populate provided properties object with the default values for the config
    public static void defaults(Properties properties, Class<? extends Config> clazz) {
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            String key = key(method);
            String value = defaultValue(method);
            if (value != null) {
                properties.put(key, value);
            }
        }
    }

    // Populate provided properties object with the comments for the config entries
    public static void comments(Properties properties, Class<? extends Config> clazz) {
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            String key = key(method);
            String value = comment(method);
            if (value != null) {
                properties.put(key, value);
            }
        }
    }

    // get the config entry's comment
    private static String comment(Method method) {
        Comment comment = method.getAnnotation(Comment.class);
        return (comment != null) ? comment.value() : null;
    }

}
