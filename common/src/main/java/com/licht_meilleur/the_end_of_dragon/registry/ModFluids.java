package com.licht_meilleur.the_end_of_dragon.registry;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.world.fluid
        .RechorusJuiceFluid;
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


    public static FlowingFluid
            RECHORUS_JUICE_SOURCE;

    public static FlowingFluid
            RECHORUS_JUICE_FLOWING;



    private static boolean fabricRegistered;

    public static void registerFabric() {
        if (fabricRegistered) {
            return;
        }

        fabricRegistered = true;

        /*
         * Registry.registerより先にSourceとFlowingの
         * 両インスタンスを作成し、Common参照へ代入する。
         *
         * これにより登録途中にgetSource()/getFlowing()が
         * 呼ばれてもnullや誤ったfallbackを返さない。
         */
        RechorusJuiceFluid.Source source =
                new RechorusJuiceFluid.Source(
                        () -> RECHORUS_JUICE_SOURCE,
                        () -> RECHORUS_JUICE_FLOWING
                );

        RechorusJuiceFluid.Flowing flowing =
                new RechorusJuiceFluid.Flowing(
                        () -> RECHORUS_JUICE_SOURCE,
                        () -> RECHORUS_JUICE_FLOWING
                );

        /*
         * 登録処理より先に相互参照を成立させる。
         */
        RECHORUS_JUICE_SOURCE =
                source;

        RECHORUS_JUICE_FLOWING =
                flowing;

        RECHORUS_JUICE_SOURCE =
                Registry.register(
                        BuiltInRegistries.FLUID,
                        RECHORUS_JUICE_SOURCE_KEY
                                .identifier(),
                        source
                );

        RECHORUS_JUICE_FLOWING =
                Registry.register(
                        BuiltInRegistries.FLUID,
                        RECHORUS_JUICE_FLOWING_KEY
                                .identifier(),
                        flowing
                );

        TheEndOfDragon.LOGGER.info(
                "Registered Rechorus Juice fluids for Fabric"
        );
    }

    public static void bindNeoForge(
            FlowingFluid source,
            FlowingFluid flowing

    ) {
        RECHORUS_JUICE_SOURCE =
                source;

        RECHORUS_JUICE_FLOWING =
                flowing;

    }

    private ModFluids() {
    }
}
