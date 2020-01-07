package autoswitch;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.server.ServerStartCallback;
import net.fabricmc.fabric.api.event.server.ServerStopCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import org.aeonbits.owner.ConfigFactory;
import org.lwjgl.glfw.GLFW;

import java.io.FileOutputStream;
import java.io.IOException;


public class AutoSwitch implements ClientModInitializer {

    //Keybinding
    private static FabricKeyBinding autoswitchToggleKeybinding;
    private static FabricKeyBinding mowingWhenFightingToggleKeybinding;

    private boolean doAS = true;

    private boolean onMP = true;

    private boolean mowing = true;

    @SuppressWarnings("ConstantConditions") //removes warnings about chat message potentially have a null player
    @Override
    public void onInitializeClient() {

        //configuration
        String config = FabricLoader.getInstance().getConfigDirectory().toString() + "/autoswitch.cfg";
        String configMats = FabricLoader.getInstance().getConfigDirectory().toString() + "/autoswitchMaterials.cfg";
        ConfigFactory.setProperty("configDir", config);
        ConfigFactory.setProperty("configDirMats", configMats);
        AutoSwitchConfig cfg = ConfigFactory.create(AutoSwitchConfig.class);
        AutoSwitchMaterialConfig matCfg = ConfigFactory.create(AutoSwitchMaterialConfig.class);

        //generate config file; removes incorrect values from existing one as well
        try {
            cfg.store(new FileOutputStream(config), "AutoSwitch Configuration File" +
                    "\nSee https://github.com/dexman545/Fabric-Autoswitch/wiki/Configuration for more details");
            matCfg.store(new FileOutputStream(configMats), "AutoSwitch Material Configuration File" +
                    "\nControls which block material the tool will target" + "" +
                    "\nSee https://github.com/dexman545/Fabric-Autoswitch/wiki/Materials-Configuration for details");
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Keybindings
        autoswitchToggleKeybinding = FabricKeyBinding.Builder.create(
                new Identifier("autoswitch", "toggle"),
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "AutoSwitch"
        ).build();

        mowingWhenFightingToggleKeybinding = FabricKeyBinding.Builder.create(
                new Identifier("autoswitch", "toggle_mowing"),
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "AutoSwitch"
        ).build();

        KeyBindingRegistry.INSTANCE.addCategory("AutoSwitch");
        KeyBindingRegistry.INSTANCE.register(autoswitchToggleKeybinding);
        KeyBindingRegistry.INSTANCE.register(mowingWhenFightingToggleKeybinding);

        //create object to store player switch state
        SwitchDataStorage data = new SwitchDataStorage();

        System.out.println("AutoSwitch Loaded");

        ClientTickCallback.EVENT.register(e ->
        {

            //keybindings implementation
            if(autoswitchToggleKeybinding.wasPressed()) {
                //The toggle
                doAS = !doAS;

                if (cfg.displayToggleMsg()) {
                    //Toggle message
                    TranslatableText msg = new TranslatableText(doAS && (!onMP || cfg.switchInMP()) ? "msg.autoswitch.toggle_true" : "msg.autoswitch.toggle_false");
                    //Display msg above hotbar, set false to display in text chat
                    e.player.addChatMessage(msg, cfg.toggleMsgOverHotbar());
                }

            }

            if (mowingWhenFightingToggleKeybinding.wasPressed()) {
                mowing = !mowing;

                if (cfg.displayToggleMsg()) {
                    //Toggle message
                    TranslatableText msg = new TranslatableText(mowing || !cfg.controlMowingWhenFighting() ? "msg.autoswitch.mow_true" : "msg.autoswitch.mow_false");
                    //Display msg above hotbar, set false to display in text chat
                    e.player.addChatMessage(msg, cfg.toggleMsgOverHotbar());
                }
            }

            //Checks for implementing switchback feature
            if (e.player != null) {
                if (data.getHasSwitched() && !e.player.isHandSwinging) {
                    if ((!data.isAttackedEntity() || !cfg.switchbackWaits()) || (e.player.getAttackCooldownProgress(-20.0f) == 1.0f && data.isAttackedEntity())) { //uses -20.0f to give player some leeway when fighting. Use 0 for perfect timing
                        data.setHasSwitched(false);
                        data.setAttackedEntity(false);
                        Targetable.of(data.getPrevSlot(), e.player).changeTool();
                    }

                }
            }

        });

        //Check if the client in on a multiplayer server
        //This is only called when starting a SP world, not on server join
        ServerStartCallback.EVENT.register((minecraftServer -> onMP = false));
        //Disable onMP when leaving SP
        ServerStopCallback.EVENT.register((minecraftServer -> onMP = true));

        //Block Swap
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) ->
        {

            //Mowing control
            //disable block breaking iff mowing is disabled and there's an entity to hit
            EntityHitResult entityResult = EmptyCollisionBoxAttack.rayTraceEntity(player, 1.0F, 4.5D);
            if (entityResult != null && cfg.controlMowingWhenFighting() && !mowing) {
                player.isHandSwinging = !cfg.disableHandSwingWhenMowing();
                return ActionResult.FAIL;
            }

            //AutoSwitch handling
            if (doAS) {
                if (!data.getHasSwitched()) {data.setPrevSlot(player.inventory.selectedSlot);}
                Targetable.of(world.getBlockState(pos), player, onMP, cfg, matCfg).changeTool().ifPresent(b -> {
                    if (b && cfg.switchbackBlocks()) {data.setHasSwitched(true);}
                });

            }

            return ActionResult.PASS;
        });

        //Entity Swap
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) ->
        {

            //AutoSwitch handling
            if (doAS) {
                if (!data.getHasSwitched()) {data.setPrevSlot(player.inventory.selectedSlot);}
                Targetable.of(entity, player, onMP, cfg, matCfg).changeTool().ifPresent(b -> {
                    if (b && cfg.switchbackMobs()) {data.setHasSwitched(true); data.setAttackedEntity(true);}
                });

            }

            return ActionResult.PASS;
        });


    }
}

