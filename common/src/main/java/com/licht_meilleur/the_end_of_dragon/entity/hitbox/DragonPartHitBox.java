package com.licht_meilleur.the_end_of_dragon.entity.hitbox;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public record DragonPartHitBox(
        DragonHitPart part,
        Vec3 root,
        Vec3 tip,
        AABB aabb
) {
    public static DragonPartHitBox fromLine(DragonHitPart part, Vec3 root, Vec3 tip) {
        double r = part.radius();

        AABB box = new AABB(
                Math.min(root.x, tip.x) - r,
                Math.min(root.y, tip.y) - r,
                Math.min(root.z, tip.z) - r,
                Math.max(root.x, tip.x) + r,
                Math.max(root.y, tip.y) + r,
                Math.max(root.z, tip.z) + r
        );

        return new DragonPartHitBox(part, root, tip, box);
    }
}