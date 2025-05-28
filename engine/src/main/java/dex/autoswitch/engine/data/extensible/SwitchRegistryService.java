package dex.autoswitch.engine.data.extensible;

import dex.autoswitch.engine.Matcher;

import java.util.Set;

public interface SwitchRegistryService {
    Set<SelectableType<?, ?, ?>> selectableTypes();

    Set<DataType<?>> dataTypes();

    default Matcher nonToolMatcher() {
        return null;
    }
}
