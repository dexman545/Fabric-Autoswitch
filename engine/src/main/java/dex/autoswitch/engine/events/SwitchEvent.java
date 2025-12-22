package dex.autoswitch.engine.events;

import dex.autoswitch.config.subentries.FeatureConfig;
import dex.autoswitch.engine.data.SelectionContext;
import dex.autoswitch.engine.data.SwitchRegistry;
import dex.autoswitch.engine.state.SwitchContext;
import dex.autoswitch.engine.state.SwitchState;

public enum SwitchEvent {
    /**
     * Event triggered for attack actions. Captures the currently selected slot,
     * attempts a slot switch for the target, and if a switch occurred,
     * schedules a subsequent {@link #SWITCHBACK}.
     */
    ATTACK {
        @Override
        public boolean perform(SwitchContext ctx) {
            handlePrevSlot(ctx);

            ctx.findSlot().ifPresent(switched -> handlePostAttack(ctx, switched));

            return true;
        }

        /**
         * Setup SwitchState for switchback feature.
         *
         * @param switched whether a switch has occurred
         */
        private void handlePostAttack(SwitchContext ctx, boolean switched) {
            if (!switched) return;

            var type = SwitchRegistry.INSTANCE.getSelectableType(ctx.target());
            if (type != null && ctx.config().featureConfig.switchBackAllowed.contains(type.targetType())) {
                scheduleSwitchback(ctx);
            }
        }
    },
    /**
     * Event triggered for interact actions. Captures the currently selected slot,
     * attempts a slot switch for the target, and if a switch occurred,
     * schedules subsequent {@link #OFFHAND} and {@link #SWITCHBACK} events.
     */
    INTERACT {
        @Override
        public boolean perform(SwitchContext ctx) {
            handlePrevSlot(ctx);

            ctx.findSlot().ifPresent(switched -> handlePostUse(ctx, switched));

            return true;
        }

        /**
         * Setup SwitchState for switchback feature.
         *
         * @param switched whether a switch has occurred
         */
        private void handlePostUse(SwitchContext ctx, boolean switched) {
            //ctx.doOffhandSwitch = shouldOffhand(ctx);
            if (switched) {
                scheduleOffhand(ctx, 0);
                scheduleSwitchback(ctx);
            }
        }
    },
    /**
     * Event for moving a {@link SwitchEvent#INTERACT} action item to the offhand.
     */
    OFFHAND {
        @Override
        public boolean perform(SwitchContext ctx) {
            for (FeatureConfig.OffhandSelector selector : ctx.config().featureConfig.offhandSelectors) {
                if (selector.type == ctx.target()) {
                    if (selector.tool != null) {
                        var c = new SelectionContext(ctx.action(), ctx.target(), ctx.attachments());
                        var tool = ctx.player().getTool(ctx.player().currentSelectedSlot());
                        var m = selector.tool.matches(0, c, tool);
                        if (m.matches()) {
                            ctx.player().moveOffhand();
                            return true;
                        }
                    }
                }
            }

            // Always remove offhand task
            return true;
        }
    },
    /**
     * Event for returning to the previously selected slot once conditions that triggered a switch are over.
     */
    SWITCHBACK {
        @Override
        public boolean perform(SwitchContext ctx) {
            if (disallowSwitchback(ctx)) return false;
            if (ctx.switchSlot(ctx.switchState().getPrevSlot())) {
                handlePostSwitchback(ctx);
                return true;
            }

            return false;
        }

        private boolean disallowSwitchback(SwitchContext ctx) {
            if (!ctx.player().canSwitchBack(ctx)) return true;

            if (ctx.switchState().preventBlockAttack()) {
                return true;
            }

            if (!ctx.switchState().awaitingSwitchback()) {
                return true;
            }

            var type = SwitchRegistry.INSTANCE.getSelectableType(ctx.target());

            if (type != null) {
                return !ctx.config().featureConfig.switchBackAllowed.contains(type.targetType());
            }

            return false;
        }

        /**
         * Cleanup switchstate after switching back.
         */
        private void handlePostSwitchback(SwitchContext ctx) {
            ctx.switchState().setAwaitingSwitchback(false);
        }
    },
    /**
     * When run sets {@link SwitchState#preventBlockAttack()} to {@code true}, preventing block attack events from being
     * scheduled or run, and preventing switchback from running. For use during combat situations, where targeting a
     * block soon after an entity would hinder combat.
     * <br>
     * Schedules {@link SwitchEvent#REMOVE_PREVENTION}.
     */
    PREVENT_BLOCK_ATTACK {
        @Override
        public boolean perform(SwitchContext ctx) {
            var delay = ctx.config().featureConfig.preventBlockSwitchAfterEntityAttack;
            if (delay <= 0) return true;
            ctx.switchState().setPreventBlockAttack(true);
            ctx.scheduler().schedule(REMOVE_PREVENTION, ctx, delay);
            return true;
        }
    },
    /**
     * When run sets {@link SwitchState#preventBlockAttack()} to {@code false}, allowing switchback and block attack
     * events to resume.
     * <br>
     * Should only be scheduled by {@link SwitchEvent#PREVENT_BLOCK_ATTACK}.
     */
    REMOVE_PREVENTION {
        @Override
        public boolean perform(SwitchContext ctx) {
            ctx.switchState().setPreventBlockAttack(false);
            return true;
        }
    },
    /**
     * Event triggered on player statistic change that may affect.
     * Captures the current slot, attempts a switch,
     * schedules {@link #OFFHAND} (after a short delay) and {@link #SWITCHBACK}.
     */
    STAT_CHANGE {
        @Override
        public boolean perform(SwitchContext ctx) {
            handlePrevSlot(ctx);

            ctx.findSlot().ifPresent(switched -> handlePostEvent(ctx, switched));
            return true;
        }

        private void handlePostEvent(SwitchContext ctx, boolean switched) {
            ctx.switchState().setDoOffhandSwitch(true);
            if (switched) {
                scheduleOffhand(ctx, 20);
                scheduleSwitchback(ctx);
            }
        }
    };

    /**
     * Execute this event using the provided context.
     *
     * @param ctx execution context containing player access, current target/action, configuration,
     *            scheduler, and the mutable {@link SwitchState}
     * @return {@code true} if the event completed successfully (even if no switch occurred), {@code false} when an
     *         event explicitly aborts its action (e.g., {@link #SWITCHBACK} when disallowed).
     *         A value of {@code true} indicates the event was handled successfully,
     *         and should be removed from the scheduler.
     */
    public abstract boolean perform(SwitchContext ctx);

    /**
     * Records the currently selected slot into {@link SwitchState} so that a later {@link #SWITCHBACK} can restore it.
     */
    protected void handlePrevSlot(SwitchContext ctx) {
        ctx.switchState().setPrevSlot(ctx.player().currentSelectedSlot());
    }

    /**
     * Enqueues {@link #SWITCHBACK} using the configured delay.
     * <p>
     * This is typically called only after a successful switch and when switch-back is permitted for the current target.
     */
    protected void scheduleSwitchback(SwitchContext ctx) {
        ctx.scheduler().schedule(SWITCHBACK, ctx, ctx.config().featureConfig.switchbackDelay);
    }

    /**
     * Enqueues {@link #OFFHAND} for the resolved target, if any.
     *
     * @param delayTicks scheduler delay in ticks before attempting to offhand
     */
    protected void scheduleOffhand(SwitchContext ctx, int delayTicks) {
        var type = SwitchRegistry.INSTANCE.getSelectableType(ctx.target());
        if (type != null) {
            ctx.scheduler().schedule(OFFHAND, ctx.withTarget(type.targetType()), delayTicks);
        }
    }
}
