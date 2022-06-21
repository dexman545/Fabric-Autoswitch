package autoswitch;

import java.lang.reflect.Field;

import autoswitch.compat.autoswitch_api.impl.ApiGenUtil;
import autoswitch.compat.autoswitch_api.impl.ApiMapGenerator;
import autoswitch.config.AutoSwitchAttackActionConfig;
import autoswitch.config.AutoSwitchConfig;
import autoswitch.config.AutoSwitchUseActionConfig;
import autoswitch.config.commands.CommandConductor;
import autoswitch.config.io.ConfigEstablishment;
import autoswitch.config.populator.AutoSwitchMapsGenerator;
import autoswitch.events.Scheduler;
import autoswitch.selectors.selectable.Selectables;
import autoswitch.selectors.selectable.WorldInterfaces;
import autoswitch.util.SwitchData;
import autoswitch.util.SwitchState;
import autoswitch.util.TickUtil;

import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

import net.minecraft.block.Material;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.EntityGroup;

public class AutoSwitch implements ClientModInitializer {

    public static final Logger logger = LoggerFactory.getLogger("AutoSwitch");
    public static final SwitchData switchData = new SwitchData();
    public static final Scheduler scheduler = new Scheduler();
    // Create object to store player switch state and relevant data
    public static SwitchState switchState = new SwitchState();
    // Init config
    public static AutoSwitchConfig featureCfg;
    public static AutoSwitchAttackActionConfig attackActionCfg;
    public static AutoSwitchUseActionConfig useActionCfg;
    public static int tickTime = 0;
    public static boolean doAS = true;

    // Constant damage threshold
    public static final int damageThreshold = 3;

    // Keybindings
    private final KeyBinding autoswitchToggleKeybinding = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key.autoswitch.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "AutoSwitch"));

    @Override
    public void onInitializeClient() {
        // Interface with other mods and generate needed tables
        ApiMapGenerator.createApiMaps();
        ApiGenUtil.pullHookedMods();

        // Create config files and load them
        ConfigEstablishment.establishConfigs();

        try {
            CommandConductor.registerAllCommands();
        } catch (Exception e) {
            logger.error("Failed to register client commands.", e);
        }

        WorldInterfaces.registerVanilla();
        Selectables.registerVanilla();

        // Pull value for delayed switching
        doAS = !featureCfg.disableSwitchingOnStartup();

        // Populate data on startup
        AutoSwitchMapsGenerator.populateAutoSwitchMaps();

        ClientTickEvents.END_CLIENT_TICK.register(e -> {
            // Keybindings implementation BEGIN ---
            if (autoswitchToggleKeybinding.wasPressed()) {
                doAS = TickUtil.keybindingToggleAction(e.player, doAS,
                                                       !doAS && (e.isInSingleplayer() || featureCfg.switchInMP()),
                                                       "msg.autoswitch.toggle_true", "msg.autoswitch.toggle_false");
                if (!doAS) scheduler.resetSchedule(); // Clear event schedule when switching is disabled
            }
            // Keybindings implementation END ---

            // Tick event system and check if scheduling a switchback is needed via EventUtil
            TickUtil.tickEventSchedule(e.player);
        });

        // Notify when AS Loaded
        logger.info("AutoSwitch Loaded");

        // Test if all Materials are configured
        if ("true".equals(System.getenv("as-dev"))) {
            logger.info("Checking code-only targets...");
            assert (testTargetsForCompletion(Material.class) | testTargetsForCompletion(EntityGroup.class)) :
                    "Missing target found!";
        }

    }

    private boolean testTargetsForCompletion(Class<?> clazz) {
        var hasMissingTarget = false;
        for (Field field : clazz.getDeclaredFields()) {
            if (!field.getType().equals(clazz)) continue;

            try {
                if (!switchData.targets.containsValue(field.get(null))) {
                    logger.error("Found missing target: {}", field.getName());
                    hasMissingTarget = true;
                }
            } catch (IllegalAccessException ignored) {
            }
        }

        return hasMissingTarget;
    }

}

