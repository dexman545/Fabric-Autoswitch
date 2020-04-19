package autoswitch;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class EmptyCollisionBoxAttack {

    /**
     * Ray-trace to find if the player can hit an entity
     * Adapted from SwingThroughGrass mod licensed under MIT. Changes made to make it work with Fabric
     * @link https://github.com/Exidex/SwingThroughGrass
     * @author Exidex, modified by Deximus-Maximus for Fabric mod loader
     * @param player player that is being evaluated against
     * @param partialTicks should use 1.0f for this
     * @param blockReachDistance how far the player can reach
     * @return EntityResult
     */
    public static EntityHitResult rayTraceEntity(PlayerEntity player, float partialTicks, double blockReachDistance) {
        Vec3d from = player.getCameraPosVec(partialTicks);
        Vec3d look = player.getRotationVec(partialTicks);
        Vec3d to = from.add(look.x * blockReachDistance, look.y * blockReachDistance, look.z * blockReachDistance);

        return ProjectileUtil.getEntityCollision(player.world, player, from, to, (new Box(from, to)), EntityPredicates.VALID_ENTITY);
    }

}
