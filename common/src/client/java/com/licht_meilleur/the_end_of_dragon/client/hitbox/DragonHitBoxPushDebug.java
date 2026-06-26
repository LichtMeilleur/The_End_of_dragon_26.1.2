package com.licht_meilleur.the_end_of_dragon.client.hitbox;

import com.licht_meilleur.the_end_of_dragon.entity.OldTheEndOfDragonEntity;
import com.licht_meilleur.the_end_of_dragon.entity.hitbox.DragonPartHitBox;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class DragonHitBoxPushDebug {
    public static boolean ENABLED = true;

    public static void pushPlayer(OldTheEndOfDragonEntity dragon, List<DragonPartHitBox> boxes) {
        if (!ENABLED) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        Player player = client.player;

        if (player == null || player.isSpectator()) {
            return;
        }

        for (DragonPartHitBox box : boxes) {
            if (!player.getBoundingBox().intersects(box.aabb())) {
                continue;
            }

            pushOut(player, box.aabb(), dragon);
            break;
        }
    }

    private static void pushOut(Entity entity, AABB box, OldTheEndOfDragonEntity dragon) {
        Vec3 entityCenter = entity.getBoundingBox().getCenter();
        Vec3 boxCenter = box.getCenter();

        Vec3 dir = entityCenter.subtract(boxCenter);

        if (dir.lengthSqr() < 0.0001D) {
            dir = entity.position().subtract(dragon.position());
        }

        if (dir.lengthSqr() < 0.0001D) {
            dir = new Vec3(0.0D, 0.0D, 1.0D);
        }

        dir = new Vec3(dir.x, 0.0D, dir.z);

        if (dir.lengthSqr() < 0.0001D) {
            dir = new Vec3(0.0D, 0.0D, 1.0D);
        }

        dir = dir.normalize();

        //押し出しの強さ
        double strength = 0.35D;

        entity.setDeltaMovement(
                entity.getDeltaMovement().add(
                        dir.x * strength,
                        0.08D,
                        dir.z * strength
                )
        );

        entity.hurtMarked = true;
    }

    private DragonHitBoxPushDebug() {
    }
}