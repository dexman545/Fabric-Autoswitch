package dex.autoswitch.engine.types.data;

import java.util.HashSet;
import java.util.Set;

import dex.autoswitch.config.data.tree.DataMap;
import dex.autoswitch.engine.data.Match;
import dex.autoswitch.engine.data.SelectionContext;
import dex.autoswitch.engine.data.extensible.DataType;

import net.minecraft.world.level.block.state.BlockState;

public class BlockStateData extends DataType<DataMap> {
    public static final BlockStateData INSTANCE = new BlockStateData();

    private BlockStateData() {
        super("blockstates", DataMap.class);
    }

    @Override
    public Match matches(int baseLevel, SelectionContext context, Object selectable, DataMap data) {
        if (selectable instanceof BlockState blockState) {
            var requiredState = process(data);

            var stateDefinition = blockState.getBlock().getStateDefinition();
            for (StateProperty stateProperty : requiredState) {
                var property = stateDefinition.getProperty(stateProperty.key);

                if (property == null) {
                    return new Match(false);
                }

                var expectedValue = property.getValue(stateProperty.val);
                if (expectedValue.isEmpty()) {
                    return new Match(false);
                }

                if (expectedValue.get() != blockState.getValue(property)) {
                    return new Match(false);
                }
            }

            return new Match(true);
        }

        return new Match(false);
    }

    private Set<StateProperty> process(DataMap dataMap) {
        var properties = new HashSet<StateProperty>();
        switch (dataMap) {
            case DataMap.Map map -> {
                for (DataMap entry : map.entries()) {
                    properties.addAll(process(entry));
                }
            }
            case DataMap.Pair(var key, DataMap.Value mv) -> {
                properties.add(new StateProperty(key, mv.value()));
            }
            case DataMap.Value value -> {
            }
            case DataMap.Pair pair -> {
            }
        }

        return properties;
    }

    private record StateProperty(String key, String val) {}
}
