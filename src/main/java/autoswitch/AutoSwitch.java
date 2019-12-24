package autoswitch;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.aeonbits.owner.ConfigFactory;
import org.lwjgl.glfw.GLFW;

import java.io.FileOutputStream;
import java.io.IOException;


public class AutoSwitch implements ClientModInitializer {

    //Keybinding
    private static FabricKeyBinding keyBinding;

    private boolean doAS = true;

    private boolean onMP = false;

    @Override
    public void onInitializeClient() {
        System.out.println("AutoSwitch Loaded");

        //configuration
        String config = FabricLoader.getInstance().getConfigDirectory().toString() + "/autoswitch.cfg";
        ConfigFactory.setProperty("configDir", config);
        AutoSwitchConfig cfg = ConfigFactory.create(AutoSwitchConfig.class);

        //generate config file; removes incorrect values from existing one as well
        try {
            cfg.store(new FileOutputStream(config), "AutoSwitch Configuration File" +
                    "\nSee https://github.com/dexman545/Fabric-Autoswitch/wiki/Configuration for more details");
        } catch (IOException e) {
            e.printStackTrace();
        }

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
            //check if client is on a server or not
            if (!cfg.switchInMP()) {
                if (e.getGame().getCurrentSession() != null) {
                    onMP = e.getGame().getCurrentSession().isRemoteServer();
                }
            }

            //keybinding implementation
            if(keyBinding.wasPressed()) {
                //The toggle
                doAS = !doAS;

                if (cfg.displayToggleMsg()) {
                    //Toggle message
                    TranslatableText msg = new TranslatableText(doAS && !onMP ? "msg.autoswitch.toggle_true" : "msg.autoswitch.toggle_false");
                    //Display msg above hotbar, set false to display in text chat
                    e.player.addChatMessage(msg, cfg.toggleMsgOverHotbar());
                }
            }

        });

        //Block Swap
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) ->
        {

            if (!player.isCreative() || cfg.switchInCreative()) {
                if (doAS && cfg.switchForBlocks() && !onMP) {
                    SwitchLogic logic = new SwitchLogic();
                    logic.changeTool(logic.toolBlockList(player, world.getBlockState(pos)), player.inventory.selectedSlot, player);
                }
            }

            return ActionResult.PASS;
        });

        //Entity Swap
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) ->
        {

            if (!player.isCreative() || cfg.switchInCreative()) {
                if (doAS && cfg.switchForMobs() && !onMP) {
                    SwitchLogic logic = new SwitchLogic();
                    logic.changeTool(logic.toolEntityList(player, entity), player.inventory.selectedSlot, player);
                }
            }
            return ActionResult.PASS;
        });


    }
}

