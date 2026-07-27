package com.licht_meilleur.the_end_of_dragon.world.block;

import com.licht_meilleur.the_end_of_dragon.registry.ModBlockEntities;
import com.licht_meilleur.the_end_of_dragon.world.block.entity
        .WaterTransferMachineABlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class WaterTransferMachineABlock
        extends BaseEntityBlock {

    private static final MapCodec<
            WaterTransferMachineABlock> CODEC =
            simpleCodec(
                    WaterTransferMachineABlock::new
            );

    public WaterTransferMachineABlock(
            BlockBehaviour.Properties properties
    ) {
        super(properties);
    }

    @Override
    protected MapCodec<
            ? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(
            BlockState state
    ) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(
            BlockPos position,
            BlockState state
    ) {
        return new WaterTransferMachineABlockEntity(
                position,
                state
        );
    }

    @Override
    public <T extends BlockEntity>
    BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> blockEntityType
    ) {
        if (level.isClientSide()) {
            return null;
        }

        return createTickerHelper(
                blockEntityType,
                ModBlockEntities.WATER_TRANSFER_MACHINE_A,
                WaterTransferMachineABlockEntity::serverTick
        );
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos position,
            Player player,
            BlockHitResult hitResult
    ) {
        return WaterTransferMachineInteraction
                .interact(
                        level,
                        position,
                        player,
                        InteractionHand.MAIN_HAND
                );
    }
}