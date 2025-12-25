package dex.autoswitch.config.data.tree;

import java.util.Objects;
import java.util.Set;

import dex.autoswitch.engine.data.Match;
import dex.autoswitch.engine.data.SelectionContext;
import org.jetbrains.annotations.NotNull;

/**
 * Operation: {@code XOR}
 * @param children the child nodes the operation will be run on
 */
public record DisjunctiveUnion(Set<ExpressionTree> children) implements ExpressionTree {
    public DisjunctiveUnion {
        Objects.requireNonNull(children);
    }

    /**
     * Evaluates whether the given {@code selectable} matches the conditions defined by this disjunctive union expression tree.
     * The method checks all child nodes with a disjunctive (XOR) operation, ensuring only one of the child nodes provides a match.
     * If more than one child node matches, the result is considered a non-match.
     * If exactly one child matches, its result is returned.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public Match matches(int baseLevel, SelectionContext context, Object selectable) {
        var foundMatch = false;
        Match match = new Match(false);

        for (ExpressionTree child : children) {
            match = child.matches(baseLevel, context, selectable);
            if (match.matches()) {
                if (foundMatch) {
                    return new Match(false);
                }
                foundMatch = true;
            }
        }

        return match;
    }

    @Override
    public @NotNull String toString() {
        return "XOR" + '{' +
                children.toString() +
                '}';
    }
}
