package dex.autoswitch;

import java.util.Collection;
import java.util.function.Predicate;

import com.mojang.blaze3d.platform.InputConstants;
import dex.autoswitch.api.impl.AutoSwitchApi;
import dex.autoswitch.debug.gui.DebugText;
import org.jetbrains.annotations.NotNull;

import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ObjectShare;

public class Autoswitch implements ClientModInitializer {
    private final KeyMapping autoswitchToggleKeybinding = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("key.autoswitch.toggle",
                    InputConstants.Type.KEYSYM, InputConstants.KEY_R,
                    KeyMapping.Category.register(ResourceLocation.parse("autoswitch:autoswitch"))));

    @Override
    public void onInitializeClient() {
        // Use Fabric to bootstrap the Common mod.
        CommonClass.init();

        DebugText.register();

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

        var share = getObjectShare();

        for (AutoSwitchApi.ApiEntry<?> entry : AutoSwitchApi.INSTANCE.getEntries()) {
            share.put(entry.id().toString(), entry.entries());
        }

        // Test of Object share
        //AutoSwitchApi.INSTANCE.DEPLETED.addEntry(stack -> stack.getDamageValue() != 0);

        Constants.LOG.info("AutoSwitch Fabric Loaded!");
    }

    /**
     * Get the object share and register inbuilt API via it.
     */
    private static @NotNull ObjectShare getObjectShare() {
        var share = FabricLoader.getInstance().getObjectShare();

        // Handle damageable items
        share.whenAvailable(AutoSwitchApi.INSTANCE.DEPLETED.id().toString(), (id, entries) -> {
            if (entries instanceof Collection<?> collection) {
                //noinspection unchecked
                ((Collection<Predicate<ItemStack>>)collection).add(stack -> {

                    if (stack.isDamageableItem()) {
                        return stack.nextDamageWillBreak();
                    }

                    return false;
                });
            }
        });

        return share;
    }
}
