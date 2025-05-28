package dex.autoswitch.engine;

import dex.autoswitch.engine.data.Match;
import dex.autoswitch.engine.data.SelectionContext;

import java.util.Objects;

/**
 * @param priority the priority of this {@code Selector} in relation to other {@code Selector}s of the same type
 */
public record Selector(int priority, Matcher matcher) {
    public Selector {
        Objects.requireNonNull(matcher);
    }

    public Match matches(SelectionContext context, Object selectable) {
        return matcher.matches(0, context, selectable);
    }
}
