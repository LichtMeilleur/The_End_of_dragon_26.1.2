package com.licht_meilleur.the_end_of_dragon.registry;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.world.block
        .EndermanVillageGatewayBlock;
import com.licht_meilleur.the_end_of_dragon.world.block.EndermanVillageReturnGatewayBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class ModBlocks {

    public static final ResourceKey<Block>
            ENDERMAN_VILLAGE_GATEWAY_KEY =
            ResourceKey.create(
                    Registries.BLOCK,
                    TheEndOfDragon.id(
                            "enderman_village_gateway"
                    )
            );

    public static final ResourceKey<Block>
            ENDERMAN_VILLAGE_RETURN_GATEWAY_KEY =
            ResourceKey.create(
                    Registries.BLOCK,
                    TheEndOfDragon.id(
                            "enderman_village_return_gateway"
                    )
            );

    public static Block ENDERMAN_VILLAGE_GATEWAY;
    public static Block ENDERMAN_VILLAGE_RETURN_GATEWAY;

    private static boolean fabricRegistered = false;

    public static void registerFabric() {
        if (fabricRegistered) {
            return;
        }

        fabricRegistered = true;

        ENDERMAN_VILLAGE_GATEWAY =
                Registry.register(
                        BuiltInRegistries.BLOCK,
                        ENDERMAN_VILLAGE_GATEWAY_KEY
                                .identifier(),
                        new EndermanVillageGatewayBlock(
                                BlockBehaviour.Properties
                                        .of()
                                        .setId(
                                                ENDERMAN_VILLAGE_GATEWAY_KEY
                                        )
                                        .strength(
                                                3.0F,
                                                1200.0F
                                        )
                                        .sound(
                                                SoundType.AMETHYST
                                        )
                                        .lightLevel(
                                                state -> 10
                                        )
                                        .noOcclusion()
                        )
                );

        ENDERMAN_VILLAGE_RETURN_GATEWAY =
                Registry.register(
                        BuiltInRegistries.BLOCK,
                        ENDERMAN_VILLAGE_RETURN_GATEWAY_KEY
                                .identifier(),
                        new EndermanVillageReturnGatewayBlock(
                                BlockBehaviour.Properties
                                        .of()
                                        .setId(
                                                ENDERMAN_VILLAGE_RETURN_GATEWAY_KEY
                                        )

                                        /*
                                         * サバイバルでは破壊不可。
                                         */
                                        .strength(
                                                -1.0F,
                                                3_600_000.0F
                                        )
                                        .sound(
                                                SoundType.AMETHYST
                                        )
                                        .lightLevel(
                                                state -> 10
                                        )
                                        .noOcclusion()
                        )
                );

        TheEndOfDragon.LOGGER.info(
                "Registered The End Of Dragon blocks for Fabric"
        );
    }

    public static void bindNeoForge(
            Block endermanVillageGateway,
            Block endermanVillageReturnGateway
    ) {
        ENDERMAN_VILLAGE_GATEWAY =
                endermanVillageGateway;

        ENDERMAN_VILLAGE_RETURN_GATEWAY =
                endermanVillageReturnGateway;

        TheEndOfDragon.LOGGER.info(
                "Bound NeoForge blocks to common registry references"
        );
    }

    private ModBlocks() {
    }
}