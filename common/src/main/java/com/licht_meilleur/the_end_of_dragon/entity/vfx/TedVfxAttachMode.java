package com.licht_meilleur.the_end_of_dragon.entity.vfx;

public enum TedVfxAttachMode {
    PART_BASIS,      // 親Partの位置・回転継承：レーザー/ジェット
    DIRECTION,       // 位置 + 飛行方向：光弾/発射後オーブ
    POSITION_ONLY    // 位置だけ：胸発光/チャージ中オーブ
}