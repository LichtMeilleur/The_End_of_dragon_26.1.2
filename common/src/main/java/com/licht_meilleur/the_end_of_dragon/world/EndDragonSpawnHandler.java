package com.licht_meilleur.the_end_of_dragon.world;

import com.licht_meilleur.the_end_of_dragon.registry.ModEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.phys.Vec3;

public final class EndDragonSpawnHandler {
    public static void spawn(ServerLevel level, Vec3 pos) {
        var boss = ModEntities.THE_END_OF_DRAGON.create(level, EntitySpawnReason.EVENT);
        if (boss == null) {
            return;
        }

        boss.snapTo(pos.x, pos.y + 8.0D, pos.z, 0.0F, 0.0F);
        boss.setHealth(boss.getMaxHealth());
        boss.setPersistenceRequired();

        level.addFreshEntity(boss);
        EndPortalSealHandler.seal(level);
    }

    private EndDragonSpawnHandler() {
    }
}