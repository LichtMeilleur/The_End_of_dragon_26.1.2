package com.licht_meilleur.the_end_of_dragon.neoforge.registry;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class NeoForgeModFluidTypes {

    public static final DeferredRegister<FluidType>
            FLUID_TYPES =
            DeferredRegister.create(
                    NeoForgeRegistries.Keys.FLUID_TYPES,
                    TheEndOfDragon.MOD_ID
            );

    public static final DeferredHolder<
            FluidType,
            FluidType>
            RECHORUS_JUICE =
            FLUID_TYPES.register(
                    "rechorus_juice",
                    () -> new FluidType(
                            FluidType.Properties
                                    .create()
                                    /*
                                     * 水より少し粘度を高くする。
                                     */
                                    .density(1050)
                                    .viscosity(1200)
                                    .lightLevel(6)
                    ) {
                    }
            );

    public static void register(
            IEventBus modBus
    ) {
        FLUID_TYPES.register(modBus);
    }

    private NeoForgeModFluidTypes() {
    }
}