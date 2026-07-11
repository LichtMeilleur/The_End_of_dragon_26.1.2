package com.licht_meilleur.the_end_of_dragon.entity.beam;

import com.licht_meilleur.the_end_of_dragon.entity.vfx.TedVfxType;

public record TedBeamSpec(
        TedVfxType type,
        float modelScale,
        double hitRadius,
        double maxLength,

        boolean destroyBlocks,
        double destroyRadius,
        double destroyStep,
        int blockBreakInterval,

        int fireStartTick,
        int fireEndTick
) {
}