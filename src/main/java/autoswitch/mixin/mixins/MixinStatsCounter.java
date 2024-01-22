package autoswitch.mixin.mixins;

import autoswitch.AutoSwitch;
import autoswitch.config.AutoSwitchConfig;
import autoswitch.mixin.impl.SwitchEventTriggerImpl;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatsCounter;
import net.minecraft.world.entity.player.Player;

@Mixin(StatsCounter.class)
public class MixinStatsCounter {
    /**
     * @see StatsCounter#setValue(Player, Stat, int)
     */
    @Inject(at = @At("HEAD"), method = "setValue")
    private void autoswitch$triggerEventSwitchOnStatChange(Player player, Stat<?> stat,
                                                           int value, CallbackInfo ci) {
        if (AutoSwitch.featureCfg.switchAllowed().contains(AutoSwitchConfig.TargetType.EVENTS) &&
            // Filters out unimportant events - most importantly the world tick ones for play time,
            // as they fill the event queue to the point of preventing switchback
            AutoSwitch.switchData.targets.containsValue(stat)) {

            // Cannot use provided player as switching does not work
            SwitchEventTriggerImpl.eventTrigger(stat, Minecraft.getInstance().player);
        }
    }
}
