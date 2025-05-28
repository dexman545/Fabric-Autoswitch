package dex.autoswitch.futures;

import dex.autoswitch.engine.data.SelectionContext;
import dex.autoswitch.engine.data.extensible.SelectableType;

public final class FutureSelectableGroup<KEY, TYPE, GROUP> extends FutureSelectable<KEY, TYPE> {
    private final GROUP group;

    public FutureSelectableGroup(KEY key, SelectableType<KEY, TYPE, GROUP> type) {
        super(key, type);
        group = type.lookupGroup(key);
        status = Status.VALID;
    }

    @Override
    public boolean matches(SelectionContext context, Object o) {
        //noinspection unchecked
        return ((SelectableType<KEY, TYPE, GROUP>) selectableType).matchesGroup(context, group, o);
    }

    @Override
    public void validate() {

    }

    public GROUP getGroup() {
        return group;
    }

    @Override
    public String toString() {
        return "FutureGroup{" +
                "key=" + key +
                ", selectorType=" + selectableType +
                '}';
    }
}
