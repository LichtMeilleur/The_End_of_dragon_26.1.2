package com.licht_meilleur.the_end_of_dragon.world.village;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.registry.ModBlocks;
import com.licht_meilleur.the_end_of_dragon.registry.ModFluids;
import com.licht_meilleur.the_end_of_dragon.world.dimension
        .TedDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public final class TedRechorusJuiceFlowManager {

    /*
     * ===== 調整用設定 =====
     */

    /*
     * 何tickごとに流路を比較するか。
     */
    private static final long CHECK_INTERVAL =
            5L;

    /*
     * 同じ状態が何回続けば確定するか。
     *
     * CHECK_INTERVAL=5、4回なら約1秒。
     */
    private static final int REQUIRED_STABLE_CHECKS =
            4;

    /*
     * 異常時の強制終了時間。
     */
    private static final int TIMEOUT_TICKS =
            20 * 10;

    /*
     * 起点から調査する半径。
     */
    private static final int SEARCH_RADIUS =
            16;

    /*
     * 仮流体の最大ブロック数。
     */
    private static final int MAX_GUIDE_BLOCKS =
            128;

    /*
     * 現在は各ディメンションにつき
     * 1つの流路生成だけを許可する。
     */
    private static final Map<
            ServerLevel,
            ActiveGuideFlow> ACTIVE_FLOWS =
            new WeakHashMap<>();

    public static boolean start(
            ServerLevel level,
            BlockPos startPosition
    ) {
        if (level == null
                || startPosition == null) {
            return false;
        }

        if (!level.dimension().equals(
                TedDimensions.ENDERMAN_VILLAGE
        )) {
            return false;
        }

        /*
         * 既に処理中なら新規開始しない。
         */
        if (ACTIVE_FLOWS.containsKey(level)) {
            return false;
        }

        BlockState current =
                level.getBlockState(
                        startPosition
                );

        /*
         * 空気か、置換可能なブロックだけ。
         */
        if (!current.isAir()
                && !current.canBeReplaced()) {
            return false;
        }

        level.setBlock(
                startPosition,
                ModBlocks.RECHORUS_JUICE_GUIDE
                        .defaultBlockState(),
                3
        );

        level.scheduleTick(
                startPosition,
                ModFluids.RECHORUS_JUICE_GUIDE_SOURCE,
                1
        );

        ACTIVE_FLOWS.put(
                level,
                new ActiveGuideFlow(
                        startPosition.immutable(),
                        level.getGameTime()
                )
        );

        TheEndOfDragon.LOGGER.info(
                "Started Rechorus juice guide flow at {}",
                startPosition
        );

        return true;
    }

    public static void tick(
            ServerLevel level
    ) {
        if (level == null
                || !level.dimension().equals(
                TedDimensions.ENDERMAN_VILLAGE
        )) {
            return;
        }

        ActiveGuideFlow active =
                ACTIVE_FLOWS.get(level);

        if (active == null) {
            return;
        }

        long age =
                level.getGameTime()
                        - active.startGameTime;

        if (age >= TIMEOUT_TICKS) {
            removeGuideFluid(
                    level,
                    active.origin
            );

            ACTIVE_FLOWS.remove(level);

            TheEndOfDragon.LOGGER.warn(
                    "Timed out Rechorus juice guide flow at {}",
                    active.origin
            );

            return;
        }

        if (level.getGameTime()
                % CHECK_INTERVAL != 0L) {
            return;
        }

        Map<Long, Integer> currentSnapshot =
                createSnapshot(
                        level,
                        active.origin
                );



        if (currentSnapshot.size()
                > MAX_GUIDE_BLOCKS) {

            removeGuideFluid(
                    level,
                    active.origin
            );

            ACTIVE_FLOWS.remove(level);

            TheEndOfDragon.LOGGER.warn(
                    "Removed oversized Rechorus guide flow: blocks={}",
                    currentSnapshot.size()
            );

            return;
        }

        if (!currentSnapshot.isEmpty()
                && currentSnapshot.equals(
                active.previousSnapshot
        )) {

            active.stableChecks++;
        } else {
            active.stableChecks =
                    0;

            active.previousSnapshot =
                    currentSnapshot;
        }

        if (active.stableChecks
                < REQUIRED_STABLE_CHECKS) {
            return;
        }

        convertGuideToRealJuice(
                level,
                active.origin
        );

        ACTIVE_FLOWS.remove(level);

        TheEndOfDragon.LOGGER.info(
                "Converted stable Rechorus guide flow at {}",
                active.origin
        );
    }

    public static boolean isActive(
            ServerLevel level
    ) {
        return ACTIVE_FLOWS.containsKey(
                level
        );
    }

    private static Map<Long, Integer>
    createSnapshot(
            ServerLevel level,
            BlockPos origin
    ) {
        Map<Long, Integer> snapshot =
                new HashMap<>();

        BlockPos.betweenClosedStream(
                origin.offset(
                        -SEARCH_RADIUS,
                        -SEARCH_RADIUS,
                        -SEARCH_RADIUS
                ),
                origin.offset(
                        SEARCH_RADIUS,
                        SEARCH_RADIUS,
                        SEARCH_RADIUS
                )
        ).forEach(position -> {
            BlockState state =
                    level.getBlockState(
                            position
                    );

            if (!state.is(
                    ModBlocks.RECHORUS_JUICE_GUIDE
            )) {
                return;
            }

            int liquidLevel =
                    state.getValue(
                            LiquidBlock.LEVEL
                    );

            snapshot.put(
                    position.asLong(),
                    liquidLevel
            );
        });

        return snapshot;
    }

    private static void spawnGuideDebugParticles(
            ServerLevel level,
            Map<Long, Integer> snapshot
    ) {
        for (long packedPosition
                : snapshot.keySet()) {

            BlockPos position =
                    BlockPos.of(
                            packedPosition
                    );

            level.sendParticles(
                    ParticleTypes.END_ROD,
                    position.getX() + 0.5D,
                    position.getY() + 0.5D,
                    position.getZ() + 0.5D,
                    1,
                    0.05D,
                    0.05D,
                    0.05D,
                    0.0D
            );
        }
    }

    private static void convertGuideToRealJuice(
            ServerLevel level,
            BlockPos origin
    ) {
        Map<Long, Integer> snapshot =
                createSnapshot(
                        level,
                        origin
                );

        if (snapshot.isEmpty()) {
            return;
        }

        BlockPos destination =
                findDestinationPosition(
                        level,
                        snapshot,
                        origin
                );

        /*
         * 先に仮流体を消す。
         */
        for (long packedPosition
                : snapshot.keySet()) {

            BlockPos position =
                    BlockPos.of(
                            packedPosition
                    );

            if (level.getBlockState(position)
                    .is(
                            ModBlocks.RECHORUS_JUICE_GUIDE
                    )) {

                level.removeBlock(
                        position,
                        false
                );
            }
        }

        if (destination == null) {
            TheEndOfDragon.LOGGER.warn(
                    "Could not find Rechorus juice destination from {}",
                    origin
            );

            return;
        }

        /*
         * 終着点の1マス上へデバッグマーカー。
         */
        BlockPos markerPosition =
                destination.above();

        if (level.getBlockState(
                markerPosition
        ).isAir()) {
            level.setBlock(
                    markerPosition,
                    ModBlocks.DEBUG_MARKER
                            .defaultBlockState(),
                    3
            );
        }

        /*
         * 終着点へ本物の果汁水源を置く。
         */
        boolean placed =
                level.setBlock(
                        destination,
                        ModBlocks.RECHORUS_JUICE
                                .defaultBlockState()
                                .setValue(
                                        LiquidBlock.LEVEL,
                                        0
                                ),
                        3
                );

        TheEndOfDragon.LOGGER.info(
                "Placed Rechorus juice source: position={}, success={}, marker={}",
                destination,
                placed,
                markerPosition
        );

        level.scheduleTick(
                destination,
                ModFluids.RECHORUS_JUICE_SOURCE,
                1
        );
    }

    private static BlockPos findDestinationPosition(
            ServerLevel level,
            Map<Long, Integer> snapshot,
            BlockPos origin
    ) {
        BlockPos bestPosition =
                null;

        int bestY =
                Integer.MAX_VALUE;

        int bestLiquidLevel =
                Integer.MAX_VALUE;

        double bestDistance =
                Double.MAX_VALUE;

        for (Map.Entry<Long, Integer> entry
                : snapshot.entrySet()) {

            BlockPos position =
                    BlockPos.of(
                            entry.getKey()
                    );

            /*
             * 下に支えがある場所だけを着地点候補にする。
             */
            BlockPos below =
                    position.below();

            boolean supported =
                    !level.getBlockState(
                            below
                    ).getCollisionShape(
                            level,
                            below
                    ).isEmpty();

            if (!supported) {
                continue;
            }

            int liquidLevel =
                    entry.getValue();

            double distance =
                    position.distSqr(
                            origin
                    );

            /*
             * 最も低い場所を優先。
             *
             * 同じ高さなら、
             * より濃い流体レベルを優先。
             *
             * それも同じなら、
             * 起点から遠い場所を終点として優先。
             */
            boolean better =
                    position.getY() < bestY
                            || position.getY() == bestY
                            && liquidLevel < bestLiquidLevel
                            || position.getY() == bestY
                            && liquidLevel == bestLiquidLevel
                            && distance > bestDistance;

            if (!better) {
                continue;
            }

            bestPosition =
                    position.immutable();

            bestY =
                    position.getY();

            bestLiquidLevel =
                    liquidLevel;

            bestDistance =
                    distance;
        }

        return bestPosition;
    }

    private static void removeGuideFluid(
            ServerLevel level,
            BlockPos origin
    ) {
        Map<Long, Integer> snapshot =
                createSnapshot(
                        level,
                        origin
                );

        for (long packedPosition
                : snapshot.keySet()) {

            BlockPos position =
                    BlockPos.of(
                            packedPosition
                    );

            if (level.getBlockState(position)
                    .is(
                            ModBlocks.RECHORUS_JUICE_GUIDE
                    )) {

                level.removeBlock(
                        position,
                        false
                );
            }
        }
    }

    private static final class ActiveGuideFlow {

        private final BlockPos origin;

        private final long startGameTime;

        private Map<Long, Integer>
                previousSnapshot =
                Map.of();

        private int stableChecks;

        private ActiveGuideFlow(
                BlockPos origin,
                long startGameTime
        ) {
            this.origin =
                    origin;

            this.startGameTime =
                    startGameTime;
        }
    }

    private TedRechorusJuiceFlowManager() {
    }
}