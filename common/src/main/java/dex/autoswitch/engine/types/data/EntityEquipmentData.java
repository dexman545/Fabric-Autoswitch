package dex.autoswitch.engine.types.data;

import dex.autoswitch.Constants;
import dex.autoswitch.config.data.tree.SingleValuedDataMap;
import dex.autoswitch.engine.data.Match;
import dex.autoswitch.engine.data.SelectionContext;
import dex.autoswitch.engine.data.extensible.DataType;
import dex.autoswitch.engine.types.selectable.ItemSelectableType;
import dex.autoswitch.futures.FutureSelectable;
import io.leangen.geantyref.TypeToken;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

public class EntityEquipmentData extends DataType<SingleValuedDataMap<String, String>> {
    public static final EntityEquipmentData INSTANCE = new EntityEquipmentData();

    protected EntityEquipmentData() {
        //noinspection Convert2Diamond
        super("equipment", new TypeToken<SingleValuedDataMap<String, String>>() {});
    }

    @Override
    public Match matches(int baseLevel, SelectionContext context, Object selectable, SingleValuedDataMap<String, String> inventory) {
        if (selectable instanceof LivingEntity entity) {
            for (var equipment : inventory.map().entrySet()) {
                var slot = getSlot(equipment.getKey());

                if (slot == null) {
                    return new Match(false);
                }

                var stack = entity.getItemBySlot(slot);
                var desired = FutureSelectable.getOrCreate(Identifier.tryParse(equipment.getValue()),
                        ItemSelectableType.INSTANCE, false);

                if (!desired.matches(context, stack)) {
                    return new Match(false);
                }
            }

            return new Match(true);
        }

        return new Match(false);
    }

    private EquipmentSlot getSlot(String slot) {
        try {
            return EquipmentSlot.byName(slot);
        } catch (Exception e) {
            Constants.LOG.warn("Unknown equipment slot: {}", slot);
        }

        return null;
    }
}
