package com.licht_meilleur.the_end_of_dragon.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

public final class EndPortalSealHandler {
    private static final int CENTER_X = 0;
    private static final int CENTER_Z = 0;
    private static final int SEARCH_RADIUS = 12;
    private static final int SEARCH_MIN_Y = 0;
    private static final int SEARCH_MAX_Y = 128;

    public static BlockPos findPortalCenter(ServerLevel level) {
        for (int y = SEARCH_MIN_Y; y <= SEARCH_MAX_Y; y++) {
            BlockPos pos = new BlockPos(CENTER_X, y, CENTER_Z);
            if (isPortalStructureBlock(level, pos)) {
                return pos;
            }
        }

        for (int y = SEARCH_MIN_Y; y <= SEARCH_MAX_Y; y++) {
            for (int x = -SEARCH_RADIUS; x <= SEARCH_RADIUS; x++) {
                for (int z = -SEARCH_RADIUS; z <= SEARCH_RADIUS; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (isPortalStructureBlock(level, pos)) {
                        return pos;
                    }
                }
            }
        }

        return new BlockPos(0, 64, 0);
    }

    public static BlockPos findActualPortalCenter(ServerLevel level, BlockPos hint) {
        int count = 0;
        int sumX = 0;
        int sumY = 0;
        int sumZ = 0;

        for (int x = -10; x <= 10; x++) {
            for (int y = -20; y <= 20; y++) {
                for (int z = -10; z <= 10; z++) {
                    BlockPos pos = hint.offset(x, y, z);

                    if (level.getBlockState(pos).is(Blocks.END_PORTAL)) {
                        sumX += pos.getX();
                        sumY += pos.getY();
                        sumZ += pos.getZ();
                        count++;
                    }
                }
            }
        }

        if (count == 0) {
            return hint;
        }

        return new BlockPos(
                Math.round((float) sumX / count),
                Math.round((float) sumY / count),
                Math.round((float) sumZ / count)
        );
    }

    private static boolean isPortalStructureBlock(ServerLevel level, BlockPos pos) {
        var state = level.getBlockState(pos);
        return state.is(Blocks.END_PORTAL)
                || state.is(Blocks.END_PORTAL_FRAME)
                || state.is(Blocks.BEDROCK);
    }

    public static void sealPortal(ServerLevel level) {
        BlockPos center = findPortalCenter(level);
        if (center == null) return;

        // ポータル本体と中央柱周辺を消す/封印する
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-3, -2, -3),
                center.offset(3, 4, 3)
        )) {
            var state = level.getBlockState(pos);

            if (state.is(Blocks.END_PORTAL)
                    || state.is(Blocks.END_PORTAL_FRAME)
                    || state.is(Blocks.BEDROCK)) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

    public static void restorePortal(ServerLevel level) {
        BlockPos center = new BlockPos(0, 64, 0);

        // 足場
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-2, -1, -2),
                center.offset(2, -1, 2)
        )) {
            level.setBlock(pos, Blocks.BEDROCK.defaultBlockState(), 3);
        }

        // ポータル面
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-1, 0, -1),
                center.offset(1, 0, 1)
        )) {
            level.setBlock(pos, Blocks.END_PORTAL.defaultBlockState(), 3);
        }

        // 中央柱
        for (int y = 1; y <= 4; y++) {
            level.setBlock(center.above(y), Blocks.BEDROCK.defaultBlockState(), 3);
        }
    }

    public static void coverPortalArea(ServerLevel level) {
        BlockPos center = findPortalCenter(level);
        center = findActualPortalCenter(level, center);

        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-3, -2, -3),
                center.offset(3, 2, 3)
        )) {
            var state = level.getBlockState(pos);

            if (state.is(Blocks.BEDROCK)) continue;
            if (state.is(Blocks.AIR)
                    || state.is(Blocks.END_PORTAL)
                    || state.is(Blocks.END_PORTAL_FRAME)
                    || state.canBeReplaced()) {
                level.setBlock(pos, Blocks.BEDROCK.defaultBlockState(), 3);
            }
        }
    }

    private EndPortalSealHandler() {
    }
}