package autoswitch.config.util;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Locale;

import autoswitch.AutoSwitch;

import org.aeonbits.owner.Converter;
import org.jetbrains.annotations.Nullable;

public class CaseInsensitiveEnumConverter<X extends Enum<X>> implements Converter<X> {

    /**
     * Could be replaced with {@link org.apache.commons.lang3.EnumUtils#getEnum(Class, String)}
     *
     * @author <a href="https://stackoverflow.com/a/28333189">Stack overflow.</a>
     */
    private static <T extends Enum<?>> @Nullable T searchEnum(Class<T> enumeration, String search) {
        for (T each : enumeration.getEnumConstants()) {
            if (each.name().compareToIgnoreCase(search) == 0) {
                return each;
            }
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public X convert(Method method, String input) {
        Class<X> t = (Class<X>) method.getReturnType();

        if (!t.isEnum()) {
            if (method.getGenericReturnType() instanceof ParameterizedType parameterizedType) {
                t = (Class<X>) parameterizedType.getActualTypeArguments()[0];
            }
        }

        X e;
        if ((e = searchEnum(t, input.toUpperCase(Locale.ENGLISH))) == null) {
            AutoSwitch.logger.error("Could not parse value: {} on {}; Defaulting to another.", input, method.getName());
            return t.getEnumConstants()[0];
        }
        return e;

    }

}
