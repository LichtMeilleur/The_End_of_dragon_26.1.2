package com.licht_meilleur.the_end_of_dragon.world.village;

import com.licht_meilleur.the_end_of_dragon.registry.ModBlocks;
import com.licht_meilleur.the_end_of_dragon.world.dimension
        .TedDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TedRechorusRegenerationManager {

    /*
     * 10秒ごと。
     */
    private static final long REGENERATION_INTERVAL =
            200L;

    /*
     * 1回に再生するブロック数。
     */
    private static final int PARTS_PER_CYCLE =
            1;

    public static void tick(
            ServerLevel level
    ) {
        if (!level.dimension().equals(
                TedDimensions.ENDERMAN_VILLAGE
        )) {
            return;
        }

        if (level.getGameTime()
                % REGENERATION_INTERVAL != 0L) {
            return;
        }

        TedVillageWorldState state =
                TedVillageWorldState.get(level);

        if (!state.isRechorusPlantBuilt()) {
            return;
        }

        BlockPos corePosition =
                state.getRechorusPlantCoreSlotPosition();

        if (!level.getBlockState(
                corePosition
        ).is(
                ModBlocks.RECHORUS_PLANT_CORE
        )) {
            return;
        }

        List<TedRechorusTreePlacer.PlantPartTarget>
                missingParts =
                new ArrayList<>();

        for (TedRechorusTreePlacer.PlantPartTarget target
                : TedRechorusTreePlacer
                .getPlantPartTargets(
                        level,
                        corePosition
                )) {

            BlockState current =
                    level.getBlockState(
                            target.position()
                    );

            /*
             * 空気だけ再生対象にする。
             * 他の建築ブロックは破壊しない。
             */
            if (current.isAir()) {
                missingParts.add(
                        target
                );
            }
        }

        if (missingParts.isEmpty()) {
            return;
        }

        Collections.shuffle(
                missingParts,
                new java.util.Random(
                        level.getGameTime()
                )
        );

        int limit =
                Math.min(
                        PARTS_PER_CYCLE,
                        missingParts.size()
                );

        for (int index = 0;
             index < limit;
             index++) {

            TedRechorusTreePlacer.PlantPartTarget target =
                    missingParts.get(index);

            level.setBlock(
                    target.position(),
                    target.state(),
                    3
            );
        }
    }

    private TedRechorusRegenerationManager() {
    }
}