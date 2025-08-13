package dex.autoswitch.config.data;

import java.util.Set;

import dex.autoswitch.engine.Action;
import dex.autoswitch.engine.Selector;

public record FallbackSelector(Set<Action> actions, Selector fallback) {
    public boolean match(Action action) {
        return actions.contains(action);
    }
}
