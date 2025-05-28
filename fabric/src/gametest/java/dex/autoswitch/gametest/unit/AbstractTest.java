package dex.autoswitch.gametest.unit;

import dex.autoswitch.Constants;
import dex.autoswitch.engine.Action;
import dex.autoswitch.engine.data.extensible.PlayerInventory;
import dex.autoswitch.engine.events.SwitchEvent;
import dex.autoswitch.engine.state.SwitchContext;
import dex.autoswitch.engine.types.SwitchedPlayer;
import dex.autoswitch.gametest.util.FabricTestPlatformHelper;
import dex.autoswitch.platform.Services;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableBoolean;

public abstract class AbstractTest {
    protected TestPlayer select(Action action, Object target, Player player) {
        TestPlayer testPlayer = new TestPlayer(new SwitchedPlayer(player));
        Constants.SCHEDULER.schedule(getEvent(action),
                new SwitchContext(testPlayer, Constants.CONFIG, action,
                        target, Constants.SWITCH_STATE, Constants.SCHEDULER), 0);
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
        public boolean canSwitchBack() {
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
