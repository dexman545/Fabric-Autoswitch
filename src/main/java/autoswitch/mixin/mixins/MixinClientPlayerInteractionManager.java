package autoswitch.mixin.mixins;

import autoswitch.mixin.impl.SwitchEventTriggerImpl;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.hit.HitResult;

@Mixin(ClientPlayerInteractionManager.class)
public class MixinClientPlayerInteractionManager {

    @Shadow
    @Final
    private MinecraftClient client;

    @Unique
    private HitResult prevTarget;

    /**
     * Trigger the switch events.
     */
    @Inject(at = @At("HEAD"), method = "syncSelectedSlot()V")
    private void autoswitch$triggerSwitchOnSlotSync(CallbackInfo ci) {
        assert this.client.player != null;
        assert this.client.crosshairTarget != null;
        if (this.client.crosshairTarget.equals(this.prevTarget)) return;
        if (this.client.options.keyAttack.isPressed() || this.client.options.keyAttack.wasPressed()) {
            SwitchEventTriggerImpl.attack(0, this.client.player, this.client.crosshairTarget);
            this.prevTarget = this.client.crosshairTarget;
        } else if (this.client.options.keyUse.isPressed() || this.client.options.keyUse.wasPressed()) {
            SwitchEventTriggerImpl.interact((ClientPlayerInteractionManager) (Object) this, this.client.player,
                                            this.client.crosshairTarget);
            this.prevTarget = this.client.crosshairTarget;
        }
    }

}
