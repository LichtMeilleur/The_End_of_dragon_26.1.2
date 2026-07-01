package com.licht_meilleur.the_end_of_dragon.entity.projectile;

import com.licht_meilleur.the_end_of_dragon.entity.vfx.TedVfxType;

public final class TedProjectileSpecs {

    public static final TedProjectileSpec ORB_OF_ANNIHILATION =
            new TedProjectileSpec(
                    TedVfxType.ORB_OF_ANIHILATION,
                    6.0F,      // scale
                    1.2D,      // speed
                    160,       // maxAge
                    6.0D,      // hitRadius
                    9999.0F,   // damage
                    true,      // destroyBlocks
                    5.5D,       // destroyRadius
                    false,      // createExplosion
                    0,          //explosionRadius
                    0           //explosionParticleCount
            );

    public static final TedProjectileSpec LIGHT_PROJECTILE =
            new TedProjectileSpec(
                    TedVfxType.LIGHT_PROJECTILE,
                    1.0F,
                    4.0D,
                    80,
                    1.2D,
                    16.0F,
                    true,
                    1.5D,
                    true,
                    2.5F,
                    28
            );

    private TedProjectileSpecs() {
    }
}