package dex.autoswitch.config.data.tree;

import dex.autoswitch.engine.data.Match;
import dex.autoswitch.engine.data.SelectionContext;
import dex.autoswitch.futures.FutureSelectable;
import dex.autoswitch.futures.FutureSelectableGroup;
import dex.autoswitch.futures.FutureSelectableValue;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;

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
