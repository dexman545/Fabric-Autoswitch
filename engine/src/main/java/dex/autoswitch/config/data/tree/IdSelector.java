package dex.autoswitch.config.data.tree;

import java.util.Objects;
import java.util.Set;

import dex.autoswitch.engine.data.Match;
import dex.autoswitch.engine.data.SelectionContext;
import dex.autoswitch.futures.FutureSelectable;
import dex.autoswitch.futures.FutureSelectableGroup;
import dex.autoswitch.futures.FutureSelectableValue;
import org.jetbrains.annotations.NotNull;

/**
 * The basic selector. Favors more specific (not a group, more required data) over more general selectors
 */
public record IdSelector(FutureSelectable<?, ?> selectable, Set<TypedData> data) implements ExpressionTree {
    public IdSelector {
        Objects.requireNonNull(selectable);
        if (data == null) {
            data = Set.of();
        }
    }

    /**
     * Evaluates whether the current selector matches a given input selectable within a specified context
     * and computes associated ratings based on the selector's specificity and data properties.
     * <p>
     * Ratings are assigned to the selector based on the following:
     * <ol>
     *     <li>isGroup</li>
     *     <li>hasData</li>
     *     <li>typeRating</li>
     *     <li>dataRating</li>
     * </ol>
     *
     * In general, the higher the rating level, the more specific the selector is, followed by the type and data ratings.
     *
     * @param baseLevel the initial rating level used as a starting point for assigning ratings.
     * @param context the selection context containing details about the action and target to evaluate.
     * @param inputSelectable the object that will be tested against the selector to determine a match.
     * @return a {@link Match} object indicating whether a match was found and containing associated ratings.
     */
    @Override
    public Match matches(int baseLevel, SelectionContext context, Object inputSelectable) {
        var match = new Match(selectable().matches(context, inputSelectable));
        if (match.matches()) {
            // Favor specific values over groups
            match.addRating(baseLevel++, () -> switch (this.selectable) {
                case FutureSelectableGroup<?, ?, ?> v -> 0;
                case FutureSelectableValue<?, ?> v -> 1;
            });

            // Favor IdSelectors with more required data types
            match.addRating(baseLevel++, data::size);

            // The type rating for the given context and input selectable
            match.addRating(baseLevel++, () -> this.selectable.rating(context, inputSelectable));

            // Merge specific data ratings, returning false if any fail to match
            for (TypedData typedData : data) {
                var m = typedData.matches(baseLevel, context, inputSelectable);
                if (!m.matches()) {
                    return m;
                }

                match.merge(m);
            }
        }

        return match;
    }

    @Override
    public @NotNull String toString() {
        var sb = new StringBuilder(selectable.getSelectorType().id());

        sb.append('[');

        switch (selectable) {
            case FutureSelectableGroup<?, ?, ?> v -> {
                sb.append('#').append(v.getKey());
            }
            case FutureSelectableValue<?, ?> v -> {
                sb.append(v.getKey());
            }
        }

        if (!data.isEmpty()) {
            sb.append(" Data: ");
            sb.append(data);
        }

        sb.append(']');
        return sb.toString();
    }
}
