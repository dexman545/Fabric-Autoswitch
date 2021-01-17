package autoswitch.config.util;

import java.lang.reflect.Method;
import java.util.Locale;

import autoswitch.AutoSwitch;

import org.aeonbits.owner.Converter;
import org.jetbrains.annotations.Nullable;

public class CaseInsensitiveEnumConverter<X extends Enum<X>> implements Converter<X> {

    @Override
    public X convert(Method method, String input) {
        @SuppressWarnings("unchecked") Class<X> t = (Class<X>) method.getReturnType();
        X e;
        if ((e = searchEnum(t, input.toUpperCase(Locale.ENGLISH))) == null) {
            AutoSwitch.logger.error("Could not parse value: {} on {}; Defaulting to another.", input, method.getName());
            return t.getEnumConstants()[0];
        }
        return e;

    }

    /**
     * Could be replaced with {@link org.apache.commons.lang3.EnumUtils#getEnum(Class, String)}
     *
     * @author https://stackoverflow.com/a/28333189
     */
    private static <T extends Enum<?>> @Nullable T searchEnum(Class<T> enumeration, String search) {
        for (T each : enumeration.getEnumConstants()) {
            if (each.name().compareToIgnoreCase(search) == 0) {
                return each;
            }
        }
        return null;
    }

}
