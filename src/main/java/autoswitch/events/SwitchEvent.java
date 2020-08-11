package autoswitch.events;

import autoswitch.AutoSwitch;
import autoswitch.targetable.AbstractTargetable;
import autoswitch.util.SwitchUtil;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;

import java.util.Optional;

import static autoswitch.AutoSwitch.cfg;

public enum SwitchEvent {
    ATTACK {
        @Override
        public boolean handlePreSwitchTasks() {
            if (protoTarget instanceof BlockState) {
                //Mowing control
                //Disable block breaking iff mowing is disabled and there's an entity to hit
                EntityHitResult entityResult = SwitchUtil.rayTraceEntity(player, 1.0F, 4.5D);
                if (entityResult != null && cfg.controlMowingWhenFighting() && !AutoSwitch.mowing) {
                    player.handSwinging = !cfg.disableHandSwingWhenMowing();
                    return false;
                }
            }

            return true;
        }

        private void handlePostSwitchTasks(boolean hasSwitched) {
            // Handles switchback
            if (protoTarget instanceof Entity) {
                if (hasSwitched && cfg.switchbackMobs()) {
                    AutoSwitch.data.setHasSwitched(true);
                    AutoSwitch.data.setAttackedEntity(true);
                }
            } else if (protoTarget instanceof BlockState) {
                if (hasSwitched && cfg.switchbackBlocks()) {
                    AutoSwitch.data.setHasSwitched(true);
                }
            }
        }

        @Override
        public boolean invoke() {
            if (canNotSwitch()) return false; // Shortcircuit to make it easier to read

            if (!handlePreSwitchTasks()) return false; // Mowing Control
            handlePrevSlot();

            AbstractTargetable targetable = AbstractTargetable.attack(protoTarget, player);
            targetable.changeTool().ifPresent(this::handlePostSwitchTasks);

            return true;
        }
    },
    USE {
        @Override
        protected boolean canNotSwitch() {
            return !clientWorld || !doSwitch || !doSwitchType;
        }

        @Override
        public boolean invoke() {
            if (canNotSwitch()) return false; // Shortcircuit to make it easier to read

            handlePrevSlot();
            Optional<Boolean> temp = AbstractTargetable.use(protoTarget, player).changeTool();
            temp.ifPresent(b -> {
                doOffhandSwitch = true;
                AutoSwitch.data.setHasSwitched(b);
                if (b) AutoSwitch.scheduler.schedule(SwitchEvent.OFFHAND, 0.1, AutoSwitch.tickTime);
            });

            return true;

        }
    },
    SWITCHBACK {
        @Override
        protected boolean canNotSwitch() {
            // Check if conditions are met for switchback
            if (AutoSwitch.data.getHasSwitched() && !player.handSwinging) {
                // Uses -20.0f to give player some leeway when fighting. Use 0 for perfect timing
                return ((AutoSwitch.data.hasAttackedEntity() && cfg.switchbackWaits()) &&
                        (player.getAttackCooldownProgress(-20.0f) != 1.0f ||
                                !AutoSwitch.data.hasAttackedEntity()));
            }

            return true;
        }

        private void handlePostSwitchTasks(boolean hasSwitched) {
            if (hasSwitched) {
                AutoSwitch.data.setHasSwitched(false);
                AutoSwitch.data.setAttackedEntity(false);
            }

        }

        @Override
        public boolean invoke() {
            if (canNotSwitch()) return false; // Shortcircuit to make it easier to read

            AbstractTargetable.switchback(AutoSwitch.data.getPrevSlot(), player)
                    .changeTool().ifPresent(this::handlePostSwitchTasks);

            return true;
        }
    },
    OFFHAND {
        @Override
        public boolean invoke() {
            SwitchUtil.handleUseSwitchConsumer().accept(doOffhandSwitch);
            doOffhandSwitch = false;
            AutoSwitch.scheduler.schedule(SwitchEvent.SWITCHBACK, 0.1, AutoSwitch.tickTime);

            return true;

        }
    };

    public static PlayerEntity player;
    private static Object protoTarget;
    private static boolean clientWorld;
    private static boolean doSwitchType;
    private static boolean doSwitch;
    private static boolean doOffhandSwitch;


    public abstract boolean invoke();

    boolean canNotSwitch() {
        // Client is checked to fix LAN worlds (Issue #18)
        return !clientWorld || !doSwitch || !doSwitchType || !player.handSwinging;
    }

    void handlePrevSlot() {
        if (!AutoSwitch.data.getHasSwitched()) {
            AutoSwitch.data.setPrevSlot(player.inventory.selectedSlot);
        }
    }

    // For evaluation conditions before switch logic, mainly for mowing control
    public boolean handlePreSwitchTasks() {
        return true;
    }


    public SwitchEvent setProtoTarget(Object protoTarget) {
        SwitchEvent.protoTarget = protoTarget;
        return this;
    }

    public SwitchEvent setPlayer(PlayerEntity player) {
        SwitchEvent.player = player;
        return this;
    }

    public SwitchEvent setWorld(boolean clientWorld) {
        SwitchEvent.clientWorld = clientWorld;
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
