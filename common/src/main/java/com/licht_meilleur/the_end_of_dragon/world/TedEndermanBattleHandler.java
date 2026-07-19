package com.licht_meilleur.the_end_of_dragon.world;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.entity.enderman.TedAllyEndermanEntity;
import com.licht_meilleur.the_end_of_dragon.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class TedEndermanBattleHandler {

    private static final double CLEAR_RADIUS = 224.0D;

    /*
     * 瀕死エンダーマンを中央から少し離れた場所へ出す。
     */
    private static final double WOUNDED_MIN_RADIUS = 22.0D;
    private static final double WOUNDED_MAX_RADIUS = 42.0D;


    public static void beginBattleEvent(
            ServerLevel level,
            Vec3 center
    ) {
        /*
         * 必ず全滅処理より先に保存状態を有効化する。
         *
         * この時点でNBTへbattleActive=trueが保存対象になる。
         */
        enableSpawnSuppression(level);

        clearNormalEndermen(
                level,
                center
        );

        if (!hasAllyEndermanNearby(
                level,
                center
        )) {
            spawnWoundedAlly(
                    level,
                    center
            );
        }
    }

    private static int clearNormalEndermen(
            ServerLevel level,
            Vec3 center
    ) {
        AABB area = new AABB(
                center.x - CLEAR_RADIUS,
                level.getMinY(),
                center.z - CLEAR_RADIUS,
                center.x + CLEAR_RADIUS,
                level.getMaxY(),
                center.z + CLEAR_RADIUS
        );

        List<EnderMan> endermen =
                level.getEntitiesOfClass(
                        EnderMan.class,
                        area,
                        EnderMan::isAlive
                );
        /*
        System.out.println(
                "[TED ENDERMAN EVENT] found normal endermen="
                        + endermen.size()
                        + " area="
                        + area
        );

         */

        for (EnderMan enderman : endermen) {
            spawnEndermanEraseEffect(
                    level,
                    enderman.position().add(
                            0.0D,
                            enderman.getBbHeight() * 0.5D,
                            0.0D
                    )
            );
            dropEnderPearl(
                    level,
                    enderman
            );

            enderman.discard();
        }

        return endermen.size();
    }

    private static void spawnEndermanEraseEffect(
            ServerLevel level,
            Vec3 position
    ) {
        level.sendParticles(
                ParticleTypes.REVERSE_PORTAL,
                position.x,
                position.y,
                position.z,
                45,
                0.65D,
                1.2D,
                0.65D,
                0.15D
        );

        level.sendParticles(
                ParticleTypes.WITCH,
                position.x,
                position.y,
                position.z,
                18,
                0.4D,
                0.8D,
                0.4D,
                0.06D
        );

        level.sendParticles(
                ParticleTypes.END_ROD,
                position.x,
                position.y,
                position.z,
                8,
                0.25D,
                0.6D,
                0.25D,
                0.04D
        );
    }

    private static void dropEnderPearl(
            ServerLevel level,
            EnderMan enderman
    ) {
        ItemEntity itemEntity =
                new ItemEntity(
                        level,
                        enderman.getX(),
                        enderman.getY() + 0.5D,
                        enderman.getZ(),
                        new ItemStack(
                                Items.ENDER_PEARL
                        )
                );

        itemEntity.setDefaultPickUpDelay();

        level.addFreshEntity(
                itemEntity
        );
    }

    private static boolean hasAllyEndermanNearby(
            ServerLevel level,
            Vec3 center
    ) {
        AABB area = new AABB(
                center,
                center
        ).inflate(
                CLEAR_RADIUS,
                128.0D,
                CLEAR_RADIUS
        );

        return !level.getEntitiesOfClass(
                TedAllyEndermanEntity.class,
                area,
                entity -> entity.isAlive()
                        && !entity.isRemoved()
        ).isEmpty();
    }

    private static void spawnWoundedAlly(
            ServerLevel level,
            Vec3 center
    ) {
        TedAllyEndermanEntity ally =
                ModEntities.TED_ALLY_ENDERMAN.create(
                        level,
                        EntitySpawnReason.EVENT
                );

        if (ally == null) {
            /*
            System.out.println(
                    "[TED ENDERMAN EVENT] ally create returned null"
            );

             */
            return;
        }

        Vec3 spawnPosition =
                findWoundedSpawnPosition(
                        level,
                        center
                );

        ally.snapTo(
                spawnPosition.x,
                spawnPosition.y,
                spawnPosition.z,
                level.getRandom().nextFloat()
                        * 360.0F,
                0.0F
        );

        ally.setPersistenceRequired();
        ally.setWoundedForBattle();

        boolean added =
                level.addFreshEntity(ally);


        if (added) {
            spawnWoundedSpawnEffect(
                    level,
                    spawnPosition
            );
        }
    }

    private static Vec3 findWoundedSpawnPosition(
            ServerLevel level,
            Vec3 center
    ) {
        for (int attempt = 0;
             attempt < 32;
             attempt++) {

            double angle =
                    level.getRandom().nextDouble()
                            * Math.PI
                            * 2.0D;

            double radius =
                    WOUNDED_MIN_RADIUS
                            + level.getRandom().nextDouble()
                            * (
                            WOUNDED_MAX_RADIUS
                                    - WOUNDED_MIN_RADIUS
                    );

            int x = (int) Math.floor(
                    center.x
                            + Math.cos(angle)
                            * radius
            );

            int z = (int) Math.floor(
                    center.z
                            + Math.sin(angle)
                            * radius
            );

            int surfaceY =
                    level.getHeight(
                            net.minecraft.world.level.levelgen
                                    .Heightmap.Types
                                    .MOTION_BLOCKING_NO_LEAVES,
                            x,
                            z
                    );

            BlockPos floor =
                    new BlockPos(
                            x,
                            surfaceY - 1,
                            z
                    );

            BlockPos feet =
                    floor.above();

            BlockPos head =
                    feet.above();

            /*
             * 奈落や不正な場所を避ける。
             */
            if (surfaceY <= level.getMinY() + 8) {
                continue;
            }

            if (level.getBlockState(floor).isAir()) {
                continue;
            }

            if (!level.getBlockState(feet).isAir()) {
                continue;
            }

            if (!level.getBlockState(head).isAir()) {
                continue;
            }

            return new Vec3(
                    x + 0.5D,
                    surfaceY,
                    z + 0.5D
            );
        }

        /*
         * 見つからない場合は中央付近へ。
         */
        int fallbackX =
                (int) Math.floor(center.x + 12.0D);

        int fallbackZ =
                (int) Math.floor(center.z);

        int fallbackY =
                level.getHeight(
                        net.minecraft.world.level.levelgen
                                .Heightmap.Types
                                .MOTION_BLOCKING_NO_LEAVES,
                        fallbackX,
                        fallbackZ
                );

        return new Vec3(
                fallbackX + 0.5D,
                fallbackY,
                fallbackZ + 0.5D
        );

    }

    private static void spawnWoundedSpawnEffect(
            ServerLevel level,
            Vec3 position
    ) {
        level.sendParticles(
                ParticleTypes.REVERSE_PORTAL,
                position.x,
                position.y + 1.4D,
                position.z,
                65,
                0.8D,
                1.4D,
                0.8D,
                0.12D
        );

        level.sendParticles(
                ParticleTypes.END_ROD,
                position.x,
                position.y + 1.4D,
                position.z,
                16,
                0.4D,
                0.8D,
                0.4D,
                0.05D
        );
    }

    private TedEndermanBattleHandler() {
    }




    public static void enableSpawnSuppression(
            ServerLevel level
    ) {
        TedBattleController.startBattle(level);
    }


    public static boolean shouldBlockNormalEndermanSpawn(
            ServerLevel level,
            Vec3 position
    ) {
        TedBattleWorldState worldState =
                TedBattleWorldState.get(level);

        /*
         * 戦闘中、または討伐後の余韻時間中は、
         * エンド全域で一般エンダーマンのスポーンを止める。
         *
         * positionはFabric／NeoForge側の呼び出し形式を
         * 変更しないために残している。
         */
        return worldState.shouldSuppressEndermanSpawns(
                level
        );
    }

    public static boolean spawnPostBattleWoundedEnderman(
            ServerLevel level,
            Vec3 fallbackCenter
    ) {
        TedBattleWorldState state =
                TedBattleWorldState.get(level);

        if (state.getAllyProgress()
                != TedBattleWorldState
                .TedAllyProgress
                .WOUNDED_AFTER_BATTLE) {

            TheEndOfDragon.LOGGER.warn(
                    "Skipped post-battle ally spawn: progress={}",
                    state.getAllyProgress()
            );

            return false;
        }

        Player player =
                level.getNearestPlayer(
                        fallbackCenter.x,
                        fallbackCenter.y,
                        fallbackCenter.z,
                        256.0D,
                        false
                );

        Vec3 searchCenter =
                player != null
                        ? player.position()
                        : fallbackCenter;

        BlockPos spawnPos =
                findSafePostBattleSpawnPosition(
                        level,
                        searchCenter
                );

        if (spawnPos == null) {
            TheEndOfDragon.LOGGER.error(
                    "Failed to find a safe post-battle ally spawn near {}",
                    searchCenter
            );

            return false;
        }

        /*
         * 対象チャンクを読み込んでから生成する。
         */
        level.getChunkAt(
                spawnPos
        );

        TedAllyEndermanEntity ally =
                ModEntities.TED_ALLY_ENDERMAN.create(
                        level,
                        EntitySpawnReason.EVENT
                );

        if (ally == null) {
            TheEndOfDragon.LOGGER.error(
                    "Failed to create TED ally Enderman entity"
            );

            return false;
        }

        ally.snapTo(
                spawnPos.getX() + 0.5D,
                spawnPos.getY(),
                spawnPos.getZ() + 0.5D,
                0.0F,
                0.0F
        );

        /*
         * 先にワールドへ追加する。
         */
        ally.setPersistenceRequired();
        ally.setWoundedAfterBattle();

        boolean added =
                level.addFreshEntity(
                        ally
                );

        if (!added) {
            TheEndOfDragon.LOGGER.error(
                    "Failed to add post-battle ally Enderman at {}",
                    spawnPos
            );

            ally.discard();

            return false;
        }

        /*
         * ワールド追加後に瀕死状態へする。
         */
        ally.setWoundedAfterBattle();

        TheEndOfDragon.LOGGER.info(
                "Spawned post-battle wounded ally Enderman at {}",
                spawnPos
        );

        if (player != null) {
            player.sendSystemMessage(
                    Component.translatable(
                            "message.the_end_of_dragon.ally_faint_voice"
                    )
            );
        }

        return true;
    }

    private static BlockPos findSafePostBattleSpawnPosition(
            ServerLevel level,
            Vec3 center
    ) {
        RandomSource random =
                level.getRandom();

        /*
         * 24～40ブロック離れた位置を探す。
         *
         * 40～70だと地形や描画距離によっては
         * 発見しづらいため少し近づける。
         */
        for (int attempt = 0;
             attempt < 64;
             attempt++) {

            double angle =
                    random.nextDouble()
                            * Math.PI
                            * 2.0D;

            double distance =
                    24.0D
                            + random.nextDouble()
                            * 16.0D;

            int x =
                    Mth.floor(
                            center.x
                                    + Math.cos(angle)
                                    * distance
                    );

            int z =
                    Mth.floor(
                            center.z
                                    + Math.sin(angle)
                                    * distance
                    );

            int groundY =
                    level.getHeight(
                            Heightmap.Types
                                    .MOTION_BLOCKING_NO_LEAVES,
                            x,
                            z
                    );

            BlockPos feetPos =
                    new BlockPos(
                            x,
                            groundY,
                            z
                    );

            BlockPos headPos =
                    feetPos.above();

            BlockPos floorPos =
                    feetPos.below();

            /*
             * 足元が空気。
             */
            if (!level.getBlockState(feetPos)
                    .isAir()) {
                continue;
            }

            /*
             * 頭部分も空気。
             */
            if (!level.getBlockState(headPos)
                    .isAir()) {
                continue;
            }

            /*
             * 下が空気や液体なら不可。
             */
            if (level.getBlockState(floorPos)
                    .isAir()) {
                continue;
            }

            if (!level.getFluidState(feetPos)
                    .isEmpty()) {
                continue;
            }

            if (!level.getFluidState(floorPos)
                    .isEmpty()) {
                continue;
            }

            /*
             * 奈落付近や異常な高さを避ける。
             */
            if (groundY
                    <= level.getMinY() + 5) {
                continue;
            }

            return feetPos;
        }

        return null;
    }


}