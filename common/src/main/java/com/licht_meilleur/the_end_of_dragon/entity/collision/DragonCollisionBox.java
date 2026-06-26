package com.licht_meilleur.the_end_of_dragon.entity.collision;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public record DragonCollisionBox(
        DragonCollisionPart part,
        AABB aabb,
        Vec3[] points
) {
    public AABB box() {
        return aabb;
    }
}