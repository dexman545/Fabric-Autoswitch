package dex.autoswitch.harness;

import com.google.auto.service.AutoService;
import dex.autoswitch.engine.Matcher;
import dex.autoswitch.engine.data.Match;
import dex.autoswitch.engine.data.extensible.DataType;
import dex.autoswitch.engine.data.extensible.SelectableType;
import dex.autoswitch.engine.data.extensible.SwitchRegistryService;

import java.util.Set;

@AutoService(SwitchRegistryService.class)
public class TestSwitchRegistry implements SwitchRegistryService {
    private static final Matcher NON_TOOL = (i, j, selectable) -> {
        if (selectable instanceof DummyTypes.DummyTool(var id, var data)) {
            return new Match(id.equalsIgnoreCase("air"));
        }

        return new Match(false);
    };

    @Override
    public Set<SelectableType<?, ?, ?>> selectableTypes() {
        return Set.of(DummyTypes.BLOCK_TYPE, DummyTypes.ENCHANTMENT_TYPE, DummyTypes.ITEM_TYPE, DummyTypes.ENTIY_TYPE);
    }

    @Override
    public Set<DataType<?>> dataTypes() {
        return Set.of(DummyTypes.COMPONENTS, DummyTypes.ENCHANTMENTS);
    }

    @Override
    public Matcher nonToolMatcher() {
        return NON_TOOL;
    }
}
