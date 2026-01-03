package dex.autoswitch.engine.types.data;

import dex.autoswitch.Constants;
import dex.autoswitch.config.data.tree.DataMap;
import dex.autoswitch.engine.data.Match;
import dex.autoswitch.engine.data.SelectionContext;
import dex.autoswitch.engine.data.extensible.DataType;
import io.leangen.geantyref.TypeToken;

import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class ComponentData extends DataType<DataMap> {
    public static final ComponentData INSTANCE = new ComponentData();

    private ComponentData() {
        //noinspection Convert2Diamond
        super("components", new TypeToken<DataMap>() {});
    }

    @Override
    public Match matches(int baseLevel, SelectionContext context, Object selectable, DataMap data) {
        if (selectable instanceof ItemStack stack) {
            return new Match(process(stack, data));
        }

        return new Match(false);
    }

    private boolean processComponent(DataComponentGetter dataComponentGetter, String id, DataMap map) {
        if (map == null) {
            return false;
        }

        var idRl = ResourceLocation.tryParse(id);
        if (idRl == null) {
            Constants.LOG.warn("Component id '{}' incorrectly formated", id);
            return false;
        }

        var maybeComp = BuiltInRegistries.DATA_COMPONENT_TYPE.get(idRl);
        //noinspection ConstantValue
        if (maybeComp.isEmpty() || maybeComp.get().value() == null) {
            Constants.LOG.warn("Component id '{}' not found", id);
            return false;
        }

        var comp = maybeComp.get().value();

        if (comp == DataComponents.POTION_CONTENTS) {
            var potions = dataComponentGetter.getTyped(DataComponents.POTION_CONTENTS);
            if (potions != null) {
                if (map instanceof DataMap.Value(var pid)) {
                    var prl = ResourceLocation.tryParse(pid);
                    if (prl != null && potions.value().potion().isPresent()) {
                        return potions.value().potion().get().is(prl);
                    }
                } else {
                    return false;
                }
            }

            return false;
        }

        return false;
    }

    private boolean process(DataComponentGetter dataComponentGetter, DataMap dataMap) {
        switch (dataMap) {
            case DataMap.Map map -> {
                return process(dataComponentGetter, map);
            }
            case DataMap.Pair(var key, DataMap.Value mv) -> {
                return processComponent(dataComponentGetter, key, mv);
            }
            case DataMap.Value value -> {
            }
            case DataMap.Pair pair -> {
            }
        }

        return false;
    }
}
