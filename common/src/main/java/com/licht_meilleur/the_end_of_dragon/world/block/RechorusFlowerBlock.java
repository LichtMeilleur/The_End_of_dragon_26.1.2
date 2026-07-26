package com.licht_meilleur.the_end_of_dragon.world.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class RechorusFlowerBlock
        extends Block {

    /*
     * UP / DOWN / NORTH / SOUTH / WEST / EAST
     * の6方向を保持する。
     */
    public static final EnumProperty<Direction> FACING =
            BlockStateProperties.FACING;

    private static final MapCodec<
            RechorusFlowerBlock> CODEC =
            simpleCodec(
                    RechorusFlowerBlock::new
            );

    public RechorusFlowerBlock(
            BlockBehaviour.Properties properties
    ) {
        super(properties);

        /*
         * モデルJSONの基準方向。
         *
         * Blockbenchで上向きに作っているため、
         * デフォルトはUP。
         */
        this.registerDefaultState(
                this.stateDefinition
                        .any()
                        .setValue(
                                FACING,
                                Direction.UP
                        )
        );
    }

    @Override
    protected MapCodec<
            ? extends Block> codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(
            BlockPlaceContext context
    ) {
        /*
         * クリックした面の外側を向く。
         *
         * 床へ置く     → UP
         * 天井へ置く   → DOWN
         * 壁へ置く     → 東西南北
         */
        return this.defaultBlockState()
                .setValue(
                        FACING,
                        context.getClickedFace()
                );
    }

    @Override
    protected BlockState rotate(
            BlockState state,
            Rotation rotation
    ) {
        return state.setValue(
                FACING,
                rotation.rotate(
                        state.getValue(FACING)
                )
        );
    }

    @Override
    protected BlockState mirror(
            BlockState state,
            Mirror mirror
    ) {
        return state.rotate(
                mirror.getRotation(
                        state.getValue(FACING)
                )
        );
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<
                    Block,
                    BlockState> builder
    ) {
        builder.add(FACING);
    }

    @Override
    protected VoxelShape getCollisionShape(
            BlockState state,
            BlockGetter level,
            BlockPos position,
            CollisionContext context
    ) {
        return Shapes.empty();
    }
}