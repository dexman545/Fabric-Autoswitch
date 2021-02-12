package autoswitch.mixin.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;

@Mixin(PlayerEntity.class)
public interface PlayerEntityAccessor {
    @Accessor
    PlayerInventory getInventory();
}
