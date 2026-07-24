package com.licht_meilleur.the_end_of_dragon.world.village;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.List;
import java.util.Optional;

public final class TedRechorusTreePlacer {

    private static final Identifier TEMPLATE_ID =
            TheEndOfDragon.id(
                    "structure_enderman_village/rechorus_tree"
            );

    private static final String CORE_MARKER_NAME =
            "ted:rechorus_core";

    public static boolean placeAtCore(
            ServerLevel level,
            BlockPos targetCorePosition
    ) {
        if (targetCorePosition == null) {
            return false;
        }

        Optional<StructureTemplate> optionalTemplate =
                level.getStructureManager()
                        .get(TEMPLATE_ID);

        if (optionalTemplate.isEmpty()) {
            TheEndOfDragon.LOGGER.error(
                    "Missing Rechorus tree structure: {}",
                    TEMPLATE_ID
            );

            return false;
        }

        StructureTemplate template =
                optionalTemplate.get();

        StructurePlaceSettings settings =
                new StructurePlaceSettings()
                        .setIgnoreEntities(false);

        BlockPos localCorePosition =
                findMarker(
                        template,
                        settings,
                        CORE_MARKER_NAME
                );

        if (localCorePosition == null) {
            TheEndOfDragon.LOGGER.error(
                    "Rechorus tree has no marker named {}",
                    CORE_MARKER_NAME
            );

            return false;
        }

        /*
         * NBT内のted:rechorus_coreを
         * targetCorePositionへ重ねる。
         */
        BlockPos structureOrigin =
                targetCorePosition.subtract(
                        localCorePosition
                );

        level.getChunk(structureOrigin);
        level.getChunk(targetCorePosition);

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
                    "Failed to place Rechorus tree at core {}",
                    targetCorePosition
            );

            return false;
        }

        /*
         * NBT配置でコア位置へジグソーが戻るため、
         * 本物のコアブロックへ置換する。
         */
        level.setBlock(
                targetCorePosition,
                ModBlocks.RECHORUS_PLANT_CORE
                        .defaultBlockState(),
                3
        );

        /*
         * 現時点ではroot / plantマーカーは
         * 次段階で専用ブロックへ置換する。
         *
         * 残っているジグソーだけ空気へする。
         */
        removeRemainingJigsaws(
                level,
                template,
                settings,
                structureOrigin,
                targetCorePosition
        );

        TheEndOfDragon.LOGGER.info(
                "Placed Rechorus tree at core {}",
                targetCorePosition
        );

        return true;
    }

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
                            .getString("name")
                            .orElse("");

            if (markerName.equals(foundName)) {
                return info.pos();
            }
        }

        return null;
    }

    private static void removeRemainingJigsaws(
            ServerLevel level,
            StructureTemplate template,
            StructurePlaceSettings settings,
            BlockPos structureOrigin,
            BlockPos corePosition
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

            BlockPos worldPosition =
                    structureOrigin.offset(
                            info.pos()
                    );

            if (worldPosition.equals(
                    corePosition
            )) {
                continue;
            }

            if (level.getBlockState(worldPosition)
                    .is(Blocks.JIGSAW)) {

                level.setBlock(
                        worldPosition,
                        Blocks.AIR.defaultBlockState(),
                        3
                );
            }
        }
    }

    private TedRechorusTreePlacer() {
    }
}