package dex.autoswitch.engine.types.data;

import dex.autoswitch.Constants;
import dex.autoswitch.config.data.tree.SingleValuedDataMap;
import dex.autoswitch.engine.data.Match;
import dex.autoswitch.engine.data.SelectionContext;
import dex.autoswitch.engine.data.extensible.DataType;
import io.leangen.geantyref.TypeToken;

import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class ComponentData extends DataType<SingleValuedDataMap<String, String>> {
    public static final ComponentData INSTANCE = new ComponentData();

    private ComponentData() {
        //noinspection Convert2Diamond
        super("components", new TypeToken<SingleValuedDataMap<String, String>>() {});
    }

    @Override
    public Match matches(int baseLevel, SelectionContext context, Object selectable, SingleValuedDataMap<String, String> data) {
        if (selectable instanceof ItemStack stack) {
            return new Match(process(stack, data));
        }

        return new Match(false);
    }

    private boolean process(DataComponentGetter dataComponentGetter, SingleValuedDataMap<String, String> map) {
        if (map == null) {
            return false;
        }

        for (var entry : map.map().entrySet()) {
            var idRl = Identifier.tryParse(entry.getKey());
            if (idRl == null) {
                Constants.LOG.warn("Component id '{}' incorrectly formated", entry.getKey());
                return false;
            }

            var maybeComp = BuiltInRegistries.DATA_COMPONENT_TYPE.get(idRl);
            //noinspection ConstantValue
            if (maybeComp.isEmpty() || maybeComp.get().value() == null) {
                Constants.LOG.warn("Component id '{}' not found", idRl);
                return false;
            }

            var comp = maybeComp.get().value();

            if (comp == DataComponents.POTION_CONTENTS) {
                var potions = dataComponentGetter.getTyped(DataComponents.POTION_CONTENTS);
                if (potions != null) {
                    var prl = Identifier.tryParse(entry.getValue());
                    if (prl != null && potions.value().potion().isPresent()) {
                        return potions.value().potion().get().is(prl);
                    }
                }

                return false;
            }
        }

        return false;
    }
}
