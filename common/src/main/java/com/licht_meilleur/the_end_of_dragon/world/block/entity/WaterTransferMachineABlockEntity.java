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

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public final class WaterTransferMachineABlockEntity
        extends WaterTransferChannelBlockEntity {

    /*
     * ========================================
     * 吸入設定
     * ========================================
     */

    /*
     * 吸入判定間隔。
     *
     * 20tick = 1秒。
     */
    private static final int
            INTAKE_INTERVAL_TICKS =
            20;

    /*
     * 通常水の、
     * 水源1個あたりの毎秒吸入量。
     */
    private static final long
            WATER_PER_SOURCE =
            200L;

    /*
     * 水以外の液体の、
     * 水源1個あたりの毎秒吸入量。
     *
     * 果汁水・溶岩・他Mod液体も
     * この値を使用する。
     */
    private static final long
            OTHER_FLUID_PER_SOURCE =
            50L;

    /*
     * 接続された液体源を数える上限。
     *
     * 海や巨大な液体領域を
     * 無制限に探索しないための制限。
     */
    private static final int
            MAX_SOURCE_SEARCH =
            32;

    /*
     * DOWNは台座があるため吸入口から除外。
     *
     * 探索順は
     * UP → 北 → 南 → 西 → 東。
     */
    private static final Direction[]
            INTAKE_DIRECTIONS = {
            Direction.UP,
            Direction.NORTH,
            Direction.SOUTH,
            Direction.WEST,
            Direction.EAST
    };

    public WaterTransferMachineABlockEntity(
            BlockPos position,
            BlockState state
    ) {
        super(
                ModBlockEntities.WATER_TRANSFER_MACHINE_A,
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
            WaterTransferMachineABlockEntity blockEntity
    ) {
        if (!(level instanceof ServerLevel
                serverLevel)) {
            return;
        }

        /*
         * 毎tick探索せず、1秒に1回だけ処理する。
         */
        if (serverLevel.getGameTime()
                % INTAKE_INTERVAL_TICKS != 0L) {
            return;
        }

        blockEntity.tickIntake(
                serverLevel,
                position
        );
    }

    private void tickIntake(
            ServerLevel level,
            BlockPos machinePosition
    ) {
        String channelName =
                this.getChannelName();

        /*
         * チャンネル未設定なら動かさない。
         *
         * 設置直後に勝手に吸入するのを防ぐ。
         */
        if (channelName == null
                || channelName.isBlank()) {
            return;
        }

        /*
         * Aへ接している液体源から、
         * 今回吸入する液体種類を決める。
         */
        Fluid targetFluid =
                findAdjacentSourceFluid(
                        level,
                        machinePosition
                );

        if (targetFluid == null
                || targetFluid == Fluids.EMPTY) {
            return;
        }

        /*
         * Aに接している同種液体源をすべて起点にし、
         * そこから接続された水源数を数える。
         *
         * A自身が中央を塞いでいても、
         * 周囲の水源を合計できる。
         */
        int connectedSourceCount =
                countConnectedFluidSourcesAroundMachine(
                        level,
                        machinePosition,
                        targetFluid
                );

        /*
         * 水源1個から使用可能。
         */
        if (connectedSourceCount <= 0) {
            return;
        }

        long amountPerSource =
                getAmountPerSource(
                        targetFluid
                );

        /*
         * 接続水源数に応じて吸入量を増やす。
         *
         * 例：
         * 水源1個 × 200mB = 200mB
         * 水源5個 × 200mB = 1000mB
         */
        long requestedAmount =
                amountPerSource
                        * connectedSourceCount;

        if (requestedAmount <= 0L) {
            return;
        }

        TedWaterTransferNetworkState network =
                TedWaterTransferNetworkState.get(
                        level
                );

        /*
         * チャンネルへ液体種類と量を登録する。
         *
         * チャンネルが空：
         *   このAが親として液体を登録。
         *
         * 同じ液体：
         *   量を追加。
         *
         * 別の液体：
         *   NetworkState側で拒否される。
         */
        long acceptedAmount =
                network.addFluid(
                        channelName,
                        targetFluid,
                        requestedAmount,
                        level.dimension(),
                        machinePosition
                );

        if (acceptedAmount <= 0L) {
            TheEndOfDragon.LOGGER.debug(
                    "Transfer A intake rejected: position={}, channel={}, fluid={}, requested={}, stored={}",
                    machinePosition,
                    channelName,
                    BuiltInRegistries.FLUID.getKey(
                            targetFluid
                    ),
                    requestedAmount,
                    network.getStoredAmount(
                            channelName
                    )
            );

            return;
        }

        /*
         * 水源ブロック自体は削除しない。
         *
         * 水以外は水より低速にすることで
         * バランスを取る。
         */
        TheEndOfDragon.LOGGER.debug(
                "Transfer A intake succeeded: position={}, channel={}, fluid={}, sources={}, ratePerSource={}, requested={}, accepted={}, stored={}",
                machinePosition,
                channelName,
                BuiltInRegistries.FLUID.getKey(
                        targetFluid
                ),
                connectedSourceCount,
                amountPerSource,
                requestedAmount,
                acceptedAmount,
                network.getStoredAmount(
                        channelName
                )
        );
    }

    /*
     * ========================================
     * 吸入液体の選択
     * ========================================
     */

    private static Fluid findAdjacentSourceFluid(
            ServerLevel level,
            BlockPos machinePosition
    ) {
        for (Direction direction
                : INTAKE_DIRECTIONS) {

            BlockPos candidatePosition =
                    machinePosition.relative(
                            direction
                    );

            FluidState fluidState =
                    level.getFluidState(
                            candidatePosition
                    );

            /*
             * 液体ではない。
             */
            if (fluidState.isEmpty()) {
                continue;
            }

            /*
             * 流動液体ではなく、
             * 水源だけを吸入対象にする。
             */
            if (!fluidState.isSource()) {
                continue;
            }

            Fluid fluid =
                    fluidState.getType();

            if (fluid == null
                    || fluid == Fluids.EMPTY) {
                continue;
            }

            return fluid;
        }

        return Fluids.EMPTY;
    }

    /*
     * ========================================
     * 接続液体源の探索
     * ========================================
     */

    private static int
    countConnectedFluidSourcesAroundMachine(
            ServerLevel level,
            BlockPos machinePosition,
            Fluid targetFluid
    ) {
        Set<BlockPos> visited =
                new HashSet<>();

        Queue<BlockPos> queue =
                new ArrayDeque<>();

        /*
         * Aへ直接接している同種水源を、
         * すべて探索開始地点として登録する。
         *
         * Aが中央を塞いでいても、
         * 北・南・西・東・上の水源を
         * それぞれ合計できる。
         */
        for (Direction direction
                : INTAKE_DIRECTIONS) {

            BlockPos adjacentPosition =
                    machinePosition.relative(
                            direction
                    );

            if (!isSourceOfFluid(
                    level,
                    adjacentPosition,
                    targetFluid
            )) {
                continue;
            }

            BlockPos immutablePosition =
                    adjacentPosition.immutable();

            if (visited.add(
                    immutablePosition
            )) {
                queue.add(
                        immutablePosition
                );
            }
        }

        if (visited.isEmpty()) {
            return 0;
        }

        /*
         * 周囲の開始地点から、
         * 面接続している同種液体源を探索する。
         */
        while (!queue.isEmpty()) {
            if (visited.size()
                    >= MAX_SOURCE_SEARCH) {
                return MAX_SOURCE_SEARCH;
            }

            BlockPos current =
                    queue.remove();

            for (Direction direction
                    : Direction.values()) {

                BlockPos next =
                        current.relative(
                                direction
                        );

                /*
                 * A本体の位置へは進入しない。
                 */
                if (next.equals(
                        machinePosition
                )) {
                    continue;
                }

                if (visited.contains(
                        next
                )) {
                    continue;
                }

                if (!isSourceOfFluid(
                        level,
                        next,
                        targetFluid
                )) {
                    continue;
                }

                BlockPos immutableNext =
                        next.immutable();

                visited.add(
                        immutableNext
                );

                queue.add(
                        immutableNext
                );

                if (visited.size()
                        >= MAX_SOURCE_SEARCH) {
                    return MAX_SOURCE_SEARCH;
                }
            }
        }

        return visited.size();
    }

    private static boolean isSourceOfFluid(
            ServerLevel level,
            BlockPos position,
            Fluid targetFluid
    ) {
        if (targetFluid == null
                || targetFluid == Fluids.EMPTY) {
            return false;
        }

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
     * 液体別の吸入量
     * ========================================
     */

    private static long getAmountPerSource(
            Fluid fluid
    ) {
        /*
         * 通常水は高速。
         */
        if (fluid == Fluids.WATER) {
            return WATER_PER_SOURCE;
        }

        /*
         * 果汁水・溶岩・他Mod液体は低速。
         */
        return OTHER_FLUID_PER_SOURCE;
    }
}