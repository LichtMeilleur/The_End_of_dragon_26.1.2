package com.licht_meilleur.the_end_of_dragon.entity.hitbox;

public record DragonHitPart(
        String name,
        String rootLocator,
        String tipLocator,
        double radius,
        double damageMultiplier
) {
}