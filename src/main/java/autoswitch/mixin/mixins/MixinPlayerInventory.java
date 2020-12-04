package autoswitch.mixin.mixins;

import autoswitch.mixin.impl.HotbarWatcher;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(PlayerInventory.class)
public abstract class MixinPlayerInventory {


    @Shadow
    @Final
    public DefaultedList<ItemStack> main;
    @Unique
    private ReferenceArrayList<ItemStack> prevHotbar;

    @Shadow
    public static int getHotbarSize() {
        return 0;
    }

    @Inject(at = @At("RETURN"), method = "setStack(ILnet/minecraft/item/ItemStack;)V")
    private void setr(int slot, ItemStack stack, CallbackInfo ci) {
        List<ItemStack> hb = this.main.subList(0, getHotbarSize());
        HotbarWatcher.handleSlotChange(slot, prevHotbar, hb);
        prevHotbar = new ReferenceArrayList<>(hb);
    }

    @Inject(at = @At("RETURN"), method = "removeStack(I)Lnet/minecraft/item/ItemStack;")
    private void rmvs(int slot, CallbackInfoReturnable<ItemStack> cir) {
        List<ItemStack> hb = this.main.subList(0, getHotbarSize());
        HotbarWatcher.handleSlotChange(slot, prevHotbar, hb);
        prevHotbar = new ReferenceArrayList<>(hb);
    }

}
