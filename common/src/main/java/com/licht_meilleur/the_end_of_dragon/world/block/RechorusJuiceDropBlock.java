package com.licht_meilleur.the_end_of_dragon.world.block;

import com.licht_meilleur.the_end_of_dragon.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public final class RechorusJuiceDropBlock
        extends Block {

    private static final int CONVERT_DELAY =
            1;

    public RechorusJuiceDropBlock(
            BlockBehaviour.Properties properties
    ) {
        super(properties);
    }

    @Override
    protected void onPlace(
            BlockState state,
            net.minecraft.world.level.Level level,
            BlockPos position,
            BlockState oldState,
            boolean movedByPiston
    ) {
        super.onPlace(
                state,
                level,
                position,
                oldState,
                movedByPiston
        );

        if (level.isClientSide()) {
            return;
        }

        level.scheduleTick(
                position,
                this,
                CONVERT_DELAY
        );
    }

    @Override
    protected void tick(
            BlockState state,
            ServerLevel level,
            BlockPos position,
            RandomSource random
    ) {
        /*
         * 着地後、果汁水源ブロックへ変換する。
         *
         * ここへ将来、
         * 「果汁水槽の範囲内だけ許可」
         * という判定を追加する。
         */
        level.setBlock(
                position,
                ModBlocks.RECHORUS_JUICE
                        .defaultBlockState(),
                3
        );
    }
}