package autoswitch.util;

import autoswitch.AutoSwitch;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class SwitchUtil {

    /**
     * Ray-trace to find if the player can hit an entity
     * Adapted from SwingThroughGrass mod licensed under MIT. Changes made to make it work with Fabric
     *
     * @param player             player that is being evaluated against
     * @param partialTicks       should use 1.0f for this
     * @param blockReachDistance how far the player can reach
     * @return EntityResult
     * @link https://github.com/Exidex/SwingThroughGrass
     * @author Exidex, modified by Deximus-Maximus for Fabric mod loader
     */
    public static EntityHitResult rayTraceEntity(PlayerEntity player, float partialTicks, double blockReachDistance) {
        Vec3d from = player.getCameraPosVec(partialTicks);
        Vec3d look = player.getRotationVec(partialTicks);
        Vec3d to = from.add(look.x * blockReachDistance, look.y * blockReachDistance, look.z * blockReachDistance);

        return ProjectileUtil.getEntityCollision(player.world, player, from, to, (new Box(from, to)), EntityPredicates.VALID_ENTITY);
    }

    /**
     * @return Consumer to handle mob switchback and moving of stack to offhand
     */
    public static Consumer<Boolean> handleUseSwitchConsumer() {
        return b -> {
            if (b && AutoSwitch.featureCfg.switchbackMobs()) {
                AutoSwitch.data.setHasSwitched(true);
            }

            if (b && AutoSwitch.featureCfg.putUseActionToolInOffHand()) {
                assert MinecraftClient.getInstance().getNetworkHandler() != null :
                        "Minecraft client was null when AutoSwitch wanted to sent a packet!";
                MinecraftClient.getInstance().getNetworkHandler().sendPacket(
                        new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND,
                                BlockPos.ORIGIN, Direction.DOWN));
            }
        };
    }

    public static String getMinecraftVersion() {
        return getVersion("minecraft");
    }

    public static String getAutoSwitchVersion() {
        return getVersion("autoswitch");
    }

    private static String getVersion(String modid) {
        Optional<ModContainer> modContainer = FabricLoader.getInstance().getModContainer(modid);
        if (modContainer.isPresent()) return modContainer.get().getMetadata().getVersion().getFriendlyString();

        AutoSwitch.logger.error("Could not find version for: {}", modid);
        return "";
    }

}
