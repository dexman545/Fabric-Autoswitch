package autoswitch.config.util;

import autoswitch.config.AutoSwitchConfig;
import org.aeonbits.owner.Converter;

import java.lang.reflect.Method;

public class CaseInsensitiveEnumConverter implements Converter<AutoSwitchConfig.SwitchDelay> {
    @Override
    public AutoSwitchConfig.SwitchDelay convert(Method method, String input) {

        switch (input.toLowerCase()) {
            case "blocks":
                return AutoSwitchConfig.SwitchDelay.BLOCKS;
            case "mobs":
                return AutoSwitchConfig.SwitchDelay.MOBS;
            case "both":
                return AutoSwitchConfig.SwitchDelay.BOTH;
            default:
                return AutoSwitchConfig.SwitchDelay.NONE;
        }

    }
}
