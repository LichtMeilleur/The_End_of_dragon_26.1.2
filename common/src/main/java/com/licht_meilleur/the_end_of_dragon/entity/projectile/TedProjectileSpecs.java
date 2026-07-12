package com.licht_meilleur.the_end_of_dragon.entity.projectile;

import com.licht_meilleur.the_end_of_dragon.entity.vfx.TedVfxType;

public final class TedProjectileSpecs {

    public static final TedProjectileSpec ORB_OF_ANNIHILATION =
            new TedProjectileSpec(
                    TedVfxType.ORB_OF_ANIHILATION,
                    6.0F,
                    1.2D,
                    160,
                    6.0D,
                    true,
                    5.5D,
                    false,
                    0.0F,
                    0,
                    false,
                    0,
                    0.0D
            );

    public static final TedProjectileSpec LIGHT_PROJECTILE =
            new TedProjectileSpec(
                    TedVfxType.LIGHT_PROJECTILE,
                    1.0F,
                    4.0D,
                    80,
                    1.2D,
                    true,
                    1.5D,
                    true,
                    2.5F,
                    28,
                    false,
                    0,
                    0.0D
            );
    public static final TedProjectileSpec JUDGMENT_RAY =
            new TedProjectileSpec(
                    TedVfxType.JUDGMENT_RAY,
                    4.0F,
                    2.4D,
                    100,
                    0.7D,
                    true,
                    1.8D,
                    true,
                    1.8F,
                    18,
                    true,
                    10,
                    0.08D
            );

    private TedProjectileSpecs() {
    }
}