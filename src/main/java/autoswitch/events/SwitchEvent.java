package autoswitch.events;

import autoswitch.AutoSwitch;
import autoswitch.Targetable;
import autoswitch.util.SwitchUtil;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;

import java.util.Objects;
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
        public void invoke() {
            if (!canSwitch()) return; // Shortcircuit to make it easier to read

            if (!handlePreSwitchTasks()) return; // Mowing Control
            handlePrevSlot();

            Targetable targetable;
            if ((targetable = Targetable.of(protoTarget, player, onMP)) != null) {
                targetable.changeTool().ifPresent(this::handlePostSwitchTasks);
            }

        }
    },
    USE {
        @Override
        public void invoke() {
            if (!canSwitch()) return; // Shortcircuit to make it easier to read

            handlePrevSlot();
            Optional<Boolean> temp = Targetable.use(protoTarget, player, onMP).changeTool();
            temp.ifPresent(b -> {
                doOffhandSwitch = true;
                if (b) AutoSwitch.scheduler.schedule(SwitchEvent.OFFHAND, 0, 0);
            });

        }
    },
    SWITCHBACK {
        @Override
        protected boolean canSwitch() {
            // Check if conditions are met for switchback
            if (AutoSwitch.data.getHasSwitched() && !player.handSwinging) {
                // Uses -20.0f to give player some leeway when fighting. Use 0 for perfect timing
                return ((!AutoSwitch.data.hasAttackedEntity() || !cfg.switchbackWaits()) ||
                        (player.getAttackCooldownProgress(-20.0f) == 1.0f &&
                                AutoSwitch.data.hasAttackedEntity()));
            }

            return false;
        }

        private void handlePostSwitchTasks(boolean hasSwitched) {
            if (hasSwitched) {
                AutoSwitch.data.setHasSwitched(false);
                AutoSwitch.data.setAttackedEntity(false);
            }

        }

        @Override
        public void invoke() {
            if (!canSwitch()) return; // Shortcircuit to make it easier to read

            Targetable.of(AutoSwitch.data.getPrevSlot(), player).changeTool().ifPresent(this::handlePostSwitchTasks);

        }
    },
    OFFHAND {
        @Override
        public void invoke() { //todo fix breaking switchback after invocation
            SwitchUtil.handleUseSwitchConsumer().accept(doOffhandSwitch);
            doOffhandSwitch = false;
            AutoSwitch.scheduler.schedule(SwitchEvent.SWITCHBACK, 0, 0);

        }
    };

    private static Object protoTarget;
    public static PlayerEntity player;
    private static boolean clientWorld;
    private static boolean onMP;
    private static boolean doSwitchType;
    private static boolean doSwitch;
    private static boolean doOffhandSwitch;


    public void invoke() {
    }

    protected boolean canSwitch() {
        // Client is checked to fix LAN worlds (Issue #18)
        return clientWorld && doSwitch && doSwitchType && player.handSwinging;
    }

    protected void handlePrevSlot() {
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

    public SwitchEvent setOnMP(boolean onMP) {
        SwitchEvent.onMP = onMP;
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
