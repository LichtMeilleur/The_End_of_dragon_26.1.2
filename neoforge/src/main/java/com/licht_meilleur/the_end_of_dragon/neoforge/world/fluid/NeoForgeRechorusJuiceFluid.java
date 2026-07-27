package com.licht_meilleur.the_end_of_dragon.neoforge.world.fluid;

import com.licht_meilleur.the_end_of_dragon.neoforge.registry
        .NeoForgeModFluidTypes;
import com.licht_meilleur.the_end_of_dragon.world.fluid
        .RechorusJuiceFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.function.Supplier;

public final class NeoForgeRechorusJuiceFluid {

    public static final class Source
            extends RechorusJuiceFluid.Source {

        public Source(
                Supplier<? extends Fluid> sourceSupplier,
                Supplier<? extends Fluid> flowingSupplier
        ) {
            super(
                    sourceSupplier,
                    flowingSupplier
            );
        }

        @Override
        public FluidType getFluidType() {
            return NeoForgeModFluidTypes
                    .RECHORUS_JUICE
                    .get();
        }
    }

    public static final class Flowing
            extends RechorusJuiceFluid.Flowing {

        public Flowing(
                Supplier<? extends Fluid> sourceSupplier,
                Supplier<? extends Fluid> flowingSupplier
        ) {
            super(
                    sourceSupplier,
                    flowingSupplier
            );
        }

        @Override
        public FluidType getFluidType() {
            return NeoForgeModFluidTypes
                    .RECHORUS_JUICE
                    .get();
        }
    }

    private NeoForgeRechorusJuiceFluid() {
    }
}