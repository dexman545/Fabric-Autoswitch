package autoswitch.mixin.mixins;

import autoswitch.mixin.impl.SwitchEventTriggerImpl;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerInteractionManager.class)
public class MixinClientPlayerInteractionManager {

    @Shadow
    @Final
    private MinecraftClient client;

    /**
     * Trigger the switch events.
     */
    @Inject(at = @At("HEAD"), method = "syncSelectedSlot()V")
    private void triggerSwitchOnSlotSync(CallbackInfo ci) {
        assert this.client.player != null;
        if (this.client.options.keyAttack.isPressed() || this.client.options.keyAttack.wasPressed()) {
            SwitchEventTriggerImpl.attack(0,
                    this.client.player, this.client.world, this.client.crosshairTarget);
        } else if (this.client.options.keyUse.isPressed()) {
            SwitchEventTriggerImpl.interact((ClientPlayerInteractionManager) (Object) this,
                    this.client.player, this.client.world, this.client.crosshairTarget);
        }
    }

}
