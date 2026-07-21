package com.licht_meilleur.the_end_of_dragon.world;

public enum TedAllyProgress {

    NOT_STARTED,

    WOUNDED_DURING_BATTLE,

    ALLY_ACTIVE,

    DIED_DURING_BATTLE,

    WOUNDED_AFTER_BATTLE,

    RECOVERED_AFTER_BATTLE,

    ITEM_GIVEN,

    /*
     * TED討伐後、味方エンダーマンが存在せず、
     * 瀕死状態で再生成する必要がある。
     */
    RESPAWN_AFTER_BATTLE_PENDING
}