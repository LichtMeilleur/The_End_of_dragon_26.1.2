package com.licht_meilleur.the_end_of_dragon.entity.hitbox;

import com.licht_meilleur.the_end_of_dragon.entity.TheEndOfDragonEntity;
import com.licht_meilleur.the_end_of_dragon.registry.ModEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public final class DragonHitBoxManager {
    public static void spawnParts(TheEndOfDragonEntity dragon) {
        if (!(dragon.level() instanceof ServerLevel level)) {
            return;
        }

        add(level, dragon, "upper_body", 0, 4.0, 0, 2.0, 2.5, 1.0);
        add(level, dragon, "head", 0, 7.0, -1.7, 2.2, 2.0, 1.25);

        add(level, dragon, "front_left_arm", -3.2, 5.0, -1.0, 2.2, 2.0, 0.9);
        add(level, dragon, "front_left_fore_arm", -5.0, 4.5, -1.0, 2.2, 2.0, 0.9);
        add(level, dragon, "front_left_hand", -6.5, 4.0, -1.0, 2.4, 2.0, 0.95);

        add(level, dragon, "front_right_arm", 3.2, 5.0, -1.0, 2.2, 2.0, 0.9);
        add(level, dragon, "front_right_fore_arm", 5.0, 4.5, -1.0, 2.2, 2.0, 0.9);
        add(level, dragon, "front_right_hand", 6.5, 4.0, -1.0, 2.4, 2.0, 0.95);

        add(level, dragon, "left_leg", -1.2, 1.8, 0.4, 1.6, 2.5, 0.85);
        add(level, dragon, "right_leg", 1.2, 1.8, 0.4, 1.6, 2.5, 0.85);

        add(level, dragon, "tail_root", 0, 2.5, 3.0, 2.0, 1.5, 0.75);
        add(level, dragon, "tail_tip", 0, 2.0, 5.5, 1.5, 1.2, 0.7);
    }

    private static void add(ServerLevel level, TheEndOfDragonEntity dragon, String name,
                            double ox, double oy, double oz,
                            double width, double height,
                            double multiplier) {
        DragonHitBoxEntity part = new DragonHitBoxEntity(ModEntities.DRAGON_HITBOX, level);
        part.setup(dragon, name, new Vec3(ox, oy, oz), width, height, multiplier);
        level.addFreshEntity(part);
    }

    private DragonHitBoxManager() {
    }
}