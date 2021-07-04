package autoswitch.mixin.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;

/**
 * In 1.17, {@link PlayerEntity#inventory} was made private, with usage replaced by {@link PlayerEntity#getInventory()}.
 * This accessor should use that method in 1.17+, while still allowing AutoSwitch to function in 1.16.
 */
@SuppressWarnings("JavadocReference")
@Mixin(PlayerEntity.class)
public interface PlayerEntityAccessor {

    /**
     * @see PlayerEntity#inventory
     * @see PlayerEntity#getInventory()
     */
    @Accessor
    PlayerInventory getInventory();

}
