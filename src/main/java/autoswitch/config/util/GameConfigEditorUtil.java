package autoswitch.config.util;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Supplier;

import autoswitch.AutoSwitch;
import autoswitch.actions.Action;
import autoswitch.config.AutoSwitchAttackActionConfig;
import autoswitch.config.AutoSwitchConfig;
import autoswitch.config.AutoSwitchEventActionConfig;
import autoswitch.config.AutoSwitchUseActionConfig;
import autoswitch.config.io.ConfigWritable;

import org.aeonbits.owner.Accessible;
import org.aeonbits.owner.Config;
import org.aeonbits.owner.Mutable;
import org.aeonbits.owner.Reloadable;

public class GameConfigEditorUtil<T extends Config & Reloadable & Accessible & Mutable> {
    private static final MethodHandles.Lookup lookup = MethodHandles.publicLookup();
    @SuppressWarnings("unchecked")
    static Class<? extends Number>[] NUMERICS = (Class<? extends Number>[]) new Class<?>[]{Float.class, Double.class,
                                                                                           Integer.class, Long.class};
    public static final GameConfigEditorUtil<AutoSwitchConfig> FEATURE_CONFIG =
            new GameConfigEditorUtil<>(() -> AutoSwitch.featureCfg, AutoSwitchConfig.class);
    public static final GameConfigEditorUtil<AutoSwitchAttackActionConfig> ATTACK_CONFIG =
            new GameConfigEditorUtil<>(() -> AutoSwitch.attackActionCfg, AutoSwitchAttackActionConfig.class, Action.ATTACK);
    public static final GameConfigEditorUtil<AutoSwitchUseActionConfig> INTERACT_CONFIG =
            new GameConfigEditorUtil<>(() -> AutoSwitch.useActionCfg, AutoSwitchUseActionConfig.class, Action.INTERACT);
    public static final GameConfigEditorUtil<AutoSwitchEventActionConfig> EVENT_CONFIG =
            new GameConfigEditorUtil<>(() -> AutoSwitch.eventActionConfig, AutoSwitchEventActionConfig.class, Action.EVENT);

    private final Supplier<T> configObject;
    private final Class<T> clazz;
    private final Action action;

    private GameConfigEditorUtil(Supplier<T> configObject, Class<T> clazz) {
        this(configObject, clazz, null);
    }

    private GameConfigEditorUtil(Supplier<T> configObject, Class<T> clazz, Action action) {
        this.configObject = configObject;
        this.clazz = clazz;
        this.action = action;
    }

    //todo default config value getter

    public boolean hasAction() {
        return action != null;
    }

    public <C> Consumer<C> modifyConfig(Method method) {
        return modifyConfig(ConfigReflection.key(method), method.getReturnType().equals(Integer.class));
    }

    public <C> Consumer<C> modifyConfig(String configEntry) {
        return modifyConfig(configEntry, false);
    }

    public <C> Consumer<C> modifyConfig(String configEntry, boolean forceInt) {
        return (newValue) -> {
            String configValue;
            if (newValue instanceof ConfigWritable configWritable) {
                configValue = configWritable.configEntry();
            } else if (newValue instanceof Collection<?> coll) {
                var arr = coll.toArray();
                configValue = "";
                for (int i = 0; i < coll.size(); i++) {
                    if (arr[i] instanceof ConfigWritable configWritable) {
                        configValue += configWritable.configEntry();
                    } else {
                        configValue += String.valueOf(arr[i]);
                    }

                    if (i < coll.size() - 1) configValue += ", ";
                }
            } else if (forceInt && newValue instanceof Number num) {
                configValue = String.valueOf(num.intValue());
            } else {
                configValue = String.valueOf(newValue);
            }

            //todo write changes to a buffer first? otherwise config file will differ from internal config state
            // could also just reload the files if not commited
            //AutoSwitch.logger.error("{} = {}", configEntry, configValue);
            configObject.get().setProperty(configEntry, configValue);
        };
    }

    public <C> C currentValue(Method configEntry) {
        try {
            return (C) configEntry.invoke(configObject.get());
        } catch (IllegalAccessException | InvocationTargetException e) {
            AutoSwitch.logger.error("Failed to get current value for " + ConfigReflection.key(configEntry), e);
            throw new RuntimeException(e);
        }
    }

    //todo ensure configEntry is methodName, not key - how to handle action configs?
    public <C> C currentValue(String configEntry, Class<C> returnType) {
        return currentValue(configEntry, returnType, false);
    }

    //todo ensure configEntry is methodName, not key - how to handle action configs?
    public <C> C currentValue(String configEntry, Class<C> returnType, boolean multiple) {
        var mt = MethodType.methodType(returnType);
        try {
            var mh = lookup.findVirtual(clazz, configEntry, mt);
            return (C) mh.invoke(configObject.get());
        } catch (Throwable e) {
            if (multiple) {
                return null;
            }
            throw new RuntimeException(e);
        }
    }

    public Number currentValueNumeric(String configEntry) {
        for (Class<? extends Number> numeric : NUMERICS) {
            Number o;
            if ((o = currentValue(configEntry, numeric, true)) != null) {
                return o;
            }
        }

        throw new IllegalStateException(configEntry);
    }
}
