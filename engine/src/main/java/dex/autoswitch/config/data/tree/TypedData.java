package dex.autoswitch.config.data.tree;

import dex.autoswitch.engine.Matcher;
import dex.autoswitch.engine.data.Match;
import dex.autoswitch.engine.data.SelectionContext;
import dex.autoswitch.engine.data.extensible.DataType;

import java.util.Objects;

public record TypedData(DataType<?> type, Data data) implements Matcher {
    public TypedData {
        Objects.requireNonNull(type);
        Objects.requireNonNull(data);
    }

    @Override
    public Match matches(int baseLevel, SelectionContext context, Object selectable) {
        return type.matches(baseLevel, context, selectable, data);
    }
}
