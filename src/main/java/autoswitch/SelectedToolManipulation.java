package autoswitch;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

abstract class SelectedToolManipulation {
    public int changeSuccessStatus;

    static void change(int slot, PlayerEntity player) {
        new StandardSelectedToolManipulation(slot, player);
    }

    static BlockSelectedToolManipulation change(BlockState block, PlayerEntity player, AutoSwitchConfig cfg, AutoSwitchMaterialConfig matCfg) {
        return new BlockSelectedToolManipulation(block, player, cfg, matCfg);
    }

    static EntitySelectedToolManipulation change(Entity entity, PlayerEntity player, AutoSwitchConfig cfg, AutoSwitchMaterialConfig matCfg) {
        return new EntitySelectedToolManipulation(entity, player, cfg, matCfg);
    }

    public int changeTool(int slot, PlayerEntity player) {
        int currentSlot = player.inventory.selectedSlot;
        if (slot == -1) {
            //Nothing to change to!
            return -1;
        }

        if (slot == currentSlot) {
            //No need to change slot!
            return 0;
        }

        //Simulate player pressing the hotbar button, fix for setting selectedslot directly on vanilla servers
        //Loop over it since scrollinhotbar only moves one pos
        for (int i = Math.abs(currentSlot - slot); i > 0; i--){
            player.inventory.scrollInHotbar(currentSlot - slot);
        }
        //player.inventory.selectedSlot = slot;
        return 1; //Slot changed

    }

}

class StandardSelectedToolManipulation extends SelectedToolManipulation {

    public StandardSelectedToolManipulation(int slot, PlayerEntity player) {
        changeTool(slot, player);
    }
}

class BlockSelectedToolManipulation extends SelectedToolManipulation {

    public BlockSelectedToolManipulation(BlockState block, PlayerEntity player, AutoSwitchConfig cfg, AutoSwitchMaterialConfig matCfg) {
        this.changeSuccessStatus = changeTool(Targetable.of(block, player, cfg, matCfg).findSlot(), player);
    }
}

class EntitySelectedToolManipulation extends SelectedToolManipulation {

    public EntitySelectedToolManipulation(Entity entity, PlayerEntity player, AutoSwitchConfig cfg, AutoSwitchMaterialConfig matCfg) {
        this.changeSuccessStatus = changeTool(Targetable.of(entity, player, cfg, matCfg).findSlot(), player);
    }
}
