package autoswitch;

import autoswitch.config.*;
import autoswitch.util.SwitchDataStorage;
import autoswitch.util.SwitchUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.server.ServerStartCallback;
import net.fabricmc.fabric.api.event.server.ServerStopCallback;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

public class AutoSwitch implements ClientModInitializer {

    public static final Logger logger = LogManager.getLogger("AutoSwitch");

    //Create object to store player switch state
    public static final SwitchDataStorage data = new SwitchDataStorage();

    //Init config
    public static AutoSwitchConfig cfg;
    public static AutoSwitchMaterialConfig matCfg;
    public static AutoSwitchUsableConfig usableCfg;

    //Keybindings
    private static FabricKeyBinding autoswitchToggleKeybinding;
    private static FabricKeyBinding mowingWhenFightingToggleKeybinding;

    private boolean doAS = true;

    private boolean onMP = true;

    private boolean mowing = true;

    @Override
    @Environment(EnvType.CLIENT)
    public void onInitializeClient() {

        // Create config files and load them
        new ConfigEstablishment();

        //Populate data on startup
        new AutoSwitchMapsGenerator();

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

        ClientTickCallback.EVENT.register(e -> {
            //Keybindings implementation BEGIN ---
            if (autoswitchToggleKeybinding.wasPressed()) {
                //The toggle
                doAS = !doAS;

                if (cfg.displayToggleMsg()) {
                    //Toggle message
                    TranslatableText msg = new TranslatableText(doAS && (!onMP || cfg.switchInMP()) ?
                            "msg.autoswitch.toggle_true" : "msg.autoswitch.toggle_false");
                    //Display msg above hotbar, set false to display in text chat
                    assert e.player != null : "Player was unexpectedly null";
                    e.player.addChatMessage(msg, cfg.toggleMsgOverHotbar());
                }

            }

            if (mowingWhenFightingToggleKeybinding.wasPressed()) {
                mowing = !mowing;

                if (cfg.displayToggleMsg()) {
                    //Toggle message
                    TranslatableText msg = new TranslatableText(mowing || !cfg.controlMowingWhenFighting() ?
                            "msg.autoswitch.mow_true" : "msg.autoswitch.mow_false");
                    //Display msg above hotbar, set false to display in text chat
                    assert e.player != null : "Player was unexpectedly null";
                    e.player.addChatMessage(msg, cfg.toggleMsgOverHotbar());
                }
            }
            //Keybindings implementation END ---

            //Checks for implementing switchback feature
            if (e.player != null) {
                if (data.getHasSwitched() && !e.player.isHandSwinging) {
                    //uses -20.0f to give player some leeway when fighting. Use 0 for perfect timing
                    if ((!data.hasAttackedEntity() || !cfg.switchbackWaits()) ||
                            (e.player.getAttackCooldownProgress(-20.0f) == 1.0f && data.hasAttackedEntity())) {
                        data.setHasSwitched(false);
                        data.setAttackedEntity(false);
                        Targetable.of(data.getPrevSlot(), e.player).changeTool();
                    }

                }
            }

        });

        //Check if the client is on a multiplayer server
        //This is only called when starting a SP world, not on server join
        ServerStartCallback.EVENT.register((minecraftServer -> onMP = false));
        //Disable onMP when leaving SP
        ServerStopCallback.EVENT.register((minecraftServer -> onMP = true));

        //Block Swap
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {

            //Mowing control
            //Disable block breaking iff mowing is disabled and there's an entity to hit
            EntityHitResult entityResult = SwitchUtil.rayTraceEntity(player, 1.0F, 4.5D);
            if (entityResult != null && cfg.controlMowingWhenFighting() && !mowing) {
                player.isHandSwinging = !cfg.disableHandSwingWhenMowing();
                return ActionResult.FAIL;
            }


            //AutoSwitch handling
            if (doAS && cfg.switchForBlocks()) {
                if (!data.getHasSwitched()) {
                    data.setPrevSlot(player.inventory.selectedSlot);
                }
                Targetable.of(world.getBlockState(pos), player, onMP).changeTool().ifPresent(b -> {
                    //Handles whether or not switchback is desired as well
                    if (b && cfg.switchbackBlocks()) {
                        data.setHasSwitched(true);
                    }

                });

            }

            return ActionResult.PASS;
        });

        //Entity Swap
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {

            //AutoSwitch handling
            if (doAS && cfg.switchForMobs()) {
                if (!data.getHasSwitched()) {
                    data.setPrevSlot(player.inventory.selectedSlot);
                }
                Targetable.of(entity, player, onMP).changeTool().ifPresent(b -> {
                    //Handles whether or not switchback is desired as well
                    if (b && cfg.switchbackMobs()) {
                        data.setHasSwitched(true);
                        data.setAttackedEntity(true);
                    }
                });

            }

            return ActionResult.PASS;
        });

        UseEntityCallback.EVENT.register((player, world, hand, entity, entityHitResult) -> {

            if (doAS && cfg.switchUseActions()) {
                if (!data.getHasSwitched()) {
                    data.setPrevSlot(player.inventory.selectedSlot);
                }
                Targetable.use(entity, player, onMP).changeTool().ifPresent(SwitchUtil.handleUseSwitchConsumer());

            }

            return ActionResult.PASS;
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {

            if (doAS && cfg.switchUseActions()) {
                if (!data.getHasSwitched()) {
                    data.setPrevSlot(player.inventory.selectedSlot);
                }
                Targetable.use(world.getBlockState(hitResult.getBlockPos()).getBlock(), player, onMP)
                        .changeTool().ifPresent(SwitchUtil.handleUseSwitchConsumer());

            }

            return ActionResult.PASS;
        });

        //Notify when AS Loaded
        logger.info("AutoSwitch Loaded");

    }
}

