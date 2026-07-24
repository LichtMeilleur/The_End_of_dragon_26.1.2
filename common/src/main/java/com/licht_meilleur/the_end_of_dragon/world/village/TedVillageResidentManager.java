package com.licht_meilleur.the_end_of_dragon.world.village;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.entity.enderman
        .AllyEndermanState;
import com.licht_meilleur.the_end_of_dragon.entity.enderman
        .TedAllyEndermanEntity;
import com.licht_meilleur.the_end_of_dragon.entity.enderman
        .TedAllyEndermanRole;
import com.licht_meilleur.the_end_of_dragon.entity.enderman.village
        .TedElderEndermanEntity;
import com.licht_meilleur.the_end_of_dragon.entity.enderman.village
        .TedTechEndermanEntity;
import com.licht_meilleur.the_end_of_dragon.registry.ModEntities;
import com.licht_meilleur.the_end_of_dragon.world.TedBattleWorldState;
import com.licht_meilleur.the_end_of_dragon.world.dimension.TedDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public final class TedVillageResidentManager {

    private static final BlockPos ELDER_POSITION =
            new BlockPos(
                    0,
                    64,
                    -14
            );

    private static final BlockPos TECHNICIAN_POSITION =
            new BlockPos(
                    -24,
                    64,
                    16
            );

    private static final BlockPos STORY_ALLY_POSITION =
            new BlockPos(
                    4,
                    64,
                    -12
            );

    public static void tick(
            ServerLevel level
    ) {
        if (!level.dimension()
                .equals(
                        TedDimensions.ENDERMAN_VILLAGE
                )) {
            return;
        }

        if (level.getGameTime() % 20L != 0L) {
            return;
        }

        TedVillageWorldState villageState =
                TedVillageWorldState.get(level);

        if (!villageState.isVillageGenerated()) {
            return;
        }

        /*
         * 村が無人なら、未ロード住民を
         * 不在扱いして再生成しない。
         */
        if (level.players().isEmpty()) {
            return;
        }

        /*
         * 過去に増殖した個体を整理。
         */
        removeDuplicateResidents(level);

        ensureElder(level);
        ensureTechnician(level);
        ensureStoryAlly(level);
    }

    private static void ensureElder(
            ServerLevel level
    ) {
        if (hasEntityNear(
                level,
                TedElderEndermanEntity.class,
                ELDER_POSITION
        )) {
            return;
        }

        TedElderEndermanEntity elder =
                ModEntities
                        .TED_ELDER_ENDERMAN
                        .create(
                                level,
                                EntitySpawnReason.TRIGGERED
                        );

        if (elder == null) {
            return;
        }

        placeEntity(
                level,
                elder,
                ELDER_POSITION
        );

        TheEndOfDragon.LOGGER.info(
                "Spawned Enderman elder at {}",
                ELDER_POSITION
        );
    }

    private static void ensureTechnician(
            ServerLevel level
    ) {
        if (hasEntityNear(
                level,
                TedTechEndermanEntity.class,
                TECHNICIAN_POSITION
        )) {
            return;
        }

        TedTechEndermanEntity technician =
                ModEntities
                        .TED_TECH_ENDERMAN
                        .create(
                                level,
                                EntitySpawnReason.TRIGGERED
                        );

        if (technician == null) {
            return;
        }

        placeEntity(
                level,
                technician,
                TECHNICIAN_POSITION
        );

        TheEndOfDragon.LOGGER.info(
                "Spawned Enderman technician at {}",
                TECHNICIAN_POSITION
        );
    }

    private static void ensureStoryAlly(
            ServerLevel villageLevel
    ) {
        if (hasEntityNear(
                villageLevel,
                TedAllyEndermanEntity.class,
                STORY_ALLY_POSITION
        )) {
            return;
        }

        ServerLevel endLevel =
                villageLevel.getServer()
                        .getLevel(Level.END);

        if (endLevel == null) {
            return;
        }

        TedBattleWorldState battleState =
                TedBattleWorldState.get(
                        endLevel
                );

        /*
         * 門を渡し終えてから村へ移住。
         */
        if (battleState.getAllyProgress()
                != TedBattleWorldState
                .TedAllyProgress.ITEM_GIVEN) {
            return;
        }

        AABB endSearchArea =
                new AABB(
                        -512.0D,
                        endLevel.getMinY(),
                        -512.0D,
                        512.0D,
                        endLevel.getMaxY(),
                        512.0D
                );

        TedAllyEndermanEntity existingAlly =
                endLevel.getEntitiesOfClass(
                                TedAllyEndermanEntity.class,
                                endSearchArea,
                                entity ->
                                        entity.isAlive()
                                                && entity.isStoryAlly()
                        )
                        .stream()
                        .findFirst()
                        .orElse(null);

        if (existingAlly != null) {
            /*
             * 同じEntityを別ディメンションへ移動。
             */
            boolean moved =
                    existingAlly.teleportTo(
                            villageLevel,
                            STORY_ALLY_POSITION.getX()
                                    + 0.5D,
                            STORY_ALLY_POSITION.getY(),
                            STORY_ALLY_POSITION.getZ()
                                    + 0.5D,
                            java.util.Set.of(),
                            existingAlly.getYRot(),
                            existingAlly.getXRot(),
                            true
                    );

            if (moved) {
                existingAlly.setAllyRole(
                        TedAllyEndermanRole
                                .VILLAGE_RESIDENT
                );

                existingAlly.setAllyState(
                        AllyEndermanState
                                .SUPPORT_IDLE
                );

                existingAlly.setPersistenceRequired();

                TheEndOfDragon.LOGGER.info(
                        "Moved story ally Enderman to village"
                );
            }

            return;
        }

        /*
         * End側Entityが既に消えていた場合の復旧。
         */
        TedAllyEndermanEntity recreated =
                ModEntities.TED_ALLY_ENDERMAN
                        .create(
                                villageLevel,
                                EntitySpawnReason.TRIGGERED
                        );

        if (recreated == null) {
            return;
        }

        recreated.setAllyRole(
                TedAllyEndermanRole.VILLAGE_RESIDENT
        );

        recreated.setAllyState(
                AllyEndermanState.SUPPORT_IDLE
        );

        placeEntity(
                villageLevel,
                recreated,
                STORY_ALLY_POSITION
        );

        TheEndOfDragon.LOGGER.warn(
                "Recreated missing story ally in Enderman village"
        );
    }

    private static void placeEntity(
            ServerLevel level,
            net.minecraft.world.entity.PathfinderMob entity,
            BlockPos position
    ) {
        level.getChunk(
                position
        );

        entity.snapTo(
                position.getX() + 0.5D,
                position.getY(),
                position.getZ() + 0.5D,
                level.getRandom()
                        .nextFloat()
                        * 360.0F,
                0.0F
        );

        entity.setPersistenceRequired();

        if (!level.addFreshEntity(
                entity
        )) {
            entity.discard();
        }
    }

    private static <T extends net.minecraft.world.entity.Entity>
    boolean hasEntityNear(
            ServerLevel level,
            Class<T> entityClass,
            BlockPos position
    ) {
        AABB area =
                new AABB(position)
                        .inflate(
                                64.0D,
                                32.0D,
                                64.0D
                        );

        return !level.getEntitiesOfClass(
                entityClass,
                area,
                entity ->
                        entity.isAlive()
                                && !entity.isRemoved()
        ).isEmpty();
    }

    private static void removeDuplicateResidents(
            ServerLevel level
    ) {
        removeDuplicateEntities(
                level,
                TedElderEndermanEntity.class,
                ELDER_POSITION
        );

        removeDuplicateEntities(
                level,
                TedTechEndermanEntity.class,
                TECHNICIAN_POSITION
        );
    }

    private static <
            T extends net.minecraft.world.entity.Entity>
    void removeDuplicateEntities(
            ServerLevel level,
            Class<T> entityClass,
            BlockPos preferredPosition
    ) {
        AABB area =
                new AABB(
                        -256.0D,
                        level.getMinY(),
                        -256.0D,
                        256.0D,
                        level.getMaxY(),
                        256.0D
                );

        java.util.List<T> entities =
                level.getEntitiesOfClass(
                        entityClass,
                        area,
                        entity ->
                                entity.isAlive()
                                        && !entity.isRemoved()
                );

        if (entities.size() <= 1) {
            return;
        }

        /*
         * 本来の配置座標に一番近い個体を残す。
         */
        T keeper =
                entities.stream()
                        .min(
                                java.util.Comparator
                                        .comparingDouble(
                                                entity ->
                                                        entity.distanceToSqr(
                                                                preferredPosition
                                                                        .getCenter()
                                                        )
                                        )
                        )
                        .orElse(null);

        for (T entity : entities) {
            if (entity == keeper) {
                continue;
            }

            entity.discard();
        }

        TheEndOfDragon.LOGGER.warn(
                "Removed {} duplicate village residents of type {}",
                entities.size() - 1,
                entityClass.getSimpleName()
        );
    }

    private TedVillageResidentManager() {
    }
}