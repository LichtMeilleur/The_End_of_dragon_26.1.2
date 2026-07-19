package com.licht_meilleur.the_end_of_dragon.world;

import com.licht_meilleur.the_end_of_dragon.entity.DragonSpawnKind;
import com.licht_meilleur.the_end_of_dragon.entity.TheEndOfDragonCoreEntity;
import com.licht_meilleur.the_end_of_dragon.registry.ModEntities;
import net.minecraft.core.BlockPos;
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
        AABB tedSearchArea = new AABB(
                -512.0D,
                level.getMinY(),
                -512.0D,
                512.0D,
                level.getMaxY(),
                512.0D
        );

        var existingBosses =
                level.getEntitiesOfClass(
                        TheEndOfDragonCoreEntity.class,
                        tedSearchArea,
                        boss -> !boss.isRemoved()
                );



        /*
         * 終焉の龍が既に存在する場合、
         * 新しい終焉の龍は生成しない。
         *
         * スポーン停止状態を同期した後にreturnすること。
         */
        if (!existingBosses.isEmpty()) {
            return;
        }

        AABB area = new AABB(
                -256.0D,
                level.getMinY(),
                -256.0D,
                256.0D,
                level.getMaxY(),
                256.0D
        );

        for (EnderDragon dragon :
                level.getEntitiesOfClass(
                        EnderDragon.class,
                        area
                )) {

            if (dragon.dragonDeathTime
                    < SPAWN_DEATH_TIME) {
                continue;
            }

            if (!SPAWNED_FOR_DRAGONS.add(
                    dragon.getUUID()
            )) {
                continue;
            }

            spawn(
                    level,
                    dragon.position()
            );

            return;
        }
    }

    public static void spawn(ServerLevel level, Vec3 pos) {
        try {
            var boss = ModEntities.THE_END_OF_DRAGON.create(level, EntitySpawnReason.EVENT);

            if (boss == null) {
                return;
            }


            BlockPos portalCenter = EndPortalSealHandler.findPortalCenter(level);

            boss.snapTo(pos.x, pos.y + 2.0D, pos.z, 0.0F, 0.0F);
            boss.setHealth(boss.getMaxHealth());
            boss.setPersistenceRequired();

            boss.setSpawnKind(
                    DragonSpawnKind.ENDER_DRAGON_EVENT
            );

            level.addFreshEntity(boss);

            boss.setEnderDragonEventFight(true);



            boss.startIntroSequence(portalCenter);
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

    private EndDragonSpawnHandler() {
    }
}