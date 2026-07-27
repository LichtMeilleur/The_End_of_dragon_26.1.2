package com.licht_meilleur.the_end_of_dragon.world.block;

import com.licht_meilleur.the_end_of_dragon.registry.ModBlockEntities;
import com.licht_meilleur.the_end_of_dragon.world.block.entity
        .RechorusPlantCoreBlockEntity;
import com.licht_meilleur.the_end_of_dragon.world.village
        .TedRechorusPlantManager;
import com.licht_meilleur.the_end_of_dragon.world.village
        .TedVillageWorldState;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public final class RechorusPlantCoreBlock
        extends BaseEntityBlock {

    private static final MapCodec<
            RechorusPlantCoreBlock> CODEC =
            simpleCodec(
                    RechorusPlantCoreBlock::new
            );

    public RechorusPlantCoreBlock(
            BlockBehaviour.Properties properties
    ) {
        super(
                properties
        );
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
        return new RechorusPlantCoreBlockEntity(
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
                ModBlockEntities.RECHORUS_PLANT_CORE,
                RechorusPlantCoreBlockEntity::serverTick
        );
    }

    /*
     * 1.21.8では、旧onRemove()の代わりに
     * affectNeighborsAfterRemoval()を使用する。
     */
    @Override
    protected void affectNeighborsAfterRemoval(
            BlockState oldState,
            ServerLevel level,
            BlockPos position,
            boolean movedByPiston
    ) {
        /*
         * このコアが管理していた
         * root・plant・花を崩壊させる。
         */
        TedRechorusPlantManager
                .collapseManagedPlant(
                        level,
                        position
                );

        /*
         * クエスト指定位置のコアだった場合だけ、
         * クエスト用の設置・完成状態を解除する。
         */
        TedVillageWorldState villageState =
                TedVillageWorldState.get(
                        level
                );

        BlockPos questCorePosition =
                villageState
                        .getRechorusPlantCoreSlotPosition();

        if (questCorePosition != null
                && questCorePosition.equals(
                position
        )) {
            villageState
                    .setRechorusPlantCoreInstalled(
                            false
                    );

            villageState
                    .setRechorusPlantBuilt(
                            false
                    );
        }

        super.affectNeighborsAfterRemoval(
                oldState,
                level,
                position,
                movedByPiston
        );
    }
}