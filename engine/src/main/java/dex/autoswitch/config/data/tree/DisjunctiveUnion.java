package dex.autoswitch.config.data.tree;

import dex.autoswitch.engine.data.Match;
import dex.autoswitch.engine.data.SelectionContext;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;

/**
 * Operation: {@code XOR}
 */
public record DisjunctiveUnion(Set<ExpressionTree> children) implements ExpressionTree {
    public DisjunctiveUnion {
        Objects.requireNonNull(children);
    }

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
