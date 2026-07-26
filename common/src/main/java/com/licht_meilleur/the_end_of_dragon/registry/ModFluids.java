package com.licht_meilleur.the_end_of_dragon.registry;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.world.fluid
        .RechorusJuiceFluid;
import com.licht_meilleur.the_end_of_dragon.world.fluid.RechorusJuiceGuideFluid;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FlowingFluid;

public final class ModFluids {

    public static final ResourceKey<Fluid>
            RECHORUS_JUICE_SOURCE_KEY =
            ResourceKey.create(
                    Registries.FLUID,
                    TheEndOfDragon.id(
                            "rechorus_juice"
                    )
            );

    public static final ResourceKey<Fluid>
            RECHORUS_JUICE_FLOWING_KEY =
            ResourceKey.create(
                    Registries.FLUID,
                    TheEndOfDragon.id(
                            "flowing_rechorus_juice"
                    )
            );

    public static final ResourceKey<Fluid>
            RECHORUS_JUICE_GUIDE_SOURCE_KEY =
            ResourceKey.create(
                    Registries.FLUID,
                    TheEndOfDragon.id(
                            "rechorus_juice_guide"
                    )
            );

    public static final ResourceKey<Fluid>
            RECHORUS_JUICE_GUIDE_FLOWING_KEY =
            ResourceKey.create(
                    Registries.FLUID,
                    TheEndOfDragon.id(
                            "flowing_rechorus_juice_guide"
                    )
            );

    public static FlowingFluid
            RECHORUS_JUICE_SOURCE;

    public static FlowingFluid
            RECHORUS_JUICE_FLOWING;

    public static FlowingFluid
            RECHORUS_JUICE_GUIDE_SOURCE;

    public static FlowingFluid
            RECHORUS_JUICE_GUIDE_FLOWING;

    private static boolean fabricRegistered;

    public static void registerFabric() {
        if (fabricRegistered) {
            return;
        }

        fabricRegistered = true;

        RECHORUS_JUICE_SOURCE =
                Registry.register(
                        BuiltInRegistries.FLUID,
                        RECHORUS_JUICE_SOURCE_KEY
                                .identifier(),
                        new RechorusJuiceFluid.Source()
                );

        RECHORUS_JUICE_FLOWING =
                Registry.register(
                        BuiltInRegistries.FLUID,
                        RECHORUS_JUICE_FLOWING_KEY
                                .identifier(),
                        new RechorusJuiceFluid.Flowing()
                );

        RECHORUS_JUICE_GUIDE_SOURCE =
                Registry.register(
                        BuiltInRegistries.FLUID,
                        RECHORUS_JUICE_GUIDE_SOURCE_KEY
                                .identifier(),
                        new RechorusJuiceGuideFluid.Source()
                );

        RECHORUS_JUICE_GUIDE_FLOWING =
                Registry.register(
                        BuiltInRegistries.FLUID,
                        RECHORUS_JUICE_GUIDE_FLOWING_KEY
                                .identifier(),
                        new RechorusJuiceGuideFluid.Flowing()
                );

        TheEndOfDragon.LOGGER.info(
                "Registered Rechorus Juice fluids for Fabric"
        );
    }

    public static void bindNeoForge(
            FlowingFluid source,
            FlowingFluid flowing,
            FlowingFluid guideSource,
            FlowingFluid guideFlowing
    ) {
        RECHORUS_JUICE_SOURCE =
                source;

        RECHORUS_JUICE_FLOWING =
                flowing;

        RECHORUS_JUICE_GUIDE_SOURCE =
                guideSource;

        RECHORUS_JUICE_GUIDE_FLOWING =
                guideFlowing;
    }

    private ModFluids() {
    }
}
