package autoswitch;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;


public class AutoSwitch implements ClientModInitializer {

    //Keybinding
    private static FabricKeyBinding keyBinding;

    private boolean doAS = true;

    @Override
    public void onInitializeClient() {
        System.out.println("AutoSwitch Loaded");

        //Keybindings
        keyBinding = FabricKeyBinding.Builder.create(
                new Identifier("autoswitch", "toggle"),
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "AutoSwitch"
        ).build();

        KeyBindingRegistry.INSTANCE.addCategory("AutoSwitch");
        KeyBindingRegistry.INSTANCE.register(keyBinding);
        ClientTickCallback.EVENT.register(e ->
        {
            if(keyBinding.wasPressed()) {
                //The toggle
                doAS = !doAS;

                //Toggle message
                TranslatableText msg = new TranslatableText(doAS ? "msg.autoswitch.toggle_true" : "msg.autoswitch.toggle_false");
                //Display msg above hotbar, set false to display in text chat
                e.player.addChatMessage(msg, true);
            }

        });

        //Block Swap
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) ->
        {

            if (doAS) {
                SwitchLogic logic = new SwitchLogic();
                logic.changeTool(logic.toolBlockList(player, world.getBlockState(pos)), player.inventory.selectedSlot, player);
            }

            return ActionResult.PASS;
        });

        //Entity Swap
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) ->
        {

            if (doAS) {
                SwitchLogic logic = new SwitchLogic();
                logic.changeTool(logic.toolEntityList(player, entity), player.inventory.selectedSlot, player);
            }

            return ActionResult.PASS;
        });


    }
}

