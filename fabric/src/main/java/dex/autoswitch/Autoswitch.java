package dex.autoswitch;

import com.mojang.blaze3d.platform.InputConstants;
import dex.autoswitch.api.impl.AutoSwitchApi;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.util.profiling.Profiler;
import org.lwjgl.glfw.GLFW;

public class Autoswitch implements ClientModInitializer {
    private final KeyMapping autoswitchToggleKeybinding = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("key.autoswitch.toggle",
                    InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "key.categories.autoswitch"));

    @Override
    public void onInitializeClient() {
        // Use Fabric to bootstrap the Common mod.
        CommonClass.init();

        ClientTickEvents.END_CLIENT_TICK.register(e -> {
            // Keybindings implementation BEGIN ---
            while (autoswitchToggleKeybinding.consumeClick()) {
                Constants.performSwitch = CommonClass.keybindingToggleAction(e.player, Constants.performSwitch,
                        !Constants.performSwitch && (e.isLocalServer() || Constants.CONFIG.featureConfig.switchInMp),
                        "msg.autoswitch.toggle_true", "msg.autoswitch.toggle_false");

                if (!Constants.performSwitch) {
                    Constants.SWITCH_STATE.reset();
                    Constants.SCHEDULER.reset();
                }
            }
            // Keybindings implementation END ---

            // Tick event system and check if scheduling a switchback is needed via EventUtil
            if (!e.isPaused() && e.level != null) {
                var profiler = Profiler.get();
                profiler.push("autoswitch:schedulerTick");
                Constants.SCHEDULER.tick();
                profiler.pop();
            }
        });

        var share = FabricLoader.getInstance().getObjectShare();
        for (AutoSwitchApi.ApiEntry<?> entry : AutoSwitchApi.INSTANCE.getEntries()) {
            share.put(entry.id().toString(), entry.entries());
        }

        // Test of Object share
        //AutoSwitchApi.INSTANCE.DEPLETED.addEntry(stack -> stack.getDamageValue() != 0);

        Constants.LOG.info("AutoSwitch Fabric Loaded!");
    }
}
