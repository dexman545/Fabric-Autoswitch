package autoswitch.mixin.mixins;

import autoswitch.mixin.impl.DisconnectHandler;

import net.minecraft.client.network.ClientPlayNetworkHandler;

import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @see ClientPlayNetworkHandler
 */
@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler {

    /**
     * For GH-35
     * @see ClientPlayNetworkHandler#onGameJoin(GameJoinS2CPacket)
     */
    @Inject(method = "onGameJoin(Lnet/minecraft/network/packet/s2c/play/GameJoinS2CPacket;)V", at = @At("HEAD"))
    private void resetOnServerJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
        DisconnectHandler.reset();
    }

}
