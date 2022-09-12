package autoswitch.mixin.mixins;

import autoswitch.AutoSwitch;
import autoswitch.config.AutoSwitchConfig;
import autoswitch.mixin.impl.SwitchEventTriggerImpl;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;

@Mixin(StatHandler.class)
public class MixinStatHandler {
    @Inject(at = @At("HEAD"), method = "setStat")
    private void autoswitch$triggerEventSwitchOnStatChange(PlayerEntity player, Stat<?> stat,
                                                           int value, CallbackInfo ci) {
        if (AutoSwitch.featureCfg.switchAllowed().contains(AutoSwitchConfig.TargetType.EVENTS) &&
            // Filters out unimportant events - most importantly the world tick ones for play time,
            // as they fill the event queue to the point of preventing switchback
            AutoSwitch.switchData.targets.containsValue(stat)) {

            // Cannot use provided player as switching does not work
            SwitchEventTriggerImpl.eventTrigger(stat, MinecraftClient.getInstance().player);
        }
    }
}
