package com.licht_meilleur.the_end_of_dragon.entity.projectile;

import com.licht_meilleur.the_end_of_dragon.entity.vfx.TedVfxType;

public record TedProjectileSpec(
        TedVfxType type,
        float scale,
        double speed,
        int maxAge,
        double hitRadius,
        float damage,
        boolean destroyBlocks,
        double destroyRadius,
        boolean createExplosion,
        float explosionRadius,
        int explosionParticleCount
) {
}