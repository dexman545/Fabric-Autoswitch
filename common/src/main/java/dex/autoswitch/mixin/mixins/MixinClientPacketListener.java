package dex.autoswitch.mixin.mixins;

import dex.autoswitch.futures.FutureSelectable;
import dex.autoswitch.mixin.impl.ConnectionHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;

/**
 * @see ClientPacketListener
 */
@Mixin(ClientPacketListener.class)
public abstract class MixinClientPacketListener {
    /**
     * For GH-35
     *
     * @see ClientPacketListener#handleLogin(ClientboundLoginPacket)
     */
    @Inject(method = "handleLogin(Lnet/minecraft/network/protocol/game/ClientboundLoginPacket;)V", at = @At("RETURN"))
    private void resetOnServerJoin(ClientboundLoginPacket packet, CallbackInfo ci) {
        ConnectionHandler.reset();
        FutureSelectable.invalidateValues();
    }
}
