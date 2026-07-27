package com.licht_meilleur.the_end_of_dragon.world.village;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.entity.RechorusJuiceBlobEntity;
import com.licht_meilleur.the_end_of_dragon.registry.ModBlocks;
import com.licht_meilleur.the_end_of_dragon.registry.ModEntities;
import com.licht_meilleur.the_end_of_dragon.world.block.RechorusFlowerBlock;
import com.licht_meilleur.the_end_of_dragon.world.dimension.TedDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TedRechorusProductionManager {

    /*
     * ========================================
     * 生産設定
     * ========================================
     */

    /*
     * 生産判定間隔。
     *
     * 200tick = 10秒。
     */
    private static final long PRODUCTION_INTERVAL =
            200L;

    /*
     * 1サイクルで消費する水量。
     */
    private static final int WATER_PER_CYCLE =
            100;

    /*
     * 花1個あたりの果汁生産量。
     */
    private static final int JUICE_PER_FLOWER =
            25;

    /*
     * Blob1個の生成に必要な果汁量。
     */
    private static final int JUICE_PER_BLOB =
            1_000;



    /*
     * 1回の生産処理で生成するBlobの最大数。
     *
     * 果汁が大量に蓄積していても、
     * 一度に大量生成しないための制限。
     */
    private static final int MAX_BLOBS_PER_CYCLE =
            1;

    public static void tick(
            ServerLevel level
    ) {
        if (level == null) {
            return;
        }



        /*
         * エンダーマン村以外では動作しない。
         */
        if (!level.dimension().equals(
                TedDimensions.ENDERMAN_VILLAGE
        )) {
            return;
        }

        if (level.getGameTime()
                % PRODUCTION_INTERVAL != 0L) {
            return;
        }

        TedVillageWorldState state =
                TedVillageWorldState.get(level);



        /*
         * 施設側の完成フラグ。
         */
        if (!state.isRechorusPlantBuilt()
                || !state.isRechorusPlantCoreInstalled()) {
            return;
        }

        BlockPos corePosition =
                state.getRechorusPlantCoreSlotPosition();

        if (corePosition == null) {
            return;
        }

        /*
         * 保存されたコア位置に、
         * 実際のコアブロックが存在するか確認。
         */
        if (!level.getBlockState(
                corePosition
        ).is(
                ModBlocks.RECHORUS_PLANT_CORE
        )) {
            return;
        }

        List<BlockPos> flowers =
                TedRechorusPlantManager
                        .getManagedFlowers(
                                level,
                                corePosition
                        );

        if (flowers.isEmpty()) {
            return;
        }

        /*
         * 通常水を消費できない場合は、
         * 果汁を生産しない。
         */
        if (!state.consumeRechorusWater(
                WATER_PER_CYCLE
        )) {
            return;
        }

        int producedJuice =
                flowers.size()
                        * JUICE_PER_FLOWER;

        state.addRechorusPendingJuice(
                producedJuice
        );

        int generatedBlobs = 0;

        /*
         * 果汁量が十分にある間、
         * 設定された上限までBlobを生成する。
         */
        while (state.getRechorusPendingJuice()
                >= JUICE_PER_BLOB
                && generatedBlobs
                < MAX_BLOBS_PER_CYCLE) {

            if (!spawnBlobFromRandomFlower(
                    level,
                    flowers
            )) {
                /*
                 * 全ての花の出口が塞がっている場合。
                 *
                 * 果汁量は消費せず、
                 * 次回へ持ち越す。
                 */
                break;
            }

            state.setRechorusPendingJuice(
                    state.getRechorusPendingJuice()
                            - JUICE_PER_BLOB
            );

            generatedBlobs++;
        }

        if (generatedBlobs > 0) {
            TheEndOfDragon.LOGGER.info(
                    "Produced {} Rechorus Juice Blob(s): core={}, flowers={}, pendingJuice={}",
                    generatedBlobs,
                    corePosition,
                    flowers.size(),
                    state.getRechorusPendingJuice()
            );
        }
    }




    private static boolean spawnBlobFromRandomFlower(
            ServerLevel level,
            List<BlockPos> flowers
    ) {
        if (flowers == null
                || flowers.isEmpty()) {
            return false;
        }

        List<BlockPos> shuffledFlowers =
                new ArrayList<>(
                        flowers
                );

        Collections.shuffle(
                shuffledFlowers,
                new java.util.Random(
                        level.getGameTime()
                                + level.getRandom().nextLong()
                )
        );

        for (BlockPos flowerPosition
                : shuffledFlowers) {

            if (spawnBlobFromFlower(
                    level,
                    flowerPosition
            )) {
                return true;
            }
        }

        return false;
    }

    private static boolean spawnBlobFromFlower(
            ServerLevel level,
            BlockPos flowerPosition
    ) {
        BlockState flowerState =
                level.getBlockState(
                        flowerPosition
                );

        if (!flowerState.is(
                ModBlocks.RECHORUS_FLOWER
        )) {
            return false;
        }

        if (!flowerState.hasProperty(
                RechorusFlowerBlock.FACING
        )) {
            return false;
        }

        if (ModEntities.RECHORUS_JUICE_BLOB
                == null) {
            return false;
        }

        Direction facing =
                flowerState.getValue(
                        RechorusFlowerBlock.FACING
                );

        /*
         * 花が向いている外側を、
         * Blobの生成候補にする。
         *
         * 花ブロック自身の座標には生成しない。
         */
        BlockPos spawnBlockPosition =
                flowerPosition.relative(
                        facing
                );

        if (!canSpawnBlobAt(
                level,
                spawnBlockPosition
        )) {
            return false;
        }

        double spawnX =
                spawnBlockPosition.getX()
                        + 0.5D;

        double spawnY =
                spawnBlockPosition.getY()
                        + 0.5D;

        double spawnZ =
                spawnBlockPosition.getZ()
                        + 0.5D;

        /*
         * 下向きの花は、
         * 花の直下寄りから垂れるようにする。
         */
        if (facing == Direction.DOWN) {
            spawnY =
                    spawnBlockPosition.getY()
                            + 0.85D;
        }

        /*
         * 上向きの花は、
         * 外側マスのやや下側へ配置する。
         *
         * そのまま上へ浮いて見えないようにする。
         */
        if (facing == Direction.UP) {
            spawnY =
                    spawnBlockPosition.getY()
                            + 0.15D;
        }

        RechorusJuiceBlobEntity blob =
                new RechorusJuiceBlobEntity(
                        ModEntities.RECHORUS_JUICE_BLOB,
                        level
                );

        blob.setPos(
                spawnX,
                spawnY,
                spawnZ
        );

        /*
         * Entityの当たり判定としても、
         * 実際に配置可能か確認する。
         */
        if (!level.noCollision(
                blob,
                blob.getBoundingBox()
        )) {
            return false;
        }

        return level.addFreshEntity(
                blob
        );
    }

    private static boolean canSpawnBlobAt(
            ServerLevel level,
            BlockPos position
    ) {
        if (!level.isInWorldBounds(
                position
        )) {
            return false;
        }

        BlockState state =
                level.getBlockState(
                        position
                );

        /*
         * 花や植物を上書きしないため、
         * canBeReplaced()は使用しない。
         *
         * 完全な空気ブロックだけを許可する。
         */
        return state.isAir();
    }

    private TedRechorusProductionManager() {
    }
}