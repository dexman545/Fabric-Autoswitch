package dex.autoswitch.gametest.unit;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import dex.autoswitch.Constants;
import dex.autoswitch.api.impl.AutoSwitchApi;
import dex.autoswitch.config.AutoSwitchConfig;
import dex.autoswitch.config.ConfigHandler;
import dex.autoswitch.engine.Action;
import dex.autoswitch.engine.ContextKeys;
import dex.autoswitch.engine.data.extensible.PlayerInventory;
import dex.autoswitch.engine.events.SwitchEvent;
import dex.autoswitch.engine.state.SwitchContext;
import dex.autoswitch.engine.types.SwitchedPlayer;
import dex.autoswitch.gametest.util.FabricTestPlatformHelper;
import dex.autoswitch.platform.Services;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.spongepowered.configurate.ConfigurateException;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public abstract class AbstractTest {
    private static final Predicate<ItemStack> DEPLETED_STACK_PREDICATE = stack -> {
        if (stack.isDamageableItem()) {
            return stack.nextDamageWillBreak();
        }

        return false;
    };

    public AbstractTest() {
        // Register API methods here as the main mod entrypoint is not run for tests,
        // meaning ObjectShare is never queried or populated
        if (!AutoSwitchApi.INSTANCE.DEPLETED.entries().contains(DEPLETED_STACK_PREDICATE)) {
            AutoSwitchApi.INSTANCE.DEPLETED.addEntry(DEPLETED_STACK_PREDICATE);
        }
    }

    protected AutoSwitchConfig loadConfig(String file) {
        try {
            var p = AbstractTest.class.getResource("/configs/" + file + ".conf");
            assert p != null;
            return ConfigHandler.readConfiguration(Path.of(p.toURI()));
        } catch (ConfigurateException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    protected TestPlayer select(Action action, Object target, Player player) {
        return select(action, target, player, Constants.CONFIG);
    }

    protected TestPlayer select(Action action, Object target, Player player, AutoSwitchConfig config) {
        return select(action, target, player, null, config);
    }

    protected TestPlayer select(Action action, Object target, Player player, BlockPos pos, AutoSwitchConfig config) {
        TestPlayer testPlayer = new TestPlayer(new SwitchedPlayer(player));
        Constants.SCHEDULER.schedule(getEvent(action),
                new SwitchContext(testPlayer, config, action,
                        target, Constants.SWITCH_STATE, Constants.SCHEDULER,
                        Map.entry(ContextKeys.PLAYER, player),
                        Map.entry(ContextKeys.BLOCK_POS, pos == null ? BlockPos.ZERO : pos)
                ), 0);
        Constants.SCHEDULER.tick();
        return testPlayer;
    }

    protected void setup(GameTestHelper helper) {
        helper.assertTrue(Constants.CONFIG != null, Component.literal("Config is null!"));
        if (Services.PLATFORM instanceof FabricTestPlatformHelper testPlatformHelper) {
            testPlatformHelper.setServer(helper.getLevel().registryAccess());
        }
    }

    protected void assertSlot(GameTestHelper helper, Player player, int expectedSlot) {
        var inv = player.getInventory();
        helper.assertValueEqual(expectedSlot, inv.getSelectedSlot(),
                Component.literal("correct slot"));
    }

    protected void assertNotSlot(GameTestHelper helper, Player player, int expectedSlot) {
        var inv = player.getInventory();
        helper.assertFalse(Objects.equals(expectedSlot, inv.getSelectedSlot()),
                Component.literal("slot to be different from expected"));
    }

    private SwitchEvent getEvent(Action action) {
        return switch (action) {
            case ATTACK -> SwitchEvent.ATTACK;
            case INTERACT -> SwitchEvent.INTERACT;
            case STAT_CHANGE -> SwitchEvent.STAT_CHANGE;
        };
    }

    // Work around offhanding needing client classes
    protected record TestPlayer(SwitchedPlayer player,
                                MutableBoolean hasOffhanded) implements PlayerInventory<ItemStack> {
        private TestPlayer(SwitchedPlayer player) {
            this(player, new MutableBoolean());
        }

        @Override
        public void selectSlot(int slot) {
            player.selectSlot(slot);
        }

        @Override
        public int currentSelectedSlot() {
            return player.currentSelectedSlot();
        }

        @Override
        public int slotCount() {
            return player.slotCount();
        }

        @Override
        public ItemStack getTool(int slot) {
            return player.getTool(slot);
        }

        @Override
        public boolean canSwitchBack(SwitchContext ctx) {
            // Can't check attack strength here as it never seems to be non-0
            return (!player.player().isUsingItem()) && !player.player().swinging;
        }

        @Override
        public void moveOffhand() {
            hasOffhanded.setTrue();
        }

        public void reset() {
            hasOffhanded.setFalse();
        }
    }
}
