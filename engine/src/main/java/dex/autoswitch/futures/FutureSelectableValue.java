package dex.autoswitch.futures;

import dex.autoswitch.engine.data.SelectionContext;
import dex.autoswitch.engine.data.extensible.SelectableType;

public final class FutureSelectableValue<KEY, TYPE> extends FutureSelectable<KEY, TYPE> {
    private TYPE value;

    FutureSelectableValue(KEY key, SelectableType<KEY, TYPE, ?> type) {
        super(key, type);
    }

    @Override
    public boolean matches(SelectionContext context, Object o) {
        validate();

        if (value == null || status == Status.INVALID) {
            status = Status.INVALID;
            return false;
        }

        return selectableType.matches(context, value, o);
    }

    @Override
    public void validate() {
        if (status != Status.UNVERIFIED) {
            return;
        }

        value = selectableType.lookup(key);
        if (value != null) {
            status = Status.VALID;
            return;
        }

        status = Status.INVALID;
    }

    public TYPE getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "FutureValue{" +
                "key=" + key +
                ", selectorType=" + selectableType +
                ", status=" + status +
                '}';
    }
}
