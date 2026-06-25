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

    public static void seal(ServerLevel level) {
        BlockPos center = findPortalCenter(level);
        if (center == null) {
            return;
        }

        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-2, 1, -2),
                center.offset(2, 1, 2)
        )) {
            if (level.getBlockState(pos).is(Blocks.END_PORTAL)) {
                level.setBlock(pos, Blocks.BEDROCK.defaultBlockState(), 3);
            }
        }
    }

    public static void unseal(ServerLevel level) {
        BlockPos center = findPortalCenter(level);
        if (center == null) {
            return;
        }

        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-2, 1, -2),
                center.offset(2, 1, 2)
        )) {
            if (level.getBlockState(pos).is(Blocks.BEDROCK)) {
                level.setBlock(pos, Blocks.END_PORTAL.defaultBlockState(), 3);
            }
        }
    }

    private static BlockPos findPortalCenter(ServerLevel level) {
        for (int y = SEARCH_MIN_Y; y <= SEARCH_MAX_Y; y++) {
            BlockPos pos = new BlockPos(CENTER_X, y, CENTER_Z);
            if (level.getBlockState(pos).is(Blocks.END_PORTAL)) {
                return pos;
            }
        }

        for (int y = SEARCH_MIN_Y; y <= SEARCH_MAX_Y; y++) {
            for (int x = -SEARCH_RADIUS; x <= SEARCH_RADIUS; x++) {
                for (int z = -SEARCH_RADIUS; z <= SEARCH_RADIUS; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (level.getBlockState(pos).is(Blocks.END_PORTAL)) {
                        return pos;
                    }
                }
            }
        }

        return null;
    }

    private EndPortalSealHandler() {
    }
}