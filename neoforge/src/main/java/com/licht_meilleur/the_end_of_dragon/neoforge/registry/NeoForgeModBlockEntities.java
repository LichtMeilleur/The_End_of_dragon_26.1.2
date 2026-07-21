package com.licht_meilleur.the_end_of_dragon.neoforge.registry;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.registry.ModBlockEntities;
import com.licht_meilleur.the_end_of_dragon.world.block.entity.EndermanVillageGatewayBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Set;

public final class NeoForgeModBlockEntities {

    public static final DeferredRegister<
            BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(
                    Registries.BLOCK_ENTITY_TYPE,
                    TheEndOfDragon.MOD_ID
            );

    public static final DeferredHolder<
            BlockEntityType<?>,
            BlockEntityType<
                    EndermanVillageGatewayBlockEntity>>
            ENDERMAN_VILLAGE_GATEWAY =
            BLOCK_ENTITY_TYPES.register(
                    "enderman_village_gateway",
                    () -> new BlockEntityType<>(
                            EndermanVillageGatewayBlockEntity::new,
                            false,
                            NeoForgeModBlocks
                                    .ENDERMAN_VILLAGE_GATEWAY
                                    .get()
                    )
            );

    public static void register(
            IEventBus modBus
    ) {
        BLOCK_ENTITY_TYPES.register(
                modBus
        );
    }

    public static void bindCommonReferences() {
        ModBlockEntities.bindNeoForge(
                ENDERMAN_VILLAGE_GATEWAY.get()
        );
    }

    private NeoForgeModBlockEntities() {
    }
}