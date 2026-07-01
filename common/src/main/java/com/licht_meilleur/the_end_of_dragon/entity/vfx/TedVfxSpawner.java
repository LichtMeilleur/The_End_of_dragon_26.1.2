package com.licht_meilleur.the_end_of_dragon.entity.vfx;

import com.licht_meilleur.the_end_of_dragon.entity.TheEndOfDragonCoreEntity;
import com.licht_meilleur.the_end_of_dragon.entity.hitbox.DragonLocatorSampler;
import com.licht_meilleur.the_end_of_dragon.registry.ModEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.phys.Vec3;

public final class TedVfxSpawner {

    public static void spawnAt(
            ServerLevel level,
            Vec3 pos,
            float yaw,
            float pitch,
            TedVfxType type,
            float scale,
            float length,
            int maxAge
    ) {
        TedVfxEntity vfx = ModEntities.TED_VFX.create(level, EntitySpawnReason.EVENT);
        if (vfx == null) {
            return;
        }

        vfx.setup(type, scale, length, maxAge);
        vfx.snapTo(pos.x, pos.y, pos.z, yaw, pitch);
        level.addFreshEntity(vfx);
    }

    public static void spawnAtLocator(
            ServerLevel level,
            TheEndOfDragonCoreEntity dragon,
            String locator,
            TedVfxType type,
            float scale,
            float length,
            int maxAge
    ) {
        Vec3 pos = DragonLocatorSampler.worldPos(dragon, locator);

        spawnAt(
                level,
                pos,
                dragon.getYRot(),
                dragon.getXRot(),
                type,
                scale,
                length,
                maxAge
        );
    }

    public static void spawnForwardFromLocator(
            ServerLevel level,
            TheEndOfDragonCoreEntity dragon,
            String locator,
            double forwardOffset,
            TedVfxType type,
            float scale,
            float length,
            int maxAge
    ) {
        Vec3 base = DragonLocatorSampler.worldPos(dragon, locator);
        Vec3 forward = DragonLocatorSampler.forward(dragon);
        Vec3 pos = base.add(forward.scale(forwardOffset));

        spawnAt(
                level,
                pos,
                dragon.getYRot(),
                dragon.getXRot(),
                type,
                scale,
                length,
                maxAge
        );
    }

    private TedVfxSpawner() {
    }
}