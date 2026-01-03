package dex.autoswitch.mixin.mixins;

import dex.autoswitch.mixin.impl.ConnectionHandler;
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
     * @see Minecraft#disconnect(Screen, boolean)
     */
    @Inject(at = @At("HEAD"), method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;Z)V")
    private void autoswitch$disconnectEvent(Screen screen, boolean keepResourcePacks, CallbackInfo ci) {
        ConnectionHandler.reset();
    }
}
