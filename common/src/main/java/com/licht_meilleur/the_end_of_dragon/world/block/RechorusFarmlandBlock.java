package com.licht_meilleur.the_end_of_dragon.world.block;

import com.licht_meilleur.the_end_of_dragon.registry.ModFluidTags;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public final class RechorusFarmlandBlock
        extends FarmlandBlock {

    public static final MapCodec<RechorusFarmlandBlock>
            CODEC =
            simpleCodec(
                    RechorusFarmlandBlock::new
            );

    public RechorusFarmlandBlock(
            BlockBehaviour.Properties properties
    ) {
        super(properties);
    }


    /**
     * 通常農地と同じ湿潤処理。
     *
     * 違いは通常水に加えて、
     * Rechorus Juiceも水分源になること。
     */
    @Override
    protected void randomTick(
            BlockState state,
            ServerLevel level,
            BlockPos position,
            RandomSource random
    ) {
        int moisture =
                state.getValue(
                        MOISTURE
                );

        boolean hydrated =
                isNearHydratingFluid(
                        level,
                        position
                );

        /*
         * 雨もバニラ農地と同様に水分源として扱う。
         */
        boolean raining =
                level.isRainingAt(
                        position.above()
                );

        if (!hydrated && !raining) {

            if (moisture > 0) {
                level.setBlock(
                        position,
                        state.setValue(
                                MOISTURE,
                                moisture - 1
                        ),
                        Block.UPDATE_CLIENTS
                );

                return;
            }

            /*
             * 上に作物などがなければ土へ戻る。
             */
            if (!shouldMaintainFarmland(
                    level,
                    position
            )) {
                FarmlandBlock.turnToDirt(
                        null,
                        state,
                        level,
                        position
                );
            }

            return;
        }

        if (moisture < MAX_MOISTURE) {
            level.setBlock(
                    position,
                    state.setValue(
                            MOISTURE,
                            MAX_MOISTURE
                    ),
                    Block.UPDATE_CLIENTS
            );
        }
    }

    /**
     * 農地を湿らせる流体を検索する。
     *
     * バニラ農地と同じ範囲：
     * 水平4ブロック、Yは同じ高さから1ブロック上。
     */
    private static boolean isNearHydratingFluid(
            LevelReader level,
            BlockPos farmlandPosition
    ) {
        BlockPos minimum =
                farmlandPosition.offset(
                        -4,
                        0,
                        -4
                );

        BlockPos maximum =
                farmlandPosition.offset(
                        4,
                        1,
                        4
                );

        for (BlockPos fluidPosition :
                BlockPos.betweenClosed(
                        minimum,
                        maximum
                )) {

            FluidState fluidState =
                    level.getFluidState(
                            fluidPosition
                    );

            if (fluidState.is(
                    FluidTags.WATER
            )) {
                return true;
            }

            if (fluidState.is(
                    ModFluidTags.RECHORUS_JUICE
            )) {
                return true;
            }
        }

        return false;
    }

    /**
     * 現在の農地が果汁水で潤っているか。
     *
     * 後の作物変異判定では、
     * MOISTUREだけでなくこのメソッドを使う。
     */
    public static boolean isHydratedByRechorusJuice(
            LevelReader level,
            BlockPos farmlandPosition
    ) {
        BlockPos minimum =
                farmlandPosition.offset(
                        -4,
                        0,
                        -4
                );

        BlockPos maximum =
                farmlandPosition.offset(
                        4,
                        1,
                        4
                );

        for (BlockPos fluidPosition :
                BlockPos.betweenClosed(
                        minimum,
                        maximum
                )) {

            if (level.getFluidState(
                    fluidPosition
            ).is(
                    ModFluidTags.RECHORUS_JUICE
            )) {
                return true;
            }
        }

        return false;
    }

    private static boolean shouldMaintainFarmland(
            BlockGetter level,
            BlockPos farmlandPosition
    ) {
        return level.getBlockState(
                farmlandPosition.above()
        ).is(
                BlockTags.MAINTAINS_FARMLAND
        );
    }
}