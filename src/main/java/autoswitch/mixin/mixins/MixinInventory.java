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

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

@Mixin(Inventory.class)
public abstract class MixinInventory {

    /**
     * @see Inventory#items
     */
    @Shadow
    @Final
    public NonNullList<ItemStack> items;

    @Unique
    private ReferenceArrayList<ItemStack> prevHotbar;

    /**
     * @see Inventory#getSelectionSize()
     */
    @Shadow
    public static int getSelectionSize() {
        return 0;
    }

    /**
     * @see Inventory#setItem(int, ItemStack)
     */
    @Inject(at = @At("RETURN"), method = "setItem(ILnet/minecraft/world/item/ItemStack;)V")
    private void autoswitch$setr(int slot, ItemStack stack, CallbackInfo ci) {
        handleHotbarUpdate(slot);
    }

    /**
     * @see Inventory#removeItemNoUpdate(int)
     */
    @Inject(at = @At("RETURN"), method = "removeItemNoUpdate(I)Lnet/minecraft/world/item/ItemStack;")
    private void autoswitch$rmvs(int slot, CallbackInfoReturnable<ItemStack> cir) {
        handleHotbarUpdate(slot);
    }


    //todo these injects don't seem to cover everything anymore
    //  instead, redirct inventory creation to one that track the changes? that seems dangerous
    /**
     * If the sot changed is on the hotbar, pass to the HotbarWatcher and update the prevHotbar.
     *
     * @param slot slot changed
     */
    @Unique
    private void handleHotbarUpdate(int slot) {
        if (!Inventory.isHotbarSlot(slot)) return;

        List<ItemStack> hb = this.items.subList(0, getSelectionSize());
        HotbarWatcher.handleSlotChange(prevHotbar, hb);
        prevHotbar = new ReferenceArrayList<>(hb);
    }

}
