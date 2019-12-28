package autoswitch;

import net.fabricmc.fabric.api.tools.FabricToolTags;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SwitchLogic {
    private ArrayList<Item> vanillaSwords = new ArrayList<Item>(Arrays.asList(Items.STONE_SWORD, Items.DIAMOND_SWORD,
            Items.GOLDEN_SWORD, Items.IRON_SWORD, Items.WOODEN_SWORD));
    private ArrayList<Item> vanillaAxes = new ArrayList<Item>(Arrays.asList(Items.STONE_AXE, Items.DIAMOND_AXE,
            Items.GOLDEN_AXE, Items.IRON_AXE, Items.WOODEN_AXE));
    private ArrayList<Item> vanillaPicks = new ArrayList<Item>(Arrays.asList(Items.STONE_PICKAXE, Items.DIAMOND_PICKAXE,
            Items.GOLDEN_PICKAXE, Items.IRON_PICKAXE, Items.WOODEN_PICKAXE));
    private ArrayList<Item> vanillaShovels = new ArrayList<Item>(Arrays.asList(Items.STONE_SHOVEL, Items.DIAMOND_SHOVEL,
            Items.GOLDEN_SHOVEL, Items.IRON_SHOVEL, Items.WOODEN_SHOVEL));
    

    public int toolBlockSlot(PlayerEntity player, BlockState block) {


        Material mat = block.getMaterial();

        //Lists for each tool/type of tool based on block Material
        //Order: shears, fortPick, silkPick, pick, sword, silkAxe, fortAxe, axe, silkShovel, shovel
        ArrayList<Material> shear = new ArrayList<Material>(Arrays.asList(
                Material.PLANT, Material.UNUSED_PLANT, Material.UNDERWATER_PLANT, Material.REPLACEABLE_PLANT,
                Material.LEAVES, Material.COBWEB, Material.WOOL
        ));
        ArrayList<Material> sword = new ArrayList<Material>(Arrays.asList(
                Material.BAMBOO, Material.BAMBOO_SAPLING, Material.PUMPKIN, Material.COBWEB, Material.LEAVES
        ));
        ArrayList<Material> axe = new ArrayList<Material>(Arrays.asList(
                Material.WOOD, Material.PLANT, Material.REPLACEABLE_PLANT, Material.PUMPKIN
        ));
        ArrayList<Material> pick = new ArrayList<Material>(Arrays.asList(
                Material.ICE, Material.PACKED_ICE, Material.METAL, Material.ANVIL, Material.SHULKER_BOX,
                Material.STONE, Material.REDSTONE_LAMP
        ));
        ArrayList<Material> shovel = new ArrayList<Material>(Arrays.asList(
                Material.EARTH, Material.ORGANIC, Material.SNOW, Material.SNOW_BLOCK, Material.CLAY,
                Material.SAND
        ));
        ArrayList<Material> silkAxe = new ArrayList<Material>(Arrays.asList(
                Material.EGG, Material.GLASS, Material.ICE, Material.PACKED_ICE, Material.GLASS, Material.WOOD, Material.PUMPKIN
        ));
        ArrayList<Material> silkPick = new ArrayList<Material>(Arrays.asList(
                Material.EGG, Material.GLASS, Material.ICE, Material.PACKED_ICE, Material.STONE
        ));
        ArrayList<Material> silkShovel = new ArrayList<Material>(Arrays.asList(
                Material.EGG, Material.GLASS, Material.ICE, Material.PACKED_ICE, Material.GLASS, Material.EARTH,
                Material.ORGANIC, Material.SAND, Material.SNOW_BLOCK, Material.SNOW
        ));
        ArrayList<Material> fortPick = new ArrayList<Material>(Arrays.asList(
                Material.STONE, Material.GLASS, Material.METAL
        ));
        ArrayList<Material> fortAxe = new ArrayList<Material>(Arrays.asList(
                Material.WOOD, Material.PLANT, Material.REPLACEABLE_PLANT, Material.PUMPKIN
        ));

        //Lists of tool slots
        ArrayList<Integer> axes = new ArrayList<Integer>();
        ArrayList<Integer> swords = new ArrayList<Integer>();
        ArrayList<Integer> shears = new ArrayList<Integer>();
        ArrayList<Integer> picks = new ArrayList<Integer>();
        ArrayList<Integer> shovels = new ArrayList<Integer>();
        ArrayList<Integer> silkShovels = new ArrayList<Integer>();
        ArrayList<Integer> silkAxes = new ArrayList<Integer>();
        ArrayList<Integer> silkPicks = new ArrayList<Integer>();
        ArrayList<Integer> fortPicks = new ArrayList<Integer>();
        ArrayList<Integer> fortAxes = new ArrayList<Integer>();

        //Get HotBar Slots of effective items
        List<ItemStack> hotbar = player.inventory.main.subList(0, 9);
        for (int i=0; i<9; i++) {
            Item item = hotbar.get(i).getItem();

            //Check if item will work on the block, if it can check what tool it is and add it to it's list
            //ineffective on logs for some reason
            //if (item.isEffectiveOn(block)) { //1.15 broke this further
                //System.out.println("meh");
                if (FabricToolTags.AXES.contains(item) || vanillaAxes.contains(item.asItem())) {
                    axes.add(i);
                    if (hotbar.get(i).getEnchantments().asString().contains("fortune")){
                        fortAxes.add(i);
                    }
                    if (hotbar.get(i).getEnchantments().asString().contains("silk")){
                        silkAxes.add(i);
                    }
                } else if (FabricToolTags.PICKAXES.contains(item) || vanillaPicks.contains(item.asItem())) {
                    picks.add(i);
                    if (hotbar.get(i).getEnchantments().asString().contains("fortune")){
                        fortPicks.add(i);
                    }
                    if (hotbar.get(i).getEnchantments().asString().contains("silk")){
                        silkPicks.add(i);

                    }
                } else if (FabricToolTags.SHOVELS.contains(item) || vanillaShovels.contains(item.asItem())) {
                    shovels.add(i);
                    if (hotbar.get(i).getEnchantments().asString().contains("silk")){
                        silkShovels.add(i);
                    }
                } else if (FabricToolTags.SWORDS.contains(item) || vanillaSwords.contains(item.asItem())) {
                    swords.add(i);
                } else if (hotbar.get(i).getItem().equals(Items.SHEARS)) {
                    shears.add(i);
                }
            //}
        }


        //Check what materials the block needs and add those to the list of potential slots
        if (shear.contains(mat)) {
            if (!shears.isEmpty()) {
                return shears.get(0);
            }
        } else if (fortPick.contains(mat)) {
            if (fortPicks.isEmpty()) {
                if (silkPick.contains(mat) && !silkPicks.isEmpty()) {
                    return silkPicks.get(0);
                } else if (!picks.isEmpty()) {
                    return picks.get(0);
                }
            } else {
                return fortPicks.get(0);
            }
        } else if (silkPick.contains(mat)) {
            if (silkPicks.isEmpty()) {
                if (!picks.isEmpty()){
                    return picks.get(0);
                }
            } else {
                return silkPicks.get(0);
            }
        } else if (pick.contains(mat)) {
            if (!picks.isEmpty()) {
                return picks.get(0);
            }
        } else if (silkAxe.contains(mat)) {
            if (silkAxes.isEmpty()) {
                if (!axes.isEmpty()){
                    return axes.get(0);
                }
            } else {
                return silkAxes.get(0);
            }
        } else if (fortAxe.contains(mat)) {
            if (fortAxes.isEmpty()) {
                if (!axes.isEmpty()){
                    return axes.get(0);
                }
            } else {
                return fortAxes.get(0);
            }
        } else if (axe.contains(mat)) {
            if (!axes.isEmpty()) {
                return axes.get(0);
            }
        } else if (sword.contains(mat)) {
            if (!swords.isEmpty()) {
                return swords.get(0);
            }
        } else if (silkShovel.contains(mat)) {
            if (silkShovels.isEmpty()) {
                if (!shovels.isEmpty()){
                    return shovels.get(0);
                }
            } else {
                return silkShovels.get(0);
            }
        } else if (shovel.contains(mat)) {
            if (!shovels.isEmpty()) {
                return shovels.get(0);
            }
        } /*else {
            System.out.println("Bare Hand Fine");
        }*/

        return -1;

    }

    public int toolEntitySlot(PlayerEntity player, Entity entity) {
        ArrayList<Integer> axes = new ArrayList<Integer>();
        ArrayList<Integer> swords = new ArrayList<Integer>();
        ArrayList<Integer> banes = new ArrayList<Integer>();
        ArrayList<Integer> smites = new ArrayList<Integer>();
        ArrayList<Integer> sharps = new ArrayList<Integer>();
        ArrayList<Integer> tridents = new ArrayList<Integer>();

        //Get HotBar Slots
        List<ItemStack> hotbar = player.inventory.main.subList(0, 9);
        for (int i=0; i<9; i++) {
            Item item = hotbar.get(i).getItem();

            if (hotbar.get(i).getEnchantments().asString().contains("bane")){
                banes.add(i);
            } else if (hotbar.get(i).getEnchantments().asString().contains("smite")){
                smites.add(i);
            } else if (hotbar.get(i).getEnchantments().asString().contains("sharp")){
                sharps.add(i);
            }
            if (FabricToolTags.AXES.contains(item) || vanillaAxes.contains(item.asItem())) {
                axes.add(i);
            }
            if (hotbar.get(i).getItem().equals(Items.TRIDENT)) {
                tridents.add(i);
            }
            if (FabricToolTags.SWORDS.contains(item) || vanillaSwords.contains(item.asItem())) {
                swords.add(i);
            }
        }

        //types of mobs to prioritize
        ArrayList<String> baneMob = new ArrayList<String>(Arrays.asList("Spider", "Bee", "fish", "mite"));
        ArrayList<String> smiteMob = new ArrayList<String>(Arrays.asList("Skeleton", "Zombie", "Wither", "Phantom",
                "Husk", "Stray", "Drowned"));
        //ArrayList<String> sharpMob = new ArrayList<String>(Arrays.asList("spider", "bee", "fish", "mite")); Disabled as sharpness is useful on all mobs
        ArrayList<String> boatMob = new ArrayList<String>(Collections.singletonList("Boat"));

        if (entity instanceof LivingEntity) {
            if (((LivingEntity) entity).getGroup() == EntityGroup.ARTHROPOD) {
                if (!banes.isEmpty()) {
                    return banes.get(0);
                }
            }

            if (((LivingEntity) entity).getGroup() == EntityGroup.UNDEAD) {
                if (!smites.isEmpty()){
                    return smites.get(0);
                }
            }
        }

        if (entity instanceof BoatEntity) {
            if (!axes.isEmpty()) {
                return axes.get(0);
            }
        }

        //generic mobs
        if (sharps.isEmpty()) {
            if (swords.isEmpty()) {
                if (axes.isEmpty()) {
                    if (!tridents.isEmpty()) {
                        return tridents.get(0);
                    }

                } else {
                    return axes.get(0);
                }

            } else {
                return swords.get(0);
            }
        } else {
            return sharps.get(0);
        }


        return -1;
    }

    public int changeTool(int slot, PlayerEntity player) {
        int currentSlot = player.inventory.selectedSlot;
        if (slot == -1) {
            //nothing to change to!
            return -1;
        }

        if (slot == currentSlot) {
            //System.out.println("No need to change slot");
            return 0;
        }

        //Simulate player pressing the hotbar button, potential fix for working on vanilla servers
        //Loop over it since scrollinhotbar only moves one pos
        for (int i = Math.abs(currentSlot - slot); i > 0; i--){
            player.inventory.scrollInHotbar(currentSlot - slot);
        }

        //player.inventory.selectedSlot = slot;
        return 1;

    }

}
