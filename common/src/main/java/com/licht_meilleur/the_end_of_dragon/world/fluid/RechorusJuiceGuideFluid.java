package com.licht_meilleur.the_end_of_dragon.world.fluid;

import com.licht_meilleur.the_end_of_dragon.registry.ModBlocks;
import com.licht_meilleur.the_end_of_dragon.registry.ModFluids;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

public abstract class RechorusJuiceGuideFluid
        extends RechorusJuiceFluid {

    @Override
    public Fluid getFlowing() {
        return ModFluids
                .RECHORUS_JUICE_GUIDE_FLOWING;
    }

    @Override
    public Fluid getSource() {
        return ModFluids
                .RECHORUS_JUICE_GUIDE_SOURCE;
    }

    @Override
    public boolean isSame(
            Fluid fluid
    ) {
        return fluid
                == ModFluids.RECHORUS_JUICE_GUIDE_SOURCE
                || fluid
                == ModFluids.RECHORUS_JUICE_GUIDE_FLOWING;
    }

    /*
     * 仮流体はバケツで回収不可。
     */
    @Override
    public Item getBucket() {
        return Items.AIR;
    }

    @Override
    protected BlockState createLegacyBlock(
            FluidState state
    ) {
        return ModBlocks.RECHORUS_JUICE_GUIDE
                .defaultBlockState()
                .setValue(
                        LiquidBlock.LEVEL,
                        getLegacyLevel(state)
                );
    }

    public static final class Flowing
            extends RechorusJuiceGuideFluid {

        @Override
        protected void createFluidStateDefinition(
                StateDefinition.Builder<
                        Fluid,
                        FluidState> builder
        ) {
            super.createFluidStateDefinition(
                    builder
            );

            builder.add(LEVEL);
        }

        @Override
        public int getAmount(
                FluidState state
        ) {
            return state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(
                FluidState state
        ) {
            return false;
        }
    }

    public static final class Source
            extends RechorusJuiceGuideFluid {

        @Override
        public int getAmount(
                FluidState state
        ) {
            return 8;
        }

        @Override
        public boolean isSource(
                FluidState state
        ) {
            return true;
        }
    }
}