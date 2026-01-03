package dex.autoswitch.engine.data.extensible;

import java.util.Set;

import dex.autoswitch.engine.Matcher;

public interface SwitchRegistryService {
    Set<SelectableType<?, ?, ?>> selectableTypes();

    Set<DataType<?>> dataTypes();

    default Matcher nonToolMatcher() {
        return null;
    }
}
