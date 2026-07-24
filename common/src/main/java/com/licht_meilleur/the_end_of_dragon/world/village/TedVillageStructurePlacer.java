package com.licht_meilleur.the_end_of_dragon.world.village;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.List;
import java.util.Optional;

public final class TedVillageStructurePlacer {

    /*
     * NBT内に設置したジグソーブロックのname。
     *
     * 綴りはNBT側と完全一致させる。
     */
    private static final String SURFACE_ANCHOR_NAME =
            "ted:surface_anchor";

    private static final String ELDER_SPAWN_NAME =
            "ted:elder_spawn";

    private static final String TECHNICIAN_SPAWN_NAME =
            "ted:technician_spawn";

    private static final String ALLY_HOME_NAME =
            "ted:ally_home";

    private static final String
            RECHORUS_FACILITY_ANCHOR_NAME =
            "ted:rechorus_facility_anchor";

    private static final String
            WATER_TRANSFER_MACHINE_B_SLOT_NAME =
            "ted:water_transfer_machine_b_slot";

    private static final String
            RECHORUS_PLANT_CORE_SLOT_NAME =
            "ted:rechorus_plant_core_slot";

    /*
     * 村の基準XZ。
     *
     * 到着地点を中心に村を配置する。
     */
    private static final int VILLAGE_CENTER_X = 0;
    private static final int VILLAGE_CENTER_Z = 0;

    /*
     * 建築物と中央広場が近すぎないよう、
     * 12～30ブロック程度離している。
     *
     * NBTサイズに応じて後から調整可能。
     */
    private static final List<VillagePiece> PIECES =
            List.of(
                    new VillagePiece(
                            "enderman_tree_01",
                            0,
                            -18,
                            Rotation.NONE
                    ),
                    new VillagePiece(
                            "enderman_house_01",
                            22,
                            -10,
                            Rotation.NONE
                    ),
                    new VillagePiece(
                            "enderman_house_02",
                            -22,
                            -10,
                            Rotation.NONE
                    ),
                    new VillagePiece(
                            "enderman_storage_01",
                            24,
                            16,
                            Rotation.NONE
                    ),
                    new VillagePiece(
                            "enderman_work_station_01",
                            -24,
                            16,
                            Rotation.NONE
                    ),
                    new VillagePiece(
                            "enderman_farm_01",
                            0,
                            26,
                            Rotation.NONE
                    ),
                    new VillagePiece(
                            "enderman_farm_02",
                            30,
                            30,
                            Rotation.NONE
                    ),
                    new VillagePiece(
                            "enderman_farm_03",
                            -30,
                            30,
                            Rotation.NONE
                    ),
                    new VillagePiece(
                            "rechorus_facility",
                            0,
                            48,
                            Rotation.NONE
                    )


            );

    /**
     * 村一式を生成する。
     *
     * 戻り値はプレイヤーの到着地点。
     */
    public static BlockPos generateVillage(
            ServerLevel level
    ) {
        BlockPos villageCenter =
                new BlockPos(
                        0,
                        64,
                        0
                );

        /*
         * 中心チャンクをロードする。
         */
        level.getChunk(
                villageCenter
        );

        int successfullyPlaced =
                0;

        BlockPos elderSpawnPosition =
                null;

        BlockPos technicianSpawnPosition =
                null;

        BlockPos allyHomePosition =
                null;

        BlockPos rechorusFacilityAnchorPosition =
                null;

        BlockPos waterTransferMachineBSlotPosition =
                null;

        BlockPos rechorusPlantCoreSlotPosition =
                null;

        for (VillagePiece piece : PIECES) {
            BlockPos targetSurfaceAnchor =
                    villageCenter.offset(
                            piece.offsetX(),
                            0,
                            piece.offsetZ()
                    );

            PlacedVillagePiece placedPiece =
                    placeAtSurfaceAnchor(
                            level,
                            piece,
                            targetSurfaceAnchor
                    );

            if (!placedPiece.placed()) {
                continue;
            }

            successfullyPlaced++;

            /*
             * 住民マーカーは、それぞれのNBT内に
             * 存在する場合だけ取得する。
             */
            if (placedPiece.elderSpawnPosition() != null) {
                elderSpawnPosition =
                        placedPiece.elderSpawnPosition();
            }

            if (placedPiece.technicianSpawnPosition() != null) {
                technicianSpawnPosition =
                        placedPiece.technicianSpawnPosition();
            }

            if (placedPiece.allyHomePosition() != null) {
                allyHomePosition =
                        placedPiece.allyHomePosition();
            }

            if (placedPiece
                    .rechorusFacilityAnchorPosition()
                    != null) {

                rechorusFacilityAnchorPosition =
                        placedPiece
                                .rechorusFacilityAnchorPosition();
            }

            if (placedPiece
                    .waterTransferMachineBSlotPosition()
                    != null) {

                waterTransferMachineBSlotPosition =
                        placedPiece
                                .waterTransferMachineBSlotPosition();
            }

            if (placedPiece
                    .rechorusPlantCoreSlotPosition()
                    != null) {

                rechorusPlantCoreSlotPosition =
                        placedPiece
                                .rechorusPlantCoreSlotPosition();
            }
        }

        TheEndOfDragon.LOGGER.info(
                "Generated Enderman village pieces: {}/{}",
                successfullyPlaced,
                PIECES.size()
        );

        BlockPos returnGatewayPos =
                villageCenter;

        level.setBlock(
                returnGatewayPos,
                ModBlocks
                        .ENDERMAN_VILLAGE_RETURN_GATEWAY
                        .defaultBlockState(),
                3
        );

        TedVillageWorldState villageState =
                TedVillageWorldState.get(
                        level
                );

        villageState.setReturnGatewayPosition(
                returnGatewayPos
        );

        if (elderSpawnPosition == null
                || technicianSpawnPosition == null
                || allyHomePosition == null) {

            TheEndOfDragon.LOGGER.error(
                    "Village resident markers are missing: elder={}, technician={}, ally={}",
                    elderSpawnPosition,
                    technicianSpawnPosition,
                    allyHomePosition
            );
        } else {
            villageState.setResidentPositions(
                    elderSpawnPosition,
                    technicianSpawnPosition,
                    allyHomePosition
            );
        }

        if (rechorusFacilityAnchorPosition == null
                || waterTransferMachineBSlotPosition == null
                || rechorusPlantCoreSlotPosition == null) {

            TheEndOfDragon.LOGGER.error(
                    "Rechorus facility markers are missing: anchor={}, machineB={}, core={}",
                    rechorusFacilityAnchorPosition,
                    waterTransferMachineBSlotPosition,
                    rechorusPlantCoreSlotPosition
            );
        } else {
            villageState.setRechorusFacilityPositions(
                    rechorusFacilityAnchorPosition,
                    waterTransferMachineBSlotPosition,
                    rechorusPlantCoreSlotPosition
            );
        }

        /*
         * 村中央へ到着させる。
         *少しずらす
         * 足元Y=64。
         */
        BlockPos arrivalCenter =
                villageCenter.offset(
                        0,
                        0,
                        4
                );

        return findSafeArrivalPosition(
                level,
                arrivalCenter
        );
    }

    /**
     * NBT内のsurface_anhorを、
     * 指定した地表座標へ合わせて配置する。
     */
    private static PlacedVillagePiece placeAtSurfaceAnchor(
            ServerLevel level,
            VillagePiece piece,
            BlockPos targetSurfaceAnchor
    ) {
        Identifier templateId =
                TheEndOfDragon.id(
                        "structure_enderman_village/"
                                + piece.templateName()
                );

        Optional<StructureTemplate> optionalTemplate =
                level.getStructureManager()
                        .get(
                                templateId
                        );

        if (optionalTemplate.isEmpty()) {
            TheEndOfDragon.LOGGER.error(
                    "Missing Enderman village structure: {}",
                    templateId
            );

            return PlacedVillagePiece.failed();
        }

        StructureTemplate template =
                optionalTemplate.get();

        StructurePlaceSettings settings =
                new StructurePlaceSettings()
                        .setRotation(
                                piece.rotation()
                        )
                        .setIgnoreEntities(
                                false
                        );

        BlockPos localSurfaceAnchor =
                findMarker(
                        template,
                        settings,
                        SURFACE_ANCHOR_NAME
                );

        if (localSurfaceAnchor == null) {
            TheEndOfDragon.LOGGER.error(
                    "Structure {} has no jigsaw marker named {}",
                    templateId,
                    SURFACE_ANCHOR_NAME
            );

            return PlacedVillagePiece.failed();
        }

        BlockPos structureOrigin =
                targetSurfaceAnchor.subtract(
                        localSurfaceAnchor
                );

        level.getChunk(
                structureOrigin
        );

        /*
         * 配置前に各住民マーカーのローカル座標を取得する。
         */
        BlockPos localElderSpawn =
                findMarker(
                        template,
                        settings,
                        ELDER_SPAWN_NAME
                );

        BlockPos localTechnicianSpawn =
                findMarker(
                        template,
                        settings,
                        TECHNICIAN_SPAWN_NAME
                );

        BlockPos localAllyHome =
                findMarker(
                        template,
                        settings,
                        ALLY_HOME_NAME
                );

        boolean placed =
                template.placeInWorld(
                        level,
                        structureOrigin,
                        structureOrigin,
                        settings,
                        level.getRandom(),
                        2
                );

        BlockPos localRechorusFacilityAnchor =
                findMarker(
                        template,
                        settings,
                        RECHORUS_FACILITY_ANCHOR_NAME
                );

        BlockPos localWaterTransferMachineBSlot =
                findMarker(
                        template,
                        settings,
                        WATER_TRANSFER_MACHINE_B_SLOT_NAME
                );

        BlockPos localRechorusPlantCoreSlot =
                findMarker(
                        template,
                        settings,
                        RECHORUS_PLANT_CORE_SLOT_NAME
                );

        if (!placed) {
            TheEndOfDragon.LOGGER.error(
                    "Failed to place structure {} at {}",
                    templateId,
                    structureOrigin
            );

            return PlacedVillagePiece.failed();
        }

        /*
         * マーカーのワールド座標を求める。
         */
        BlockPos elderSpawnPosition =
                toWorldPosition(
                        structureOrigin,
                        localElderSpawn
                );

        BlockPos technicianSpawnPosition =
                toWorldPosition(
                        structureOrigin,
                        localTechnicianSpawn
                );

        BlockPos allyHomePosition =
                toWorldPosition(
                        structureOrigin,
                        localAllyHome
                );

        BlockPos rechorusFacilityAnchorPosition =
                toWorldPosition(
                        structureOrigin,
                        localRechorusFacilityAnchor
                );

        BlockPos waterTransferMachineBSlotPosition =
                toWorldPosition(
                        structureOrigin,
                        localWaterTransferMachineBSlot
                );

        BlockPos rechorusPlantCoreSlotPosition =
                toWorldPosition(
                        structureOrigin,
                        localRechorusPlantCoreSlot
                );

        /*
         * 配置後、使用したジグソーマーカーを削除する。
         */
        removeJigsawMarker(
                level,
                structureOrigin.offset(
                        localSurfaceAnchor
                )
        );

        removeJigsawMarker(
                level,
                elderSpawnPosition
        );

        removeJigsawMarker(
                level,
                technicianSpawnPosition
        );

        removeJigsawMarker(
                level,
                allyHomePosition
        );

        removeJigsawMarker(
                level,
                rechorusFacilityAnchorPosition
        );

        removeJigsawMarker(
                level,
                waterTransferMachineBSlotPosition
        );

        removeJigsawMarker(
                level,
                rechorusPlantCoreSlotPosition
        );

        TheEndOfDragon.LOGGER.info(
                "Placed village structure {} with anchor at {}",
                templateId,
                targetSurfaceAnchor
        );

        return new PlacedVillagePiece(
                true,
                elderSpawnPosition,
                technicianSpawnPosition,
                allyHomePosition,
                rechorusFacilityAnchorPosition,
                waterTransferMachineBSlotPosition,
                rechorusPlantCoreSlotPosition
        );
    }

    /**
     * StructureTemplate内から、
     * name=ted:surface_anhor のジグソーを探す。
     */
    private static BlockPos findMarker(
            StructureTemplate template,
            StructurePlaceSettings settings,
            String markerName
    ) {
        List<StructureTemplate.StructureBlockInfo>
                jigsawBlocks =
                template.filterBlocks(
                        BlockPos.ZERO,
                        settings,
                        Blocks.JIGSAW
                );

        for (StructureTemplate.StructureBlockInfo info
                : jigsawBlocks) {

            if (info.nbt() == null) {
                continue;
            }

            String foundName =
                    info.nbt()
                            .getString(
                                    "name"
                            )
                            .orElse("");

            if (!markerName.equals(
                    foundName
            )) {
                continue;
            }

            return info.pos();
        }

        return null;
    }

    private static BlockPos toWorldPosition(
            BlockPos structureOrigin,
            BlockPos localPosition
    ) {
        if (localPosition == null) {
            return null;
        }

        return structureOrigin.offset(
                localPosition
        );
    }

    private static void removeJigsawMarker(
            ServerLevel level,
            BlockPos position
    ) {
        if (position == null) {
            return;
        }

        if (!level.getBlockState(
                position
        ).is(
                Blocks.JIGSAW
        )) {
            return;
        }

        level.setBlock(
                position,
                Blocks.AIR.defaultBlockState(),
                3
        );
    }

    private static int getSurfaceY(
            ServerLevel level,
            int x,
            int z
    ) {
        return level.getHeight(
                Heightmap.Types
                        .MOTION_BLOCKING_NO_LEAVES,
                x,
                z
        );
    }

    /**
     * 中央地点の周囲から安全な到着場所を探す。
     */
    private static BlockPos findSafeArrivalPosition(
            ServerLevel level,
            BlockPos center
    ) {
        for (int radius = 0;
             radius <= 8;
             radius++) {

            for (int offsetX = -radius;
                 offsetX <= radius;
                 offsetX++) {

                for (int offsetZ = -radius;
                     offsetZ <= radius;
                     offsetZ++) {

                    int x =
                            center.getX()
                                    + offsetX;

                    int z =
                            center.getZ()
                                    + offsetZ;

                    int surfaceY =
                            getSurfaceY(
                                    level,
                                    x,
                                    z
                            );

                    BlockPos feet =
                            new BlockPos(
                                    x,
                                    surfaceY,
                                    z
                            );

                    BlockPos head =
                            feet.above();

                    BlockPos floor =
                            feet.below();

                    if (!level.getBlockState(
                            feet
                    ).isAir()) {
                        continue;
                    }

                    if (!level.getBlockState(
                            head
                    ).isAir()) {
                        continue;
                    }

                    if (level.getBlockState(
                            floor
                    ).isAir()) {
                        continue;
                    }

                    if (!level.getFluidState(
                            feet
                    ).isEmpty()) {
                        continue;
                    }

                    return feet;
                }
            }
        }

        /*
         * 見つからなかった場合の最終地点。
         */
        return center.above();
    }

    private record VillagePiece(
            String templateName,
            int offsetX,
            int offsetZ,
            Rotation rotation
    ) {
    }

    private record PlacedVillagePiece(
            boolean placed,
            BlockPos elderSpawnPosition,
            BlockPos technicianSpawnPosition,
            BlockPos allyHomePosition,
            BlockPos rechorusFacilityAnchorPosition,
            BlockPos waterTransferMachineBSlotPosition,
            BlockPos rechorusPlantCoreSlotPosition
    ) {
        private static PlacedVillagePiece failed() {
            return new PlacedVillagePiece(
                    false,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }
    }

    private TedVillageStructurePlacer() {
    }
}