package dex.autoswitch.config.data.tree;

import java.util.Objects;

import dex.autoswitch.engine.data.Match;
import dex.autoswitch.engine.data.SelectionContext;
import org.jetbrains.annotations.NotNull;

/**
 * Operation: {@code NOT}
 *
 * @param child the child node the operation will be run on
 */
public record Invert(ExpressionTree child) implements ExpressionTree {
    public Invert {
        Objects.requireNonNull(child);
    }

    /**
     * Evaluates whether the given {@code selectable} does NOT match the conditions defined by this expression tree.
     * The result of this method inverts the matching outcome of the child expression while retaining its ratings.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public Match matches(int baseLevel, SelectionContext context, Object selectable) {
        var m = child.matches(baseLevel, context, selectable);
        return new Match(!m.matches(), m.ratings());//todo return 0 for ratings? or make them negative?
    }

    @Override
    public @NotNull String toString() {
        return "NOT" + '[' +
                child.toString() +
                ']';
    }
}
