package com.licht_meilleur.the_end_of_dragon.world.village;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.registry.ModBlocks;
import com.licht_meilleur.the_end_of_dragon.world.block
        .RechorusFlowerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem
        .StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem
        .StructureTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public final class TedRechorusTreePlacer {

    private static final Identifier TEMPLATE_ID =
            TheEndOfDragon.id(
                    "structure_enderman_village/rechorus_tree"
            );

    /*
     * rechorus_tree.nbt内のマーカー名。
     */
    private static final String CORE_MARKER_NAME =
            "ted:rechorus_core";

    private static final String ROOT_MARKER_NAME =
            "ted:rechorus_root";

    /*
     * 名前はplantだが、実際には幹として扱う。
     */
    private static final String STEM_MARKER_NAME =
            "ted:rechorus_stem";

    /*
     * ========================================
     * 花生成設定
     * ========================================
     */


    /*
     * 生成する最低花数。
     */
    private static final int MIN_FLOWERS =
            2;

    /*
     * 生成する最大花数。
     *
     * 花の量を変えたい場合は、
     * 基本的にこの値を調整する。
     */
    private static final int MAX_FLOWERS =
            4;

    /*
     * 上向きの花が選ばれる重み。
     */
    private static final int UP_FLOWER_WEIGHT =
            6;

    /*
     * 東西南北の側面へ咲く重み。
     */
    private static final int SIDE_FLOWER_WEIGHT =
            3;

    /*
     * 下向きへ咲く重み。
     */
    private static final int DOWN_FLOWER_WEIGHT =
            1;

    public static boolean placeAtCore(
            ServerLevel level,
            BlockPos targetCorePosition
    ) {
        if (level == null
                || targetCorePosition == null) {
            return false;
        }

        var optionalTemplate =
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
         * NBT内のコアマーカーを、
         * 施設側の指定コア位置へ重ねる。
         */
        BlockPos structureOrigin =
                targetCorePosition.subtract(
                        localCorePosition
                );

        level.getChunk(structureOrigin);
        level.getChunk(targetCorePosition);

        /*
         * rechorus_tree.nbt自体はワールドへ配置しない。
         *
         * NBTは根・幹・コアの位置を示す
         * 設計図としてのみ使用する。
         */
        level.getChunk(structureOrigin);
        level.getChunk(targetCorePosition);

        /*
         * プレイヤーが設置した本物のコアを維持する。
         */
        level.setBlock(
                targetCorePosition,
                ModBlocks.RECHORUS_PLANT_CORE
                        .defaultBlockState(),
                3
        );

        /*
         * NBT内のマーカー座標だけを読み、
         * 根と幹を個別に配置する。
         */
        int rootCount =
                replaceRootMarkers(
                        level,
                        template,
                        settings,
                        structureOrigin
                );

        List<BlockPos> stemPositions =
                replaceStemMarkers(
                        level,
                        template,
                        settings,
                        structureOrigin
                );

        int flowerCount =
                placeFlowers(
                        level,
                        stemPositions,
                        targetCorePosition
                );

        TheEndOfDragon.LOGGER.info(
                "Placed Rechorus tree markers at core {}: roots={}, stems={}, flowers={}",
                targetCorePosition,
                rootCount,
                stemPositions.size(),
                flowerCount
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

            String foundName =
                    getMarkerName(info);

            if (markerName.equals(foundName)) {
                return info.pos();
            }
        }

        return null;
    }

    /*
     * ted:rechorus_rootを
     * RECHORUS_ROOTへ置換する。
     */
    private static int replaceRootMarkers(
            ServerLevel level,
            StructureTemplate template,
            StructurePlaceSettings settings,
            BlockPos structureOrigin
    ) {
        int replacedCount =
                0;

        List<StructureTemplate.StructureBlockInfo>
                jigsawBlocks =
                template.filterBlocks(
                        BlockPos.ZERO,
                        settings,
                        Blocks.JIGSAW
                );

        for (StructureTemplate.StructureBlockInfo info
                : jigsawBlocks) {

            if (!ROOT_MARKER_NAME.equals(
                    getMarkerName(info)
            )) {
                continue;
            }

            BlockPos worldPosition =
                    structureOrigin.offset(
                            info.pos()
                    );

            level.setBlock(
                    worldPosition,
                    ModBlocks.RECHORUS_ROOT
                            .defaultBlockState(),
                    3
            );

            replacedCount++;
        }

        return replacedCount;
    }

    /*
     * ted:rechorus_plantを
     * RECHORUS_PLANTへ置換する。
     *
     * 戻り値は実際に配置した幹のワールド座標。
     */
    private static List<BlockPos> replaceStemMarkers(
            ServerLevel level,
            StructureTemplate template,
            StructurePlaceSettings settings,
            BlockPos structureOrigin
    ) {
        List<BlockPos> stemPositions =
                new ArrayList<>();

        List<StructureTemplate.StructureBlockInfo>
                jigsawBlocks =
                template.filterBlocks(
                        BlockPos.ZERO,
                        settings,
                        Blocks.JIGSAW
                );

        for (StructureTemplate.StructureBlockInfo info
                : jigsawBlocks) {

            if (!STEM_MARKER_NAME.equals(
                    getMarkerName(info)
            )) {
                continue;
            }

            BlockPos worldPosition =
                    structureOrigin.offset(
                            info.pos()
                    );

            level.setBlock(
                    worldPosition,
                    ModBlocks.RECHORUS_PLANT
                            .defaultBlockState(),
                    3
            );

            stemPositions.add(
                    worldPosition.immutable()
            );
        }

        return stemPositions;
    }

    /*
     * 幹候補から花を生成する。
     *
     * 1つの幹につき最大1個だけ候補を作る。
     */
    private static int placeFlowers(
            ServerLevel level,
            List<BlockPos> stemPositions,
            BlockPos corePosition
    ) {
        if (stemPositions == null
                || stemPositions.isEmpty()) {
            return 0;
        }

        /*
         * コア座標をシードに使うことで、
         * 同じ場所なら同じ花配置になりやすくする。
         */
        Random random =
                new Random(
                        corePosition.asLong()
                );

        List<FlowerCandidate> candidates =
                new ArrayList<>();

        Set<BlockPos> reservedFlowerPositions =
                new HashSet<>();

        for (BlockPos stemPosition
                : stemPositions) {

            List<Direction> availableDirections =
                    getAvailableFlowerDirections(
                            level,
                            stemPosition
                    );

            if (availableDirections.isEmpty()) {
                continue;
            }

            Direction selectedDirection =
                    selectWeightedDirection(
                            availableDirections,
                            random
                    );

            if (selectedDirection == null) {
                continue;
            }

            BlockPos flowerPosition =
                    stemPosition.relative(
                            selectedDirection
                    );

            /*
             * 隣接した幹から同じ位置へ花を出そうとする場合を除外。
             */
            if (!reservedFlowerPositions.add(
                    flowerPosition.immutable()
            )) {
                continue;
            }

            candidates.add(
                    new FlowerCandidate(
                            stemPosition.immutable(),
                            flowerPosition.immutable(),
                            selectedDirection
                    )
            );
        }

        if (candidates.isEmpty()) {
            return 0;
        }

        Collections.shuffle(
                candidates,
                random
        );

        int requestedFlowerCount =
                getRequestedFlowerCount(
                        random
                );

        int actualFlowerLimit =
                Math.min(
                        requestedFlowerCount,
                        candidates.size()
                );

        int placedCount =
                0;

        for (int index = 0;
             index < actualFlowerLimit;
             index++) {

            FlowerCandidate candidate =
                    candidates.get(index);

            if (placeFlower(
                    level,
                    candidate
            )) {
                placedCount++;
            }
        }

        return placedCount;
    }

    /*
     * 指定した幹の周囲から、
     * 花を配置可能な方向を取得する。
     */
    private static List<Direction>
    getAvailableFlowerDirections(
            ServerLevel level,
            BlockPos stemPosition
    ) {
        List<Direction> directions =
                new ArrayList<>();

        for (Direction direction
                : Direction.values()) {

            BlockPos flowerPosition =
                    stemPosition.relative(
                            direction
                    );

            BlockState flowerPositionState =
                    level.getBlockState(
                            flowerPosition
                    );

            /*
             * 空気以外には花を置かない。
             */
            if (!flowerPositionState.isAir()) {
                continue;
            }

            /*
             * 念のため、隣が幹なら候補にしない。
             */
            if (flowerPositionState.is(
                    ModBlocks.RECHORUS_PLANT
            )) {
                continue;
            }

            directions.add(
                    direction
            );
        }

        return directions;
    }

    /*
     * 方向ごとの重みを使って、
     * 花の向きを1つ選ぶ。
     */
    private static Direction selectWeightedDirection(
            List<Direction> directions,
            Random random
    ) {
        int totalWeight =
                0;

        for (Direction direction
                : directions) {

            totalWeight +=
                    getFlowerDirectionWeight(
                            direction
                    );
        }

        if (totalWeight <= 0) {
            return null;
        }

        int selectedValue =
                random.nextInt(
                        totalWeight
                );

        for (Direction direction
                : directions) {

            selectedValue -=
                    getFlowerDirectionWeight(
                            direction
                    );

            if (selectedValue < 0) {
                return direction;
            }
        }

        return directions.getFirst();
    }

    private static int getFlowerDirectionWeight(
            Direction direction
    ) {
        return switch (direction) {
            case UP ->
                    UP_FLOWER_WEIGHT;

            case NORTH,
                 SOUTH,
                 EAST,
                 WEST ->
                    SIDE_FLOWER_WEIGHT;

            case DOWN ->
                    DOWN_FLOWER_WEIGHT;
        };
    }

    /*
     * MIN_FLOWERS～MAX_FLOWERSの範囲から、
     * 今回生成する花数を決める。
     */
    private static int getRequestedFlowerCount(
            Random random
    ) {
        int minimum =
                Math.max(
                        0,
                        MIN_FLOWERS
                );

        int maximum =
                Math.max(
                        minimum,
                        MAX_FLOWERS
                );

        if (minimum == maximum) {
            return minimum;
        }

        return minimum
                + random.nextInt(
                maximum
                        - minimum
                        + 1
        );
    }

    private static boolean placeFlower(
            ServerLevel level,
            FlowerCandidate candidate
    ) {
        if (!level.getBlockState(
                candidate.flowerPosition()
        ).isAir()) {
            return false;
        }


        BlockState flowerState =
                ModBlocks.RECHORUS_FLOWER
                        .defaultBlockState()
                        .setValue(
                                RechorusFlowerBlock.FACING,
                                candidate.direction()
                        );

        return level.setBlock(
                candidate.flowerPosition(),
                flowerState,
                3
        );
    }

    private static String getMarkerName(
            StructureTemplate.StructureBlockInfo info
    ) {
        if (info == null
                || info.nbt() == null) {
            return "";
        }

        return info.nbt()
                .getString("name")
                .orElse("");
    }

    /*
     * 既知のマーカー以外のジグソーが残っていた場合、
     * 空気へ置換する。
     */
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

            if (!level.getBlockState(
                    worldPosition
            ).is(
                    Blocks.JIGSAW
            )) {
                continue;
            }

            level.setBlock(
                    worldPosition,
                    Blocks.AIR.defaultBlockState(),
                    3
            );
        }
    }

    private record FlowerCandidate(
            BlockPos stemPosition,
            BlockPos flowerPosition,
            Direction direction
    ) {
    }

    public static List<PlantPartTarget> getPlantPartTargets(
            ServerLevel level,
            BlockPos corePosition
    ) {
        if (level == null
                || corePosition == null) {
            return List.of();
        }

        var optionalTemplate =
                level.getStructureManager()
                        .get(TEMPLATE_ID);

        if (optionalTemplate.isEmpty()) {
            TheEndOfDragon.LOGGER.error(
                    "Missing Rechorus tree structure while reading regeneration targets: {}",
                    TEMPLATE_ID
            );

            return List.of();
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
                    "Rechorus tree has no core marker while reading regeneration targets: {}",
                    CORE_MARKER_NAME
            );

            return List.of();
        }

        /*
         * NBT内のコア位置を、
         * 実際のコア位置へ重ねたときの原点。
         */
        BlockPos structureOrigin =
                corePosition.subtract(
                        localCorePosition
                );

        List<PlantPartTarget> targets =
                new ArrayList<>();

        List<StructureTemplate.StructureBlockInfo>
                jigsawBlocks =
                template.filterBlocks(
                        BlockPos.ZERO,
                        settings,
                        Blocks.JIGSAW
                );

        for (StructureTemplate.StructureBlockInfo info
                : jigsawBlocks) {

            String markerName =
                    getMarkerName(info);

            BlockPos worldPosition =
                    structureOrigin.offset(
                            info.pos()
                    ).immutable();

            if (ROOT_MARKER_NAME.equals(
                    markerName
            )) {
                targets.add(
                        new PlantPartTarget(
                                worldPosition,
                                ModBlocks.RECHORUS_ROOT
                                        .defaultBlockState()
                        )
                );

                continue;
            }

            if (STEM_MARKER_NAME.equals(
                    markerName
            )) {
                targets.add(
                        new PlantPartTarget(
                                worldPosition,
                                ModBlocks.RECHORUS_PLANT
                                        .defaultBlockState()
                        )
                );
            }
        }

        return List.copyOf(
                targets
        );
    }

    public record PlantPartTarget(
            BlockPos position,
            BlockState state
    ) {
    }

    public static int getMaximumFlowerCount() {
        return Math.max(
                0,
                MAX_FLOWERS
        );
    }

    private TedRechorusTreePlacer() {
    }
}