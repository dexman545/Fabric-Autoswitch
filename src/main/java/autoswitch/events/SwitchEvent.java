package autoswitch.events;

import static autoswitch.AutoSwitch.featureCfg;
import static autoswitch.AutoSwitch.tickTime;

import java.util.Optional;

import autoswitch.AutoSwitch;
import autoswitch.config.AutoSwitchConfig;
import autoswitch.config.AutoSwitchConfig.TargetType;
import autoswitch.targetable.Targetable;
import autoswitch.util.EventUtil;
import autoswitch.util.SwitchState;
import autoswitch.util.SwitchUtil;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PlayerRideable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Processing of switch events.
 */
public enum SwitchEvent {
    /**
     * Event for "attack" action of the player.
     */
    ATTACK {
        /**
         * Setup SwitchState for switchback feature.
         *
         * @param hasSwitched whether a switch has occurred
         */
        private void handlePostSwitchTasks(boolean hasSwitched) {
            if (hasSwitched) {
                if (protoTarget instanceof Entity) {
                    if (featureCfg.switchbackAllowed().contains(TargetType.MOBS)) {
                        AutoSwitch.switchState.setHasSwitched(true);
                        AutoSwitch.switchState.setAttackedEntity(true);
                    }
                } else if (protoTarget instanceof BlockState) {
                    if (featureCfg.switchbackAllowed().contains(TargetType.BLOCKS)) {
                        AutoSwitch.switchState.setHasSwitched(true);
                    }
                }

                EventUtil.eventHandler(AutoSwitch.tickTime, AutoSwitch.featureCfg.switchbackDelay(), SWITCHBACK);
            }
        }

        @Override
        public boolean invoke() {
            if (canNotSwitch()) return false; // Shortcircuit to make it easier to read

            handlePrevSlot();

            Targetable.attack(protoTarget, player).changeTool().ifPresent(this::handlePostSwitchTasks);

            return true;
        }
    },
    /**
     * Event for "interact" or "use" action of the player.
     */
    USE {
        private boolean doOffhand() {
            if (AutoSwitch.featureCfg.putUseActionToolInOffHand() == AutoSwitchConfig.OffhandType.SADDLE) {
                return protoTarget instanceof PlayerRideable;
            }
            return AutoSwitch.featureCfg.putUseActionToolInOffHand().allowed();
        }

        /**
         * Setup SwitchState for switchback feature.
         *
         * @param hasSwitched whether a switch has occurred
         */
        private void handlePostSwitchTasks(boolean hasSwitched) {
            doOffhandSwitch = doOffhand();
            AutoSwitch.switchState.setHasSwitched(hasSwitched);
            if (hasSwitched) {
                EventUtil.eventHandler(AutoSwitch.tickTime, 0.1, OFFHAND);
                EventUtil.eventHandler(AutoSwitch.tickTime, featureCfg.switchbackDelay(), SWITCHBACK);
            }
        }

        @Override
        public boolean invoke() {
            if (canNotSwitch()) return false; // Shortcircuit to make it easier to read

            handlePrevSlot();
            Targetable.use(protoTarget, player).changeTool().ifPresent(this::handlePostSwitchTasks);

            return true;

        }
    },
    /**
     * Event for returning to the previously selected slot once conditions that triggered a switch are over.
     */
    SWITCHBACK {
        @Override
        public boolean canNotSwitch() {
            // False to prevent switchback from not being scheduled
            return false;
        }

        /**
         * @return if the attack cooldown has progressed enough.
         */
        private boolean doSwitchback() {
            // Uses -20.0f to give player some leeway when fighting. Use 0 for perfect timing
            return player.getAttackStrengthScale(-20.0f) != 1.0f;
        }

        private boolean doMobSwitchback() {
            return AutoSwitch.switchState.hasAttackedEntity() && featureCfg.switchbackWaits().contains(TargetType.MOBS);
        }

        private boolean doBlockSwitchback() {
            return !AutoSwitch.switchState.hasAttackedEntity() && featureCfg.switchbackWaits().contains(TargetType.BLOCKS);
        }

        private boolean disallowSwitchback() {
            // Check if conditions are met for switchback
            if (AutoSwitch.switchState.getHasSwitched() && !player.swinging) {
                return SwitchState.preventBlockAttack || ((doBlockSwitchback() || doMobSwitchback()) && doSwitchback());
            }
            return true;
        }

        /**
         * Cleanup switchstate after switching back.
         *
         * @param hasSwitched whether a switch has occurred
         */
        private void handlePostSwitchTasks(boolean hasSwitched) {
            if (hasSwitched) {
                AutoSwitch.switchState.setHasSwitched(false);
                AutoSwitch.switchState.setAttackedEntity(false);
            }
        }

        @Override
        public boolean invoke() {
            if (disallowSwitchback()) return false; // Shortcircuit to make it easier to read

            Optional<Boolean> x = Targetable.switchback(AutoSwitch.switchState.getPrevSlot(), player).changeTool();
            x.ifPresent(this::handlePostSwitchTasks);

            return x.orElse(true);
        }
    },
    /**
     * Event for moving a USE action item to the offhand.
     */
    OFFHAND {
        @Override
        public boolean invoke() {
            SwitchUtil.handleUseSwitchConsumer().accept(doOffhandSwitch);
            doOffhandSwitch = false;
            EventUtil.eventHandler(tickTime, 0.1, SWITCHBACK);

            return true;

        }
    },
    /**
     * When run sets {@link SwitchState#preventBlockAttack} to {@code true}, preventing block attack events from being
     * scheduled or run, and preventing switchback from running. For use during combat situations, where targeting a
     * block soon after an entity would hinder combat.
     * <br>
     * Schedules {@link SwitchEvent#REMOVE_PREVENTION}.
     */
    PREVENT_BLOCK_ATTACK {
        @Override
        public boolean invoke() {
            float delay = featureCfg.preventBlockSwitchAfterEntityAttack();
            if (delay == 0) return true;
            SwitchState.preventBlockAttack = true;

            AutoSwitch.scheduler.schedule(SwitchEvent.REMOVE_PREVENTION, delay, AutoSwitch.tickTime);
            return true;
        }
    },
    /**
     * When run sets {@link SwitchState#preventBlockAttack} to {@code false}, allowing switchback and block attack
     * events to resume.
     * <br>
     * Should only be scheduled by {@link SwitchEvent#PREVENT_BLOCK_ATTACK}.
     */
    REMOVE_PREVENTION {
        @Override
        public boolean invoke() {
            SwitchState.preventBlockAttack = false;
            return true;
        }
    },
    EVENT_TRIGGER {
        @Override
        public boolean invoke() {
            if (canNotSwitch()) return false;

            handlePrevSlot();

            Targetable.event(protoTarget, player).changeTool().ifPresent(this::handlePostSwitchTasks);

            return true;
        }

        private void handlePostSwitchTasks(boolean hasSwitched) {
            AutoSwitch.switchState.setHasSwitched(hasSwitched);
            doOffhandSwitch = doOffhand();
            if (hasSwitched) {
                EventUtil.eventHandler(AutoSwitch.tickTime, 0.1, OFFHAND);
                EventUtil.eventHandler(AutoSwitch.tickTime, featureCfg.switchbackDelay(), SWITCHBACK);
            }
        }

        private boolean doOffhand() {
            return AutoSwitch.featureCfg.putUseActionToolInOffHand() == AutoSwitchConfig.OffhandType.ALL;
        }
    };

    public static Player player;
    private static Object protoTarget;
    private static boolean doSwitchType;
    private static boolean doSwitch;
    private static boolean doOffhandSwitch;


    /**
     * Try to perform the switch.
     *
     * @return false if the switch could not be performed
     */
    public abstract boolean invoke();

    /**
     * @return whether switching should NOT occur based on current conditions.
     */
    public boolean canNotSwitch() {
        // Client is checked to fix LAN worlds (Issue #18)
        return !doSwitch || !doSwitchType || (featureCfg.disableSwitchingWhenCrouching() && player.isShiftKeyDown());
    }

    /**
     * Store the previously selected slot for use in the SWITCHBACK feature.
     */
    void handlePrevSlot() {
        if (!AutoSwitch.switchState.getHasSwitched()) {
            AutoSwitch.switchState.setPrevSlot(player.getInventory().getSelectedSlot());
        }
    }


    /**
     * Run extra checks before performing a switch.
     *
     * @return whether evaluation before switching indicates that a switch should not occur.
     */
    // Formerly used to ensure mowing control did not trigger a switch
    public boolean handlePreSwitchTasks() {
        return true;
    }


    public SwitchEvent setProtoTarget(Object protoTarget) {
        SwitchEvent.protoTarget = protoTarget;
        return this;
    }

    public SwitchEvent setPlayer(Player player) {
        SwitchEvent.player = player;
        return this;
    }

    public SwitchEvent setDoSwitchType(boolean doSwitchType) {
        SwitchEvent.doSwitchType = doSwitchType;
        return this;
    }

    public SwitchEvent setDoSwitch(boolean doAS) {
        doSwitch = doAS;
        return this;
    }
}
