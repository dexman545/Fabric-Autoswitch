package dex.autoswitch.mixin.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.resources.ResourceLocation;

@Mixin(net.minecraft.client.gui.components.debug.DebugScreenEntries.class)
public interface DebugScreenEntriesAccessor {
    @Invoker
    static ResourceLocation callRegister(ResourceLocation p_435372_, DebugScreenEntry p_433580_) {
        throw new UnsupportedOperationException();
    }
}
