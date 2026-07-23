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

    public static final ResourceKey<Block> RECHORUS_MELON_KEY =
            ResourceKey.create(
                    Registries.BLOCK,
                    TheEndOfDragon.id("rechorus_melon")
            );

    public static final ResourceKey<Block> RECHORUS_PLANT_CORE_KEY =
            ResourceKey.create(
                    Registries.BLOCK,
                    TheEndOfDragon.id("rechorus_plant_core")
            );

    public static final ResourceKey<Block> WATER_TRANSFER_MACHINE_A_KEY =
            ResourceKey.create(
                    Registries.BLOCK,
                    TheEndOfDragon.id("water_transfer_machine_a")
            );

    public static final ResourceKey<Block> WATER_TRANSFER_MACHINE_B_KEY =
            ResourceKey.create(
                    Registries.BLOCK,
                    TheEndOfDragon.id("water_transfer_machine_b")
            );

    public static Block ENDERMAN_VILLAGE_GATEWAY;
    public static Block ENDERMAN_VILLAGE_RETURN_GATEWAY;
    public static Block RECHORUS_MELON;
    public static Block RECHORUS_PLANT_CORE;
    public static Block WATER_TRANSFER_MACHINE_A;
    public static Block WATER_TRANSFER_MACHINE_B;

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

        RECHORUS_MELON =
                Registry.register(
                        BuiltInRegistries.BLOCK,
                        RECHORUS_MELON_KEY.identifier(),
                        new Block(
                                BlockBehaviour.Properties
                                        .of()
                                        .setId(RECHORUS_MELON_KEY)
                                        .strength(1.0F)
                                        .sound(SoundType.WOOD)
                        )
                );

        RECHORUS_PLANT_CORE =
                Registry.register(
                        BuiltInRegistries.BLOCK,
                        RECHORUS_PLANT_CORE_KEY.identifier(),
                        new Block(
                                BlockBehaviour.Properties
                                        .of()
                                        .setId(RECHORUS_PLANT_CORE_KEY)
                                        .strength(2.0F, 6.0F)
                                        .sound(SoundType.WOOD)
                                        .lightLevel(state -> 5)
                                        .noOcclusion()
                        )
                );

        WATER_TRANSFER_MACHINE_A =
                Registry.register(
                        BuiltInRegistries.BLOCK,
                        WATER_TRANSFER_MACHINE_A_KEY.identifier(),
                        new Block(
                                BlockBehaviour.Properties
                                        .of()
                                        .setId(WATER_TRANSFER_MACHINE_A_KEY)
                                        .strength(3.0F, 12.0F)
                                        .sound(SoundType.METAL)
                                        .lightLevel(state -> 4)
                                        .noOcclusion()
                        )
                );

        WATER_TRANSFER_MACHINE_B =
                Registry.register(
                        BuiltInRegistries.BLOCK,
                        WATER_TRANSFER_MACHINE_B_KEY.identifier(),
                        new Block(
                                BlockBehaviour.Properties
                                        .of()
                                        .setId(WATER_TRANSFER_MACHINE_B_KEY)
                                        .strength(3.0F, 12.0F)
                                        .sound(SoundType.METAL)
                                        .lightLevel(state -> 4)
                                        .noOcclusion()
                        )
                );



        TheEndOfDragon.LOGGER.info(
                "Registered The End Of Dragon blocks for Fabric"
        );
    }

    public static void bindNeoForge(
            Block endermanVillageGateway,
            Block endermanVillageReturnGateway,
            Block rechorusMelon,
            Block rechorusPlantCore,
            Block waterTransferMachineA,
            Block waterTransferMachineB
    ) {
        ENDERMAN_VILLAGE_GATEWAY =
                endermanVillageGateway;

        ENDERMAN_VILLAGE_RETURN_GATEWAY =
                endermanVillageReturnGateway;

        RECHORUS_MELON =
                rechorusMelon;

        RECHORUS_PLANT_CORE =
                rechorusPlantCore;

        WATER_TRANSFER_MACHINE_A =
                waterTransferMachineA;

        WATER_TRANSFER_MACHINE_B =
                waterTransferMachineB;

        TheEndOfDragon.LOGGER.info(
                "Bound NeoForge blocks to common registry references"
        );
    }

    private ModBlocks() {
    }
}