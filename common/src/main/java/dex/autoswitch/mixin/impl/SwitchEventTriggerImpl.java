package dex.autoswitch.mixin.impl;

import java.util.Map;

import dex.autoswitch.Constants;
import dex.autoswitch.engine.Action;
import dex.autoswitch.engine.ContextKeys;
import dex.autoswitch.engine.TargetType;
import dex.autoswitch.engine.events.SwitchEvent;
import dex.autoswitch.engine.state.SwitchContext;
import dex.autoswitch.engine.types.SwitchedPlayer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;


/**
 * Implementation of the logic for the MinecraftClient mixin
 */
@SuppressWarnings("JavadocReference")
public class SwitchEventTriggerImpl {

    /**
     * Logic for handling ATTACK type actions.
     * <p>
     * Duplicates short-circuit conditions from {@link net.minecraft.client.Minecraft#startAttack()}
     *
     * @param attackCooldown  the attack cooldown
     * @param player          the player
     * @param crosshairTarget the crosshair target
     */
    public static void attack(int attackCooldown, LocalPlayer player, HitResult crosshairTarget) {
        if (attackCooldown > 0 || player.isHandsBusy() || crosshairTarget == null) return;

        triggerSwitch(DesiredType.ATTACK, crosshairTarget, player);

    }

    /**
     * Logic for handling USE actions.
     * <p>
     * Duplicates short-circuit conditions from {@link net.minecraft.client.Minecraft#startUseItem()}
     *
     * @param isBreakingBlock if the player is currently breaking a block
     * @param player          the player
     * @param crosshairTarget the crosshair target
     */
    public static void interact(boolean isBreakingBlock, LocalPlayer player, HitResult crosshairTarget) {
        if (isBreakingBlock || player.isHandsBusy() || crosshairTarget == null) return;

        triggerSwitch(DesiredType.USE, crosshairTarget, player);

    }

    public static void eventTrigger(Stat<?> stat, Player player) {
        if (stat == null || player == null || !Constants.performSwitch ||
                !Constants.CONFIG.featureConfig.switchAllowed.contains(TargetType.EVENTS) || canNotSwitch(player) ||
                !Constants.CONFIG.featureConfig.switchActions.contains(Action.STAT_CHANGE)) {
            return;
        }

        var context = new SwitchContext(new SwitchedPlayer(player), Constants.CONFIG,
                Action.STAT_CHANGE, stat, Constants.SWITCH_STATE, Constants.SCHEDULER,
                Map.entry(ContextKeys.PLAYER, player));
        Constants.SCHEDULER.schedule(SwitchEvent.STAT_CHANGE, context, 0);

        // Run scheduler here as well as in the clock to ensure immediate-eval switches occur
        Constants.SCHEDULER.tick();
    }

    /**
     * Process type of action made and desired switch action.
     * <p>Tick scheduler clock to ensure immediate-mode actions are taken on time.</p>
     *
     * @param desiredType     type of action to process for switching.
     * @param crosshairTarget target that the player is looking at.
     * @param player          the player
     */
    private static void triggerSwitch(DesiredType desiredType, HitResult crosshairTarget, LocalPlayer player) {
        // Set event and doSwitchType
        SwitchEvent event;
        var doSwitchType = switch (desiredType) {
            case USE -> {
                event = SwitchEvent.INTERACT;
                yield Constants.CONFIG.featureConfig.switchActions.contains(Action.INTERACT);
            }
            case ATTACK -> {
                event = SwitchEvent.ATTACK;
                yield Constants.CONFIG.featureConfig.switchActions.contains(Action.ATTACK) &&
                        Constants.CONFIG.featureConfig.switchAllowed.contains(crosshairTarget.getType() == HitResult.Type.ENTITY ?
                                TargetType.ENTITIES :
                                TargetType.BLOCKS);
            }
        };

        if (!doSwitchType || !Constants.performSwitch || canNotSwitch(player)) {
            return;
        }

        // Trigger switch
        switch (crosshairTarget.getType()) {
            case MISS -> {
                if (desiredType != DesiredType.USE)
                    break;
            }
            case ENTITY -> {
                var entityHitResult = (EntityHitResult) crosshairTarget;
                var entity = entityHitResult.getEntity();
                var context = new SwitchContext(new SwitchedPlayer(player), Constants.CONFIG,
                        desiredType.action, entity, Constants.SWITCH_STATE, Constants.SCHEDULER,
                        Map.entry(ContextKeys.PLAYER, player));
                Constants.SCHEDULER.schedule(event, context, 0);
            }
            case BLOCK -> {
                if (desiredType == DesiredType.ATTACK && Constants.SWITCH_STATE.preventBlockAttack())
                    break;
                var blockHitResult = ((BlockHitResult) crosshairTarget);
                var blockPos = blockHitResult.getBlockPos();
                @SuppressWarnings("resource")
                var blockState = player.level().getBlockState(blockPos);
                if (blockState.isAir())
                    break;
                var context = new SwitchContext(new SwitchedPlayer(player), Constants.CONFIG,
                        desiredType.action, blockState, Constants.SWITCH_STATE, Constants.SCHEDULER,
                        Map.entry(ContextKeys.BLOCK_POS, blockPos),
                        Map.entry(ContextKeys.PLAYER, player));
                Constants.SCHEDULER.schedule(event, context, 0);
            }
        }

        // Run scheduler here as well as in the clock to ensure immediate-eval switches occur
        Constants.SCHEDULER.tick();
    }

    private static boolean canNotSwitch(Player player) {
        var mc = Minecraft.getInstance();
        var inSP = true;
        //noinspection ConstantValue
        if (mc != null) {
            inSP = mc.isLocalServer();
        }

        return (Constants.CONFIG.featureConfig.disableWhenCrouched && player.isCrouching()) ||
                !(Constants.CONFIG.featureConfig.switchInCreative || !player.isCreative()) ||
                !(Constants.CONFIG.featureConfig.switchInMp || !inSP);
    }

    /**
     * Type used to control processing of user action for switching in a unified manner.
     */
    enum DesiredType {
        /**
         * Player "interact" or "use" actions.
         */
        USE(Action.INTERACT),
        /**
         * Player "attack" actions.
         */
        ATTACK(Action.ATTACK);

        private final Action action;

        DesiredType(Action action) {
            this.action = action;
        }
    }

}
