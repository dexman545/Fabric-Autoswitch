package dex.autoswitch.gametest.util;

import dex.autoswitch.gametest.util.RegistryObject.Enchant;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;

public class Hotbars {
    public static Player pickaxePlayer(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var inv = player.getInventory();

        inv.add(0, RegistryObject.stack(
                helper, Items.DIAMOND_PICKAXE
        ));
        inv.add(1, RegistryObject.stack(
                helper, Items.NETHERITE_PICKAXE
        ));
        inv.add(2, RegistryObject.stack(
                helper, Items.IRON_PICKAXE
        ));
        inv.add(3, RegistryObject.stack(
                helper, Items.STONE_PICKAXE
        ));
        inv.add(4, RegistryObject.stack(
                helper, Items.DIAMOND_PICKAXE,
                Enchant.of(Enchantments.FORTUNE, 3)
        ));
        inv.add(5, RegistryObject.stack(
                helper, Items.DIAMOND_PICKAXE,
                Enchant.of(Enchantments.SILK_TOUCH, 1)
        ));
        inv.add(6, RegistryObject.stack(
                helper, Items.DIAMOND_PICKAXE,
                Enchant.of(Enchantments.SILK_TOUCH, 1),
                Enchant.of(Enchantments.EFFICIENCY, 5)
        ));
        inv.add(7, RegistryObject.stack(
                helper, Items.DIAMOND_PICKAXE
        ));
        inv.add(8, RegistryObject.stack(
                helper, Items.DIAMOND_PICKAXE
        ));

        return player;
    }

    public static Player fightingPlayer(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var inv = player.getInventory();

        inv.add(0, RegistryObject.stack(
                helper, Items.WARPED_FUNGUS_ON_A_STICK
        ));
        inv.add(1, RegistryObject.stack(
                helper, Items.IRON_SWORD
        ));
        inv.add(2, RegistryObject.stack(
                helper, Items.IRON_INGOT
        ));
        inv.add(3, RegistryObject.stack(
                helper, Items.DIAMOND_AXE
        ));
        inv.add(4, RegistryObject.stack(
                helper, Items.DIAMOND_SWORD
        ));
        inv.add(5, RegistryObject.stack(
                helper, Items.DIAMOND_SWORD,
                Enchant.of(Enchantments.BANE_OF_ARTHROPODS, 1)
        ));
        inv.add(6, RegistryObject.stack(
                helper, Items.DIAMOND_SWORD,
                Enchant.of(Enchantments.SMITE, 1)
        ));
        inv.add(7, RegistryObject.stack(
                helper, Items.DIAMOND_SWORD,
                Enchant.of(Enchantments.SHARPNESS, 5)
        ));
        inv.add(8, RegistryObject.stack(
                helper, Items.FLINT_AND_STEEL
        ));

        return player;
    }

    public static Player bambooPlayer(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var inv = player.getInventory();

        inv.add(3, RegistryObject.stack(
                helper, Items.DIAMOND_SWORD
        ));
        inv.add(7, RegistryObject.stack(
                helper, Items.DIAMOND_AXE
        ));

        return player;
    }

    public static Player potionPlayer(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var inv = player.getInventory();

        inv.add(2, PotionContents.createItemStack(Items.SPLASH_POTION, Potions.WATER_BREATHING));
        inv.add(3, PotionContents.createItemStack(Items.SPLASH_POTION, Potions.WATER));

        return player;
    }

    public static Player shearingPlayer(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var inv = player.getInventory();

        inv.add(3, RegistryObject.stack(
                helper, Items.SHEARS
        ));

        return player;
    }

    public static Player milkPlayer(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var inv = player.getInventory();

        inv.add(3, RegistryObject.stack(
                helper, Items.MILK_BUCKET
        ));

        return player;
    }

    public static Player wornFighter(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var inv = player.getInventory();

        inv.add(1, RegistryObject.stack(
                helper, Items.IRON_SWORD
        ));
        inv.add(2, RegistryObject.stack(
                helper, Items.IRON_SWORD
        ));

        return player;
    }

    public static Player createLevelSensitive(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var inv = player.getInventory();

        inv.add(0, RegistryObject.stack(
                helper, Items.DIAMOND_PICKAXE
        ));
        inv.add(1, RegistryObject.stack(
                helper, Items.DIAMOND_PICKAXE,
                Enchant.of(Enchantments.EFFICIENCY, 5)
        ));
        inv.add(2, RegistryObject.stack(
                helper, Items.DIAMOND_PICKAXE,
                Enchant.of(Enchantments.EFFICIENCY, 4)
        ));
        inv.add(3, RegistryObject.stack(
                helper, Items.DIAMOND_PICKAXE,
                Enchant.of(Enchantments.EFFICIENCY, 3)
        ));
        inv.add(4, RegistryObject.stack(
                helper, Items.DIAMOND_PICKAXE,
                Enchant.of(Enchantments.EFFICIENCY, 2)
        ));
        inv.add(5, RegistryObject.stack(
                helper, Items.DIAMOND_PICKAXE,
                Enchant.of(Enchantments.EFFICIENCY, 1)
        ));

        return player;
    }

    /*public static Player createStandardPlayer(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var inv = player.getInventory();

        inv.add(0, );
        inv.add(1, );
        inv.add(2, );
        inv.add(3, );
        inv.add(4, );
        inv.add(5, );
        inv.add(6, );
        inv.add(7, );
        inv.add(8, );

        return player;
    }*/
}
