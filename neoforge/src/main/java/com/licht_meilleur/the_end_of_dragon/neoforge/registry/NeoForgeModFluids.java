package com.licht_meilleur.the_end_of_dragon.neoforge.registry;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
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

    public static final DeferredHolder<
            Fluid,
            RechorusJuiceFluid.Source>
            RECHORUS_JUICE_SOURCE =
            FLUIDS.register(
                    "rechorus_juice",
                    RechorusJuiceFluid.Source::new
            );

    public static final DeferredHolder<
            Fluid,
            RechorusJuiceFluid.Flowing>
            RECHORUS_JUICE_FLOWING =
            FLUIDS.register(
                    "flowing_rechorus_juice",
                    RechorusJuiceFluid.Flowing::new
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