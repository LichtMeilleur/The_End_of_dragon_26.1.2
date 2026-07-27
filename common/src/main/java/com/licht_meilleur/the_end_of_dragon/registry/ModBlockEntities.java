package com.licht_meilleur.the_end_of_dragon.registry;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.world.block.entity
        .EndermanVillageGatewayBlockEntity;
import com.licht_meilleur.the_end_of_dragon.world.block.entity.RechorusPlantCoreBlockEntity;
import com.licht_meilleur.the_end_of_dragon.world.block.entity
        .WaterTransferMachineABlockEntity;
import com.licht_meilleur.the_end_of_dragon.world.block.entity
        .WaterTransferMachineBBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {

    public static final ResourceKey<BlockEntityType<?>>
            ENDERMAN_VILLAGE_GATEWAY_KEY =
            ResourceKey.create(
                    Registries.BLOCK_ENTITY_TYPE,
                    TheEndOfDragon.id(
                            "enderman_village_gateway"
                    )
            );

    public static final ResourceKey<BlockEntityType<?>>
            WATER_TRANSFER_MACHINE_A_KEY =
            ResourceKey.create(
                    Registries.BLOCK_ENTITY_TYPE,
                    TheEndOfDragon.id(
                            "water_transfer_machine_a"
                    )
            );

    public static final ResourceKey<BlockEntityType<?>>
            WATER_TRANSFER_MACHINE_B_KEY =
            ResourceKey.create(
                    Registries.BLOCK_ENTITY_TYPE,
                    TheEndOfDragon.id(
                            "water_transfer_machine_b"
                    )
            );
    public static final ResourceKey<BlockEntityType<?>>
            RECHORUS_PLANT_CORE_KEY =
            ResourceKey.create(
                    Registries.BLOCK_ENTITY_TYPE,
                    TheEndOfDragon.id(
                            "rechorus_plant_core"
                    )
            );



    public static BlockEntityType<
            EndermanVillageGatewayBlockEntity>
            ENDERMAN_VILLAGE_GATEWAY;

    public static BlockEntityType<
            WaterTransferMachineABlockEntity>
            WATER_TRANSFER_MACHINE_A;

    public static BlockEntityType<
            WaterTransferMachineBBlockEntity>
            WATER_TRANSFER_MACHINE_B;

    public static BlockEntityType<
            RechorusPlantCoreBlockEntity>
            RECHORUS_PLANT_CORE;

    public static void bindFabric(
            BlockEntityType<
                    EndermanVillageGatewayBlockEntity>
                    gateway,
            BlockEntityType<
                    WaterTransferMachineABlockEntity>
                    machineA,
            BlockEntityType<
                    WaterTransferMachineBBlockEntity>
                    machineB,
            BlockEntityType<
                    RechorusPlantCoreBlockEntity>
                    plantCore
    ) {
        ENDERMAN_VILLAGE_GATEWAY =
                gateway;

        WATER_TRANSFER_MACHINE_A =
                machineA;

        WATER_TRANSFER_MACHINE_B =
                machineB;

        RECHORUS_PLANT_CORE =
                plantCore;
    }

    public static void bindNeoForge(
            BlockEntityType<
                    EndermanVillageGatewayBlockEntity>
                    gateway,
            BlockEntityType<
                    WaterTransferMachineABlockEntity>
                    machineA,
            BlockEntityType<
                    WaterTransferMachineBBlockEntity>
                    machineB,

            BlockEntityType<
                    RechorusPlantCoreBlockEntity>
                    plantCore
    ) {
        ENDERMAN_VILLAGE_GATEWAY =
                gateway;

        WATER_TRANSFER_MACHINE_A =
                machineA;

        WATER_TRANSFER_MACHINE_B =
                machineB;

        RECHORUS_PLANT_CORE =
                plantCore;
    }

    private ModBlockEntities() {
    }
}