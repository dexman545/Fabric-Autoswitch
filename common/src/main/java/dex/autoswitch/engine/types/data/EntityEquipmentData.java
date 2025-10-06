package dex.autoswitch.engine.types.data;

import java.util.HashSet;
import java.util.Set;

import dex.autoswitch.Constants;
import dex.autoswitch.config.data.tree.DataMap;
import dex.autoswitch.engine.data.Match;
import dex.autoswitch.engine.data.SelectionContext;
import dex.autoswitch.engine.data.extensible.DataType;
import dex.autoswitch.engine.types.selectable.ItemSelectableType;
import dex.autoswitch.futures.FutureSelectable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

public class EntityEquipmentData extends DataType<DataMap> {
    public static final EntityEquipmentData INSTANCE = new EntityEquipmentData();

    protected EntityEquipmentData() {
        super("equipment", DataMap.class);
    }

    @Override
    public Match matches(int baseLevel, SelectionContext context, Object selectable, DataMap data) {
        if (selectable instanceof LivingEntity entity) {
            var requiredState = process(data);

            for (Equipment equipment : requiredState) {
                var slot = getSlot(equipment);

                if (slot == null) {
                    return new Match(false);
                }

                var stack = entity.getItemBySlot(slot);
                var desired = FutureSelectable.getOrCreate(ResourceLocation.tryParse(equipment.item()),
                        ItemSelectableType.INSTANCE, false);

                if (!desired.matches(context, stack)) {
                    return new Match(false);
                }
            }

            return new Match(true);
        }

        return new Match(false);
    }

    private EquipmentSlot getSlot(Equipment equipment) {
        try {
            return EquipmentSlot.byName(equipment.slot());
        } catch (Exception e) {
            Constants.LOG.warn("Unknown equipment slot: {}", equipment.slot());
        }

        return null;
    }

    private Set<Equipment> process(DataMap dataMap) {
        var properties = new HashSet<Equipment>();
        switch (dataMap) {
            case DataMap.Map map -> {
                for (DataMap entry : map.entries()) {
                    properties.addAll(process(entry));
                }
            }
            case DataMap.Pair(var key, DataMap.Value mv) -> {
                properties.add(new Equipment(key, mv.value()));
            }
            case DataMap.Value value -> {
            }
            case DataMap.Pair pair -> {
            }
        }

        return properties;
    }

    private record Equipment(String slot, String item) {}
}
