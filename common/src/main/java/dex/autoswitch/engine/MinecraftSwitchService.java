package dex.autoswitch.engine;


import com.google.auto.service.AutoService;
import dex.autoswitch.engine.data.Match;
import dex.autoswitch.engine.data.extensible.DataType;
import dex.autoswitch.engine.data.extensible.SelectableType;
import dex.autoswitch.engine.data.extensible.SwitchRegistryService;
import dex.autoswitch.engine.types.data.BlockStateData;
import dex.autoswitch.engine.types.data.ComponentData;
import dex.autoswitch.engine.types.data.EnchantmentData;
import dex.autoswitch.engine.types.data.EntityEquipmentData;
import dex.autoswitch.engine.types.selectable.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

@AutoService(SwitchRegistryService.class)
public class MinecraftSwitchService implements SwitchRegistryService {
    private static final Matcher NON_TOOL_SELECTOR = (baseLevel, context, selectable) -> {
        if (selectable instanceof ItemStack stack) {
            var tool = stack.getComponents().get(DataComponents.TOOL);
            return new Match(tool == null);
        }

        if (selectable instanceof Item item) {
            var tool = item.components().get(DataComponents.TOOL);
            return new Match(tool == null);
        }

        return new Match(false);
    };

    @Override
    public Set<SelectableType<?, ?, ?>> selectableTypes() {
        return Set.of(
                BlockSelectableType.INSTANCE,
                EnchantmentSelectableType.INSTANCE,
                EntitySelectableType.INSTANCE,
                ItemSelectableType.INSTANCE,
                StatSelectableType.INSTANCE
        );
    }

    @Override
    public Set<DataType<?>> dataTypes() {
        return Set.of(
                EnchantmentData.INSTANCE,
                BlockStateData.INSTANCE,
                EntityEquipmentData.INSTANCE,
                ComponentData.INSTANCE
                //EnchantmentLevelData.INSTANCE
        );
    }

    @Override
    public Matcher nonToolMatcher() {
        return NON_TOOL_SELECTOR;
    }
}
