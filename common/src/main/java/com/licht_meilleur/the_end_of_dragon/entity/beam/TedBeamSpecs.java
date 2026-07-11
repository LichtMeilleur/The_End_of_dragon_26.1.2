package com.licht_meilleur.the_end_of_dragon.entity.beam;

import com.licht_meilleur.the_end_of_dragon.entity.vfx.TedVfxType;

public final class TedBeamSpecs {

    public static final TedBeamSpec PHOTON_BUSTER =
            new TedBeamSpec(
                    TedVfxType.TED_LASER_BEAM,

                    5.0F,      // モデルの太さ
                    5.0D,      // ダメージ判定半径
                    140.0D,    // 最大射程

                    true,      // ブロック破壊
                    2.5D,      // 破壊半径
                    5.0D,      // 射線上の破壊間隔
                    4,         // 4tickごとに破壊

                    25,        // 発射開始
                    60         // 発射終了
            );

    private TedBeamSpecs() {
    }
}