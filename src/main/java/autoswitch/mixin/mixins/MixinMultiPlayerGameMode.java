package autoswitch.mixin.mixins;

import autoswitch.mixin.impl.SwitchEventTriggerImpl;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.phys.HitResult;

@SuppressWarnings("JavadocReference")
@Mixin(MultiPlayerGameMode.class)
public abstract class MixinMultiPlayerGameMode {

    /**
     * @see MultiPlayerGameMode#minecraft
     */
    @Shadow
    @Final
    private Minecraft minecraft;

    /**
     * @see MultiPlayerGameMode#isDestroying
     */
    @Shadow
    private boolean isDestroying;

    @Unique
    private HitResult prevTarget;

    /**
     * Trigger the switch events.
     *
     * @see MultiPlayerGameMode#ensureHasSentCarriedItem()
     */
    @Inject(at = @At("HEAD"), method = "ensureHasSentCarriedItem()V")
    private void autoswitch$triggerSwitchOnSlotSync(CallbackInfo ci) {
        assert this.minecraft.player != null;
        assert this.minecraft.hitResult != null;
        if (this.minecraft.hitResult.equals(this.prevTarget)) return;
        if (this.minecraft.options.keyAttack.isDown()) {
            SwitchEventTriggerImpl.attack(0, this.minecraft.player, this.minecraft.hitResult);
            this.prevTarget = this.minecraft.hitResult;
        } else if (this.minecraft.options.keyUse.isDown()) {
            SwitchEventTriggerImpl.interact(this.isDestroying, this.minecraft.player, this.minecraft.hitResult);
            this.prevTarget = this.minecraft.hitResult;
        }
    }

}
