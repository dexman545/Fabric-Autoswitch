package autoswitch;

import net.fabricmc.fabric.api.tools.FabricToolTags;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.*;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

abstract class Targetable {
    HashMap<String, ArrayList<Object>> toolTargetLists = new AutoSwitchLists().getToolTargetLists();
    LinkedHashMap<String, ArrayList<Integer>> toolLists = new AutoSwitchLists().getToolLists();
    PlayerEntity player;

    static Targetable of(Entity target, PlayerEntity player) {
        return new TargetableEntity(target, player);
    }

    static Targetable of(BlockState target, PlayerEntity player) {
        return new TargetableMaterial(target, player);
    }

    //populate all of the tool lists
    public void populateToolLists(PlayerEntity player) {
        List<ItemStack> hotbar = player.inventory.main.subList(0, 9);
        for (int i=0; i<9; i++) {
            populateCommonToolList(hotbar.get(i), i);
            populateTargetTools(hotbar.get(i), i);

        }

    }

    //Populate Common Tool Lists
    protected void populateCommonToolList(ItemStack stack, int i) {
        Item item = stack.getItem();
        if (FabricToolTags.AXES.contains(item) || item instanceof AxeItem) {
            this.toolLists.get("axes").add(i);
            //Genning these in common as there's no good way to do it separately without repeating the axe check
            if (EnchantmentHelper.getLevel(Enchantments.FORTUNE, stack) > 0){
                this.toolLists.get("fortAxes").add(i);
            }
            if (EnchantmentHelper.getLevel(Enchantments.SILK_TOUCH, stack) > 0){
                this.toolLists.get("silkAxes").add(i);
            }
        } else if (FabricToolTags.SWORDS.contains(item) || item instanceof SwordItem) {
            this.toolLists.get("swords").add(i);
        }

    }


    //Overrides
    abstract void populateTargetTools(ItemStack stack, int i);

    abstract int findSlot();

}

class TargetableEntity extends Targetable {
    private final Entity entity;

    public TargetableEntity(Entity target, PlayerEntity player) {
        populateToolLists(player);
        this.entity = target;
        this.player = player;
    }


    @Override
    void populateTargetTools(ItemStack stack, int i) {
        Item item = stack.getItem();
        if (EnchantmentHelper.getLevel(Enchantments.BANE_OF_ARTHROPODS, stack) > 0){
            this.toolLists.get("banes").add(i);
        } else if (EnchantmentHelper.getLevel(Enchantments.SMITE, stack) > 0){
            this.toolLists.get("smites").add(i);
        } else if (EnchantmentHelper.getLevel(Enchantments.SHARPNESS, stack) > 0){
            this.toolLists.get("sharps").add(i);
        } else if (EnchantmentHelper.getLevel(Enchantments.IMPALING, stack) > 0){
            this.toolLists.get("impalingTridents").add(i);
        }
        if (item instanceof TridentItem) {
            this.toolLists.get("tridents").add(i);
        }

    }

    @Override
    int findSlot() {
        if (entity instanceof LivingEntity) {
            if (((LivingEntity) entity).getGroup() == EntityGroup.ARTHROPOD) {
                if (!toolLists.get("banes").isEmpty()) {
                    return toolLists.get("banes").get(0);
                }
            }

            if (((LivingEntity) entity).getGroup() == EntityGroup.UNDEAD) {
                if (!toolLists.get("smites").isEmpty()){
                    return toolLists.get("smites").get(0);
                }
            }

            if (((LivingEntity) entity).getGroup() == EntityGroup.AQUATIC) {
                if (!toolLists.get("impalingTridents").isEmpty()){
                    return toolLists.get("impalingTridents").get(0);
                }
            }

        }

        for (Map.Entry<String, ArrayList<Integer>> toolList : toolLists.entrySet()){
            if (!toolList.getValue().isEmpty()) {
                return toolList.getValue().get(0);
            }
        }

        if (entity instanceof BoatEntity) {
            if (!toolLists.get("axes").isEmpty()) {
                return toolLists.get("axes").get(0);
            }
        }


        return -1;
    }
}

class TargetableMaterial extends Targetable {
    private final Material target;

    public TargetableMaterial(BlockState target, PlayerEntity player) {
        populateToolLists(player);
        this.player = player;
        this.target = target.getMaterial();
    }


    @Override
    void populateTargetTools(ItemStack stack, int i) {
        Item item = stack.getItem();
        if (FabricToolTags.PICKAXES.contains(item) || item instanceof PickaxeItem) {
            this.toolLists.get("picks").add(i);
            if (EnchantmentHelper.getLevel(Enchantments.FORTUNE, stack) > 0){
                this.toolLists.get("fortPicks").add(i);
            }
            if (EnchantmentHelper.getLevel(Enchantments.SILK_TOUCH, stack) > 0){
                this.toolLists.get("silkPicks").add(i);
            }
        } else if (FabricToolTags.SHOVELS.contains(item) || item instanceof ShovelItem) {
            this.toolLists.get("shovels").add(i);
            if (EnchantmentHelper.getLevel(Enchantments.SILK_TOUCH, stack) > 0){
                this.toolLists.get("silkShovels").add(i);
            }
        } else if (item instanceof ShearsItem) {
            this.toolLists.get("shears").add(i);
        }

    }

    @Override
    int findSlot() {
        for (Map.Entry<String, ArrayList<Integer>> toolList : toolLists.entrySet()){
            if (!toolList.getValue().isEmpty()) {
                if (toolTargetLists.get(StringUtils.chop(toolList.getKey())).contains(target)) {
                    return toolList.getValue().get(0);
                }

            }
        }

        return -1;
    }
}
