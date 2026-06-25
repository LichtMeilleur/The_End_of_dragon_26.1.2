package com.licht_meilleur.the_end_of_dragon.entity.hitbox;

import java.util.List;

public final class DragonHitParts {
    public static final List<DragonHitPart> ALL = List.of(
            new DragonHitPart("upper_body", DragonLocators.UPPER_BODY_ROOT, DragonLocators.UPPER_BODY_TIP, 1.25D, 1.0D),
            new DragonHitPart("head", DragonLocators.HEAD_ROOT, DragonLocators.HEAD_TIP, 0.85D, 1.25D),

            new DragonHitPart("front_left_arm", DragonLocators.FRONT_LEFT_ARM_ROOT, DragonLocators.FRONT_LEFT_ARM_TIP, 0.70D, 0.9D),
            new DragonHitPart("front_left_fore_arm", DragonLocators.FRONT_LEFT_FORE_ARM_ROOT, DragonLocators.FRONT_LEFT_FORE_ARM_TIP, 0.65D, 0.9D),
            new DragonHitPart("front_left_hand", DragonLocators.FRONT_LEFT_HAND_ROOT, DragonLocators.FRONT_LEFT_HAND_TIP, 0.75D, 0.95D),

            new DragonHitPart("front_right_arm", DragonLocators.FRONT_RIGHT_ARM_ROOT, DragonLocators.FRONT_RIGHT_ARM_TIP, 0.70D, 0.9D),
            new DragonHitPart("front_right_fore_arm", DragonLocators.FRONT_RIGHT_FORE_ARM_ROOT, DragonLocators.FRONT_RIGHT_FORE_ARM_TIP, 0.65D, 0.9D),
            new DragonHitPart("front_right_hand", DragonLocators.FRONT_RIGHT_HAND_ROOT, DragonLocators.FRONT_RIGHT_HAND_TIP, 0.75D, 0.95D),

            new DragonHitPart("back_left_arm", DragonLocators.BACK_LEFT_ARM_ROOT, DragonLocators.BACK_LEFT_ARM_TIP, 0.70D, 0.9D),
            new DragonHitPart("back_left_fore_arm", DragonLocators.BACK_LEFT_FORE_ARM_ROOT, DragonLocators.BACK_LEFT_FORE_ARM_TIP, 0.65D, 0.9D),
            new DragonHitPart("back_left_hand", DragonLocators.BACK_LEFT_HAND_ROOT, DragonLocators.BACK_LEFT_HAND_TIP, 0.75D, 0.95D),

            new DragonHitPart("back_right_arm", DragonLocators.BACK_RIGHT_ARM_ROOT, DragonLocators.BACK_RIGHT_ARM_TIP, 0.70D, 0.9D),
            new DragonHitPart("back_right_fore_arm", DragonLocators.BACK_RIGHT_FORE_ARM_ROOT, DragonLocators.BACK_RIGHT_FORE_ARM_TIP, 0.65D, 0.9D),
            new DragonHitPart("back_right_hand", DragonLocators.BACK_RIGHT_HAND_ROOT, DragonLocators.BACK_RIGHT_HAND_TIP, 0.75D, 0.95D),

            new DragonHitPart("left_leg", DragonLocators.LEFT_LEG_ROOT, DragonLocators.LEFT_LEG_TIP, 0.75D, 0.9D),
            new DragonHitPart("left_lower_leg", DragonLocators.LEFT_LOWER_LEG_ROOT, DragonLocators.LEFT_LOWER_LEG_TIP, 0.70D, 0.9D),

            new DragonHitPart("right_leg", DragonLocators.RIGHT_LEG_ROOT, DragonLocators.RIGHT_LEG_TIP, 0.75D, 0.9D),
            new DragonHitPart("right_lower_leg", DragonLocators.RIGHT_LOWER_LEG_ROOT, DragonLocators.RIGHT_LOWER_LEG_TIP, 0.70D, 0.9D),

            new DragonHitPart("tail_root_middle", DragonLocators.TAIL_ROOT, DragonLocators.TAIL_MIDDLE, 0.55D, 0.8D),
            new DragonHitPart("tail_middle_tip", DragonLocators.TAIL_MIDDLE, DragonLocators.TAIL_TIP, 0.45D, 0.75D)
    );

    private DragonHitParts() {
    }
}