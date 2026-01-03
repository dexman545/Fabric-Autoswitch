package dex.autoswitch.gametest.unit;

import java.util.Objects;

import dex.autoswitch.Constants;
import dex.autoswitch.config.AutoSwitchConfig;
import dex.autoswitch.engine.Action;
import dex.autoswitch.engine.types.selectable.StatSelectableType;
import dex.autoswitch.gametest.util.Hotbars;
import dex.autoswitch.gametest.util.RegistryObject;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Blocks;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

public class UnitGameTest extends AbstractTest {
    private final AutoSwitchConfig playerDataTestConfig = loadConfig("playerDataTest");
    private final AutoSwitchConfig enchantmentLevelTestConfig = loadConfig("enchantmentLevelTest");

    @GameTest
    public void pickaxeStone(GameTestHelper helper) {
        setup(helper);
        var player = Hotbars.pickaxePlayer(helper);
        select(Action.ATTACK, RegistryObject.block(Blocks.STONE), player);
        assertSlot(helper, player, 6);
        helper.succeed();
    }

    @GameTest
    public void pickaxeGlass(GameTestHelper helper) {
        setup(helper);
        var player = Hotbars.pickaxePlayer(helper);
        select(Action.ATTACK, RegistryObject.block(Blocks.GLASS), player);
        assertSlot(helper, player, 5);
        helper.succeed();
    }

    @GameTest
    public void pickaxeEnderChest(GameTestHelper helper) {
        setup(helper);
        var player = Hotbars.pickaxePlayer(helper);
        select(Action.ATTACK, RegistryObject.block(Blocks.ENDER_CHEST), player);
        assertSlot(helper, player, 5);
        helper.succeed();
    }

    @GameTest
    public void attackChicken(GameTestHelper helper) {
        setup(helper);
        var player = Hotbars.fightingPlayer(helper);
        select(Action.ATTACK, RegistryObject.entity(helper, EntityType.CHICKEN), player);
        assertSlot(helper, player, 7);
        helper.succeed();
    }

    @GameTest
    public void attackSpider(GameTestHelper helper) {
        setup(helper);
        var player = Hotbars.fightingPlayer(helper);
        select(Action.ATTACK, RegistryObject.entity(helper, EntityType.SPIDER), player);
        assertSlot(helper, player, 5);
        helper.succeed();
    }

    @GameTest
    public void attackZombie(GameTestHelper helper) {
        setup(helper);
        var player = Hotbars.fightingPlayer(helper);
        select(Action.ATTACK, RegistryObject.entity(helper, EntityType.ZOMBIE), player);
        assertSlot(helper, player, 6);
        helper.succeed();
    }

    @GameTest
    public void interactCreeperNoOffhand(GameTestHelper helper) {
        setup(helper);
        var player = Hotbars.fightingPlayer(helper);
        var testPlayer = select(Action.INTERACT, RegistryObject.entity(helper, EntityType.CREEPER), player);
        assertSlot(helper, player, 8);
        assertOffhand(helper, testPlayer, false);
        helper.succeed();
    }

    @GameTest
    public void interactIronGolemNoOffhand(GameTestHelper helper) {
        setup(helper);

        var player = Hotbars.fightingPlayer(helper);
        var testPlayer = select(Action.INTERACT, RegistryObject.entity(helper, EntityType.IRON_GOLEM), player);
        assertSlot(helper, player, 2);
        assertOffhand(helper, testPlayer, false);

        helper.succeed();
    }

    @GameTest
    public void interactStriderOffhand(GameTestHelper helper) {
        setup(helper);
        var player = Hotbars.fightingPlayer(helper);
        var strider = RegistryObject.entity(helper, EntityType.STRIDER);

        // Interaction where the slot is already correct
        player.getInventory().setSelectedSlot(2);
        var testPlayer = select(Action.INTERACT, strider, player);
        assertSlot(helper, player, 2);
        assertOffhand(helper, testPlayer, false);

        // Equip saddle and expect offhand behavior
        strider.equipItemIfPossible(helper.getLevel(), RegistryObject.stack(helper, Items.SADDLE));
        testPlayer = select(Action.INTERACT, strider, player);
        assertSlot(helper, player, 0);
        Constants.SCHEDULER.tick();
        assertOffhand(helper, testPlayer, true);

        helper.succeed();
    }

    @GameTest
    public void fallbackAndMultiTargetMatchTest(GameTestHelper helper) {
        setup(helper);

        var player = Hotbars.bambooPlayer(helper);
        var bamboo = RegistryObject.block(Blocks.BAMBOO);

        select(Action.ATTACK, bamboo, player);
        assertSlot(helper, player, 3);

        // Remove sword
        player.getInventory().removeItem(3, 1);
        select(Action.ATTACK, bamboo, player);
        assertSlot(helper, player, 7);

        helper.succeed();
    }

    @GameTest
    public void potionTest(GameTestHelper helper) {
        setup(helper);

        var player = Hotbars.potionPlayer(helper);
        var fire = RegistryObject.block(Blocks.FIRE);

        select(Action.INTERACT, fire, player);
        assertSlot(helper, player, 3);

        // Remove water potion
        player.getInventory().setSelectedSlot(0);
        player.getInventory().removeItem(3, 1);
        select(Action.INTERACT, fire, player);
        assertSlot(helper, player, 0);

        helper.succeed();
    }

    @GameTest
    public void statChangeTest(GameTestHelper helper) {
        setup(helper);

        var player = Hotbars.milkPlayer(helper);
        var bedStat = StatSelectableType.INSTANCE.lookup(Objects.requireNonNull(ResourceLocation.tryParse("custom:sleep_in_bed")));

        // Ideally, we'd actually trigger the stat change, but that isn't trivial on the server
        select(Action.STAT_CHANGE, bedStat, player);
        assertSlot(helper, player, 3);

        helper.succeed();
    }

    @GameTest
    public void beehiveLowHoney(GameTestHelper helper) {
        setup(helper);
        var player = Hotbars.shearingPlayer(helper);
        var beehive = RegistryObject.block(Blocks.BEEHIVE, RegistryObject.State.of(BeehiveBlock.HONEY_LEVEL, 1));
        select(Action.INTERACT, beehive, player);
        assertNotSlot(helper, player, 3);
        helper.succeed();
    }

    @GameTest
    public void beehiveFullHoney(GameTestHelper helper) {
        setup(helper);
        var player = Hotbars.shearingPlayer(helper);
        var beehive = RegistryObject.block(Blocks.BEEHIVE, RegistryObject.State.of(BeehiveBlock.HONEY_LEVEL, 5));
        select(Action.INTERACT, beehive, player);
        assertSlot(helper, player, 3);
        helper.succeed();
    }

    @GameTest
    public void skipDepletedSword(GameTestHelper helper) {
        setup(helper);
        var player = Hotbars.wornFighter(helper);

        var creeper = RegistryObject.entity(helper, EntityType.CREEPER);

        // Ensure the primary weapon was chosen
        select(Action.ATTACK, creeper, player);
        assertSlot(helper, player, 1);

        // Should now skip the depleted sword and pick the next best tool
        var sword = player.getInventory().getItem(1);
        sword.setDamageValue(sword.getMaxDamage() - 1);

        select(Action.ATTACK, creeper, player);
        assertSlot(helper, player, 2);
        helper.succeed();
    }

    @GameTest
    public void enchantmentLevelClosedRange(GameTestHelper helper) {
        setup(helper);
        var player = Hotbars.createLevelSensitive(helper);

        select(Action.ATTACK, RegistryObject.block(Blocks.DEEPSLATE), player, enchantmentLevelTestConfig);
        assertSlot(helper, player, 4);
        helper.succeed();
    }

    @GameTest
    public void enchantmentLevelUnboundedRange(GameTestHelper helper) {
        setup(helper);
        var player = Hotbars.createLevelSensitive(helper);

        select(Action.ATTACK, RegistryObject.block(Blocks.OBSIDIAN), player, enchantmentLevelTestConfig);
        assertSlot(helper, player, 2);
        helper.succeed();
    }

    @GameTest
    public void enchantmentLevelSpecificLevel(GameTestHelper helper) {
        setup(helper);
        var player = Hotbars.createLevelSensitive(helper);

        select(Action.ATTACK, RegistryObject.block(Blocks.STONE), player, enchantmentLevelTestConfig);
        assertSlot(helper, player, 1);

        select(Action.ATTACK, RegistryObject.block(Blocks.COBBLESTONE), player, enchantmentLevelTestConfig);
        assertSlot(helper, player, 5);

        helper.succeed();
    }

    @GameTest
    public void playerIsFlyingSelection(GameTestHelper helper) {
        setup(helper);
        var player = Hotbars.createLevelSensitive(helper);

        player.startFallFlying();
        assertActionSlot(helper, Action.ATTACK, RegistryObject.block(Blocks.STONE), player, 1, playerDataTestConfig);
        assertActionSlot(helper, Action.ATTACK, RegistryObject.block(Blocks.COBBLESTONE), player, 5, playerDataTestConfig);
        helper.succeed();
    }

    @GameTest
    public void playerDistanceClosedRange(GameTestHelper helper) {
        setup(helper);
        var player = Hotbars.createLevelSensitive(helper);
        player.setPose(Pose.CROUCHING);

        select(Action.ATTACK, RegistryObject.block(Blocks.OBSIDIAN), player, playerDataTestConfig);
        assertSlot(helper, player, 2);
        helper.succeed();
    }

    @GameTest
    public void playerDistanceUnboundedRange(GameTestHelper helper) {
        setup(helper);
        var player = Hotbars.createLevelSensitive(helper);

        select(Action.ATTACK, RegistryObject.block(Blocks.DEEPSLATE), player, player.blockPosition().above(15), playerDataTestConfig);
        assertSlot(helper, player, 4);

        select(Action.ATTACK, RegistryObject.block(Blocks.DEEPSLATE), player, player.blockPosition(), playerDataTestConfig);
        assertNotSlot(helper, player, 4);

        helper.succeed();
    }

    @GameTest
    public void playerDistanceEntityDistance(GameTestHelper helper) {
        setup(helper);
        var player = Hotbars.createLevelSensitive(helper);

        var creeper = RegistryObject.entity(helper, EntityType.CREEPER);
        moveEntity(player, creeper, 11);
        assertActionSlot(helper, Action.ATTACK, creeper, player, 6, playerDataTestConfig);

        moveEntity(player, creeper, 10);
        assertNotActionSlot(helper, Action.ATTACK, creeper, player, 6, playerDataTestConfig);
        helper.succeed();
    }

    @GameTest
    public void playerIsCrouching(GameTestHelper helper) {
        setup(helper);
        var player = Hotbars.createLevelSensitive(helper);
        assertNotActionSlot(helper, Action.ATTACK, RegistryObject.block(Blocks.OBSIDIAN), player, 2, playerDataTestConfig);

        player.setPose(Pose.CROUCHING);
        assertActionSlot(helper, Action.ATTACK, RegistryObject.block(Blocks.OBSIDIAN), player, 2, playerDataTestConfig);

        helper.succeed();
    }

    @GameTest
    public void oresTargetTest(GameTestHelper helper) {
        setup(helper);
        var player = Hotbars.pickaxePlayer(helper);
        select(Action.ATTACK, RegistryObject.block(Blocks.DIAMOND_ORE), player);
        assertSlot(helper, player, 4);

        player.setPose(Pose.CROUCHING);
        select(Action.ATTACK, RegistryObject.block(Blocks.DIAMOND_ORE), player);
        assertSlot(helper, player, 5);

        player.getInventory().removeItem(5, 1);
        select(Action.ATTACK, RegistryObject.block(Blocks.DIAMOND_ORE), player);
        assertSlot(helper, player, 6);

        player.getInventory().removeItem(6, 1);
        select(Action.ATTACK, RegistryObject.block(Blocks.DIAMOND_ORE), player);
        assertSlot(helper, player, 4);

        helper.succeed();
    }
}
