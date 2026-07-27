package com.licht_meilleur.the_end_of_dragon.world.village;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.registry.ModBlocks;
import com.licht_meilleur.the_end_of_dragon.world.dimension.TedDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class TedRechorusFacilityManager {

    private static final long CHECK_INTERVAL =
            20L;

    public static void tick(
            ServerLevel level
    ) {
        /*
         * エンダーマン村ディメンション専用。
         */
        if (!level.dimension().equals(
                TedDimensions.ENDERMAN_VILLAGE
        )) {
            return;
        }

        /*
         * 1秒に1回確認する。
         */
        if (level.getGameTime()
                % CHECK_INTERVAL != 0L) {
            return;
        }



        TedVillageWorldState state =
                TedVillageWorldState.get(level);



        TedVillageQuestStage stage =
                state.getVillageQuest();
        /*
         * 第4クエスト完了前は、
         * 施設Previewも設置判定も動かさない。
         */

        if (!stage.isAtLeast(
                TedVillageQuestStage
                        .RECHORUS_PLANT_PROTOTYPE
        )) {
            return;
        }

        /*
         * 第5クエスト報告後は処理終了。
         */
        if (stage.isAtLeast(
                TedVillageQuestStage
                        .RECHORUS_PLANT_BUILT
        )) {
            removePreview(
                    level,
                    state.getWaterTransferMachineBSlotPosition(),
                    ModBlocks.WATER_TRANSFER_MACHINE_B_PREVIEW
            );

            removePreview(
                    level,
                    state.getRechorusPlantCoreSlotPosition(),
                    ModBlocks.RECHORUS_PLANT_CORE_PREVIEW
            );

            return;
        }

        if (!state.isVillageGenerated()
                || !state.hasRechorusFacilityMarkers()) {
            return;
        }

        BlockPos machineBPosition =
                state.getWaterTransferMachineBSlotPosition();

        BlockPos corePosition =
                state.getRechorusPlantCoreSlotPosition();

        level.getChunk(machineBPosition);
        level.getChunk(corePosition);

        TedVillageQuestStage questStage =
                state.getVillageQuest();

        /*
         * 第5クエスト開始前はPreviewを表示しない。
         *
         * デバッグや旧セーブで残っていた場合は削除する。
         */
        if (!questStage.isAtLeast(
                TedVillageQuestStage
                        .RECHORUS_PLANT_PROTOTYPE
        )) {
            removePreview(
                    level,
                    machineBPosition,
                    ModBlocks
                            .WATER_TRANSFER_MACHINE_B_PREVIEW
            );

            removePreview(
                    level,
                    corePosition,
                    ModBlocks
                            .RECHORUS_PLANT_CORE_PREVIEW
            );

            return;
        }

        /*
         * クエスト報告完了後はPreviewを残さない。
         */
        if (questStage.isAtLeast(
                TedVillageQuestStage
                        .RECHORUS_PLANT_BUILT
        )) {
            removePreview(
                    level,
                    machineBPosition,
                    ModBlocks
                            .WATER_TRANSFER_MACHINE_B_PREVIEW
            );

            removePreview(
                    level,
                    corePosition,
                    ModBlocks
                            .RECHORUS_PLANT_CORE_PREVIEW
            );

            return;
        }

        BlockState machineBState =
                level.getBlockState(
                        machineBPosition
                );

        BlockState coreState =
                level.getBlockState(
                        corePosition
                );

        boolean machineBInstalled =
                machineBState.is(
                        ModBlocks
                                .WATER_TRANSFER_MACHINE_B
                );

        boolean coreInstalled =
                coreState.is(
                        ModBlocks
                                .RECHORUS_PLANT_CORE
                );

        /*
         * Preview表示
         */

        updatePreview(
                level,
                machineBPosition,
                machineBInstalled,
                ModBlocks.WATER_TRANSFER_MACHINE_B_PREVIEW
        );

        updatePreview(
                level,
                corePosition,
                coreInstalled,
                ModBlocks.RECHORUS_PLANT_CORE_PREVIEW
        );

        state.setWaterTransferMachineBInstalled(
                machineBInstalled
        );

        state.setRechorusPlantCoreInstalled(
                coreInstalled
        );

        /*
         * 未設置の指定位置へPreviewを表示する。
         *
         * 空気か、既存Previewの場合だけ扱う。
         * 別のブロックがある場合は破壊しない。
         */
        if (!machineBInstalled) {
            ensurePreview(
                    level,
                    machineBPosition,
                    ModBlocks
                            .WATER_TRANSFER_MACHINE_B_PREVIEW
            );
        }

        if (!coreInstalled) {
            ensurePreview(
                    level,
                    corePosition,
                    ModBlocks
                            .RECHORUS_PLANT_CORE_PREVIEW
            );
        }

        /*
         * 両方揃うまでは木を生成しない。
         */
        if (!machineBInstalled
                || !coreInstalled) {
            return;
        }

        /*
         * 両方設置済みなのでPreviewを完全に消す。
         *
         * 通常は実ブロックへ置換済みなので、
         * この処理では何も起きない。
         */
        removePreview(
                level,
                machineBPosition,
                ModBlocks
                        .WATER_TRANSFER_MACHINE_B_PREVIEW
        );

        removePreview(
                level,
                corePosition,
                ModBlocks
                        .RECHORUS_PLANT_CORE_PREVIEW
        );

        /*
         * 両方設置済みなのでPreviewを完全に消す。
         */
        removePreview(
                level,
                machineBPosition,
                ModBlocks.WATER_TRANSFER_MACHINE_B_PREVIEW
        );

        removePreview(
                level,
                corePosition,
                ModBlocks.RECHORUS_PLANT_CORE_PREVIEW
        );

        /*
         * プラント本体の生成はここでは行わない。
         *
         * コアが水を必要量吸収した後、
         * TedRechorusPlantManagerが成長処理を行う。
         */
        if (!state.isRechorusPlantBuilt()) {
            TheEndOfDragon.LOGGER.debug(
                    "Rechorus facility installed and waiting for water growth: core={}",
                    corePosition
            );
        }
    }

    private static void ensurePreview(
            ServerLevel level,
            BlockPos position,
            Block previewBlock
    ) {
        if (position == null
                || previewBlock == null) {
            return;
        }

        BlockState currentState =
                level.getBlockState(
                        position
                );

        /*
         * 既に正しいPreviewなら変更不要。
         */
        if (currentState.is(
                previewBlock
        )) {
            return;
        }

        /*
         * 他の実ブロックを勝手に破壊しない。
         */
        if (!currentState.isAir()
                && !currentState.is(
                Blocks.WATER
        )) {
            return;
        }

        level.setBlock(
                position,
                previewBlock.defaultBlockState(),
                3
        );
    }

    private static void removePreview(
            ServerLevel level,
            BlockPos position,
            Block previewBlock
    ) {
        if (position == null
                || previewBlock == null) {
            return;
        }

        if (!level.getBlockState(
                position
        ).is(
                previewBlock
        )) {
            return;
        }

        level.setBlock(
                position,
                Blocks.AIR.defaultBlockState(),
                3
        );
    }

    private static void updatePreview(
            ServerLevel level,
            BlockPos position,
            boolean installed,
            Block preview
    ) {
        if (position == null) {
            return;
        }

        BlockState state =
                level.getBlockState(position);

        /*
         * 未設置ならPreviewを表示
         */
        if (!installed) {

            if (state.isAir()) {

                level.setBlock(
                        position,
                        preview.defaultBlockState(),
                        3
                );
            }

            return;
        }

        /*
         * 設置済みならPreview削除
         */
        if (state.is(preview)) {

            level.setBlock(
                    position,
                    Blocks.AIR.defaultBlockState(),
                    3
            );
        }
    }

    private TedRechorusFacilityManager() {
    }
}