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

        for (VillagePiece piece : PIECES) {
            BlockPos targetSurfaceAnchor =
                    villageCenter.offset(
                            piece.offsetX(),
                            0,
                            piece.offsetZ()
                    );

            boolean placed =
                    placeAtSurfaceAnchor(
                            level,
                            piece,
                            targetSurfaceAnchor
                    );

            if (placed) {
                successfullyPlaced++;
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
    private static boolean placeAtSurfaceAnchor(
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

            return false;
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

        BlockPos localAnchor =
                findSurfaceAnchor(
                        template,
                        settings
                );

        if (localAnchor == null) {
            TheEndOfDragon.LOGGER.error(
                    "Structure {} has no jigsaw marker named {}",
                    templateId,
                    SURFACE_ANCHOR_NAME
            );

            return false;
        }

        /*
         * NBT内部のanchor座標がtargetSurfaceAnchorへ
         * 来るように、構造物原点を逆算する。
         *
         * 現段階ではRotation.NONEなので、
         * 単純な座標減算で一致する。
         */
        BlockPos structureOrigin =
                targetSurfaceAnchor.subtract(
                        localAnchor
                );

        /*
         * 配置予定チャンクをロードする。
         */
        level.getChunk(
                structureOrigin
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

        if (!placed) {
            TheEndOfDragon.LOGGER.error(
                    "Failed to place structure {} at {}",
                    templateId,
                    structureOrigin
            );

            return false;
        }

        /*
         * 配置後、目印として使ったジグソーブロックを消す。
         */
        BlockPos placedAnchorPos =
                structureOrigin.offset(
                        localAnchor
                );

        if (level.getBlockState(
                placedAnchorPos
        ).is(Blocks.JIGSAW)) {
            level.setBlock(
                    placedAnchorPos,
                    Blocks.AIR.defaultBlockState(),
                    3
            );
        }

        TheEndOfDragon.LOGGER.info(
                "Placed village structure {} with anchor at {}",
                templateId,
                targetSurfaceAnchor
        );

        return true;
    }

    /**
     * StructureTemplate内から、
     * name=ted:surface_anhor のジグソーを探す。
     */
    private static BlockPos findSurfaceAnchor(
            StructureTemplate template,
            StructurePlaceSettings settings
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

            String markerName =
                    info.nbt()
                            .getString(
                                    "name"
                            )
                            .orElse("");

            if (!SURFACE_ANCHOR_NAME.equals(
                    markerName
            )) {
                continue;
            }

            return info.pos();
        }

        return null;
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

    private TedVillageStructurePlacer() {
    }
}