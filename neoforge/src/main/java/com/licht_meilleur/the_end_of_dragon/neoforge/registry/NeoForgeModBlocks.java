package com.licht_meilleur.the_end_of_dragon.neoforge.registry;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.registry.ModBlocks;
import com.licht_meilleur.the_end_of_dragon.world.block.EndermanVillageGatewayBlock;
import com.licht_meilleur.the_end_of_dragon.world.block.EndermanVillageReturnGatewayBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NeoForgeModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(
                    Registries.BLOCK,
                    TheEndOfDragon.MOD_ID
            );

    public static final DeferredHolder<
            Block,
            EndermanVillageGatewayBlock> ENDERMAN_VILLAGE_GATEWAY =
            BLOCKS.register(
                    "enderman_village_gateway",
                    () -> new EndermanVillageGatewayBlock(
                            BlockBehaviour.Properties
                                    .of()
                                    .setId(
                                            ModBlocks.ENDERMAN_VILLAGE_GATEWAY_KEY
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

    public static final DeferredHolder<
            Block,
            EndermanVillageReturnGatewayBlock>
            ENDERMAN_VILLAGE_RETURN_GATEWAY =
            BLOCKS.register(
                    "enderman_village_return_gateway",
                    () -> new EndermanVillageReturnGatewayBlock(
                            BlockBehaviour.Properties
                                    .of()
                                    .setId(
                                            ModBlocks
                                                    .ENDERMAN_VILLAGE_RETURN_GATEWAY_KEY
                                    )
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

    public static void register(
            IEventBus modBus
    ) {
        BLOCKS.register(modBus);
    }

    public static void bindCommonReferences() {
        ModBlocks.bindNeoForge(
                ENDERMAN_VILLAGE_GATEWAY.get(),
                ENDERMAN_VILLAGE_RETURN_GATEWAY.get()
        );
    }

    private NeoForgeModBlocks() {
    }
}