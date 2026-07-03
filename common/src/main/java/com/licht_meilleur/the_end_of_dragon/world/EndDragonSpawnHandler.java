package com.licht_meilleur.the_end_of_dragon.world;

import com.licht_meilleur.the_end_of_dragon.registry.ModEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class EndDragonSpawnHandler {
    private static final Set<UUID> SPAWNED_FOR_DRAGONS = new HashSet<>();

    private static final int SPAWN_DEATH_TIME = 40;

    public static void tick(ServerLevel level) {
        boolean tedAlreadyExists = !level.getEntitiesOfClass(
                com.licht_meilleur.the_end_of_dragon.entity.TheEndOfDragonCoreEntity.class,
                new AABB(-512, 0, -512, 512, 256, 512)
        ).isEmpty();

        if (tedAlreadyExists) {
            return;
        }

        AABB area = new AABB(-256, 0, -256, 256, 256, 256);

        for (EnderDragon dragon : level.getEntitiesOfClass(EnderDragon.class, area)) {
            if (dragon.dragonDeathTime < SPAWN_DEATH_TIME) continue;
            if (!SPAWNED_FOR_DRAGONS.add(dragon.getUUID())) continue;

            spawn(level, dragon.position());
            return;
        }
    }

    public static void spawn(ServerLevel level, Vec3 pos) {
        try {
            var boss = ModEntities.THE_END_OF_DRAGON.create(level, EntitySpawnReason.EVENT);

            if (boss == null) {
                return;
            }

            var portalCenter = EndPortalSealHandler.findPortalCenter(level);

            Vec3 portalAbove = new Vec3(
                    portalCenter.getX() + 0.5D,
                    portalCenter.getY() + 170.0D,
                    portalCenter.getZ() + 0.5D
            );

            boss.snapTo(pos.x, pos.y + 2.0D, pos.z, 0.0F, 0.0F);
            boss.setHealth(boss.getMaxHealth());
            boss.setPersistenceRequired();

            level.addFreshEntity(boss);

            boss.startIntroSequence(portalAbove);
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

    private EndDragonSpawnHandler() {
    }
}