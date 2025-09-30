package dex.autoswitch;


import java.util.function.Predicate;

import com.mojang.blaze3d.platform.InputConstants;
import dex.autoswitch.api.impl.AutoSwitchApi;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.Lazy;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.item.ItemStack;

@Mod(value = Constants.MOD_ID, dist = Dist.CLIENT)
public class Autoswitch {
    public static KeyMapping.Category CATEGORY = new KeyMapping.Category(ResourceLocation.parse("autoswitch:autoswitch"));
    public static final Lazy<KeyMapping> SWITCH_TOGGLE = Lazy.of(() ->
            new KeyMapping("key.autoswitch.toggle",
                    InputConstants.Type.KEYSYM, InputConstants.KEY_R,
                    CATEGORY));

    // Use net.neoforged.neoforge.event.TagsUpdatedEvent to reset cache
    public Autoswitch(IEventBus eventBus) {
        // Use NeoForge to bootstrap the Common mod.
        CommonClass.init();

        NeoForge.EVENT_BUS.addListener(Autoswitch::onTick);
        eventBus.addListener(Autoswitch::registerBindings);
        eventBus.addListener(Autoswitch::sendIMC);
        eventBus.addListener(Autoswitch::receiveIMC);

        Constants.LOG.info("AutoSwitch Neoforge Loaded!");
    }

    // Register Forge Energy API
    private static void sendIMC(InterModEnqueueEvent event) {
        InterModComms.sendTo(Constants.MOD_ID, AutoSwitchApi.INSTANCE.DEPLETED.id().getPath(),
                () -> (Predicate<ItemStack>) stack -> {
                    var cap = stack.getCapability(Capabilities.EnergyStorage.ITEM);
                    if (cap != null) {
                        return cap.getEnergyStored() == 0;
                    }

                    return false;
                });
        InterModComms.sendTo(Constants.MOD_ID, AutoSwitchApi.INSTANCE.DEPLETED.id().getPath(),
                () -> (Predicate<ItemStack>) stack -> {
                    if (stack.isDamageableItem()) {
                        return stack.nextDamageWillBreak();
                    }

                    return false;
                });
        // Test of IMC
        /*InterModComms.sendTo(Constants.MOD_ID, AutoSwitchApi.INSTANCE.DEPLETED.id().getPath(),
                () -> (Predicate<ItemStack>) stack -> stack.getDamageValue() != 0);*/
    }

    private static void receiveIMC(InterModProcessEvent event) {
        InterModComms.getMessages(Constants.MOD_ID).forEach(imc -> {
            Constants.LOG.info("Processing IMC: {}", imc);
            var entry = AutoSwitchApi.INSTANCE.getEntryMap().get(ResourceLocation.tryBuild(imc.modId(), imc.method()));
            if (entry != null) {
                try {
                    Constants.LOG.info("Registered API entry");
                    entry.addUnknown(imc.messageSupplier().get());
                } catch (ClassCastException e) {
                    //noinspection StringConcatenationArgumentToLogCall
                    Constants.LOG.error("A mod (maybe '" + imc.senderModId() +
                            "') has passed the incorrect object to AutoSwitch via the IMC", e);
                }
            } else {
                Constants.LOG.warn("Unknown IMC: {}", ResourceLocation.tryBuild(imc.modId(), imc.method()));
            }
        });
    }

    private static void onTick(ClientTickEvent.Post event) {
        var mc = Minecraft.getInstance();

        // Keybindings implementation BEGIN ---
        while (SWITCH_TOGGLE.get().consumeClick()) {
            Constants.performSwitch = CommonClass.keybindingToggleAction(mc.player, Constants.performSwitch,
                    !Constants.performSwitch && (mc.isLocalServer() || Constants.CONFIG.featureConfig.switchInMp),
                    "msg.autoswitch.toggle_true", "msg.autoswitch.toggle_false");

            if (!Constants.performSwitch) {
                Constants.SWITCH_STATE.reset();
                Constants.SCHEDULER.reset();
            }
        }
        // Keybindings implementation END ---

        if (!mc.isPaused() && mc.level != null) {
            var profiler = Profiler.get();
            profiler.push("autoswitch:schedulerTick");
            Constants.SCHEDULER.tick();
            profiler.pop();
        }
    }

    public static void registerBindings(RegisterKeyMappingsEvent event) {
        event.registerCategory(CATEGORY);
        event.register(SWITCH_TOGGLE.get());
    }
}
