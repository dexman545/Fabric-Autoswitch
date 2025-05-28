package dex.autoswitch.engine.data.extensible;

import dex.autoswitch.engine.TargetType;
import dex.autoswitch.engine.data.SelectionContext;
import dex.autoswitch.futures.FutureSelectable;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;

/**
 * Define lookup and match methods for selections, e.g. Block or Item.
 * <p>
 * Should be registered before config creation as it will look up available instances during deserialization.
 *
 * @param <KEY>   The type of key used to lookup values
 * @param <VALUE> The type this SelectableType can select
 * @param <GROUP> The type of group that can contain this type
 */
public abstract class SelectableType<KEY, VALUE, GROUP> {
    private final String id;

    protected SelectableType(String id) {
        Objects.requireNonNull(id);
        this.id = id.toUpperCase(Locale.ENGLISH);
    }

    /**
     * Get the instance that matches the given key
     */
    public abstract VALUE lookup(KEY key);

    /**
     * Get the group instance that matches the given key
     */
    public abstract GROUP lookupGroup(KEY key);

    /**
     * @param selectable the selectable, e.g. the tool or target,
     *                   {@link DataType} should route to a {@link SelectableType} as necessary and expect to see
     *                   the original type if it is a holder of this type, e.g. a {@code Holder(Set<Enchantment>)}
     * @return if these two values match. Should not consider data.
     */
    public abstract boolean matches(SelectionContext context, VALUE v, Object selectable);

    /**
     * @param selectable the selectable, e.g. the tool or target,
     *                   {@link DataType} should route to a {@link SelectableType} as necessary and expect to see
     *                   the original type if it is a holder of this type, e.g. a {@code Holder(Set<Enchantment>)}
     * @return if the given value matches the group. Should not consider data.
     */
    public abstract boolean matchesGroup(SelectionContext context, GROUP group, Object selectable);

    /**
     * Convert the given key to a String for writing
     */
    public abstract String serializeKey(KEY key);

    /**
     * Convert the given key string to a KEY instance
     */
    public abstract KEY deserializeKey(String key);

    /**
     * @return the {@link TargetType} of this type for use in the config.
     */
    public abstract @Nullable TargetType targetType();

    /**
     * @return {@code true} if the given object can be handled by this type.
     */
    public abstract boolean isOf(Object o);

    /**
     * The rating for this type in the given context.
     *
     * @param context     the context of this match, e.g. the target block for tool matching
     * @param futureValue the value to the ratings are for
     * @param selectable  the object to generate the ratings for
     * @return the rating for the given input, should be normalized to the range of [0, 1]
     */
    public abstract double typeRating(SelectionContext context, FutureSelectable<KEY, VALUE> futureValue, Object selectable);

    /**
     * @return the id of this SelectorType
     */
    public String id() {
        return id;
    }

    @Override
    public String toString() {
        return id;
    }
}
