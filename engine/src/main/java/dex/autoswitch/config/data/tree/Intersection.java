package dex.autoswitch.config.data.tree;

import java.util.Objects;
import java.util.Set;

import dex.autoswitch.engine.data.Match;
import dex.autoswitch.engine.data.SelectionContext;
import org.jetbrains.annotations.NotNull;

/**
 * Operation: {@code OR}
 */
public record Intersection(Set<ExpressionTree> children) implements ExpressionTree {
    public Intersection {
        Objects.requireNonNull(children);
    }

    /**
     * Evaluates whether the given {@code selectable} object matches the conditions specified by any child
     * {@link ExpressionTree} nodes. If a match is found, the method merges the ratings of all matching
     * children into a single {@link Match} object.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public Match matches(int baseLevel, SelectionContext context, Object selectable) {
        Match match = null;

        // Instead of bailing on the first match, we keep processing to merge the ratings together
        for (ExpressionTree child : children) {
            var m = child.matches(baseLevel, context, selectable);

            if (match != null) {
                match.merge(m);
            } else if (m.matches()) {
                match = m;
            }
        }

        return match != null ? match : new Match(false);
    }

    @Override
    public @NotNull String toString() {
        return "OR" + '{' +
                children.toString() +
                '}';
    }
}
