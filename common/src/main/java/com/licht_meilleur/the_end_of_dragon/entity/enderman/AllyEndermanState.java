package com.licht_meilleur.the_end_of_dragon.entity.enderman;

public enum AllyEndermanState {
    /*
     * 瀕死状態。
     * 移動・攻撃不可。食料を受け取れる。
     */
    WOUNDED,
    /*
    *
    *
    *
    *
    *
    *
    * アイテムを手渡しする状態
    *
    *
    */
    HAND_OVER,
    /*
     * 必要な食料を受け取り、立ち上がる途中。
     *
     * 現在は専用復活アニメーションがないため、
     * dyingを逆再生せず、短い待機状態として扱う。
     */
    RECOVERING,

    /*
     * 復活後の通常待機。
     */
    SUPPORT_IDLE,

    /*
     * 敵の前へワープする準備。
     */
    ATTACK_WARP_PREPARE,

    /*
     * ワープパンチ。
     */
    WARP_PUNCH,

    /*
     * ワープキック。
     */
    WARP_KICK,

    /*
     * ワープ強打。
     */
    WARP_SMASH,

    /*
     * プレイヤー救助ワープ。
     */
    WITH_PLAYER_WARP,

    /*
     * 戦闘不能。
     */
    DEFEATED,

    /*
     * ボス討伐後の勝利待機。
     */
    VICTORY
}