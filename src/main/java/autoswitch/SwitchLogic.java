package autoswitch;

import com.mojang.datafixers.types.templates.Tag;
import net.fabricmc.fabric.api.tools.FabricToolTags;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.data.server.ItemTagsProvider;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.packet.PlayerInputC2SPacket;
import net.minecraft.tag.ItemTags;
import net.minecraft.util.TagHelper;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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

    //helper function
    private static boolean stringContainsItemFromList(String inputStr, ArrayList<String> items) {
        return items.parallelStream().anyMatch(inputStr::contains);
    }

    //list of potential slots
    private ArrayList<Integer> potSlots = new ArrayList<Integer>();

    public ArrayList<Integer> toolBlockList(PlayerEntity player, BlockState block) {


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
            //ineffective on logs for some reason if (item.isEffectiveOn(block)) {
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
                    //System.out.println(hotbar.get(i).getEnchantments().asString().contains("silk"));
                } else if (FabricToolTags.SWORDS.contains(item) || vanillaSwords.contains(item.asItem())) {
                    swords.add(i);
                } else if (hotbar.get(i).getItem().equals(Items.SHEARS)) {
                    shears.add(i);
                }
            //}
        }


        //Check what materials the block needs and add those to the list of potential slots
        if (shear.contains(mat)) {
            if (shears.isEmpty()) {

            } else {
                potSlots.add(shears.get(0));
            }
        } else if (fortPick.contains(mat)) {
            if (fortPicks.isEmpty()) {
                if (silkPick.contains(mat) && !silkPicks.isEmpty()) {
                    potSlots.add(silkPicks.get(0));
                } else if (picks.isEmpty()) {

                } else potSlots.add(picks.get(0));
            } else {
                potSlots.add(fortPicks.get(0));
            }
        } else if (silkPick.contains(mat)) {
            if (silkPicks.isEmpty()) {
                if (picks.isEmpty()){

                } else {
                    potSlots.add(picks.get(0));
                }
            } else {
                potSlots.add(silkPicks.get(0));
            }
        } else if (pick.contains(mat)) {
            if (picks.isEmpty()) {

            } else {
                potSlots.add(picks.get(0));
            }
        } else if (silkAxe.contains(mat)) {
            if (silkAxes.isEmpty()) {
                if (axes.isEmpty()){

                } else {
                    potSlots.add(axes.get(0));
                }
            } else {
                potSlots.add(silkAxes.get(0));
            }
        } else if (fortAxe.contains(mat)) {
            if (fortAxes.isEmpty()) {
                if (axes.isEmpty()){

                } else {
                    potSlots.add(axes.get(0));
                }
            } else {
                potSlots.add(fortAxes.get(0));
            }
        } else if (axe.contains(mat)) {
            if (axes.isEmpty()) {

            } else {
                potSlots.add(axes.get(0));
            }
        } else if (sword.contains(mat)) {
            if (swords.isEmpty()) {

            } else {
                potSlots.add(swords.get(0));
            }
        } else if (silkShovel.contains(mat)) {
            if (silkShovels.isEmpty()) {
                if (shovels.isEmpty()){

                } else {
                    potSlots.add(shovels.get(0));
                }
            } else {
                potSlots.add(silkShovels.get(0));
            }
        } else if (shovel.contains(mat)) {
            if (shovels.isEmpty()) {

            } else {
                potSlots.add(shovels.get(0));
            }
        } else {
            System.out.println("Bare Hand Fine");
        }

        return potSlots;

    }

    public ArrayList<Integer> toolEntityList(PlayerEntity player, Entity entity) {
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
            //ItemStack item = hotbar.get(i);
            //System.out.println(item.getEnchantments().contains(Enchantments.BANE_OF_ARTHROPODS));

            if (hotbar.get(i).getEnchantments().asString().contains("bane")){
                banes.add(i);
            } else if (hotbar.get(i).getEnchantments().asString().contains("smite")){
                smites.add(i);
            } else if (hotbar.get(i).getEnchantments().asString().contains("sharp")){
                sharps.add(i);
            }
            if (FabricToolTags.AXES.contains(item)) {
                axes.add(i);
            }
            if (hotbar.get(i).getItem().equals(Items.TRIDENT)) {
                tridents.add(i);
            }
            if (FabricToolTags.SWORDS.contains(item)) {
                swords.add(i);
            }
        }

        //types of mobs to prioritize
        ArrayList<String> baneMob = new ArrayList<String>(Arrays.asList("Spider", "Bee", "fish", "mite"));
        ArrayList<String> smiteMob = new ArrayList<String>(Arrays.asList("Skeleton", "Zombie", "Wither", "Phantom",
                "Husk", "Stray", "Drowned"));
        //ArrayList<String> sharpMob = new ArrayList<String>(Arrays.asList("spider", "bee", "fish", "mite"));
        ArrayList<String> boatMob = new ArrayList<String>(Arrays.asList("Boat"));

        if (stringContainsItemFromList(entity.toString(), baneMob)) {
            if (banes.isEmpty()) {

            } else {
                potSlots.add(banes.get(0));
                return potSlots;
            }
        }

        if ((stringContainsItemFromList(entity.toString(), smiteMob))) {
            if (smites.isEmpty()){

            } else {
                potSlots.add(smites.get(0));
                return potSlots;
            }
        }

        if (stringContainsItemFromList(entity.toString(), boatMob)) {
            if (axes.isEmpty()) {

            } else {
                potSlots.add(axes.get(0));
                return potSlots;
            }
        }

        //generic mobs
        if (sharps.isEmpty()) {
            if (swords.isEmpty()) {
                if (axes.isEmpty()) {
                    if (tridents.isEmpty()) {

                    } else {
                        potSlots.add(tridents.get(0));
                    }

                } else {
                    potSlots.add(axes.get(0));
                }

            } else {
                potSlots.add(swords.get(0));
            }
        } else {
            potSlots.add(sharps.get(0));
        }


        return potSlots;
    }

    public int changeTool(ArrayList<Integer> slots, int currentSlot, PlayerEntity player) {
        if (slots.isEmpty()) {
            return -1;
        } else {
            if (slots.get(0) == currentSlot) {
                System.out.println("No need to change slot");
                return 0;
            }

            //Simulate player pressing the hotbar button, potential fix for working on vanilla servers
            //try {
                //Robot robot = new Robot();
                // Simulate a key press
                //robot.keyPress(slots.get(0));
                //robot.keyRelease(slots.get(0));

                //Loop over it since scrollinhotbar only moves one pos
                for (int i = Math.abs(currentSlot - slots.get(0)); i > 0; i--){
                    player.inventory.scrollInHotbar(currentSlot - slots.get(0));
                }

                //player.inventory.selectedSlot = potSlots.get(0);
                return 1;

            //} catch (AWTException e) {
                /*e.printStackTrace();
                return -2;
            } */
        }
    }

}
