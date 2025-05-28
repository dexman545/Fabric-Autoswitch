package dex.autoswitch.engine.types;

import dex.autoswitch.engine.data.extensible.PlayerInventory;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record SwitchedPlayer(Player player) implements PlayerInventory<ItemStack> {
    @Override
    public void selectSlot(int slot) {
        player.getInventory().setSelectedSlot(slot);
    }

    @Override
    public int currentSelectedSlot() {
        return player.getInventory().getSelectedSlot();
    }

    @Override
    public int slotCount() {
        return Inventory.getSelectionSize();
    }

    @Override
    public ItemStack getTool(int slot) {
        return player.getInventory().getItem(slot);
    }

    @Override
    public boolean canSwitchBack() {
        var gm = Minecraft.getInstance().gameMode;
        if (gm == null) {
            return false;
        }

        return (!gm.isDestroying() && !player.isUsingItem()) && (player.getAttackStrengthScale(-20f) == 1.0f) && !player.swinging;
    }

    @Override
    public void moveOffhand() {
        var mc = Minecraft.getInstance();
        //noinspection ConstantValue
        if (mc != null) {
            var con = mc.getConnection();
            if (con != null) {
                con.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND,
                        BlockPos.ZERO, Direction.DOWN));
            }
        }
    }
}
