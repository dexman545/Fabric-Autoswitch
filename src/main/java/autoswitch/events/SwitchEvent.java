package autoswitch.events;

import autoswitch.AutoSwitch;
import autoswitch.Targetable;
import autoswitch.util.SwitchUtil;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.EntityHitResult;

import java.util.Objects;
import java.util.Optional;

import static autoswitch.AutoSwitch.cfg;

public enum SwitchEvent {
    ATTACK {
        @Override
        public ActionResult invoke() {
            if (!canSwitch()) return ActionResult.PASS; // Shortcircuit to make it easier to read

            if (!handlePreSwitchTasks()) return ActionResult.FAIL; // Mowing Control
            handlePrevSlot();

            Objects.requireNonNull(Targetable.of(protoTarget, player, onMP)).changeTool().ifPresent(this::handlePostSwitchTasks);

            return ActionResult.PASS;
        }
    },
    USE {
        @Override
        public ActionResult invoke() {
            if (!canSwitch()) return ActionResult.PASS; // Shortcircuit to make it easier to read

            handlePrevSlot();
            Optional<Boolean> temp = Targetable.use(protoTarget, player, onMP).changeTool();
            temp.ifPresent(b -> {
                doOffhandSwitch = true;
                if (b) AutoSwitch.scheduler.schedule(SwitchEvent.OFFHAND, 0, 0);
            });

            return ActionResult.PASS;
        }
    },
    SWITCHBACK {
        @Override
        protected boolean canSwitch() {
            if (AutoSwitch.data.getHasSwitched() && !player.handSwinging) { // Check if conditions are met for switchback
                // Uses -20.0f to give player some leeway when fighting. Use 0 for perfect timing
                return ((!AutoSwitch.data.hasAttackedEntity() || !cfg.switchbackWaits()) ||
                        (player.getAttackCooldownProgress(-20.0f) == 1.0f && AutoSwitch.data.hasAttackedEntity()));
            }

            return false;
        }

        @Override
        protected boolean handlePreSwitchTasks() {
            AutoSwitch.data.setHasSwitched(false);
            AutoSwitch.data.setAttackedEntity(false);

            return true;
        }

        @Override
        public ActionResult invoke() {
            if (!canSwitch()) return ActionResult.PASS; // Shortcircuit to make it easier to read

            handlePreSwitchTasks();
            Targetable.of(AutoSwitch.data.getPrevSlot(), player).changeTool();

            return ActionResult.PASS;
        }
    },
    OFFHAND {
        @Override
        public ActionResult invoke() {
            SwitchUtil.handleUseSwitchConsumer().accept(doOffhandSwitch);
            doOffhandSwitch = false;

            return ActionResult.PASS;
        }
    },
    HAS_SWITCHED {
        @Override
        public ActionResult invoke() {
            //hasSwitched = false;

            return ActionResult.PASS;
        }
    };

    private static Object protoTarget;
    public static PlayerEntity player;
    private static boolean clientWorld;
    private static boolean onMP;
    private static boolean doSwitchType;
    private static boolean doSwitch;
    private static boolean doOffhandSwitch;
    private static boolean hasSwitched;


    public ActionResult invoke() {
        return ActionResult.PASS;
    }

    protected boolean canSwitch() {
        return clientWorld && doSwitch && doSwitchType && player.handSwinging; // Client is checked to fix LAN worlds (Issue #18)
    }

    protected boolean setHasSwitch() {
        return hasSwitched = true;
    }

    protected void handlePrevSlot() {
        if (!AutoSwitch.data.getHasSwitched()) {
            AutoSwitch.data.setPrevSlot(player.inventory.selectedSlot);
        }
    }

    protected boolean switched() {
        return hasSwitched;
    }

    // For evaluation conditions before switch logic, mainly for mowing control
    protected boolean handlePreSwitchTasks() {
        if (protoTarget instanceof BlockState && this.equals(SwitchEvent.ATTACK)) {
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

    protected void handlePostSwitchTasks(boolean hasSwitched) {
        if (this.equals(SwitchEvent.ATTACK)) {
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
    }


    public SwitchEvent setProtoTarget(Object protoTarget) {
        this.protoTarget = protoTarget;
        return this;
    }

    public SwitchEvent setPlayer(PlayerEntity player) {
        this.player = player;
        return this;
    }

    public SwitchEvent setWorld(boolean clientWorld) {
        this.clientWorld = clientWorld;
        return this;
    }

    public SwitchEvent setOnMP(boolean onMP) {
        this.onMP = onMP;
        return this;
    }

    public SwitchEvent setDoSwitchType(boolean doSwitchType) {
        this.doSwitchType = doSwitchType;
        return this;
    }

    public SwitchEvent setDoSwitch(boolean doAS) {
        this.doSwitch = doAS;
        return this;
    }
}
