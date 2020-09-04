package autoswitch.mixins;

import autoswitch.mixin_impl.DisconnectHandler;
import autoswitch.mixin_impl.SwitchEventTriggerImpl;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient {

    @Shadow
    @Nullable
    public HitResult crosshairTarget;

    @Shadow
    @Nullable
    public ClientPlayerEntity player;

    @Shadow
    @Nullable
    public ClientWorld world;

    @Shadow
    @Nullable
    public ClientPlayerInteractionManager interactionManager;
    @Shadow
    protected int attackCooldown;
    @Unique
    private Vec3d target;

    @Shadow
    @Nullable
    public abstract ClientPlayNetworkHandler getNetworkHandler();

    /**
     * Trigger for ATTACK event.
     */
    @Inject(at = @At("INVOKE"), method = "doAttack")
    private void attackEvent(CallbackInfo ci) {
        assert this.player != null;

        SwitchEventTriggerImpl.attack(this.attackCooldown, this.player, this.world, this.crosshairTarget);

        // Notify the server that the slot has changed
        Objects.requireNonNull(this.getNetworkHandler())
                .sendPacket(new UpdateSelectedSlotC2SPacket(this.player.inventory.selectedSlot));

    }

    /**
     * Trigger for USE event.
     */
    @Inject(at = @At("INVOKE"), method = "doItemUse")
    private void useEvent(CallbackInfo ci) {
        assert this.player != null;
        assert this.interactionManager != null;

        if (this.target == (this.crosshairTarget != null ? this.crosshairTarget.getPos() : null)) return;

        this.target = this.crosshairTarget.getPos();

        SwitchEventTriggerImpl.interact(this.interactionManager, this.player, this.world, this.crosshairTarget);

        // Notify the server that the slot has changed
        Objects.requireNonNull(this.getNetworkHandler())
                .sendPacket(new UpdateSelectedSlotC2SPacket(this.player.inventory.selectedSlot));

    }

    /**
     * Fix for doAttack only being called one the key is first pressed, this fixes not switching tools when moving
     * to a new block from the previous one, such as when the first block is broken.
     */
    @Inject(at = @At("INVOKE"), method = "handleBlockBreaking")
    private void blockAttackEventSecondary(boolean bl, CallbackInfo ci) {
        //todo see if a secondary event is needed for entities, targetedEntity and tick seem like good places to use
        //todo see if primary attack event should be removed
        if (!bl || this.crosshairTarget == null || this.crosshairTarget.getType() != HitResult.Type.BLOCK) return;

        if (this.target == this.crosshairTarget.getPos()) return;

        this.target = this.crosshairTarget.getPos();

        assert this.player != null;

        SwitchEventTriggerImpl.attack(this.attackCooldown, this.player, this.world, this.crosshairTarget);

        // Notify the server that the slot has changed
        Objects.requireNonNull(this.getNetworkHandler())
                .sendPacket(new UpdateSelectedSlotC2SPacket(this.player.inventory.selectedSlot));

    }

    /**
     * Reset keybinding params and switch state when leaving a world.
     */
    @Inject(at = @At("INVOKE"), method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V")
    private void disconnectEvent(Screen screen, CallbackInfo ci) {
        DisconnectHandler.reset();

    }


}
