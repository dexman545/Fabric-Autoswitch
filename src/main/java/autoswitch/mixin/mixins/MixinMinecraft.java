package autoswitch.mixin.mixins;

import autoswitch.mixin.impl.ConnectionHandler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

    /**
     * Reset keybinding params and switch state when leaving a world.
     *
     * @see Minecraft#disconnect(Screen)
     */
    @Inject(at = @At("HEAD"), method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;)V")
    private void autoswitch$disconnectEvent(Screen screen, CallbackInfo ci) {
        ConnectionHandler.reset();

    }


}
