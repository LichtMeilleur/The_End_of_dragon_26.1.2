package com.licht_meilleur.the_end_of_dragon.registry;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.world.block.entity
        .EndermanVillageGatewayBlockEntity;
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

    public static BlockEntityType<
            EndermanVillageGatewayBlockEntity>
            ENDERMAN_VILLAGE_GATEWAY;

    public static void bindFabric(
            BlockEntityType<
                    EndermanVillageGatewayBlockEntity>
                    gateway
    ) {
        ENDERMAN_VILLAGE_GATEWAY =
                gateway;
    }

    public static void bindNeoForge(
            BlockEntityType<
                    EndermanVillageGatewayBlockEntity>
                    gateway
    ) {
        ENDERMAN_VILLAGE_GATEWAY =
                gateway;
    }

    private ModBlockEntities() {
    }
}