package com.licht_meilleur.the_end_of_dragon.entity.vfx;

import com.licht_meilleur.the_end_of_dragon.entity.collision.DragonCollisionPart;

public final class TedVfxSpecs {
    public static final TedVfxSpec FRONT_LEFT_LASER = laser(DragonCollisionPart.FRONT_LEFT_HAND);
    public static final TedVfxSpec FRONT_RIGHT_LASER = laser(DragonCollisionPart.FRONT_RIGHT_HAND);
    public static final TedVfxSpec BACK_LEFT_LASER = laser(DragonCollisionPart.BACK_LEFT_HAND);
    public static final TedVfxSpec BACK_RIGHT_LASER = laser(DragonCollisionPart.BACK_RIGHT_HAND);

    public static TedVfxSpec laser(DragonCollisionPart part) {
        return new TedVfxSpec(
                TedVfxType.TED_LASER_BEAM,
                TedVfxAttachMode.PART_BASIS,
                part,
                0.0D, 0.0D, 0.0D,
                1.5F,
                10.0F,
                999999
        );
    }

    public static TedVfxSpec jet(DragonCollisionPart part, double x, double y, double z) {
        return new TedVfxSpec(
                TedVfxType.TED_JET,
                TedVfxAttachMode.PART_BASIS,
                part,
                x, y, z,
                1.0F,
                1.0F,
                20
        );
    }

    private TedVfxSpecs() {
    }
}