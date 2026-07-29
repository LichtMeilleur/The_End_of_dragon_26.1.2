package com.licht_meilleur.the_end_of_dragon.world.village.trust;

import com.licht_meilleur.the_end_of_dragon.world.village.TedVillageQuestStage;

public final class TedVillageTrustConstants {

    public static final int POINTS_PER_LEVEL = 100;
    public static final int MAX_LEVEL = 6;
    public static final int ABSOLUTE_MAX_TRUST =
            POINTS_PER_LEVEL * MAX_LEVEL;

    public static final int BLOCK_BREAK_PENALTY = 10;
    public static final int ATTACK_PENALTY = 25;
    public static final int KILL_PENALTY = 100;

    /*
     * 取引成立ごとの回復量。
     */
    public static final int TRADE_REWARD = 5;

    private TedVillageTrustConstants() {
    }

    public static int levelFromPoints(
            int points
    ) {
        if (points <= 0) {
            return 0;
        }

        return Math.min(
                MAX_LEVEL,
                (points + POINTS_PER_LEVEL - 1)
                        / POINTS_PER_LEVEL
        );
    }

    public static int getTrustCapForStage(
            TedVillageQuestStage stage
    ) {
        if (stage.isAtLeast(
                TedVillageQuestStage.JUICE_WATER_PRODUCED
        )) {
            return 600;
        }

        if (stage.isAtLeast(
                TedVillageQuestStage.RECHORUS_PLANT_BUILT
        )) {
            return 500;
        }

        if (stage.isAtLeast(
                TedVillageQuestStage.RECHORUS_PLANT_CORE_DELIVERY
        )) {
            return 400;
        }

        if (stage.isAtLeast(
                TedVillageQuestStage.RECHORUS_MELON_SEED_DELIVERY
        )) {
            return 300;
        }

        if (stage.isAtLeast(
                TedVillageQuestStage.RECHORUS_MELON_PROTOTYPE
        )) {
            return 200;
        }

        if (stage.isAtLeast(
                TedVillageQuestStage.WATER_TRANSFER_RESEARCH
        )) {
            return 100;
        }

        return 0;
    }
}