package dex.autoswitch.mixin.mixins;

import dex.autoswitch.Constants;
import dex.autoswitch.config.AutoSwitchConfig;
import dex.autoswitch.engine.Action;
import dex.autoswitch.engine.TargetType;
import dex.autoswitch.engine.data.SelectionContext;
import dex.autoswitch.mixin.impl.SwitchEventTriggerImpl;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatsCounter;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StatsCounter.class)
public class MixinStatsCounter {
    /**
     * @see StatsCounter#setValue(Player, Stat, int)
     */
    @Inject(at = @At("HEAD"), method = "setValue")
    private void autoswitch$triggerEventSwitchOnStatChange(Player player, Stat<?> stat,
                                                           int value, CallbackInfo ci) {
        var profiler = Profiler.get();
        profiler.push("autoswitch:eventTrigger");
        if (Constants.CONFIG.featureConfig.switchAllowed.contains(TargetType.EVENTS) &&
                // Filters out unimportant events
                autoswitch$statRelevant(stat)) {

            SwitchEventTriggerImpl.eventTrigger(stat, player);
        }
        profiler.pop();
    }

    @Unique
    private boolean autoswitch$statRelevant(Stat<?> stat) {
        if (Constants.CONFIG.isEventRelevant(stat)) {
            return true;
        }

        for (AutoSwitchConfig.TargetEntry entry : Constants.CONFIG.statChangeAction) {
            if (entry.target.matches(0, new SelectionContext(Action.STAT_CHANGE, stat), stat).matches()) {
                Constants.CONFIG.addEventRelevant(stat);
                return true;
            }
        }

        return false;
    }
}
