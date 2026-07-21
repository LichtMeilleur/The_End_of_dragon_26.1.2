package com.licht_meilleur.the_end_of_dragon.registry;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceKey;

public final class ModDataComponents {

    public static final ResourceKey<
            DataComponentType<?>>
            TRUE_ENDER_PEARL_LEVEL_KEY =
            ResourceKey.create(
                    Registries.DATA_COMPONENT_TYPE,
                    TheEndOfDragon.id(
                            "true_ender_pearl_level"
                    )
            );

    public static DataComponentType<Integer>
            TRUE_ENDER_PEARL_LEVEL;

    private static boolean fabricRegistered =
            false;

    public static void registerFabric() {
        if (fabricRegistered) {
            return;
        }

        fabricRegistered = true;

        TRUE_ENDER_PEARL_LEVEL =
                Registry.register(
                        BuiltInRegistries
                                .DATA_COMPONENT_TYPE,
                        TRUE_ENDER_PEARL_LEVEL_KEY
                                .identifier(),
                        DataComponentType
                                .<Integer>builder()
                                .persistent(
                                        Codec.INT
                                )
                                .build()
                );

        TheEndOfDragon.LOGGER.info(
                "Registered The End Of Dragon data components for Fabric"
        );
    }

    public static void bindNeoForge(
            DataComponentType<Integer>
                    trueEnderPearlLevel
    ) {
        TRUE_ENDER_PEARL_LEVEL =
                trueEnderPearlLevel;
    }

    private ModDataComponents() {
    }
}