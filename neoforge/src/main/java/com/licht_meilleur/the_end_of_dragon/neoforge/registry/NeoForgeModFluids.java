package com.licht_meilleur.the_end_of_dragon.neoforge.registry;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.neoforge.world.fluid.NeoForgeRechorusJuiceFluid;
import com.licht_meilleur.the_end_of_dragon.registry.ModFluids;
import com.licht_meilleur.the_end_of_dragon.world.fluid
        .RechorusJuiceFluid;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NeoForgeModFluids {

    /*
     * FLUIDレジストリの基底型はFluid。
     */
    public static final DeferredRegister<Fluid>
            FLUIDS =
            DeferredRegister.create(
                    Registries.FLUID,
                    TheEndOfDragon.MOD_ID
            );

    private static Fluid getSourceFluid() {
        return RECHORUS_JUICE_SOURCE.get();
    }

    private static Fluid getFlowingFluid() {
        return RECHORUS_JUICE_FLOWING.get();
    }

    public static final DeferredHolder<
            Fluid,
            NeoForgeRechorusJuiceFluid.Source>
            RECHORUS_JUICE_SOURCE =
            FLUIDS.register(
                    "rechorus_juice",
                    () -> new NeoForgeRechorusJuiceFluid.Source(
                            NeoForgeModFluids::getSourceFluid,
                            NeoForgeModFluids::getFlowingFluid
                    )
            );

    public static final DeferredHolder<
            Fluid,
            NeoForgeRechorusJuiceFluid.Flowing>
            RECHORUS_JUICE_FLOWING =
            FLUIDS.register(
                    "flowing_rechorus_juice",
                    () -> new NeoForgeRechorusJuiceFluid.Flowing(
                            NeoForgeModFluids::getSourceFluid,
                            NeoForgeModFluids::getFlowingFluid
                    )
            );



    public static void register(
            IEventBus modBus
    ) {
        FLUIDS.register(modBus);
    }

    public static void bindCommonReferences() {
        ModFluids.bindNeoForge(
                RECHORUS_JUICE_SOURCE.get(),
                RECHORUS_JUICE_FLOWING.get()
        );
    }

    private NeoForgeModFluids() {
    }
}