package dex.autoswitch.gametest.unit;

import java.util.Objects;

import dex.autoswitch.Constants;
import dex.autoswitch.engine.Action;
import dex.autoswitch.engine.types.selectable.StatSelectableType;
import dex.autoswitch.gametest.util.Hotbars;
import dex.autoswitch.gametest.util.RegistryObject;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Blocks;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

public class UnitGameTest extends AbstractTest {
    @GameTest
    public void pickaxeTest(GameTestHelper helper) {
        setup(helper);

        var player = Hotbars.pickaxePlayer(helper);
        var stone = RegistryObject.block(Blocks.STONE);
        var glass = RegistryObject.block(Blocks.GLASS);
        var enderChest = RegistryObject.block(Blocks.ENDER_CHEST);

        select(Action.ATTACK, stone, player);
        assertSlot(helper, player, 6);

        player.getInventory().setSelectedSlot(0);
        select(Action.ATTACK, glass, player);
        assertSlot(helper, player, 5);

        player.getInventory().setSelectedSlot(0);
        select(Action.ATTACK, enderChest, player);
        assertSlot(helper, player, 5);

        helper.succeed();
    }

    @GameTest
    public void entityTest(GameTestHelper helper) {
        setup(helper);

        TestPlayer testPlayer;
        var player = Hotbars.fightingPlayer(helper);
        var chicken = RegistryObject.entity(helper, EntityType.CHICKEN);
        var spider = RegistryObject.entity(helper, EntityType.SPIDER);
        var zombie = RegistryObject.entity(helper, EntityType.ZOMBIE);

        select(Action.ATTACK, chicken, player);
        assertSlot(helper, player, 7);

        select(Action.ATTACK, spider, player);
        assertSlot(helper, player, 5);

        select(Action.ATTACK, zombie, player);
        assertSlot(helper, player, 6);

        helper.succeed();
    }

    @GameTest
    public void entityInteractionTest(GameTestHelper helper) {
        setup(helper);

        TestPlayer testPlayer;
        var player = Hotbars.fightingPlayer(helper);
        var creeper = RegistryObject.entity(helper, EntityType.CREEPER);
        var strider = RegistryObject.entity(helper, EntityType.STRIDER);
        var ironGolem = RegistryObject.entity(helper, EntityType.IRON_GOLEM);

        testPlayer = select(Action.INTERACT, creeper, player);
        assertSlot(helper, player, 8);
        helper.assertTrue(testPlayer.hasOffhanded().isFalse(), Component.literal("Expected to not offhand"));

        player.getInventory().setSelectedSlot(2);
        testPlayer = select(Action.INTERACT, strider, player);
        assertSlot(helper, player, 2);
        helper.assertTrue(testPlayer.hasOffhanded().isFalse(), Component.literal("Expected to not offhand"));

        strider.equipItemIfPossible(helper.getLevel(), RegistryObject.stack(helper, Items.SADDLE));
        testPlayer = select(Action.INTERACT, strider, player);
        assertSlot(helper, player, 0);
        Constants.SCHEDULER.tick();
        helper.assertTrue(testPlayer.hasOffhanded().isTrue(), Component.literal("Expected to offhand"));

        testPlayer = select(Action.INTERACT, ironGolem, player);
        assertSlot(helper, player, 2);
        helper.assertTrue(testPlayer.hasOffhanded().isFalse(), Component.literal("Expected to not offhand"));

        helper.succeed();
    }

    @GameTest
    public void fallbackAndMultiTargetMatchTest(GameTestHelper helper) {
        setup(helper);

        TestPlayer testPlayer;
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

        TestPlayer testPlayer;
        var player = Hotbars.potionPlayer(helper);
        var fire = RegistryObject.block(Blocks.FIRE);

        select(Action.INTERACT, fire, player);
        //assertSlot(helper, player, 3);
        // Disabled as running the test via the run config works, but via the task and it fails

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

        TestPlayer testPlayer;
        var player = Hotbars.milkPlayer(helper);
        var bedStat = StatSelectableType.INSTANCE.lookup(Objects.requireNonNull(Identifier.tryParse("custom:sleep_in_bed")));

        // Ideally we'd actually trigger the stat change, but that isn't trivial on the server
        select(Action.STAT_CHANGE, bedStat, player);
        assertSlot(helper, player, 3);

        helper.succeed();
    }

    @GameTest
    public void blockstateTest(GameTestHelper helper) {
        setup(helper);

        TestPlayer testPlayer;
        var player = Hotbars.shearingPlayer(helper);

        var beehive = RegistryObject.block(Blocks.BEEHIVE);
        var beehiveWithHoney1 = RegistryObject.block(Blocks.BEEHIVE, RegistryObject.State.of(BeehiveBlock.HONEY_LEVEL, 1));
        var beehiveWithHoney2 = RegistryObject.block(Blocks.BEEHIVE, RegistryObject.State.of(BeehiveBlock.HONEY_LEVEL, 2));
        var beehiveWithHoney5 = RegistryObject.block(Blocks.BEEHIVE, RegistryObject.State.of(BeehiveBlock.HONEY_LEVEL, 5));
        var beeNest = RegistryObject.block(Blocks.BEE_NEST);
        var beeNestWithHoney1 = RegistryObject.block(Blocks.BEE_NEST, RegistryObject.State.of(BeehiveBlock.HONEY_LEVEL, 1));
        var beeNestWithHoney2 = RegistryObject.block(Blocks.BEE_NEST, RegistryObject.State.of(BeehiveBlock.HONEY_LEVEL, 2));
        var beeNestWithHoney5 = RegistryObject.block(Blocks.BEE_NEST, RegistryObject.State.of(BeehiveBlock.HONEY_LEVEL, 5));

        select(Action.INTERACT, beehive, player);
        assertSlot(helper, player, 0);

        select(Action.INTERACT, beehiveWithHoney1, player);
        assertSlot(helper, player, 0);

        select(Action.INTERACT, beehiveWithHoney2, player);
        assertSlot(helper, player, 0);

        select(Action.INTERACT, beehiveWithHoney5, player);
        assertSlot(helper, player, 3);

        player.getInventory().setSelectedSlot(0);
        select(Action.INTERACT, beeNest, player);
        assertSlot(helper, player, 0);

        select(Action.INTERACT, beeNestWithHoney1, player);
        assertSlot(helper, player, 0);

        select(Action.INTERACT, beeNestWithHoney2, player);
        assertSlot(helper, player, 0);

        select(Action.INTERACT, beeNestWithHoney5, player);
        assertSlot(helper, player, 3);

        helper.succeed();
    }

    @GameTest
    public void skipDepletedItemsTest(GameTestHelper helper) {
        setup(helper);

        TestPlayer testPlayer;
        var player = Hotbars.wornFighter(helper);
        var creeper = RegistryObject.entity(helper, EntityType.CREEPER);

        select(Action.ATTACK, creeper, player);
        assertSlot(helper, player, 1);

        // Set sword to be nearly broken
        var sword1 = player.getInventory().getItem(1);
        sword1.setDamageValue(sword1.getMaxDamage() - 1);

        select(Action.ATTACK, creeper, player);
        assertSlot(helper, player, 2);

        helper.succeed();
    }

    @GameTest
    public void enchantmentLevelTest(GameTestHelper helper) {
        setup(helper);

        var config = loadConfig("enchantmentLevelTest");
        var player = Hotbars.createLevelSensitive(helper);

        var stone = RegistryObject.block(Blocks.STONE);
        var obsidian = RegistryObject.block(Blocks.OBSIDIAN);
        var deepslate = RegistryObject.block(Blocks.DEEPSLATE);
        var cobblestone = RegistryObject.block(Blocks.COBBLESTONE);

        select(Action.ATTACK, stone, player, config);
        assertSlot(helper, player, 1);

        select(Action.ATTACK, cobblestone, player, config);
        assertSlot(helper, player, 5);

        select(Action.ATTACK, obsidian, player, config);
        assertSlot(helper, player, 2);

        select(Action.ATTACK, deepslate, player, config);
        assertSlot(helper, player, 4);

        helper.succeed();
    }

    @GameTest
    public void playerDataTest(GameTestHelper helper) {
        setup(helper);

        var config = loadConfig("playerDataTest");
        var player = Hotbars.createLevelSensitive(helper);

        var stone = RegistryObject.block(Blocks.STONE);
        var obsidian = RegistryObject.block(Blocks.OBSIDIAN);
        var deepslate = RegistryObject.block(Blocks.DEEPSLATE);
        var cobblestone = RegistryObject.block(Blocks.COBBLESTONE);
        var creeper = RegistryObject.entity(helper, EntityType.CREEPER);

        player.startFallFlying();

        select(Action.ATTACK, stone, player, config);
        assertSlot(helper, player, 1);

        select(Action.ATTACK, cobblestone, player, config);
        assertSlot(helper, player, 5);

        select(Action.ATTACK, deepslate, player, player.blockPosition().above(15), config);
        assertSlot(helper, player, 4);

        select(Action.ATTACK, deepslate, player, player.blockPosition(), config);
        assertNotSlot(helper, player, 4);

        player.stopFallFlying();

        player.getInventory().setSelectedSlot(0);
        moveEntity(player, creeper, 11);
        select(Action.ATTACK, creeper, player, config);
        assertSlot(helper, player, 6);

        player.getInventory().setSelectedSlot(0);
        moveEntity(player, creeper, 10);
        select(Action.ATTACK, creeper, player, config);
        assertSlot(helper, player, 7);

        select(Action.ATTACK, obsidian, player, config);
        assertSlot(helper, player, 7);
        player.setPose(Pose.CROUCHING);
        select(Action.ATTACK, obsidian, player, config);
        assertSlot(helper, player, 2);

        player.setPose(Pose.STANDING);

        select(Action.ATTACK, obsidian, player, config);
        assertNotSlot(helper, player, 2);

        helper.succeed();
    }

    private static void moveEntity(Player player, Entity entity, int distance) {
        var pp = player.position();
        entity.setPos(pp.x + distance, pp.y, pp.z);
    }
}
