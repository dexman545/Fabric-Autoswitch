package autoswitch.mixin.impl;

import autoswitch.AutoSwitch;
import autoswitch.config.AutoSwitchConfig.TargetType;
import autoswitch.events.SwitchEvent;
import autoswitch.selectors.ItemTarget;
import autoswitch.util.EventUtil;
import autoswitch.util.SwitchState;

import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.stat.Stat;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;


/**
 * Implementation of the logic for the MinecraftClient mixin
 */
@SuppressWarnings("JavadocReference")
public class SwitchEventTriggerImpl {

    /**
     * Logic for handling ATTACK type actions.
     * <p>
     * Duplicates short-circuit conditions from {@link net.minecraft.client.MinecraftClient#doAttack()}
     *
     * @param attackCooldown  the attack cooldown
     * @param player          the player
     * @param crosshairTarget the crosshair target
     */
    public static void attack(int attackCooldown, ClientPlayerEntity player, HitResult crosshairTarget) {
        if (attackCooldown > 0 || player.isRiding() || crosshairTarget == null) return;

        triggerSwitch(DesiredType.ATTACK, crosshairTarget, player);

    }

    /**
     * Logic for handling USE actions.
     * <p>
     * Duplicates short-circuit conditions from {@link net.minecraft.client.MinecraftClient#doItemUse()}
     *
     * @param isBreakingBlock if the player is currently breaking a block
     * @param player          the player
     * @param crosshairTarget the crosshair target
     */
    public static void interact(boolean isBreakingBlock, ClientPlayerEntity player, HitResult crosshairTarget) {
        if (isBreakingBlock || player.isRiding() || crosshairTarget == null) return;

        triggerSwitch(DesiredType.USE, crosshairTarget, player);

    }

    public static void eventTrigger(Stat<?> stat, PlayerEntity player) {
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
    private static void triggerSwitch(DesiredType desiredType, HitResult crosshairTarget, ClientPlayerEntity player) {
        SwitchEvent event;
        boolean doSwitchType;

        // Set event and doSwitchType
        switch (desiredType) {
            case USE:
                event = SwitchEvent.USE;
                doSwitchType = AutoSwitch.featureCfg.switchUseActions();
                break;
            case ATTACK:
                event = SwitchEvent.ATTACK;
                doSwitchType = AutoSwitch.featureCfg.switchAllowed()
                                                    .contains(crosshairTarget.getType() == HitResult.Type.ENTITY ?
                                                              TargetType.MOBS :
                                                              TargetType.BLOCKS);
                break;
            default:
                throw new IllegalStateException("AutoSwitch encountered an unexpected enum value: " + desiredType +
                                                "\nSome mod has fiddled with AutoSwitch's internals!");
        }

        // Trigger switch
        switch (crosshairTarget.getType()) {
            case MISS:
                if (desiredType != DesiredType.USE) break;
                if (AutoSwitch.useActionCfg.bow_action().length == 0) {
                    return; // guard to help prevent lag when rclicking into empty space
                }
                EventUtil.scheduleEvent(event, AutoSwitch.doAS, player, doSwitchType, ItemTarget.INSTANCE);
                break;
            case ENTITY:
                EntityHitResult entityHitResult = (EntityHitResult) crosshairTarget;
                Entity entity = entityHitResult.getEntity();
                EventUtil.schedulePrimaryEvent(SwitchEvent.PREVENT_BLOCK_ATTACK);
                EventUtil.scheduleEvent(event, AutoSwitch.doAS, player, doSwitchType, entity);
                break;
            case BLOCK:
                if (desiredType == DesiredType.ATTACK && SwitchState.preventBlockAttack) break;
                BlockHitResult blockHitResult = ((BlockHitResult) crosshairTarget);
                BlockPos blockPos = blockHitResult.getBlockPos();
                BlockState blockState = player.clientWorld.getBlockState(blockPos);
                if (blockState.isAir()) break;
                EventUtil.scheduleEvent(event, AutoSwitch.doAS, player, doSwitchType, blockState);
                break;
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
