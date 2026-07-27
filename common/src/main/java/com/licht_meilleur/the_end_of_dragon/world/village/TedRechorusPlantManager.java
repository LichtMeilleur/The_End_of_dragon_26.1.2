package com.licht_meilleur.the_end_of_dragon.world.village;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.registry.ModBlocks;
import com.licht_meilleur.the_end_of_dragon.world.block.RechorusFlowerBlock;
import com.licht_meilleur.the_end_of_dragon.world.block.entity.RechorusPlantCoreBlockEntity;
import com.licht_meilleur.the_end_of_dragon.world.dimension.TedDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public final class TedRechorusPlantManager {

    /*
     * ========================================
     * 基本設定
     * ========================================
     */
    /*
     * root・plantを1個再生するための水量。
     */
    private static final int WATER_PER_REGENERATED_PART =
            250;

    /*
     * 花を1個再生するための水量。
     */
    private static final int WATER_PER_REGENERATED_FLOWER =
            150;

    /*
     * 1回に再生するroot・plant数。
     */
    private static final int MAX_PARTS_PER_CYCLE =
            1;

    /*
     * 1回に再生する花数。
     */
    private static final int MAX_FLOWERS_PER_CYCLE =
            1;

    /*
     * 花の最大数。
     *
     * 既にTreePlacer側に同じ設定があるなら、
     * そちらを参照して構いません。
     */
    private static final int MAX_FLOWER_COUNT =
            8;
    /*
     * 判定間隔。
     *
     * 20tick = 1秒。
     */
    private static final long UPDATE_INTERVAL =
            20L;

    /*
     * コアが直接水源を探す範囲。
     *
     * 転送装置Bから出た水がコアへ触れる用途を想定。
     */
    private static final int WATER_SEARCH_RADIUS =
            2;

    /*
     * コア内部の最大水量。
     */
    public static final int CORE_WATER_CAPACITY =
            16_000;

    /*
     * 水源1個から1回に吸収する量。
     *
     * 水源そのものは削除しない。
     */
    private static final int WATER_PER_ABSORPTION =
            250;

    /*
     * プラント全体が成長するために必要な水量。
     */
    private static final int WATER_REQUIRED_TO_GROW =
            4_000;


    /*
     * 1回に再生するroot / plant数。
     */
    private static final int MAX_REGENERATED_PARTS_PER_CYCLE =
            1;

    /*
     * 1回に再生する花数。
     */
    private static final int MAX_REGENERATED_FLOWERS_PER_CYCLE =
            1;

    /*
     * コア消失時、1回に崩壊させる部品数。
     */
    private static final int COLLAPSE_PARTS_PER_CYCLE =
            3;

    public static void tick(
            ServerLevel level
    ) {
        if (level == null) {
            return;
        }

        if (!level.dimension().equals(
                TedDimensions.ENDERMAN_VILLAGE
        )) {
            return;
        }

        if (level.getGameTime()
                % UPDATE_INTERVAL != 0L) {
            return;
        }

        TedVillageWorldState state =
                TedVillageWorldState.get(
                        level
                );

        BlockPos corePosition =
                state.getRechorusPlantCoreSlotPosition();

        if (corePosition == null
                || corePosition.equals(
                BlockPos.ZERO
        )) {
            return;
        }

        boolean coreExists =
                level.getBlockState(
                        corePosition
                ).is(
                        ModBlocks.RECHORUS_PLANT_CORE
                );

        /*
         * コアが失われた場合は、
         * 管理していたプラントを崩壊させる。
         */
        if (!coreExists) {
            collapsePlantWithoutCore(
                    level,
                    state,
                    corePosition
            );

            return;
        }

        /*
         * 実ブロックが存在するので、
         * 設置状態を同期。
         */
        if (!state.isRechorusPlantCoreInstalled()) {
            state.setRechorusPlantCoreInstalled(
                    true
            );
        }

        /*
         * コアへ接触している通常水源から吸水。
         */
        absorbTouchingWater(
                level,
                state,
                corePosition
        );

        /*
         * まだ成長していなければ、
         * 必要水量へ達した時点で一括成長。
         */
        if (!state.isRechorusPlantBuilt()) {
            tryGrowPlant(
                    level,
                    state,
                    corePosition
            );

            return;
        }

        /*
         * 成長済みプラントの設計位置を取得。
         */
        List<TedRechorusTreePlacer.PlantPartTarget>
                targets =
                TedRechorusTreePlacer
                        .getPlantPartTargets(
                                level,
                                corePosition
                        );

        if (targets.isEmpty()) {
            return;
        }

        /*
         * 欠損したroot / plantを再生。
         */
        regenerateConnectedParts(
                level,
                state,
                corePosition,
                targets
        );

        Set<BlockPos> connectedParts =
                getConnectedManagedPlantParts(
                        level,
                        corePosition
                );

        /*
         * 花数が上限未満なら再生。
         */
        regenerateFlowers(
                level,
                state,
                corePosition,
                connectedParts
        );
    }

    /*
     * ========================================
     * コア吸水
     * ========================================
     */

    private static void absorbTouchingWater(
            ServerLevel level,
            TedVillageWorldState state,
            BlockPos corePosition
    ) {
        if (state.getRechorusStoredWater()
                >= CORE_WATER_CAPACITY) {
            return;
        }

        /*
         * コアへ接している水を優先する。
         *
         * DOWNは台座があるため除外。
         */
        Direction[] directions = {
                Direction.UP,
                Direction.NORTH,
                Direction.SOUTH,
                Direction.WEST,
                Direction.EAST
        };

        for (Direction direction : directions) {
            BlockPos waterPosition =
                    corePosition.relative(
                            direction
                    );

            if (!isNormalWaterSource(
                    level,
                    waterPosition
            )) {
                continue;
            }

            state.addRechorusWater(
                    WATER_PER_ABSORPTION,
                    CORE_WATER_CAPACITY
            );
            

            /*
             * 1回のtick処理では1方向からだけ吸う。
             */
            return;
        }

        /*
         * コアに直接隣接しなくても、
         * ごく近距離の水源を補助的に検索する。
         *
         * 不要ならこの部分は削除可能。
         */
        BlockPos minimum =
                corePosition.offset(
                        -WATER_SEARCH_RADIUS,
                        -WATER_SEARCH_RADIUS,
                        -WATER_SEARCH_RADIUS
                );

        BlockPos maximum =
                corePosition.offset(
                        WATER_SEARCH_RADIUS,
                        WATER_SEARCH_RADIUS,
                        WATER_SEARCH_RADIUS
                );

        BlockPos nearestWater =
                null;

        double nearestDistance =
                Double.MAX_VALUE;

        for (BlockPos position
                : BlockPos.betweenClosed(
                minimum,
                maximum
        )) {

            /*
             * コアの下側は台座として扱うため除外。
             */
            if (position.getY()
                    < corePosition.getY()) {
                continue;
            }

            if (!isNormalWaterSource(
                    level,
                    position
            )) {
                continue;
            }

            double distance =
                    position.distSqr(
                            corePosition
                    );

            if (distance >= nearestDistance) {
                continue;
            }

            nearestDistance =
                    distance;

            nearestWater =
                    position.immutable();
        }

        if (nearestWater == null) {
            return;
        }

        state.addRechorusWater(
                WATER_PER_ABSORPTION,
                CORE_WATER_CAPACITY
        );

    }

    private static boolean isNormalWaterSource(
            ServerLevel level,
            BlockPos position
    ) {
        FluidState fluidState =
                level.getFluidState(
                        position
                );

        return fluidState.is(
                Fluids.WATER
        )
                && fluidState.isSource();
    }

    /*
     * ========================================
     * 初回成長
     * ========================================
     */

    private static void tryGrowPlant(
            ServerLevel level,
            TedVillageWorldState state,
            BlockPos corePosition
    ) {
        if (state.getRechorusStoredWater()
                < WATER_REQUIRED_TO_GROW) {
            return;
        }

        boolean placed =
                TedRechorusTreePlacer.placeAtCore(
                        level,
                        corePosition
                );

        if (!placed) {
            TheEndOfDragon.LOGGER.warn(
                    "Failed to grow Rechorus plant at core {}",
                    corePosition
            );

            return;
        }

        /*
         * 成長成功後にだけ水を消費する。
         */
        if (!state.consumeRechorusWater(
                WATER_REQUIRED_TO_GROW
        )) {
            /*
             * 通常ここには来ない。
             */
            TheEndOfDragon.LOGGER.error(
                    "Rechorus plant grew but water consumption failed at {}",
                    corePosition
            );
        }

        state.setRechorusPlantBuilt(
                true
        );

        state.setRechorusPlantCoreInstalled(
                true
        );

        TheEndOfDragon.LOGGER.info(
                "Rechorus plant grew: core={}, consumedWater={}, remainingWater={}",
                corePosition,
                WATER_REQUIRED_TO_GROW,
                state.getRechorusStoredWater()
        );
    }

    /*
     * ========================================
     * root / plant再生
     * ========================================
     */

    private static void regenerateConnectedParts(
            ServerLevel level,
            TedVillageWorldState state,
            BlockPos corePosition,
            List<TedRechorusTreePlacer.PlantPartTarget>
                    targets
    ) {
        if (state.getRechorusStoredWater()
                < WATER_PER_REGENERATED_PART) {
            return;
        }

        Set<BlockPos> validTargetPositions =
                createTargetPositionSet(
                        targets
                );

        Set<BlockPos> connectedParts =
                collectConnectedExistingParts(
                        level,
                        corePosition,
                        validTargetPositions
                );

        List<TedRechorusTreePlacer.PlantPartTarget>
                candidates =
                new ArrayList<>();

        for (TedRechorusTreePlacer.PlantPartTarget target
                : targets) {

            BlockState current =
                    level.getBlockState(
                            target.position()
                    );

            if (!current.isAir()) {
                continue;
            }

            if (!isAdjacentToManagedNetwork(
                    target.position(),
                    corePosition,
                    connectedParts
            )) {
                continue;
            }

            candidates.add(
                    target
            );
        }

        if (candidates.isEmpty()) {
            return;
        }

        /*
         * コアに近い部品から再生する。
         */
        candidates.sort(
                Comparator.comparingDouble(
                        target ->
                                target.position()
                                        .distSqr(
                                                corePosition
                                        )
                )
        );

        int regenerated =
                0;

        for (TedRechorusTreePlacer.PlantPartTarget
                target : candidates) {

            if (regenerated
                    >= MAX_REGENERATED_PARTS_PER_CYCLE) {
                break;
            }

            if (state.getRechorusStoredWater()
                    < WATER_PER_REGENERATED_PART) {
                break;
            }

            if (!level.getBlockState(
                    target.position()
            ).isAir()) {
                continue;
            }

            boolean placed =
                    level.setBlock(
                            target.position(),
                            target.state(),
                            3
                    );

            if (!placed) {
                continue;
            }

            if (!state.consumeRechorusWater(
                    WATER_PER_REGENERATED_PART
            )) {
                level.setBlock(
                        target.position(),
                        Blocks.AIR.defaultBlockState(),
                        3
                );

                break;
            }

            connectedParts.add(
                    target.position()
                            .immutable()
            );

            regenerated++;

            TheEndOfDragon.LOGGER.info(
                    "Regenerated Rechorus part: core={}, position={}, water={}",
                    corePosition,
                    target.position(),
                    state.getRechorusStoredWater()
            );
        }
    }

    /*
     * ========================================
     * 花再生
     * ========================================
     */

    private static void regenerateFlowers(
            ServerLevel level,
            TedVillageWorldState state,
            BlockPos corePosition,
            Set<BlockPos> connectedParts
    ) {
        if (connectedParts.isEmpty()) {
            return;
        }

        int maximumFlowers =
                TedRechorusTreePlacer
                        .getMaximumFlowerCount();

        Set<BlockPos> currentFlowers =
                getManagedFlowerPositions(
                        level,
                        connectedParts
                );

        if (currentFlowers.size()
                >= maximumFlowers) {
            return;
        }

        if (state.getRechorusStoredWater()
                < WATER_PER_REGENERATED_FLOWER) {
            return;
        }

        List<FlowerCandidate> candidates =
                new ArrayList<>();

        for (BlockPos plantPosition
                : connectedParts) {

            if (!level.getBlockState(
                    plantPosition
            ).is(
                    ModBlocks.RECHORUS_PLANT
            )) {
                continue;
            }

            for (Direction direction
                    : Direction.values()) {

                BlockPos flowerPosition =
                        plantPosition.relative(
                                direction
                        );

                if (!level.getBlockState(
                        flowerPosition
                ).isAir()) {
                    continue;
                }

                candidates.add(
                        new FlowerCandidate(
                                flowerPosition.immutable(),
                                direction
                        )
                );
            }
        }

        if (candidates.isEmpty()) {
            return;
        }

        Collections.shuffle(
                candidates,
                new Random(
                        level.getGameTime()
                                ^ corePosition.asLong()
                )
        );

        int regenerated =
                0;

        for (FlowerCandidate candidate
                : candidates) {

            if (regenerated
                    >= MAX_REGENERATED_FLOWERS_PER_CYCLE) {
                break;
            }

            if (currentFlowers.size()
                    >= maximumFlowers) {
                break;
            }

            BlockState flowerState =
                    ModBlocks.RECHORUS_FLOWER
                            .defaultBlockState()
                            .setValue(
                                    RechorusFlowerBlock.FACING,
                                    candidate.direction()
                            );

            boolean placed =
                    level.setBlock(
                            candidate.position(),
                            flowerState,
                            3
                    );

            if (!placed) {
                continue;
            }

            if (!state.consumeRechorusWater(
                    WATER_PER_REGENERATED_FLOWER
            )) {
                level.setBlock(
                        candidate.position(),
                        Blocks.AIR.defaultBlockState(),
                        3
                );

                break;
            }

            currentFlowers.add(
                    candidate.position()
                            .immutable()
            );

            regenerated++;

        }
    }

    /*
     * ========================================
     * コア消失時の崩壊
     * ========================================
     */

    private static void collapsePlantWithoutCore(
            ServerLevel level,
            TedVillageWorldState state,
            BlockPos corePosition
    ) {
        List<TedRechorusTreePlacer.PlantPartTarget>
                targets =
                TedRechorusTreePlacer
                        .getPlantPartTargets(
                                level,
                                corePosition
                        );

        /*
         * 先に管理対象plantへ付いている花を消す。
         */
        Set<BlockPos> designPositions =
                createTargetPositionSet(
                        targets
                );

        Set<BlockPos> existingPlantParts =
                new HashSet<>();

        for (BlockPos position
                : designPositions) {

            BlockState blockState =
                    level.getBlockState(
                            position
                    );

            if (blockState.is(
                    ModBlocks.RECHORUS_ROOT
            )
                    || blockState.is(
                    ModBlocks.RECHORUS_PLANT
            )) {

                existingPlantParts.add(
                        position.immutable()
                );
            }
        }

        Set<BlockPos> flowerPositions =
                getManagedFlowerPositions(
                        level,
                        existingPlantParts
                );

        for (BlockPos flowerPosition
                : flowerPositions) {

            if (level.getBlockState(
                    flowerPosition
            ).is(
                    ModBlocks.RECHORUS_FLOWER
            )) {

                level.setBlock(
                        flowerPosition,
                        Blocks.AIR.defaultBlockState(),
                        3
                );
            }
        }

        /*
         * root / plantは先端側から崩壊させる。
         */
        List<BlockPos> collapsibleParts =
                new ArrayList<>();

        for (BlockPos position
                : designPositions) {

            BlockState current =
                    level.getBlockState(
                            position
                    );

            if (current.is(
                    ModBlocks.RECHORUS_ROOT
            )
                    || current.is(
                    ModBlocks.RECHORUS_PLANT
            )) {

                collapsibleParts.add(
                        position.immutable()
                );
            }
        }

        collapsibleParts.sort(
                Comparator.comparingDouble(
                        position ->
                                -position.distSqr(
                                        corePosition
                                )
                )
        );

        int removedCount =
                0;

        for (BlockPos position
                : collapsibleParts) {

            if (removedCount
                    >= COLLAPSE_PARTS_PER_CYCLE) {
                break;
            }

            level.setBlock(
                    position,
                    Blocks.AIR.defaultBlockState(),
                    3
            );

            removedCount++;
        }

        /*
         * 完成・設置状態を解除する。
         */
        if (state.isRechorusPlantBuilt()) {
            state.setRechorusPlantBuilt(
                    false
            );
        }

        if (state.isRechorusPlantCoreInstalled()) {
            state.setRechorusPlantCoreInstalled(
                    false
            );
        }

        /*
         * コアが失われたので、
         * 内部水と生産待ち果汁も失われる。
         */
        if (state.getRechorusStoredWater() > 0) {
            state.setRechorusStoredWater(
                    0
            );
        }

        if (state.getRechorusPendingJuice() > 0) {
            state.setRechorusPendingJuice(
                    0
            );
        }

        if (removedCount > 0
                || !flowerPositions.isEmpty()) {


        }
    }

    /*
     * ========================================
     * 管理対象探索
     * ========================================
     */

    public static Set<BlockPos>
    getConnectedManagedPlantParts(
            ServerLevel level,
            BlockPos corePosition
    ) {
        List<TedRechorusTreePlacer.PlantPartTarget>
                targets =
                TedRechorusTreePlacer
                        .getPlantPartTargets(
                                level,
                                corePosition
                        );

        if (targets.isEmpty()) {
            return Set.of();
        }

        return collectConnectedExistingParts(
                level,
                corePosition,
                createTargetPositionSet(
                        targets
                )
        );
    }

    private static Set<BlockPos>
    createTargetPositionSet(
            List<TedRechorusTreePlacer.PlantPartTarget>
                    targets
    ) {
        Set<BlockPos> positions =
                new HashSet<>();

        for (TedRechorusTreePlacer.PlantPartTarget target
                : targets) {

            positions.add(
                    target.position()
                            .immutable()
            );
        }

        return positions;
    }

    private static Set<BlockPos>
    collectConnectedExistingParts(
            ServerLevel level,
            BlockPos corePosition,
            Set<BlockPos> validPositions
    ) {
        Set<BlockPos> connected =
                new HashSet<>();

        Set<BlockPos> frontier =
                new HashSet<>();

        frontier.add(
                corePosition.immutable()
        );

        while (!frontier.isEmpty()) {
            BlockPos current =
                    frontier.iterator()
                            .next();

            frontier.remove(
                    current
            );

            for (Direction direction
                    : Direction.values()) {

                BlockPos next =
                        current.relative(
                                direction
                        );

                if (!validPositions.contains(
                        next
                )) {
                    continue;
                }

                if (connected.contains(
                        next
                )) {
                    continue;
                }

                BlockState nextState =
                        level.getBlockState(
                                next
                        );

                if (!isPlantNetworkBlock(
                        nextState
                )) {
                    continue;
                }

                BlockPos immutableNext =
                        next.immutable();

                connected.add(
                        immutableNext
                );

                frontier.add(
                        immutableNext
                );
            }
        }

        return connected;
    }

    private static boolean isAdjacentToManagedNetwork(
            BlockPos position,
            BlockPos corePosition,
            Set<BlockPos> connectedParts
    ) {
        for (Direction direction
                : Direction.values()) {

            BlockPos neighbour =
                    position.relative(
                            direction
                    );

            if (neighbour.equals(
                    corePosition
            )) {
                return true;
            }

            if (connectedParts.contains(
                    neighbour
            )) {
                return true;
            }
        }

        return false;
    }

    private static boolean isPlantNetworkBlock(
            BlockState state
    ) {
        return state.is(
                ModBlocks.RECHORUS_ROOT
        )
                || state.is(
                ModBlocks.RECHORUS_PLANT
        );
    }

    private static Set<BlockPos>
    getManagedFlowerPositions(
            ServerLevel level,
            Set<BlockPos> connectedParts
    ) {
        Set<BlockPos> flowers =
                new HashSet<>();

        for (BlockPos plantPosition
                : connectedParts) {

            if (!level.getBlockState(
                    plantPosition
            ).is(
                    ModBlocks.RECHORUS_PLANT
            )) {
                continue;
            }

            for (Direction direction
                    : Direction.values()) {

                BlockPos flowerPosition =
                        plantPosition.relative(
                                direction
                        );

                BlockState flowerState =
                        level.getBlockState(
                                flowerPosition
                        );

                if (!flowerState.is(
                        ModBlocks.RECHORUS_FLOWER
                )) {
                    continue;
                }

                if (!flowerState.hasProperty(
                        RechorusFlowerBlock.FACING
                )) {
                    continue;
                }

                /*
                 * plantから見た外側方向と、
                 * 花のFACINGが一致するものだけ
                 * このplantに所属する花とする。
                 */
                if (flowerState.getValue(
                        RechorusFlowerBlock.FACING
                ) != direction) {
                    continue;
                }

                flowers.add(
                        flowerPosition.immutable()
                );
            }
        }

        return flowers;
    }

    public static List<BlockPos> getManagedFlowers(
            ServerLevel level,
            BlockPos corePosition
    ) {
        List<TedRechorusTreePlacer.PlantPartTarget>
                targets =
                TedRechorusTreePlacer
                        .getPlantPartTargets(
                                level,
                                corePosition
                        );

        if (targets.isEmpty()) {
            return List.of();
        }

        Set<BlockPos> connectedParts =
                collectConnectedExistingParts(
                        level,
                        corePosition,
                        createTargetPositionSet(
                                targets
                        )
                );

        if (connectedParts.isEmpty()) {
            return List.of();
        }

        return List.copyOf(
                getManagedFlowerPositions(
                        level,
                        connectedParts
                )
        );
    }


    public static void regenerateManagedPlant(
            ServerLevel level,
            BlockPos corePosition,
            RechorusPlantCoreBlockEntity core
    ) {
        if (level == null
                || corePosition == null
                || core == null
                || !core.isPlantBuilt()) {
            return;
        }

        /*
         * このコアを基準にしたNBT設計位置を取得する。
         *
         * 村施設の保存位置には依存しない。
         */
        List<TedRechorusTreePlacer.PlantPartTarget>
                targets =
                TedRechorusTreePlacer
                        .getPlantPartTargets(
                                level,
                                corePosition
                        );

        if (targets.isEmpty()) {
            return;
        }

        Set<BlockPos> targetPositions =
                createTargetPositionSet(
                        targets
                );

        /*
         * 現在コアへつながっている既存部品。
         */
        Set<BlockPos> connectedParts =
                collectConnectedExistingParts(
                        level,
                        corePosition,
                        targetPositions
                );

        /*
         * まず幹・根を再生する。
         */
        regeneratePlantParts(
                level,
                corePosition,
                core,
                targets,
                connectedParts
        );

        /*
         * 幹が再生された可能性があるので、
         * 接続部品を再取得する。
         */
        connectedParts =
                collectConnectedExistingParts(
                        level,
                        corePosition,
                        targetPositions
                );

        /*
         * その後に花を再生する。
         */
        regenerateFlowers(
                level,
                corePosition,
                core,
                connectedParts
        );
    }

    private static void regeneratePlantParts(
            ServerLevel level,
            BlockPos corePosition,
            RechorusPlantCoreBlockEntity core,
            List<TedRechorusTreePlacer.PlantPartTarget>
                    targets,
            Set<BlockPos> connectedParts
    ) {
        if (core.getStoredWater()
                < WATER_PER_REGENERATED_PART) {
            return;
        }

        List<TedRechorusTreePlacer.PlantPartTarget>
                candidates =
                new ArrayList<>();

        for (TedRechorusTreePlacer.PlantPartTarget target
                : targets) {

            BlockPos targetPosition =
                    target.position();

            BlockState currentState =
                    level.getBlockState(
                            targetPosition
                    );

            /*
             * 既に正しい部品がある。
             */
            if (currentState.is(
                    target.state().getBlock()
            )) {
                continue;
            }

            /*
             * プレイヤーが置いた別ブロックは壊さない。
             */
            if (!currentState.isAir()) {
                continue;
            }

            /*
             * コアまたは既存の接続部品に
             * 隣接している箇所だけ再生する。
             */
            if (!isAdjacentToManagedNetwork(
                    targetPosition,
                    corePosition,
                    connectedParts
            )) {
                continue;
            }

            candidates.add(
                    target
            );
        }

        if (candidates.isEmpty()) {
            return;
        }

        /*
         * コアに近い部品から再生する。
         */
        candidates.sort(
                Comparator.comparingDouble(
                        target ->
                                target.position()
                                        .distSqr(
                                                corePosition
                                        )
                )
        );

        int regenerated =
                0;

        for (TedRechorusTreePlacer.PlantPartTarget target
                : candidates) {

            if (regenerated
                    >= MAX_PARTS_PER_CYCLE) {
                break;
            }

            if (core.getStoredWater()
                    < WATER_PER_REGENERATED_PART) {
                break;
            }

            BlockPos targetPosition =
                    target.position();

            if (!level.getBlockState(
                    targetPosition
            ).isAir()) {
                continue;
            }

            boolean placed =
                    level.setBlock(
                            targetPosition,
                            target.state(),
                            3
                    );

            if (!placed) {
                continue;
            }

            /*
             * 水消費に失敗した場合は配置を戻す。
             */
            if (!core.consumeWater(
                    WATER_PER_REGENERATED_PART
            )) {
                level.setBlock(
                        targetPosition,
                        Blocks.AIR.defaultBlockState(),
                        3
                );

                break;
            }

            connectedParts.add(
                    targetPosition.immutable()
            );

            regenerated++;


        }
    }

    private static void regenerateFlowers(
            ServerLevel level,
            BlockPos corePosition,
            RechorusPlantCoreBlockEntity core,
            Set<BlockPos> connectedParts
    ) {
        if (connectedParts.isEmpty()) {
            return;
        }

        if (core.getStoredWater()
                < WATER_PER_REGENERATED_FLOWER) {
            return;
        }

        Set<BlockPos> currentFlowers =
                getManagedFlowerPositions(
                        level,
                        connectedParts
                );

        if (currentFlowers.size()
                >= MAX_FLOWER_COUNT) {
            return;
        }

        List<FlowerCandidate> candidates =
                new ArrayList<>();

        for (BlockPos plantPosition
                : connectedParts) {

            if (!level.getBlockState(
                    plantPosition
            ).is(
                    ModBlocks.RECHORUS_PLANT
            )) {
                continue;
            }

            for (Direction direction
                    : Direction.values()) {

                BlockPos flowerPosition =
                        plantPosition.relative(
                                direction
                        );

                if (!level.getBlockState(
                        flowerPosition
                ).isAir()) {
                    continue;
                }

                /*
                 * 花を置く幹側が、実際にこのコアへ
                 * 接続されたplantであることを確認済み。
                 */
                candidates.add(
                        new FlowerCandidate(
                                flowerPosition.immutable(),
                                direction
                        )
                );
            }
        }

        if (candidates.isEmpty()) {
            return;
        }

        Collections.shuffle(
                candidates,
                new Random(
                        level.getGameTime()
                                ^ corePosition.asLong()
                )
        );

        int regenerated =
                0;

        for (FlowerCandidate candidate
                : candidates) {

            if (regenerated
                    >= MAX_FLOWERS_PER_CYCLE) {
                break;
            }

            if (currentFlowers.size()
                    >= MAX_FLOWER_COUNT) {
                break;
            }

            if (core.getStoredWater()
                    < WATER_PER_REGENERATED_FLOWER) {
                break;
            }

            if (!level.getBlockState(
                    candidate.position()
            ).isAir()) {
                continue;
            }

            BlockState flowerState =
                    ModBlocks.RECHORUS_FLOWER
                            .defaultBlockState()
                            .setValue(
                                    RechorusFlowerBlock.FACING,
                                    candidate.direction()
                            );

            boolean placed =
                    level.setBlock(
                            candidate.position(),
                            flowerState,
                            3
                    );

            if (!placed) {
                continue;
            }

            if (!core.consumeWater(
                    WATER_PER_REGENERATED_FLOWER
            )) {
                level.setBlock(
                        candidate.position(),
                        Blocks.AIR.defaultBlockState(),
                        3
                );

                break;
            }

            currentFlowers.add(
                    candidate.position()
                            .immutable()
            );

            regenerated++;

        }
    }

    private record FlowerCandidate(
            BlockPos position,
            Direction direction
    ) {
    }



    public static void collapseManagedPlant(
            ServerLevel level,
            BlockPos corePosition
    ) {
        if (level == null
                || corePosition == null) {
            return;
        }

        List<TedRechorusTreePlacer.PlantPartTarget>
                targets =
                TedRechorusTreePlacer
                        .getPlantPartTargets(
                                level,
                                corePosition
                        );

        if (targets.isEmpty()) {
            return;
        }

        java.util.Set<BlockPos> targetPositions =
                new java.util.HashSet<>();

        for (TedRechorusTreePlacer.PlantPartTarget
                target : targets) {

            targetPositions.add(
                    target.position()
                            .immutable()
            );
        }

        /*
         * 花を先に削除。
         */
        for (BlockPos position
                : targetPositions) {

            if (!level.getBlockState(
                    position
            ).is(
                    ModBlocks.RECHORUS_PLANT
            )) {
                continue;
            }

            for (Direction direction
                    : Direction.values()) {

                BlockPos flowerPosition =
                        position.relative(
                                direction
                        );

                BlockState flowerState =
                        level.getBlockState(
                                flowerPosition
                        );

                if (!flowerState.is(
                        ModBlocks.RECHORUS_FLOWER
                )) {
                    continue;
                }

                if (flowerState.hasProperty(
                        RechorusFlowerBlock.FACING
                )
                        && flowerState.getValue(
                        RechorusFlowerBlock.FACING
                ) == direction) {

                    level.removeBlock(
                            flowerPosition,
                            false
                    );
                }
            }
        }

        /*
         * NBT設計位置のroot・plantだけ削除。
         * プレイヤーの別建築は削除しない。
         */
        for (TedRechorusTreePlacer.PlantPartTarget
                target : targets) {

            BlockState current =
                    level.getBlockState(
                            target.position()
                    );

            if (!current.is(
                    ModBlocks.RECHORUS_ROOT
            )
                    && !current.is(
                    ModBlocks.RECHORUS_PLANT
            )) {
                continue;
            }

            level.removeBlock(
                    target.position(),
                    false
            );
        }
    }

    private TedRechorusPlantManager() {
    }
}