package dex.autoswitch.mixin.mixins.switchback_cancel;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dex.autoswitch.Constants;
import dex.autoswitch.engine.events.SwitchEvent;
import net.minecraft.client.MouseHandler;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MouseHandler.class)
public class MixinMouseHandler {
    @WrapOperation(method = {
            "onScroll(JDD)V"
    }, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;setSelectedSlot(I)V"),
            require = 0)
    private void autoswitch$cancelSwitchback(Inventory instance, int slot, Operation<Void> original) {
        Constants.SCHEDULER.cancel(SwitchEvent.SWITCHBACK);
        original.call(instance, slot);
    }
}
