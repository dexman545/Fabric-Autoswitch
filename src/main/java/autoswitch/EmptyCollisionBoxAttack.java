package autoswitch;

import net.minecraft.entity.ProjectileUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/*
This method is adapted from https://github.com/Exidex/SwingThroughGrass, a mod licensed under MIT by Exidex for use with
the Fabric modloader
 */

public class EmptyCollisionBoxAttack {

    public static EntityHitResult rayTraceEntity(PlayerEntity player, float partialTicks, double blockReachDistance) {
        Vec3d from = player.getCameraPosVec(partialTicks);
        Vec3d look = player.getRotationVec(partialTicks);
        Vec3d to = from.add(look.x * blockReachDistance, look.y * blockReachDistance, look.z * blockReachDistance);

        return ProjectileUtil.getEntityCollision(player.world, player, from, to, (new Box(from, to)), EntityPredicates.VALID_ENTITY);
    }

}
