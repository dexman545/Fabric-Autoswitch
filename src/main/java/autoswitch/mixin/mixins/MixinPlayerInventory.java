package autoswitch.mixin.mixins;

import java.util.List;

import autoswitch.mixin.impl.HotbarWatcher;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

@Mixin(PlayerInventory.class)
public abstract class MixinPlayerInventory {

    /**
     * @see PlayerInventory#main
     */
    @Shadow
    @Final
    public DefaultedList<ItemStack> main;

    @Unique
    private ReferenceArrayList<ItemStack> prevHotbar;

    /**
     * @see PlayerInventory#getHotbarSize()
     */
    @Shadow
    public static int getHotbarSize() {
        return 0;
    }

    /**
     * @see PlayerInventory#setStack(int, ItemStack)
     */
    @Inject(at = @At("RETURN"), method = "setStack(ILnet/minecraft/item/ItemStack;)V")
    private void autoswitch$setr(int slot, ItemStack stack, CallbackInfo ci) {
        handleHotbarUpdate(slot);
    }

    /**
     * @see PlayerInventory#removeStack(int)
     */
    @Inject(at = @At("RETURN"), method = "removeStack(I)Lnet/minecraft/item/ItemStack;")
    private void autoswitch$rmvs(int slot, CallbackInfoReturnable<ItemStack> cir) {
        handleHotbarUpdate(slot);
    }

    /**
     * If the sot changed is on the hotbar, pass to the HotbarWatcher and update the prevHotbar.
     *
     * @param slot slot changed
     */
    @Unique
    private void handleHotbarUpdate(int slot) {
        if (!PlayerInventory.isValidHotbarIndex(slot)) return;

        List<ItemStack> hb = this.main.subList(0, getHotbarSize());
        HotbarWatcher.handleSlotChange(prevHotbar, hb);
        prevHotbar = new ReferenceArrayList<>(hb);
    }

}
