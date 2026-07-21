package com.licht_meilleur.the_end_of_dragon.world;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public final class TedBattleController {

    private TedBattleController() {
    }

    /*
     * エンダーマン連動イベント開始。
     */
    public static void startBattle(
            ServerLevel level
    ) {
        if (level.dimension() != Level.END) {
            return;
        }

        TedBattleWorldState state =
                TedBattleWorldState.get(level);

        if (state.isBattleActive()) {
            return;
        }

        state.setBattleActive(true);

        System.out.println(
                "[TED ENDERMAN EVENT] "
                        + "battle state enabled and saved"
        );
    }

    /*
     * エンダーマン連動イベント終了。
     */
    public static void endBattle(
            ServerLevel level
    ) {
        if (level.dimension() != Level.END) {
            return;
        }

        TedBattleWorldState state =
                TedBattleWorldState.get(level);

        if (!state.isBattleActive()) {
            return;
        }

        state.setBattleActive(false);

        System.out.println(
                "[TED ENDERMAN EVENT] "
                        + "battle state disabled and saved"
        );
    }

    /*
     * 現在スポーン停止中か。
     *
     * 毎回SavedDataから取得するため、
     * サーバー再起動後も保存状態を参照できる。
     */
    public static boolean isBattleActive(
            ServerLevel level
    ) {
        if (level.dimension() != Level.END) {
            return false;
        }

        return TedBattleWorldState
                .get(level)
                .isBattleActive();
    }

    /*
     * Endディメンションの定期更新。
     *
     * Fabric / NeoForge側のServerLevel tickイベントから
     * 毎tick呼び出す。
     */
    public static void tick(
            ServerLevel level
    ) {
        if (level.dimension() != Level.END) {
            return;
        }

        /*
         * 討伐後に消えた味方エンダーマンを、
         * プレイヤーがEndへ戻った際に再生成する。
         *
         * メソッド内部で20tickに1回へ制限されている。
         */
        TedEndermanBattleHandler
                .tickPostBattleRespawn(
                        level
                );
    }
}