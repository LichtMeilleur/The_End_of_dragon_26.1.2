package com.licht_meilleur.the_end_of_dragon.entity.hitbox;

import com.licht_meilleur.the_end_of_dragon.entity.TheEndOfDragonEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class DragonServerPushHandler {
    public static void tick(TheEndOfDragonEntity dragon) {
        if (!(dragon.level() instanceof ServerLevel level)) {
            return;
        }

        List<AABB> boxes = List.of(
                box(dragon, 0, 3.5, 0, 5.0, 4.0, 5.0),      // 胴体
                box(dragon, 0, 7.0, -2.0, 3.0, 3.0, 3.0),     // 頭
                box(dragon, 4.0, 4.0, 0, 3.0, 3.0, 3.0),      // 右腕ざっくり
                box(dragon, -4.0, 4.0, 0, 3.0, 3.0, 3.0),     // 左腕ざっくり
                box(dragon, 0, 2.0, 4.0, 4.0, 2.0, 4.0),      // 尻尾側
                box(dragon, 0, 1.5, -3.0, 4.0, 2.0, 4.0)      // 脚側
        );

        AABB search = dragon.getBoundingBox().inflate(10.0D, 10.0D, 10.0D);

        for (Entity entity : level.getEntities(dragon, search)) {
            if (!(entity instanceof LivingEntity)) {
                continue;
            }

            if (entity == dragon || entity.isSpectator()) {
                continue;
            }

            for (AABB box : boxes) {
                if (entity.getBoundingBox().intersects(box)) {
                    pushOut(entity, box, dragon);
                    break;
                }
            }
        }
    }

    private static AABB box(
            TheEndOfDragonEntity dragon,
            double ox, double oy, double oz,
            double sx, double sy, double sz
    ) {
        Vec3 center = rotateYaw(new Vec3(ox, oy, oz), dragon.getYRot()).add(dragon.position());

        return new AABB(
                center.x - sx / 2.0D,
                center.y - sy / 2.0D,
                center.z - sz / 2.0D,
                center.x + sx / 2.0D,
                center.y + sy / 2.0D,
                center.z + sz / 2.0D
        );
    }

    private static Vec3 rotateYaw(Vec3 local, float yawDegrees) {
        double rad = Math.toRadians(yawDegrees);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);

        double x = local.x * cos - local.z * sin;
        double z = local.x * sin + local.z * cos;

        return new Vec3(x, local.y, z);
    }

    private static void pushOut(Entity entity, AABB box, TheEndOfDragonEntity dragon) {
        Vec3 dir = entity.getBoundingBox().getCenter().subtract(box.getCenter());

        dir = new Vec3(dir.x, 0.0D, dir.z);

        if (dir.lengthSqr() < 0.0001D) {
            dir = entity.position().subtract(dragon.position());
            dir = new Vec3(dir.x, 0.0D, dir.z);
        }

        if (dir.lengthSqr() < 0.0001D) {
            dir = new Vec3(0.0D, 0.0D, 1.0D);
        }

        dir = dir.normalize();

        double strength = 0.18D;

        entity.setDeltaMovement(
                entity.getDeltaMovement().add(
                        dir.x * strength,
                        0.04D,
                        dir.z * strength
                )
        );

        entity.hurtMarked = true;
    }

    private DragonServerPushHandler() {
    }
}