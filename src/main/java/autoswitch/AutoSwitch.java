package autoswitch;

import autoswitch.config.AutoSwitchConfig;
import autoswitch.config.AutoSwitchMaterialConfig;
import autoswitch.config.AutoSwitchUsableConfig;
import autoswitch.config.io.ConfigEstablishment;
import autoswitch.config.populator.ApiMapGenerator;
import autoswitch.config.populator.AutoSwitchMapsGenerator;
import autoswitch.events.Scheduler;
import autoswitch.events.SwitchEvent;
import autoswitch.util.ApiGenUtil;
import autoswitch.util.EventUtil;
import autoswitch.util.SwitchDataStorage;
import autoswitch.util.TickUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

public class AutoSwitch implements ClientModInitializer {

    public static final Logger logger = LogManager.getLogger("AutoSwitch");

    //Create object to store player switch state
    public static final SwitchDataStorage data = new SwitchDataStorage();
    public static final Scheduler scheduler = new Scheduler();

    //Init config
    public static AutoSwitchConfig featureCfg;
    public static AutoSwitchMaterialConfig attackActionCfg;
    public static AutoSwitchUsableConfig useActionCfg;

    public static boolean mowing = true;
    public static int tickTime = 0;

    //Keybindings
    private final KeyBinding autoswitchToggleKeybinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.autoswitch.toggle",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "AutoSwitch"
    ));

    private final KeyBinding mowingWhenFightingToggleKeybinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.autoswitch.toggle_mowing",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "AutoSwitch"
    ));

    private boolean doAS = true;

    @Override
    @Environment(EnvType.CLIENT)
    public void onInitializeClient() {
        // Interface with other mods and generate needed tables
        ApiMapGenerator.createApiMaps();
        ApiGenUtil.pullHookedMods();

        // Create config files and load them
        ConfigEstablishment.establishConfigs();

        // Pull value for delayed switching
        doAS = !featureCfg.disableSwitchingOnStartup();

        // Populate data on startup
        AutoSwitchMapsGenerator.populateAutoSwitchMaps();

        ClientTickEvents.END_CLIENT_TICK.register(e -> {
            //Keybindings implementation BEGIN ---
            if (autoswitchToggleKeybinding.wasPressed()) {
                doAS = TickUtil.keybindingToggleAction(e.player, doAS, !doAS && (e.isInSingleplayer() || featureCfg.switchInMP()),
                        "msg.autoswitch.toggle_true", "msg.autoswitch.toggle_false");
            }

            if (mowingWhenFightingToggleKeybinding.wasPressed()) {
                mowing = TickUtil.keybindingToggleAction(e.player, mowing, !mowing || !featureCfg.controlMowingWhenFighting(),
                        "msg.autoswitch.mow_true", "msg.autoswitch.mow_false");
            }
            //Keybindings implementation END ---

            // Tick event system and check if scheduling a switchback is needed via EventUtil
            TickUtil.eventScheduleTick(e.player, e.world);
        });

        //Block Swaps
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) ->
                EventUtil.scheduleEvent(SwitchEvent.ATTACK, doAS, world, player, featureCfg.switchForBlocks(),
                        world.getBlockState(pos)));

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) ->
                EventUtil.scheduleEvent(SwitchEvent.USE, doAS, world, player, featureCfg.switchUseActions(),
                        world.getBlockState(hitResult.getBlockPos())));

        //Entity Swaps
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) ->
                EventUtil.scheduleEvent(SwitchEvent.ATTACK, doAS, world, player, featureCfg.switchForMobs(), entity));

        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) ->
                EventUtil.scheduleEvent(SwitchEvent.USE, doAS, world, player, featureCfg.switchUseActions(), entity));

        //Notify when AS Loaded
        logger.info("AutoSwitch Loaded");

    }
}

