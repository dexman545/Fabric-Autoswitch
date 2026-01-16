package dex.autoswitch.engine.types.data;

import java.util.Map;

import dex.autoswitch.config.data.tree.SingleValuedDataMap;
import dex.autoswitch.engine.data.Match;
import dex.autoswitch.engine.data.SelectionContext;
import dex.autoswitch.engine.data.extensible.DataType;
import io.leangen.geantyref.TypeToken;

import net.minecraft.world.level.block.state.BlockState;

public class BlockStateData extends DataType<SingleValuedDataMap<String, String>> {
    public static final BlockStateData INSTANCE = new BlockStateData();

    private BlockStateData() {
        //noinspection Convert2Diamond
        super("blockstates", new TypeToken<SingleValuedDataMap<String, String>>() {});
    }

    @Override
    public Match matches(int baseLevel, SelectionContext context, Object selectable, SingleValuedDataMap<String, String> data) {
        if (selectable instanceof BlockState blockState) {
            var stateDefinition = blockState.getBlock().getStateDefinition();
            for (Map.Entry<String, String> stateProperty : data.map().entrySet()) {
                var property = stateDefinition.getProperty(stateProperty.getKey());

                if (property == null) {
                    return new Match(false);
                }

                var expectedValue = property.getValue(stateProperty.getValue());
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
}
