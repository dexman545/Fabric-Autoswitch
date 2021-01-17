package autoswitch.util;

import java.util.Optional;
import java.util.function.Consumer;

import autoswitch.AutoSwitch;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

@Environment(EnvType.CLIENT)
public class SwitchUtil {

    /**
     * @return Consumer to handle mob switchback and moving of stack to offhand
     */
    public static Consumer<Boolean> handleUseSwitchConsumer() {
        return b -> {
            if (b && (AutoSwitch.featureCfg.switchbackAllowed().ordinal() <= 2)) {
                AutoSwitch.switchState.setHasSwitched(true);
            }

            assert MinecraftClient.getInstance().getNetworkHandler() !=
                   null : "Minecraft client was null when AutoSwitch wanted to sent a packet!";

            if (b && doPutActionOffhandCheck()) {
                MinecraftClient.getInstance().getNetworkHandler().sendPacket(
                        new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN,
                                                  Direction.DOWN));
            }
        };
    }

    private static boolean doPutActionOffhandCheck() {
        assert MinecraftClient.getInstance().player != null;
        return !(AutoSwitch.featureCfg.preserveOffhandItem() &&
                 MinecraftClient.getInstance().player.getOffHandStack() != ItemStack.EMPTY);
    }

    public static String getMinecraftVersion() {
        return getVersion("minecraft");
    }

    private static String getVersion(String modid) {
        Optional<ModContainer> modContainer = FabricLoader.getInstance().getModContainer(modid);
        if (modContainer.isPresent()) return modContainer.get().getMetadata().getVersion().getFriendlyString();

        AutoSwitch.logger.error("Could not find version for: {}", modid);
        return "";
    }

    public static String getAutoSwitchVersion() {
        return getVersion("autoswitch");
    }

}
