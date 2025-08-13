package dex.autoswitch.mixin.mixins.switchback_cancel;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dex.autoswitch.Constants;
import dex.autoswitch.engine.events.SwitchEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.entity.player.Inventory;

@Mixin(ClientPacketListener.class)
public class MixinClientPacketListener {
    @WrapOperation(method = {
            "handleSetHeldSlot(Lnet/minecraft/network/protocol/game/ClientboundSetHeldSlotPacket;)V"
    }, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;setSelectedSlot(I)V"),
            require = 0)
    private void autoswitch$cancelSwitchback(Inventory instance, int slot, Operation<Void> original) {
        Constants.SCHEDULER.cancel(SwitchEvent.SWITCHBACK);
        Constants.SWITCH_STATE.reset();
        original.call(instance, slot);
    }
}
