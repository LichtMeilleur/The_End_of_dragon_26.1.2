package com.licht_meilleur.the_end_of_dragon.entity.vfx;

import com.licht_meilleur.the_end_of_dragon.entity.collision.DragonCollisionPart;

public record TedVfxSpec(
        TedVfxType type,
        TedVfxAttachMode mode,
        DragonCollisionPart part,
        double offsetX,
        double offsetY,
        double offsetZ,
        float scale,
        float length,
        float radius,
        float damage,
        int maxAge
) {
}