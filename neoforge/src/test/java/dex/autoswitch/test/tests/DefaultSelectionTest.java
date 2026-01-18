package dex.autoswitch.test.tests;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.Function;

import dex.autoswitch.Constants;
import dex.autoswitch.config.AutoSwitchConfig;
import dex.autoswitch.engine.Action;
import dex.autoswitch.engine.SelectionEngine;
import dex.autoswitch.fapi.client.api.ClientTags;
import dex.autoswitch.harness.DummyInventory;
import dex.autoswitch.harness.NeoForgeTestPlatformHelper;
import dex.autoswitch.platform.Services;
import dex.autoswitch.test.util.AbstractSelectionTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Blocks;

public class DefaultSelectionTest extends AbstractSelectionTest {
    private final AutoSwitchConfig autoSwitchConfig = Constants.CONFIG;

    private static final Function<MinecraftServer, DummyInventory> NORMAL_HOTBAR = server ->
            new DummyInventory(List.of(
                    // 0
                    ItemStack.EMPTY,
                    // 1
                    stack(server, Items.DIAMOND_PICKAXE),
                    // 2
                    stack(server, Items.DIAMOND_PICKAXE,
                            Enchant.of(Enchantments.FORTUNE, 3)),
                    // 3
                    stack(server, Items.DIAMOND_PICKAXE,
                            Enchant.of(Enchantments.FORTUNE, 3),
                            Enchant.of(Enchantments.EFFICIENCY, 1)),
                    // 4
                    stack(server, Items.DIAMOND_PICKAXE,
                            Enchant.of(Enchantments.FORTUNE, 3),
                            Enchant.of(Enchantments.EFFICIENCY, 5)),
                    // 5
                    stack(server, Items.DIAMOND_PICKAXE,
                            Enchant.of(Enchantments.SILK_TOUCH, 1)),
                    // 6
                    stack(server, Items.DIAMOND_PICKAXE,
                            Enchant.of(Enchantments.SILK_TOUCH, 1),
                            Enchant.of(Enchantments.EFFICIENCY, 5)),
                    // 7
                    stack(server, Items.WOODEN_PICKAXE),
                    // 8
                    stack(server, Items.DIAMOND_AXE),
                    // 9
                    stack(server, Items.DIAMOND_AXE,
                            Enchant.of(Enchantments.EFFICIENCY, 5)),
                    // 10
                    stack(server, Items.GOLDEN_SHOVEL),
                    // 11
                    stack(server, Items.SHEARS),
                    // 12
                    stack(server, Items.TRIDENT),
                    // 13
                    stack(server, Items.TRIDENT,
                            Enchant.of(Enchantments.IMPALING, 1)),
                    // 14
                    stack(server, Items.IRON_HOE),
                    // 15
                    stack(server, Items.IRON_HOE,
                            Enchant.of(Enchantments.SILK_TOUCH, 1)),
                    // 16
                    stack(server, Items.SEA_PICKLE),
                    // 17
                    stack(server, Items.STONE_SWORD),
                    // 18
                    stack(server, Items.FLINT_AND_STEEL),
                    // 19
                    stack(server, Items.IRON_INGOT)
            ));

    private static final Function<MinecraftServer, DummyInventory> NO_SWORD_HOTBAR = server ->
            new DummyInventory(List.of(
                    // 0
                    ItemStack.EMPTY,
                    // 1
                    stack(server, Items.DIAMOND_PICKAXE),
                    // 2
                    stack(server, Items.DIAMOND_PICKAXE,
                            Enchant.of(Enchantments.FORTUNE, 3)),
                    // 3
                    stack(server, Items.DIAMOND_PICKAXE,
                            Enchant.of(Enchantments.FORTUNE, 3),
                            Enchant.of(Enchantments.EFFICIENCY, 1)),
                    // 4
                    stack(server, Items.DIAMOND_PICKAXE,
                            Enchant.of(Enchantments.FORTUNE, 3),
                            Enchant.of(Enchantments.EFFICIENCY, 5)),
                    // 5
                    stack(server, Items.DIAMOND_PICKAXE,
                            Enchant.of(Enchantments.SILK_TOUCH, 1)),
                    // 6
                    stack(server, Items.DIAMOND_PICKAXE,
                            Enchant.of(Enchantments.SILK_TOUCH, 1),
                            Enchant.of(Enchantments.EFFICIENCY, 5)),
                    // 7
                    stack(server, Items.WOODEN_PICKAXE)
            ));

    private static final Function<MinecraftServer, DummyInventory> NO_AXE_HOTBAR = server ->
            new DummyInventory(List.of(
                    // 0
                    ItemStack.EMPTY,
                    // 1
                    stack(server, Items.DIAMOND_PICKAXE),
                    // 2
                    stack(server, Items.DIAMOND_PICKAXE,
                            Enchant.of(Enchantments.FORTUNE, 3)),
                    // 3
                    stack(server, Items.DIAMOND_PICKAXE,
                            Enchant.of(Enchantments.FORTUNE, 3),
                            Enchant.of(Enchantments.EFFICIENCY, 1)),
                    // 4
                    stack(server, Items.DIAMOND_PICKAXE,
                            Enchant.of(Enchantments.FORTUNE, 3),
                            Enchant.of(Enchantments.EFFICIENCY, 5)),
                    // 5
                    stack(server, Items.DIAMOND_PICKAXE,
                            Enchant.of(Enchantments.SILK_TOUCH, 1)),
                    // 6
                    stack(server, Items.DIAMOND_PICKAXE,
                            Enchant.of(Enchantments.SILK_TOUCH, 1),
                            Enchant.of(Enchantments.EFFICIENCY, 5)),
                    // 7
                    stack(server, Items.SEA_PICKLE)
            ));

    @Override
    public SelectionEngine getEngine() {
        return autoSwitchConfig.getEngine();
    }

    @Test
    void defaultPickaxeAttack(MinecraftServer server) {
        if (Services.PLATFORM instanceof NeoForgeTestPlatformHelper neoForgeTestPlatformHelper) {
            neoForgeTestPlatformHelper.setServer(server);
        }

        var hotbar = NORMAL_HOTBAR.apply(server);

        var stone = block(Blocks.STONE);
        var glass = block(Blocks.GLASS);
        var enderChest = block(Blocks.ENDER_CHEST);

        engine.select(hotbar, Action.ATTACK, stone);
        assertSelectedSlot(4, hotbar);

        engine.select(hotbar, Action.ATTACK, enderChest);
        assertSelectedSlot(5, hotbar);

        hotbar.selectSlot(0);

        engine.select(hotbar, Action.ATTACK, glass);
        assertSelectedSlot(5, hotbar);
    }

    @Test
    @Disabled
    void timingTest(MinecraftServer server) {
        if (Services.PLATFORM instanceof NeoForgeTestPlatformHelper neoForgeTestPlatformHelper) {
            neoForgeTestPlatformHelper.setServer(server);
        }

        var hotbar = NORMAL_HOTBAR.apply(server);

        var stone = block(Blocks.STONE);
        var glass = block(Blocks.GLASS);
        var enderChest = block(Blocks.ENDER_CHEST);

        var condition = new String[]{"Worst", "Best"};
        for (String s : condition) {
            var runs = 1_000_000;
            var max = Long.MIN_VALUE;
            var min = Long.MAX_VALUE;
            var avg = 0D;
            for (int i = 0; i < runs; i++) {
                var start = System.nanoTime();
                engine.select(hotbar, Action.ATTACK, stone);
                assertSelectedSlot(4, hotbar);

                hotbar.selectSlot(0);
                engine.select(hotbar, Action.ATTACK, glass);
                assertSelectedSlot(5, hotbar);

                hotbar.selectSlot(0);
                engine.select(hotbar, Action.ATTACK, enderChest);
                assertSelectedSlot(5, hotbar);

                var d = System.nanoTime() - start;
                max = Math.max(max, d);
                min = Math.min(min, d);
                avg += d;
                // Reset engine to mimic empty cache
                if (s.equals("Worst")) {
                    autoSwitchConfig.resetConfiguration();
                    engine = autoSwitchConfig.getEngine();
                }
            }

            System.out.println(s);
            System.out.println("max " + max);
            System.out.println("min " + min);
            System.out.println("avg " + avg / runs);
        }
    }

    @Test
    void defaultSwordAttack(MinecraftServer server) {
        if (Services.PLATFORM instanceof NeoForgeTestPlatformHelper neoForgeTestPlatformHelper) {
            neoForgeTestPlatformHelper.setServer(server);
        }

        var hotbar = NORMAL_HOTBAR.apply(server);
        var noSwordHotbar = NO_SWORD_HOTBAR.apply(server);

        var oakLog = block(Blocks.OAK_LOG);
        var jackOLantern = block(Blocks.JACK_O_LANTERN);
        var bamboo = block(Blocks.BAMBOO);

        engine.select(hotbar, Action.ATTACK, bamboo);
        assertSelectedSlot(17, hotbar);

        engine.select(noSwordHotbar, Action.ATTACK, oakLog);
        assertSelectedSlot(0, noSwordHotbar);

        engine.select(noSwordHotbar, Action.ATTACK, jackOLantern);
        assertSelectedSlot(0, noSwordHotbar);
    }

    @Test
    void defaultAxeAttack(MinecraftServer server) {
        if (Services.PLATFORM instanceof NeoForgeTestPlatformHelper neoForgeTestPlatformHelper) {
            neoForgeTestPlatformHelper.setServer(server);
        }

        var hotbar = NORMAL_HOTBAR.apply(server);
        var noAxeHotbar = NO_AXE_HOTBAR.apply(server);

        var oakLog = block(Blocks.OAK_LOG);
        var oakLeaves = block(Blocks.OAK_LEAVES);
        var mushroom = block(Blocks.MUSHROOM_STEM);

        engine.select(hotbar, Action.ATTACK, oakLog);
        assertSelectedSlot(9, hotbar);

        engine.select(noAxeHotbar, Action.ATTACK, oakLog);
        assertSelectedSlot(0, noAxeHotbar);

        engine.select(hotbar, Action.ATTACK, mushroom);
        assertSelectedSlot(9, hotbar);

        engine.select(noAxeHotbar, Action.ATTACK, oakLeaves);
        assertSelectedSlot(0, noAxeHotbar);
    }

    @Test
    void defaultHarvestInteraction(MinecraftServer server) {
        if (Services.PLATFORM instanceof NeoForgeTestPlatformHelper neoForgeTestPlatformHelper) {
            neoForgeTestPlatformHelper.setServer(server);
        }

        var hotbar = NORMAL_HOTBAR.apply(server);

        var beehive = block(Blocks.BEEHIVE);
        var beehiveWithHoney1 = block(Blocks.BEEHIVE, State.of(BeehiveBlock.HONEY_LEVEL, 1));
        var beehiveWithHoney2 = block(Blocks.BEEHIVE, State.of(BeehiveBlock.HONEY_LEVEL, 2));
        var beehiveWithHoney5 = block(Blocks.BEEHIVE, State.of(BeehiveBlock.HONEY_LEVEL, 5));
        var beeNest = block(Blocks.BEE_NEST);
        var beeNestWithHoney1 = block(Blocks.BEE_NEST, State.of(BeehiveBlock.HONEY_LEVEL, 1));
        var beeNestWithHoney2 = block(Blocks.BEE_NEST, State.of(BeehiveBlock.HONEY_LEVEL, 2));
        var beeNestWithHoney5 = block(Blocks.BEE_NEST, State.of(BeehiveBlock.HONEY_LEVEL, 5));

        engine.select(hotbar, Action.INTERACT, beehive);
        assertSelectedSlot(0, hotbar);

        engine.select(hotbar, Action.INTERACT, beehiveWithHoney1);
        assertSelectedSlot(0, hotbar);

        engine.select(hotbar, Action.INTERACT, beehiveWithHoney2);
        assertSelectedSlot(0, hotbar);

        engine.select(hotbar, Action.INTERACT, beehiveWithHoney5);
        assertSelectedSlot(11, hotbar);

        hotbar.selectSlot(0);
        engine.select(hotbar, Action.INTERACT, beeNest);
        assertSelectedSlot(0, hotbar);

        engine.select(hotbar, Action.INTERACT, beeNestWithHoney1);
        assertSelectedSlot(0, hotbar);

        engine.select(hotbar, Action.INTERACT, beeNestWithHoney2);
        assertSelectedSlot(0, hotbar);

        engine.select(hotbar, Action.INTERACT, beeNestWithHoney5);
        assertSelectedSlot(11, hotbar);
    }

    @Test
    @Disabled("Need world to create entity")
    void funInteraction(MinecraftServer server) {
        if (Services.PLATFORM instanceof NeoForgeTestPlatformHelper neoForgeTestPlatformHelper) {
            neoForgeTestPlatformHelper.setServer(server);
        }

        var hotbar = NORMAL_HOTBAR.apply(server);

        var creeper = entity(server.overworld(), EntityType.CREEPER);

        engine.select(hotbar, Action.INTERACT, creeper);
        assertSelectedSlot(17, hotbar);
    }

    @Test
    @Disabled("Need world to create entity")
    void defaultHealInteraction(MinecraftServer server) {
        if (Services.PLATFORM instanceof NeoForgeTestPlatformHelper neoForgeTestPlatformHelper) {
            neoForgeTestPlatformHelper.setServer(server);
        }

        var hotbar = NORMAL_HOTBAR.apply(server);

        var golem = entity(server.overworld(), EntityType.IRON_GOLEM);

        engine.select(hotbar, Action.INTERACT, golem);
        assertSelectedSlot(18, hotbar);
    }

    @Test
    void testLoadingClientTags(MinecraftServer server) {
        if (Services.PLATFORM instanceof NeoForgeTestPlatformHelper neoForgeTestPlatformHelper) {
            neoForgeTestPlatformHelper.setServer(server);
        }

        var ore = block(Blocks.DIAMOND_ORE);
        var oresTag = TagKey.create(Registries.BLOCK,
                Identifier.fromNamespaceAndPath("c", "ores"));
        var badTag = TagKey.create(Registries.BLOCK,
                Identifier.fromNamespaceAndPath("c", "ores2"));

        assertTrue(ClientTags.isInLocal(oresTag, ore.typeHolder().getKey()));
        assertFalse(ClientTags.isInLocal(badTag, ore.typeHolder().getKey()));
    }
}
