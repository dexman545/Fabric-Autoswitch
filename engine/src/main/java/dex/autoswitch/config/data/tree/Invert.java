package dex.autoswitch.config.data.tree;

import dex.autoswitch.engine.data.Match;
import dex.autoswitch.engine.data.SelectionContext;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Operation: {@code NOT}
 */
public record Invert(ExpressionTree child) implements ExpressionTree {
    public Invert {
        Objects.requireNonNull(child);
    }

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
