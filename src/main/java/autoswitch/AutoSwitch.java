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

        //create object to store player state
        SwitchDataStorage data = new SwitchDataStorage();

        System.out.println("AutoSwitch Loaded");

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


            //Checks for implementing switchback feature
            if (e.player != null) {
                if (data.getHasSwitched() && !e.player.isHandSwinging) {
                    data.setHasSwitched(false);
                    SwitchLogic logic = new SwitchLogic();
                    logic.changeTool(data.getPrevSlot(), e.player);

                }
            }

        });


        System.out.println("AutoSwitch Loaded");

        //Block Swap
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) ->
        {

            int m;
            if (!player.isCreative() || cfg.switchInCreative()) {
                if (doAS && cfg.switchForBlocks() && !onMP) {
                    if (!data.getHasSwitched()) {data.setPrevSlot(player.inventory.selectedSlot);}
                    SwitchLogic logic = new SwitchLogic();
                    m = logic.changeTool(logic.toolBlockSlot(player, world.getBlockState(pos)), player);
                    if (m == 1 && cfg.switchbackBlocks()){
                        data.setHasSwitched(true);
                    }

                }
            }

            return ActionResult.PASS;
        });

        //Entity Swap
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) ->
        {

            int m;
            if (!player.isCreative() || cfg.switchInCreative()) {
                if (doAS && cfg.switchForMobs() && !onMP) {
                    if (!data.getHasSwitched()) {data.setPrevSlot(player.inventory.selectedSlot);}
                    SwitchLogic logic = new SwitchLogic();
                    m = logic.changeTool(logic.toolEntitySlot(player, entity), player);
                    if (m == 1 && cfg.switchbackMobs()){
                        data.setHasSwitched(true);
                    }
                }
            }

            return ActionResult.PASS;
        });


    }
}

