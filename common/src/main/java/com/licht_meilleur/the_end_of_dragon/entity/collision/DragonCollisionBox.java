package com.licht_meilleur.the_end_of_dragon.entity.collision;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public record DragonCollisionBox(
        DragonCollisionPart part,
        AABB box,
        Vec3[] points,
        DragonOBB obb
) {
}