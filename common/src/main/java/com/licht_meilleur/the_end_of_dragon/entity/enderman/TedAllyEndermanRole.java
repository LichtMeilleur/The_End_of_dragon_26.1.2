package com.licht_meilleur.the_end_of_dragon.entity.enderman;

/*
 * エンダーマン個体の恒久的な役割。
 *
 * AllyEndermanStateは、
 * 攻撃・瀕死・手渡しなどの一時的な行動状態。
 *
 * TedAllyEndermanRoleは、
 * その個体が何者なのかを表す恒久情報。
 */
public enum TedAllyEndermanRole {

    /*
     * TED戦で救助される物語上の個体。
     */
    STORY_ALLY,

    /*
     * 村で生活する一般住民。
     */
    VILLAGE_RESIDENT,

    /*
     * 村を守る個体。
     */
    VILLAGE_GUARD,

    /*
     * 将来の用途が不明な個体。
     */
    UNKNOWN
}