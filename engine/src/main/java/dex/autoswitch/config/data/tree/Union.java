package dex.autoswitch.config.data.tree;

import dex.autoswitch.engine.data.Match;
import dex.autoswitch.engine.data.SelectionContext;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;

/**
 * Operation: {@code AND}
 */
public record Union(Set<ExpressionTree> children) implements ExpressionTree {
    public Union {
        Objects.requireNonNull(children);
    }

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
