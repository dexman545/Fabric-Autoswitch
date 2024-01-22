package autoswitch.util;

import java.util.Optional;
import java.util.function.Consumer;

import autoswitch.AutoSwitch;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class SwitchUtil {

    /**
     * @return Consumer to handle mob switchback and moving of stack to offhand
     */
    public static Consumer<Boolean> handleUseSwitchConsumer() {
        return moveToOffhand -> {
            if (moveToOffhand && (!AutoSwitch.featureCfg.switchbackAllowed().isEmpty())) {
                AutoSwitch.switchState.setHasSwitched(true);
            }

            assert Minecraft.getInstance().getConnection() !=
                   null : "Minecraft client was null when AutoSwitch wanted to sent a packet!";

            if (moveToOffhand && doPutActionOffhandCheck()) {
                Minecraft.getInstance().getConnection().send(
                        new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO,
                                                  Direction.DOWN));
            }
        };
    }

    private static boolean doPutActionOffhandCheck() {
        assert Minecraft.getInstance().player != null;
        return !(AutoSwitch.featureCfg.preserveOffhandItem() &&
                 Minecraft.getInstance().player.getOffhandItem() != ItemStack.EMPTY);
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

    public static boolean isAcceptableVersion(String minVersion) {
        try {
            return SemanticVersion.parse(getMinecraftVersion())
                                  .compareTo((Version) SemanticVersion.parse(minVersion)) >= 0;
        } catch (VersionParsingException e) {
            AutoSwitch.logger.error("Failed to compare MC versions for Material registration", e);
        }

        return false;
    }

}
