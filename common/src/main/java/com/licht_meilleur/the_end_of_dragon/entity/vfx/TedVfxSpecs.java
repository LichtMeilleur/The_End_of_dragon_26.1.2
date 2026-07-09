package com.licht_meilleur.the_end_of_dragon.entity.vfx;

import com.licht_meilleur.the_end_of_dragon.entity.collision.DragonCollisionPart;

public final class TedVfxSpecs {
    public static final TedVfxSpec FRONT_LEFT_PHOTON =
            photonLaser(DragonCollisionPart.FRONT_LEFT_HAND);

    public static final TedVfxSpec FRONT_RIGHT_PHOTON =
            photonLaser(DragonCollisionPart.FRONT_RIGHT_HAND);

    public static final TedVfxSpec BACK_LEFT_PHOTON =
            photonLaser(DragonCollisionPart.BACK_LEFT_HAND);

    public static final TedVfxSpec BACK_RIGHT_PHOTON =
            photonLaser(DragonCollisionPart.BACK_RIGHT_HAND);



    public static final TedVfxSpec FRONT_LEFT_RAGNAROK =
            ragnarokLaser(DragonCollisionPart.FRONT_LEFT_HAND);

    public static final TedVfxSpec FRONT_RIGHT_RAGNAROK =
            ragnarokLaser(DragonCollisionPart.FRONT_RIGHT_HAND);

    public static final TedVfxSpec BACK_LEFT_RAGNAROK =
            ragnarokLaser(DragonCollisionPart.BACK_LEFT_HAND);

    public static final TedVfxSpec BACK_RIGHT_RAGNAROK =
            ragnarokLaser(DragonCollisionPart.BACK_RIGHT_HAND);


    public static TedVfxSpec photonLaser(DragonCollisionPart part) {
        return new TedVfxSpec(
                TedVfxType.TED_LASER_BEAM,
                TedVfxAttachMode.PART_BASIS,
                part,
                0.0D, 0.0D, 0.0D,
                3.5F,
                64.0F,
                0.75F,
                999999
        );
    }

    private static TedVfxSpec ragnarokLaser(DragonCollisionPart part) {
        return new TedVfxSpec(
                TedVfxType.TED_LASER_BEAM,
                TedVfxAttachMode.PART_BASIS,
                part,
                0.0D,0.0D,0.0D,
                6.0F,
                96.0F,
                2.2F,
                999999
        );
    }

    public static TedVfxSpec jet(DragonCollisionPart part, double x, double y, double z) {
        return new TedVfxSpec(
                TedVfxType.TED_JET,
                TedVfxAttachMode.PART_BASIS,
                part,
                x, y, z,
                1.5F,
                5.0F,
                1.0F,
                20
        );
    }

    private TedVfxSpecs() {
    }
}