package autoswitch;

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
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.function.Consumer;

public class AutoSwitch implements ClientModInitializer {

    public static final Logger logger = LogManager.getLogger("AutoSwitch");

    //Create object to store player switch state
    public static final SwitchDataStorage data = new SwitchDataStorage();

    //Init config
    public static AutoSwitchConfig cfg;
    public static AutoSwitchMaterialConfig matCfg;

    //Keybindings
    private static FabricKeyBinding autoswitchToggleKeybinding;
    private static FabricKeyBinding mowingWhenFightingToggleKeybinding;

    private boolean doAS = true;

    private boolean onMP = true;

    private boolean mowing = true;

    @Override
    @Environment(EnvType.CLIENT)
    public void onInitializeClient() {

        //Configuration BEGIN ---
        String config = FabricLoader.getInstance().getConfigDirectory().toString() + "/autoswitch.cfg";
        String configMats = FabricLoader.getInstance().getConfigDirectory().toString() + "/autoswitchMaterials.cfg";
        ConfigFactory.setProperty("configDir", config);
        ConfigFactory.setProperty("configDirMats", configMats);
        cfg = ConfigFactory.create(AutoSwitchConfig.class);
        matCfg = ConfigFactory.create(AutoSwitchMaterialConfig.class);

        //generate config file; removes incorrect values from existing one as well
        try {
            cfg.store(new FileOutputStream(config), "AutoSwitch Configuration File" +
                    "\nSee https://github.com/dexman545/Fabric-Autoswitch/wiki/Configuration for more details" +
                    "\nTool priority order values must match exactly with what is in the material config, both tool and enchantment");
            matCfg.store(new FileOutputStream(configMats), "AutoSwitch Material Configuration File" +
                    "\nformat is a comma separated list of 'toolname[;enchantment id]', where toolname is any:" +
                    "\n\t any, pickaxe, shears, axe, shovel, hoe, trident, sword, or a specific item id, with same formatting rules as enchantments" +
                    "\nEnchant id is optional. If present, it must be separated from the tool by a semicolon (';')" +
                    "\nEnchant id uses '-' instead of colons. A colon can be used, but must be preceded by a backslash" +
                    "\nList is ordered and will effect tool selection" +
                    "\n'useTool' is for the right-click action of the player. Format: 'targetID;toolname' no support for enchantments. No repeats.");
        } catch (IOException e) {
            logger.error("AutoSwitch failed to obtain the configs during writing!");
            logger.error(e);
        }

        //Clear data and recreate it based on new config
        matCfg.addReloadListener(event -> {
            data.toolTargetLists.clear();
            data.enchantToolMap.clear();
            data.useMap.clear();
            data.toolTargetLists = new AutoSwitchLists().getToolTargetLists();
        });

        cfg.addReloadListener(event -> {
            data.toolLists.clear();
            data.toolLists = new AutoSwitchLists().getToolLists();
        });

        //Configuration END ---

        //Populate data on startup
        data.toolTargetLists = new AutoSwitchLists().getToolTargetLists();
        data.toolLists = new AutoSwitchLists().getToolLists();

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
            if(autoswitchToggleKeybinding.wasPressed()) {
                //The toggle
                doAS = !doAS;

                if (cfg.displayToggleMsg()) {
                    //Toggle message
                    TranslatableText msg = new TranslatableText(doAS && (!onMP || cfg.switchInMP()) ?
                            "msg.autoswitch.toggle_true" : "msg.autoswitch.toggle_false");
                    //Display msg above hotbar, set false to display in text chat
                    assert e.player != null : "Player was unexpectedly null";
                    e.player.sendMessage(msg, cfg.toggleMsgOverHotbar());
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
                    e.player.sendMessage(msg, cfg.toggleMsgOverHotbar());
                }
            }
            //Keybindings implementation END ---

            //Checks for implementing switchback feature
            if (e.player != null) {
                if (data.getHasSwitched() && !e.player.handSwinging) {
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
            EntityHitResult entityResult = EmptyCollisionBoxAttack.rayTraceEntity(player, 1.0F, 4.5D);
            if (entityResult != null && cfg.controlMowingWhenFighting() && !mowing) {
                player.handSwinging = !cfg.disableHandSwingWhenMowing();
                return ActionResult.FAIL;
            }


            //AutoSwitch handling
            if (doAS) {
                if (!data.getHasSwitched()) {data.setPrevSlot(player.inventory.selectedSlot);}
                Targetable.of(world.getBlockState(pos), player, onMP).changeTool().ifPresent(b -> {
                    //Handles whether or not switchback is desired as well
                    if (b && cfg.switchbackBlocks()) {data.setHasSwitched(true);}

                });

            }

            return ActionResult.PASS;
        });

        //Entity Swap
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {

            //AutoSwitch handling
            if (doAS) {
                if (!data.getHasSwitched()) {data.setPrevSlot(player.inventory.selectedSlot);}
                Targetable.of(entity, player, onMP).changeTool().ifPresent(b -> {
                    //Handles whether or not switchback is desired as well
                    if (b && cfg.switchbackMobs()) {data.setHasSwitched(true); data.setAttackedEntity(true);}
                });

            }

            return ActionResult.PASS;
        });

        UseEntityCallback.EVENT.register((player, world, hand, entity, entityHitResult) -> {

            if (doAS) {
                if (!data.getHasSwitched()) {data.setPrevSlot(player.inventory.selectedSlot);}
                Targetable.use(entity, player, onMP).changeTool().ifPresent(handleUseSwitchConsumer());

            }

            return ActionResult.PASS;
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {

            if (doAS) {
                if (!data.getHasSwitched()) {data.setPrevSlot(player.inventory.selectedSlot);}
                Targetable.use(world.getBlockState(hitResult.getBlockPos()).getBlock(), player, onMP)
                        .changeTool().ifPresent(handleUseSwitchConsumer());

            }

            return ActionResult.PASS;
        });

        //Notify when AS Loaded
        logger.info("AutoSwitch Loaded");

    }

    /**
     * @return Consumer to handle mob switchback and moving of stack to offhand
     */
    @Environment(EnvType.CLIENT)
    Consumer<Boolean> handleUseSwitchConsumer() {
        return b -> {
            if (b && cfg.switchbackMobs()) {
                data.setHasSwitched(true);
                data.setAttackedEntity(true);
            }

            if (b && cfg.putUseActionToolInOffHand()) {
                assert  MinecraftClient.getInstance().getNetworkHandler() != null :
                        "Minecraft client was null when AutoSwitch wanted to sent a packet!";
                MinecraftClient.getInstance().getNetworkHandler().sendPacket(
                        new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_HELD_ITEMS,
                                BlockPos.ORIGIN, Direction.DOWN));
            }
        };
    }
}

