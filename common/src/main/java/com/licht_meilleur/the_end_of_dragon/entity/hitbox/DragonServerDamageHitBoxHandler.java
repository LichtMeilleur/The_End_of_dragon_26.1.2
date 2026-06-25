package com.licht_meilleur.the_end_of_dragon.entity.hitbox;

import com.licht_meilleur.the_end_of_dragon.entity.TheEndOfDragonEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class DragonServerDamageHitBoxHandler {
    public static void tick(TheEndOfDragonEntity dragon) {
        if (!(dragon.level() instanceof ServerLevel level)) {
            return;
        }

        List<ServerPartBox> boxes = createBoxes(dragon);
        AABB search = dragon.getBoundingBox().inflate(16.0D, 16.0D, 16.0D);

        for (Projectile projectile : level.getEntitiesOfClass(Projectile.class, search)) {
            if (!projectile.isAlive()) {
                continue;
            }

            for (ServerPartBox box : boxes) {
                if (!projectile.getBoundingBox().intersects(box.aabb())) {
                    continue;
                }

                Entity owner = projectile.getOwner();

                DamageSource source;

                if (owner instanceof LivingEntity livingOwner) {
                    source = dragon.damageSources().mobProjectile(projectile, livingOwner);
                } else {
                    source = dragon.damageSources().generic();
                }

                float damage = (float) (12.0F * box.damageMultiplier());

                dragon.hurtServer(level, source, damage);
                projectile.discard();

                break;
            }
        }
    }

    private static List<ServerPartBox> createBoxes(TheEndOfDragonEntity dragon) {
        return List.of(
                box(dragon, "upper_body", 0, 3.5, 0, 5.0, 4.0, 5.0, 1.0D),
                box(dragon, "head", 0, 7.0, -2.0, 3.0, 3.0, 3.0, 1.25D),

                box(dragon, "front_left_arm", -5.0, 4.5, -1.0, 5.0, 4.0, 5.0, 0.9D),
                box(dragon, "front_right_arm", 5.0, 4.5, -1.0, 5.0, 4.0, 5.0, 0.9D),

                box(dragon, "back_left_arm", -5.0, 4.5, 2.0, 5.0, 4.0, 5.0, 0.9D),
                box(dragon, "back_right_arm", 5.0, 4.5, 2.0, 5.0, 4.0, 5.0, 0.9D),

                box(dragon, "left_leg", -1.5, 1.5, 0.5, 2.0, 3.0, 2.0, 0.85D),
                box(dragon, "right_leg", 1.5, 1.5, 0.5, 2.0, 3.0, 2.0, 0.85D),

                box(dragon, "tail", 0, 2.0, 5.0, 3.0, 2.0, 5.0, 0.75D)
        );
    }

    private static ServerPartBox box(
            TheEndOfDragonEntity dragon,
            String name,
            double ox, double oy, double oz,
            double sx, double sy, double sz,
            double damageMultiplier
    ) {
        Vec3 center = rotateYaw(new Vec3(ox, oy, oz), dragon.getYRot()).add(dragon.position());

        AABB aabb = new AABB(
                center.x - sx / 2.0D,
                center.y - sy / 2.0D,
                center.z - sz / 2.0D,
                center.x + sx / 2.0D,
                center.y + sy / 2.0D,
                center.z + sz / 2.0D
        );

        return new ServerPartBox(name, aabb, damageMultiplier);
    }

    private static Vec3 rotateYaw(Vec3 local, float yawDegrees) {
        double rad = Math.toRadians(yawDegrees);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);

        double x = local.x * cos - local.z * sin;
        double z = local.x * sin + local.z * cos;

        return new Vec3(x, local.y, z);
    }

    private record ServerPartBox(String name, AABB aabb, double damageMultiplier) {
    }

    private DragonServerDamageHitBoxHandler() {
    }
}