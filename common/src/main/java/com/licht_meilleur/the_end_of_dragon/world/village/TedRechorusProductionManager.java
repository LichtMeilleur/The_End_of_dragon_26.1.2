package com.licht_meilleur.the_end_of_dragon.world.village;

import com.licht_meilleur.the_end_of_dragon.registry.ModBlocks;
import com.licht_meilleur.the_end_of_dragon.world.block
        .RechorusFlowerBlock;
import com.licht_meilleur.the_end_of_dragon.world.dimension
        .TedDimensions;
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
     * 生産判定を行う間隔。
     *
     * 200tick = 10秒。
     */
    private static final long PRODUCTION_INTERVAL =
            200L;

    /*
     * 1回の生産処理で消費する通常水量。
     *
     * mB想定。
     */
    private static final int WATER_PER_CYCLE =
            100;

    /*
     * 花1個あたり、
     * 1サイクルで生産する果汁量。
     */
    private static final int JUICE_PER_FLOWER =
            25;

    /*
     * 仮流体を1回生成するために必要な果汁量。
     *
     * 1000mB = バケツ1杯分想定。
     */
    private static final int JUICE_PER_FLOW =
            1_000;

    /*
     * コアを中心に花を探す範囲。
     */
    private static final int FLOWER_SEARCH_RADIUS =
            24;

    public static void tick(
            ServerLevel level
    ) {
        /*
         * 村ディメンション以外では処理しない。
         */
        if (!level.dimension().equals(
                TedDimensions.ENDERMAN_VILLAGE
        )) {
            return;
        }

        /*
         * 指定間隔でのみ処理する。
         */
        if (level.getGameTime()
                % PRODUCTION_INTERVAL != 0L) {
            return;
        }

        TedVillageWorldState state =
                TedVillageWorldState.get(level);

        /*
         * プラントが完成していなければ生産しない。
         */
        if (!state.isRechorusPlantBuilt()) {
            return;
        }

        /*
         * 既に仮流体の生成処理中なら、
         * 次の流体はまだ出さない。
         */
        if (TedRechorusJuiceFlowManager.isActive(
                level
        )) {
            return;
        }

        BlockPos corePosition =
                state.getRechorusPlantCoreSlotPosition();

        /*
         * コアブロックが存在しなければ停止。
         */
        if (!level.getBlockState(
                corePosition
        ).is(
                ModBlocks.RECHORUS_PLANT_CORE
        )) {
            return;
        }

        List<BlockPos> flowers =
                findFlowers(
                        level,
                        corePosition
                );

        if (flowers.isEmpty()) {
            return;
        }

        /*
         * 通常水が足りなければ生産しない。
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

        /*
         * バケツ1杯分に満たない場合は、
         * 蓄積だけして終了。
         */
        if (state.getRechorusPendingJuice()
                < JUICE_PER_FLOW) {
            return;
        }

        /*
         * 毎回同じ花にならないよう、
         * 候補順をランダム化。
         */
        List<BlockPos> shuffledFlowers =
                new ArrayList<>(
                        flowers
                );

        Collections.shuffle(
                shuffledFlowers,
                new java.util.Random(
                        level.getGameTime()
                )
        );

        /*
         * 空いている開始地点を持つ花を順に探す。
         */
        for (BlockPos flowerPosition
                : shuffledFlowers) {

            if (!startGuideFlowFromFlower(
                    level,
                    flowerPosition
            )) {
                continue;
            }

            /*
             * 仮流体生成に成功した時だけ、
             * バケツ1杯分を消費する。
             */
            state.setRechorusPendingJuice(
                    state.getRechorusPendingJuice()
                            - JUICE_PER_FLOW
            );

            return;
        }
    }

    private static List<BlockPos> findFlowers(
            ServerLevel level,
            BlockPos center
    ) {
        List<BlockPos> positions =
                new ArrayList<>();

        BlockPos minimum =
                center.offset(
                        -FLOWER_SEARCH_RADIUS,
                        -FLOWER_SEARCH_RADIUS,
                        -FLOWER_SEARCH_RADIUS
                );

        BlockPos maximum =
                center.offset(
                        FLOWER_SEARCH_RADIUS,
                        FLOWER_SEARCH_RADIUS,
                        FLOWER_SEARCH_RADIUS
                );

        BlockPos.betweenClosedStream(
                minimum,
                maximum
        ).forEach(position -> {
            if (!level.getBlockState(position)
                    .is(
                            ModBlocks.RECHORUS_FLOWER
                    )) {
                return;
            }

            positions.add(
                    position.immutable()
            );
        });

        return positions;
    }

    private static boolean startGuideFlowFromFlower(
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

        Direction facing =
                flowerState.getValue(
                        RechorusFlowerBlock.FACING
                );

        BlockPos startPosition =
                findGuideStartPosition(
                        level,
                        flowerPosition,
                        facing
                );

        if (startPosition == null) {
            return false;
        }

        return TedRechorusJuiceFlowManager.start(
                level,
                startPosition
        );
    }

    private static BlockPos findGuideStartPosition(
            ServerLevel level,
            BlockPos flowerPosition,
            Direction facing
    ) {
        /*
         * 側面に咲いた花は、
         * 花が向いている外側から流し始める。
         */
        if (facing.getAxis()
                .isHorizontal()) {

            BlockPos outsidePosition =
                    flowerPosition.relative(
                            facing
                    );

            if (canStartFluidAt(
                    level,
                    outsidePosition
            )) {
                return outsidePosition;
            }
        }

        /*
         * 下向きの花は直下を優先。
         */
        if (facing == Direction.DOWN) {
            BlockPos belowPosition =
                    flowerPosition.below();

            if (canStartFluidAt(
                    level,
                    belowPosition
            )) {
                return belowPosition;
            }
        }

        /*
         * 上向きの花は真下に幹がある可能性が高い。
         *
         * まず周囲4方向から、
         * さらに下へ流れられる位置を優先する。
         */
        Direction[] horizontalDirections = {
                Direction.NORTH,
                Direction.SOUTH,
                Direction.WEST,
                Direction.EAST
        };

        for (Direction direction
                : horizontalDirections) {

            BlockPos sidePosition =
                    flowerPosition.relative(
                            direction
                    );

            if (!canStartFluidAt(
                    level,
                    sidePosition
            )) {
                continue;
            }

            if (canStartFluidAt(
                    level,
                    sidePosition.below()
            )) {
                return sidePosition;
            }
        }

        /*
         * 下が塞がっていても、
         * 横が空いていれば仮流体に流路を探させる。
         */
        for (Direction direction
                : horizontalDirections) {

            BlockPos sidePosition =
                    flowerPosition.relative(
                            direction
                    );

            if (canStartFluidAt(
                    level,
                    sidePosition
            )) {
                return sidePosition;
            }
        }

        /*
         * 最後に直下を試す。
         */
        BlockPos belowPosition =
                flowerPosition.below();

        if (canStartFluidAt(
                level,
                belowPosition
        )) {
            return belowPosition;
        }

        return null;
    }

    private static boolean canStartFluidAt(
            ServerLevel level,
            BlockPos position
    ) {
        BlockState state =
                level.getBlockState(
                        position
                );

        /*
         * 現段階では空気だけを許可。
         *
         * replaceable判定を広げると、
         * 草や植物を上書きする可能性があるため。
         */
        return state.isAir();
    }

    private TedRechorusProductionManager() {
    }
}