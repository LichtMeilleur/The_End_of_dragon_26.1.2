package com.licht_meilleur.the_end_of_dragon.world.village;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.registry.ModBlocks;
import com.licht_meilleur.the_end_of_dragon.world.dimension.TedDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public final class TedRechorusFacilityManager {

    private static final long CHECK_INTERVAL =
            20L;

    public static void tick(
            ServerLevel level
    ) {
        if (!level.dimension()
                .equals(
                        TedDimensions.ENDERMAN_VILLAGE
                )) {
            return;
        }

        if (level.getGameTime()
                % CHECK_INTERVAL != 0L) {
            return;
        }

        TedVillageWorldState state =
                TedVillageWorldState.get(level);

        if (!state.isVillageGenerated()
                || !state.hasRechorusFacilityMarkers()) {
            return;
        }

        BlockPos machineBPosition =
                state
                        .getWaterTransferMachineBSlotPosition();

        BlockPos corePosition =
                state
                        .getRechorusPlantCoreSlotPosition();

        level.getChunk(machineBPosition);
        level.getChunk(corePosition);

        boolean machineBInstalled =
                level.getBlockState(
                        machineBPosition
                ).is(
                        ModBlocks.WATER_TRANSFER_MACHINE_B
                );

        boolean coreInstalled =
                level.getBlockState(
                        corePosition
                ).is(
                        ModBlocks.RECHORUS_PLANT_CORE
                );

        state.setWaterTransferMachineBInstalled(
                machineBInstalled
        );

        state.setRechorusPlantCoreInstalled(
                coreInstalled
        );

        refreshQuestStage(
                state,
                machineBInstalled,
                coreInstalled
        );

        /*
         * コアと装置Bの両方が揃った最初の1回だけ、
         * リコーラスツリーを展開する。
         */
        if (!machineBInstalled
                || !coreInstalled
                || state.isRechorusPlantBuilt()) {
            return;
        }

        boolean built =
                TedRechorusTreePlacer.placeAtCore(
                        level,
                        corePosition
                );

        if (!built) {
            return;
        }

        state.setRechorusPlantBuilt(true);

        state.setVillageQuestStage(
                TedVillageQuestStage
                        .RECHORUS_PLANT_BUILT
        );

        TheEndOfDragon.LOGGER.info(
                "Completed Rechorus facility construction"
        );
    }

    private static void refreshQuestStage(
            TedVillageWorldState state,
            boolean machineBInstalled,
            boolean coreInstalled
    ) {
        /*
         * 後のクエスト実装時には、
         * FACILITY_CONSTRUCTION_AVAILABLE未満では
         * 進行させない条件を有効にできる。
         *
         * 現在は設置テストを優先して常時判定する。
         */

        if (machineBInstalled
                && !coreInstalled) {

            state.setVillageQuestStage(
                    TedVillageQuestStage
                            .MACHINE_B_INSTALLED
            );

            return;
        }

        if (coreInstalled
                && !machineBInstalled) {

            state.setVillageQuestStage(
                    TedVillageQuestStage
                            .PLANT_CORE_INSTALLED
            );
        }
    }

    private TedRechorusFacilityManager() {
    }
}