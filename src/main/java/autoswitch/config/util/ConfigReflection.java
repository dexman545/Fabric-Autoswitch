package autoswitch.config.util;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
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
    public static <T extends AccessibleObject & Member> String key(T accessibleMember) {
        Key key = accessibleMember.getAnnotation(Key.class);
        return (key == null) ? accessibleMember.getName() : key.value().replaceAll("(?<!\\\\)(?:\\\\{2})*:", "\\:");
    }

    // Get the config entry's value
    private static String defaultValue(AccessibleObject accessibleObject) {
        DefaultValue defaultValue = accessibleObject.getAnnotation(DefaultValue.class);
        return defaultValue != null ? defaultValue.value() : null;
    }

    // Populate provided properties object with the default values for the config
    public static void defaults(Properties properties, Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            String key = key(field);
            String value = defaultValue(field);
            if (value != null) {
                properties.put(key, value);
            }
        }
    }

    // Populate provided properties object with the comments for the config entries
    public static void comments(Properties properties, Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            String key = key(field);
            String value = comment(field);
            if (value != null) {
                properties.put(key, value);
            }
        }
    }

    // get the config entry's comment
    private static String comment(AccessibleObject accessibleObject) {
        Comment comment = accessibleObject.getAnnotation(Comment.class);
        return (comment != null) ? comment.value() : null;
    }

}
