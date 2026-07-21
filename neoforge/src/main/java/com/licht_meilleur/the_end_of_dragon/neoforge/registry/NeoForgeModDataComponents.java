package com.licht_meilleur.the_end_of_dragon.neoforge.registry;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.registry.ModDataComponents;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NeoForgeModDataComponents {

    public static final DeferredRegister<
            DataComponentType<?>>
            DATA_COMPONENT_TYPES =
            DeferredRegister.create(
                    Registries.DATA_COMPONENT_TYPE,
                    TheEndOfDragon.MOD_ID
            );

    public static final DeferredHolder<
            DataComponentType<?>,
            DataComponentType<Integer>>
            TRUE_ENDER_PEARL_LEVEL =
            DATA_COMPONENT_TYPES.register(
                    "true_ender_pearl_level",
                    () ->
                            DataComponentType
                                    .<Integer>builder()
                                    .persistent(
                                            Codec.INT
                                    )
                                    .build()
            );

    public static void register(
            IEventBus modBus
    ) {
        DATA_COMPONENT_TYPES.register(
                modBus
        );
    }

    public static void bindCommonReferences() {
        ModDataComponents.bindNeoForge(
                TRUE_ENDER_PEARL_LEVEL.get()
        );
    }

    private NeoForgeModDataComponents() {
    }
}