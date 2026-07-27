package com.licht_meilleur.the_end_of_dragon.world.block.entity;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.registry.ModBlockEntities;
import com.licht_meilleur.the_end_of_dragon.world.village
        .TedWaterTransferNetworkState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class WaterTransferMachineBBlockEntity
        extends WaterTransferChannelBlockEntity {

    /*
     * ========================================
     * 放出設定
     * ========================================
     */

    /*
     * 1秒ごとに放出判定。
     */
    private static final int
            OUTPUT_INTERVAL_TICKS =
            20;

    /*
     * 水源1個を生成するための消費量。
     */
    private static final long
            FLUID_PER_OUTPUT =
            1_000L;

    /*
     * 1回の判定で生成する最大水源数。
     *
     * 現在は1個ずつ。
     */
    private static final int
            MAX_OUTPUTS_PER_CYCLE =
            1;

    /*
     * DOWNは台座があるため放出しない。
     */
    private static final Direction[]
            OUTPUT_DIRECTIONS = {
            Direction.UP,
            Direction.NORTH,
            Direction.SOUTH,
            Direction.WEST,
            Direction.EAST
    };

    /*
     * 次回どの方向から探索するか。
     */
    private int nextOutputDirectionIndex;

    public WaterTransferMachineBBlockEntity(
            BlockPos position,
            BlockState state
    ) {
        super(
                ModBlockEntities.WATER_TRANSFER_MACHINE_B,
                position,
                state
        );
    }

    /*
     * ========================================
     * Tick
     * ========================================
     */

    public static void serverTick(
            Level level,
            BlockPos position,
            BlockState ignoredState,
            WaterTransferMachineBBlockEntity blockEntity
    ) {
        if (!(level instanceof ServerLevel
                serverLevel)) {
            return;
        }

        if (serverLevel.getGameTime()
                % OUTPUT_INTERVAL_TICKS != 0L) {
            return;
        }

        blockEntity.tickOutput(
                serverLevel,
                position
        );
    }

    private void tickOutput(
            ServerLevel level,
            BlockPos machinePosition
    ) {
        TedWaterTransferNetworkState network =
                TedWaterTransferNetworkState.get(
                        level
                );

        String channelName =
                this.getChannelName();

        if (channelName.isBlank()) {
            return;
        }


        if (network.getStoredAmount(
                channelName
        ) < FLUID_PER_OUTPUT) {
            return;
        }

        Fluid channelFluid =
                network.getFluid(
                        channelName
                );

        if (channelFluid == null
                || channelFluid == Fluids.EMPTY) {
            return;
        }

        int placedSources =
                0;

        for (int offset = 0;
             offset < OUTPUT_DIRECTIONS.length;
             offset++) {

            if (placedSources
                    >= MAX_OUTPUTS_PER_CYCLE) {
                break;
            }

            if (network.getStoredAmount(
                    channelName
            ) < FLUID_PER_OUTPUT) {
                break;
            }

            int directionIndex =
                    (
                            this.nextOutputDirectionIndex
                                    + offset
                    )
                            % OUTPUT_DIRECTIONS.length;

            Direction direction =
                    OUTPUT_DIRECTIONS[
                            directionIndex
                            ];

            BlockPos outputPosition =
                    machinePosition.relative(
                            direction
                    );

            /*
             * 同じ液体の水源が既にある場合は、
             * 重複して液体量を消費しない。
             */
            if (isExistingFluidSource(
                    level,
                    outputPosition,
                    channelFluid
            )) {
                continue;
            }

            /*
             * 他のブロックや植物は破壊しない。
             *
             * 完全な空気ブロックだけに出力する。
             */
            if (!level.getBlockState(
                    outputPosition
            ).isAir()) {
                continue;
            }

            boolean placed =
                    level.setBlock(
                            outputPosition,
                            channelFluid
                                    .defaultFluidState()
                                    .createLegacyBlock(),
                            3
                    );

            if (!placed) {
                continue;
            }

            /*
             * 配置後、本当に目的の液体が
             * Sourceとして存在しているか確認する。
             *
             * 特殊な他Mod流体でLegacyBlock生成に
             * 対応していない場合を検出できる。
             */
            if (!isExistingFluidSource(
                    level,
                    outputPosition,
                    channelFluid
            )) {
                level.removeBlock(
                        outputPosition,
                        false
                );

                continue;
            }

            /*
             * 配置成功後にだけチャンネルから消費する。
             */
            if (!network.consumeFluid(
                    channelName,
                    FLUID_PER_OUTPUT
            )) {
                level.removeBlock(
                        outputPosition,
                        false
                );

                break;
            }

            placedSources++;

            this.nextOutputDirectionIndex =
                    (
                            directionIndex + 1
                    )
                            % OUTPUT_DIRECTIONS.length;

            this.setChanged();
        }

        if (placedSources <= 0) {
            return;
        }

        TheEndOfDragon.LOGGER.debug(
                "Transfer B output succeeded: position={}, channel={}, fluid={}, sources={}, remaining={}",
                machinePosition,
                channelName,
                BuiltInRegistries.FLUID.getKey(
                        channelFluid
                ),
                placedSources,
                network.getStoredAmount(
                        channelName
                )
        );
    }

    /*
     * ========================================
     * 液体確認
     * ========================================
     */

    private static boolean isExistingFluidSource(
            ServerLevel level,
            BlockPos position,
            Fluid targetFluid
    ) {
        FluidState fluidState =
                level.getFluidState(
                        position
                );

        return !fluidState.isEmpty()
                && fluidState.isSource()
                && fluidState.getType()
                == targetFluid;
    }

    /*
     * ========================================
     * 保存
     * ========================================
     */

    @Override
    protected void saveAdditional(
            ValueOutput output
    ) {
        super.saveAdditional(
                output
        );

        output.putInt(
                "NextOutputDirectionIndex",
                Math.floorMod(
                        this.nextOutputDirectionIndex,
                        OUTPUT_DIRECTIONS.length
                )
        );
    }

    @Override
    protected void loadAdditional(
            ValueInput input
    ) {
        super.loadAdditional(
                input
        );

        this.nextOutputDirectionIndex =
                Math.floorMod(
                        input.getIntOr(
                                "NextOutputDirectionIndex",
                                0
                        ),
                        OUTPUT_DIRECTIONS.length
                );
    }
}