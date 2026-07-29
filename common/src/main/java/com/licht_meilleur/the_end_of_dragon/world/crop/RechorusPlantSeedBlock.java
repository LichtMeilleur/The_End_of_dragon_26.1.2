package com.licht_meilleur.the_end_of_dragon.world.crop;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.world.village
        .TedRechorusTreePlacer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.MangrovePropaguleBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class RechorusPlantSeedBlock
        extends MangrovePropaguleBlock {

    public RechorusPlantSeedBlock(
            TreeGrower treeGrower,
            BlockBehaviour.Properties properties
    ) {
        super(
                treeGrower,
                properties
        );
    }

    @Override
    public void advanceTree(
            ServerLevel level,
            BlockPos position,
            BlockState state,
            RandomSource random
    ) {
        /*
         * 吊り下がっているマングローブの芽は、
         * 通常どおり成長段階だけ進める。
         *
         * 地面へ植えた試作苗だけを
         * リコーラス変異の対象にする。
         */
        if (state.getValue(HANGING)) {
            super.advanceTree(
                    level,
                    position,
                    state,
                    random
            );

            return;
        }

        RechorusIrrigationType irrigationType =
                RechorusIrrigationHelper.findIrrigation(
                        level,
                        position.below()
                );

        float mutationChance =
                RechorusMutationChances.getChance(
                        RechorusSeedType.PROTOTYPE,
                        irrigationType
                );

        boolean mutated =
                random.nextFloat()
                        < mutationChance;

        /*
         * 変異しなかった場合は、
         * 通常のマングローブとして成長させる。
         */
        if (!mutated) {
            super.advanceTree(
                    level,
                    position,
                    state,
                    random
            );

            return;
        }

        /*
         * 変異成功。
         *
         * 苗木の位置をそのまま
         * リコーラスプラントのコア位置として扱う。
         */
        boolean placed =
                TedRechorusTreePlacer.placeAtCore(
                        level,
                        position
                );

        if (placed) {
            TheEndOfDragon.LOGGER.info(
                    "Prototype Mangrove Propagule mutated into Rechorus Plant: position={}, irrigation={}, chance={}",
                    position,
                    irrigationType,
                    mutationChance
            );

            return;
        }

        /*
         * NBT欠落などでリコーラス生成に失敗した場合、
         * 苗木を消失させず通常マングローブを試す。
         */
        TheEndOfDragon.LOGGER.warn(
                "Failed to place mutated Rechorus Plant; falling back to Mangrove: position={}",
                position
        );

        super.advanceTree(
                level,
                position,
                state,
                random
        );
    }
}