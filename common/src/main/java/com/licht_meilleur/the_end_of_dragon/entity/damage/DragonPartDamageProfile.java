package com.licht_meilleur.the_end_of_dragon.entity.damage;

import com.licht_meilleur.the_end_of_dragon.entity.collision.DragonCollisionPart;

public final class DragonPartDamageProfile {

    private DragonPartDamageProfile() {
    }

    public static float getMultiplier(
            DragonCollisionPart part
    ) {
        return switch (part) {
            case HEAD ->
                    1.50F;

            case CHEST_CRYSTAL ->
                    2.00F;

            case UPPER_BODY ->
                    1.00F;

            case FRONT_LEFT_ARM,
                 FRONT_RIGHT_ARM,
                 BACK_LEFT_ARM,
                 BACK_RIGHT_ARM ->
                    0.90F;

            case FRONT_LEFT_FORE_ARM,
                 FRONT_RIGHT_FORE_ARM,
                 BACK_LEFT_FORE_ARM,
                 BACK_RIGHT_FORE_ARM ->
                    0.85F;

            case FRONT_LEFT_HAND,
                 FRONT_RIGHT_HAND,
                 BACK_LEFT_HAND,
                 BACK_RIGHT_HAND ->
                    0.75F;

            case LEFT_LEG,
                 RIGHT_LEG ->
                    0.80F;

            case TAIL_ROOT ->
                    0.70F;

            case TAIL_TIP ->
                    0.60F;

            case UNKNOWN ->
                    1.00F;
        };
    }
}