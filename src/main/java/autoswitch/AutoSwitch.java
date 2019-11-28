package autoswitch;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;


public class AutoSwitch implements ModInitializer {

    //Keybinding
    private static FabricKeyBinding keyBinding;

    private boolean doAS = true;

    @Override
    public void onInitialize() {
        System.out.println("Autoswitch Loaded");

        //Keybindings
        keyBinding = FabricKeyBinding.Builder.create(
                new Identifier("autoswitch"),
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "Autoswitch"
        ).build();

        KeyBindingRegistry.INSTANCE.register(keyBinding);
        ClientTickCallback.EVENT.register(e ->
        {
            if(keyBinding.wasPressed()) {doAS = !doAS;}
        });

        //Block Swap
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) ->
        {
            if (doAS) {
                SwitchLogic logic = new SwitchLogic();
                System.out.println(logic.changeTool(logic.toolBlockList(player, world.getBlockState(pos)), player.inventory.selectedSlot, player));
            }
            return ActionResult.PASS;
        });

        //Entity Swap
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) ->
        {
            //Initial Hotbar Slot
            int prevSlot = player.inventory.selectedSlot;

            if (doAS) {
                SwitchLogic logic = new SwitchLogic();
                logic.changeTool(logic.toolEntityList(player, entity), player.inventory.selectedSlot, player);
            }

            return ActionResult.PASS;
        });


    }
}

