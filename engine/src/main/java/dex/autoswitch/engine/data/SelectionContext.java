package dex.autoswitch.engine.data;

import dex.autoswitch.engine.Action;

public record SelectionContext(Action action, Object target) {
}
