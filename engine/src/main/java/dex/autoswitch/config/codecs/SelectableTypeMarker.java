package dex.autoswitch.config.codecs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jspecify.annotations.NonNull;

/**
 * A marker annotation for to provide the type so that users may elide the {@code type=<value>} from
 * {@link dex.autoswitch.config.data.tree.IdSelector} definitions in a config file in certain contexts.
 */
@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SelectableTypeMarker {
    @NonNull String value();
}
