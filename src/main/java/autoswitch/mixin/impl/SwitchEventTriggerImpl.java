package autoswitch.mixin.impl;

import autoswitch.AutoSwitch;
import autoswitch.config.AutoSwitchConfig.TargetType;
import autoswitch.events.SwitchEvent;
import autoswitch.selectors.ItemTarget;
import autoswitch.util.EventUtil;
import autoswitch.util.SwitchState;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.stats.Stat;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
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
        if (stat == null) return;

        EventUtil.scheduleEvent(SwitchEvent.EVENT_TRIGGER, AutoSwitch.doAS, player,
                                AutoSwitch.featureCfg.switchAllowed().contains(TargetType.EVENTS), stat);

        // Run scheduler here as well as in the clock to ensure immediate-eval switches occur
        AutoSwitch.scheduler.execute(AutoSwitch.tickTime);
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
        boolean doSwitchType;
        switch (desiredType) {
            case USE -> {
                event = SwitchEvent.USE;
                doSwitchType = AutoSwitch.featureCfg.switchUseActions();
            }
            case ATTACK -> {
                event = SwitchEvent.ATTACK;
                doSwitchType = AutoSwitch.featureCfg.switchAllowed()
                        .contains(crosshairTarget.getType() == HitResult.Type.ENTITY ?
                                TargetType.MOBS :
                                TargetType.BLOCKS);
            }
            default -> throw new IllegalStateException("AutoSwitch encountered an unexpected enum value: " +
                                                       desiredType +
                                                       "\nSome mod has fiddled with AutoSwitch's internals!");
        };

        // Trigger switch
        switch (crosshairTarget.getType()) {
            case MISS -> {
                if (desiredType != DesiredType.USE)
                    break;
                if (AutoSwitch.useActionCfg.bow_action().length == 0) {
                    return; // guard to help prevent lag when rclicking into empty space
                }
                EventUtil.scheduleEvent(event, AutoSwitch.doAS, player, doSwitchType, ItemTarget.INSTANCE);
            }
            case ENTITY -> {
                EntityHitResult entityHitResult = (EntityHitResult) crosshairTarget;
                Entity entity = entityHitResult.getEntity();
                EventUtil.schedulePrimaryEvent(SwitchEvent.PREVENT_BLOCK_ATTACK);
                EventUtil.scheduleEvent(event, AutoSwitch.doAS, player, doSwitchType, entity);
            }
            case BLOCK -> {
                if (desiredType == DesiredType.ATTACK && SwitchState.preventBlockAttack)
                    break;
                BlockHitResult blockHitResult = ((BlockHitResult) crosshairTarget);
                BlockPos blockPos = blockHitResult.getBlockPos();
                BlockState blockState = player.clientLevel.getBlockState(blockPos);
                if (blockState.isAir())
                    break;
                EventUtil.scheduleEvent(event, AutoSwitch.doAS, player, doSwitchType, blockState);
            }
        }

        // Run scheduler here as well as in the clock to ensure immediate-eval switches occur
        AutoSwitch.scheduler.execute(AutoSwitch.tickTime);
    }

    /**
     * Type used to control processing of user action for switching in a unified manner.
     */
    enum DesiredType {
        /**
         * Player "interact" or "use" actions.
         */
        USE,
        /**
         * Player "attack" actions.
         */
        ATTACK
    }

}
