package dex.autoswitch.config.data.tree;

import java.util.Objects;
import java.util.Set;

import dex.autoswitch.engine.data.Match;
import dex.autoswitch.engine.data.SelectionContext;
import org.jetbrains.annotations.NotNull;

/**
 * Operation: {@code AND}
 *
 * @param children the child nodes the operation will be run on
 */
public record Union(Set<ExpressionTree> children) implements ExpressionTree {
    public Union {
        Objects.requireNonNull(children);
    }

    /**
     * Evaluates whether the given {@code selectable} matches the conditions defined by this expression tree.
     * The method checks all child nodes within with a union (and) operation, returning a match result
     * only if all child nodes evaluate to true. If any child node returns a non-matching result,
     * the method immediately returns that result. Otherwise, it combines the match results from all
     * child nodes and returns the merged result.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public Match matches(int baseLevel, SelectionContext context, Object selectable) {
        Match match = new Match(true);

        for (ExpressionTree child : children) {
            var m = child.matches(baseLevel, context, selectable);
            if (!m.matches()) {
                return m;
            }

            match.merge(m);
        }

        return match;
    }

    @Override
    public @NotNull String toString() {
        return "AND" + '{' +
                children.toString() +
                '}';
    }
}
