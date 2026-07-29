package com.licht_meilleur.the_end_of_dragon.world.village.trust;

import com.licht_meilleur.the_end_of_dragon.world.village
        .TedVillageQuestStage;
import com.licht_meilleur.the_end_of_dragon.world.village
        .TedVillageWorldState;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public final class TedVillageTrustManager {

    private TedVillageTrustManager() {
    }

    /*
     * クエスト進行によって増えた信頼度上限と
     * 同じ量を、達成したプレイヤーへ加算する。
     *
     * 例：
     * 旧上限300、新上限400なら信頼度+100。
     */
    public static int rewardQuestProgress(
            TedVillageWorldState state,
            ServerPlayer player,
            TedVillageQuestStage oldStage,
            TedVillageQuestStage newStage
    ) {
        if (state == null
                || player == null
                || oldStage == null
                || newStage == null) {
            return 0;
        }

        int oldCap =
                TedVillageTrustConstants
                        .getTrustCapForStage(
                                oldStage
                        );

        int newCap =
                TedVillageTrustConstants
                        .getTrustCapForStage(
                                newStage
                        );

        int reward =
                Math.max(
                        0,
                        newCap - oldCap
                );

        if (reward <= 0) {
            return state.getTrustPoints(
                    player.getUUID()
            );
        }

        return state.addTrustPoints(
                player.getUUID(),
                reward
        );
    }

    public static int getTrustPoints(
            TedVillageWorldState state,
            UUID playerId
    ) {
        if (state == null
                || playerId == null) {
            return 0;
        }

        return state.getTrustPoints(
                playerId
        );
    }

    public static int getTrustLevel(
            TedVillageWorldState state,
            UUID playerId
    ) {
        if (state == null
                || playerId == null) {
            return 0;
        }

        return state.getTrustLevel(
                playerId
        );
    }

    public static boolean canUseTradeLevel(
            TedVillageWorldState state,
            UUID playerId,
            int requiredLevel
    ) {
        if (state == null
                || playerId == null) {
            return false;
        }

        return state.hasTrustLevel(
                playerId,
                requiredLevel
        );
    }

    /*
     * 後で取引成立時に呼び出す。
     */
    public static int rewardTrade(
            TedVillageWorldState state,
            UUID playerId
    ) {
        if (state == null
                || playerId == null) {
            return 0;
        }

        return state.addTrustPoints(
                playerId,
                TedVillageTrustConstants
                        .TRADE_REWARD
        );
    }

    /*
     * 後で破壊・攻撃ペナルティから使用する。
     */
    public static int applyPenalty(
            TedVillageWorldState state,
            UUID playerId,
            int penalty
    ) {
        if (state == null
                || playerId == null
                || penalty <= 0) {
            return getTrustPoints(
                    state,
                    playerId
            );
        }

        return state.addTrustPoints(
                playerId,
                -penalty
        );
    }
}