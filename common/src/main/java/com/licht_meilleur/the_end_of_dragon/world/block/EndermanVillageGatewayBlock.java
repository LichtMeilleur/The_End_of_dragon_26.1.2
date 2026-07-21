package com.licht_meilleur.the_end_of_dragon.world.block;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.registry
        .ModBlockEntities;
import com.licht_meilleur.the_end_of_dragon.world.TedBattleWorldState;
import com.licht_meilleur.the_end_of_dragon.world.block.entity
        .EndermanVillageGatewayBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class EndermanVillageGatewayBlock
        extends BaseEntityBlock {

    public static final MapCodec<
            EndermanVillageGatewayBlock> CODEC =
            simpleCodec(
                    EndermanVillageGatewayBlock::new
            );

    /*
     * 高さ8/16、つまりハーフブロック相当。
     */
    private static final VoxelShape SHAPE =
            box(
                    0.0D,
                    0.0D,
                    0.0D,
                    16.0D,
                    8.0D,
                    16.0D
            );

    public EndermanVillageGatewayBlock(
            Properties properties
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
    protected VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(
            BlockPos pos,
            BlockState state
    ) {
        return new EndermanVillageGatewayBlockEntity(
                pos,
                state
        );
    }

    @Nullable
    @Override
    public <T extends BlockEntity>
    BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type
    ) {
        if (level.isClientSide()) {
            return null;
        }

        return createTickerHelper(
                type,
                ModBlockEntities
                        .ENDERMAN_VILLAGE_GATEWAY,
                EndermanVillageGatewayBlockEntity
                        ::serverTick
        );
    }

    protected boolean registersAsReturnDestination() {
        return true;
    }

    @Override
    public void setPlacedBy(
            Level level,
            BlockPos pos,
            BlockState state,
            LivingEntity placer,
            ItemStack stack
    ) {
        super.setPlacedBy(
                level,
                pos,
                state,
                placer,
                stack
        );

        if (!registersAsReturnDestination()) {
            return;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        ServerLevel endLevel =
                serverLevel.getServer()
                        .getLevel(Level.END);

        if (endLevel == null) {
            return;
        }

        TedBattleWorldState worldState =
                TedBattleWorldState.get(endLevel);

        worldState.registerReturnGateway(
                serverLevel,
                pos
        );

        TheEndOfDragon.LOGGER.info(
                "Registered village gateway A at {} in {}",
                pos,
                serverLevel.dimension().identifier()
        );
    }

    @Override
    public BlockState playerWillDestroy(
            Level level,
            BlockPos pos,
            BlockState state,
            Player player
    ) {
        if (registersAsReturnDestination()
                && level instanceof ServerLevel serverLevel) {

            unregisterGatewayPosition(
                    serverLevel,
                    pos
            );
        }

        return super.playerWillDestroy(
                level,
                pos,
                state,
                player
        );
    }

    private static void unregisterGatewayPosition(
            ServerLevel gatewayLevel,
            BlockPos gatewayPos
    ) {
        ServerLevel endLevel =
                gatewayLevel.getServer()
                        .getLevel(
                                Level.END
                        );

        if (endLevel == null) {
            return;
        }

        TedBattleWorldState worldState =
                TedBattleWorldState.get(
                        endLevel
                );

        worldState.unregisterReturnGateway(
                gatewayLevel,
                gatewayPos
        );

        TheEndOfDragon.LOGGER.info(
                "Unregistered village gateway A at {} in {}",
                gatewayPos,
                gatewayLevel.dimension()
                        .identifier()
        );
    }
}