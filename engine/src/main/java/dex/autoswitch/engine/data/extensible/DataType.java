package dex.autoswitch.engine.data.extensible;

import java.util.Locale;
import java.util.Objects;

import dex.autoswitch.config.data.tree.Data;
import dex.autoswitch.engine.data.Match;
import dex.autoswitch.engine.data.SelectionContext;

public abstract class DataType<T extends Data> {
    private final String id;
    private final Class<T> supportedData;

    protected DataType(String id, Class<T> supportedData) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(supportedData);
        this.supportedData = supportedData;
        this.id = id.toUpperCase(Locale.ENGLISH);
    }

    /**
     * Determines if the given data matches the selectable and includes ratings for this data
     *
     * @param baseLevel  the level to use when generating ratings levels
     * @param context    the context of this match, e.g. the target block for tool matching
     * @param selectable the object to generate the ratings for. This may be a holder type such
     *                   as a {@code Holder(Set<Enchantment>)}
     * @return the match for the given input, ratings should be normalized to the range of [0, 1]
     */
    public abstract Match matches(int baseLevel, SelectionContext context, Object selectable, Data data);

    /**
     * @return the id of this DataType
     */
    public String id() {
        return id;
    }

    @Override
    public String toString() {
        return id;
    }

    public Class<T> getSupportedData() {
        return supportedData;
    }

    /**
     * @return if {@code true}, the data type matching will be given a new context during matching.
     */
    public boolean recontextualize() {
        return false;
    }
}
