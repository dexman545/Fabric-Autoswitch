package dex.autoswitch.engine;

import java.util.Objects;

import dex.autoswitch.engine.data.Match;
import dex.autoswitch.engine.data.SelectionContext;

/**
 * A wrapper around a {@link Matcher} that associates it with a given {@code priority} and a base level of {@code 0} to
 * begin the matching process.
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
