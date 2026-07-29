package com.licht_meilleur.the_end_of_dragon.neoforge.registry;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.registry.ModBlocks;
import com.licht_meilleur.the_end_of_dragon.registry.ModItems;
import com.licht_meilleur.the_end_of_dragon.world.block.*;
import com.licht_meilleur.the_end_of_dragon.world.crop.RechorusMelonStemBlock;
import com.licht_meilleur.the_end_of_dragon.world.crop.RechorusPlantSeedBlock;
import com.licht_meilleur.the_end_of_dragon.world.crop.RechorusSeedType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
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

    public static final DeferredHolder<Block, Block>
            RECHORUS_MELON =
            BLOCKS.register(
                    "rechorus_melon",
                    () -> new Block(
                            BlockBehaviour.Properties
                                    .of()
                                    .setId(
                                            ModBlocks.RECHORUS_MELON_KEY
                                    )
                                    .strength(1.0F)
                                    .sound(SoundType.WOOD)
                    )
            );

    public static final DeferredHolder<Block, RechorusPlantCoreBlock>
            RECHORUS_PLANT_CORE =
            BLOCKS.register(
                    "rechorus_plant_core",
                    () -> new RechorusPlantCoreBlock(
                            BlockBehaviour.Properties
                                    .of()
                                    .setId(
                                            ModBlocks.RECHORUS_PLANT_CORE_KEY
                                    )
                                    .strength(2.0F, 6.0F)
                                    .sound(SoundType.WOOD)
                                    .lightLevel(state -> 5)
                                    .noOcclusion()
                    )
            );

    public static final DeferredHolder<
            Block,
            WaterTransferMachineABlock>
            WATER_TRANSFER_MACHINE_A =
            BLOCKS.register(
                    "water_transfer_machine_a",
                    () -> new WaterTransferMachineABlock(
                            BlockBehaviour.Properties
                                    .of()
                                    .setId(
                                            ModBlocks
                                                    .WATER_TRANSFER_MACHINE_A_KEY
                                    )
                                    .strength(
                                            3.0F,
                                            12.0F
                                    )
                                    .sound(
                                            SoundType.METAL
                                    )
                                    .lightLevel(
                                            state -> 4
                                    )
                                    .noOcclusion()
                    )
            );

    public static final DeferredHolder<
            Block,
            WaterTransferMachineBBlock>
            WATER_TRANSFER_MACHINE_B =
            BLOCKS.register(
                    "water_transfer_machine_b",
                    () -> new WaterTransferMachineBBlock(
                            BlockBehaviour.Properties
                                    .of()
                                    .setId(
                                            ModBlocks
                                                    .WATER_TRANSFER_MACHINE_B_KEY
                                    )
                                    .strength(
                                            3.0F,
                                            12.0F
                                    )
                                    .sound(
                                            SoundType.METAL
                                    )
                                    .lightLevel(
                                            state -> 4
                                    )
                                    .noOcclusion()
                    )
            );

    public static final DeferredHolder<Block, Block>
            RECHORUS_PLANT_CORE_PREVIEW =
            BLOCKS.register(
                    "rechorus_plant_core_preview",
                    () -> new Block(
                            BlockBehaviour.Properties
                                    .of()
                                    .setId(
                                            ModBlocks
                                                    .RECHORUS_PLANT_CORE_PREVIEW_KEY
                                    )
                                    .strength(
                                            -1.0F,
                                            3_600_000.0F
                                    )
                                    .noCollision()
                                    .noOcclusion()
                                    .replaceable()
                                    .lightLevel(
                                            state -> 3
                                    )
                    )
            );

    public static final DeferredHolder<Block, Block>
            WATER_TRANSFER_MACHINE_B_PREVIEW =
            BLOCKS.register(
                    "water_transfer_machine_b_preview",
                    () -> new Block(
                            BlockBehaviour.Properties
                                    .of()
                                    .setId(
                                            ModBlocks
                                                    .WATER_TRANSFER_MACHINE_B_PREVIEW_KEY
                                    )
                                    .strength(
                                            -1.0F,
                                            3_600_000.0F
                                    )
                                    .noCollision()
                                    .noOcclusion()
                                    .replaceable()
                                    .lightLevel(
                                            state -> 3
                                    )
                    )
            );

    public static final DeferredHolder<Block, Block>
            RECHORUS_ROOT =
            BLOCKS.register(
                    "rechorus_root",
                    () -> new Block(
                            BlockBehaviour.Properties
                                    .of()
                                    .setId(
                                            ModBlocks.RECHORUS_ROOT_KEY
                                    )
                                    .strength(
                                            2.0F,
                                            6.0F
                                    )
                                    .sound(
                                            SoundType.WOOD
                                    )
                                    .noOcclusion()
                    )
            );

    public static final DeferredHolder<Block, Block>
            RECHORUS_PLANT =
            BLOCKS.register(
                    "rechorus_plant",
                    () -> new Block(
                            BlockBehaviour.Properties
                                    .of()
                                    .setId(
                                            ModBlocks.RECHORUS_PLANT_KEY
                                    )
                                    .strength(
                                            2.0F,
                                            6.0F
                                    )
                                    .sound(
                                            SoundType.WOOD
                                    )
                                    .noOcclusion()
                    )
            );

    public static final DeferredHolder<
            Block,
            RechorusFlowerBlock> RECHORUS_FLOWER =
            BLOCKS.register(
                    "rechorus_flower",
                    () -> new RechorusFlowerBlock(
                            BlockBehaviour.Properties
                                    .of()
                                    .setId(
                                            ModBlocks.RECHORUS_FLOWER_KEY
                                    )
                                    .strength(
                                            0.5F
                                    )
                                    .sound(
                                            SoundType.AZALEA
                                    )
                                    .noCollision()
                                    .noOcclusion()
                    )
            );

    public static final DeferredHolder<
            Block,
            RechorusMelonStemBlock>
            RECHORUS_MELON_STEM_PROTOTYPE =
            BLOCKS.register(
                    "rechorus_melon_stem_prototype",
                    () ->
                            new RechorusMelonStemBlock(
                                    RechorusSeedType.PROTOTYPE,
                                    () ->
                                            ModItems
                                                    .RECHORUS_MELON_SEED_PROTOTYPE,
                                    () ->
                                            ModBlocks
                                                    .RECHORUS_MELON,
                                    BlockBehaviour.Properties
                                            .ofFullCopy(
                                                    Blocks.MELON_STEM
                                            )
                                            .setId(
                                                    ModBlocks
                                                            .RECHORUS_MELON_STEM_PROTOTYPE_KEY
                                            )
                                            .randomTicks()
                            )
            );

    public static final DeferredHolder<
            Block,
            RechorusMelonStemBlock>
            RECHORUS_MELON_STEM =
            BLOCKS.register(
                    "rechorus_melon_stem",
                    () ->
                            new RechorusMelonStemBlock(
                                    RechorusSeedType.STABLE_MUTANT,
                                    () ->
                                            ModItems
                                                    .RECHORUS_MELON_SEED,
                                    () ->
                                            ModBlocks
                                                    .RECHORUS_MELON,
                                    BlockBehaviour.Properties
                                            .ofFullCopy(
                                                    Blocks.MELON_STEM
                                            )
                                            .setId(
                                                    ModBlocks
                                                            .RECHORUS_MELON_STEM_KEY
                                            )
                                            .randomTicks()
                            )
            );

    public static final DeferredHolder<Block, LiquidBlock>
            RECHORUS_JUICE =
            BLOCKS.register(
                    "rechorus_juice",
                    () -> {
                        FlowingFluid source =
                                NeoForgeModFluids
                                        .RECHORUS_JUICE_SOURCE
                                        .get();

                        TheEndOfDragon.LOGGER.info(
                                "Creating Rechorus Juice block: sourceClass={}, getSource={}",
                                source.getClass().getName(),
                                source.getSource()
                        );

                        return new LiquidBlock(
                                source,
                                BlockBehaviour.Properties
                                        .ofFullCopy(
                                                Blocks.WATER
                                        )
                                        .setId(
                                                ModBlocks
                                                        .RECHORUS_JUICE_KEY
                                        )
                                        .noLootTable()
                                        .lightLevel(
                                                state -> 6
                                        )
                        );
                    }
            );

    public static final DeferredHolder<
            Block,
            RechorusFarmlandBlock>
            RECHORUS_FARMLAND =
            BLOCKS.register(
                    "rechorus_farmland",
                    () -> new RechorusFarmlandBlock(
                            BlockBehaviour.Properties
                                    .ofFullCopy(
                                            Blocks.FARMLAND
                                    )
                                    .setId(
                                            ModBlocks
                                                    .RECHORUS_FARMLAND_KEY
                                    )
                    )
            );

    public static final DeferredHolder<
            Block,
            RechorusPlantSeedBlock>
            RECHORUS_PLANT_SEED =
            BLOCKS.register(
                    "rechorus_plant_seed",
                    () -> new RechorusPlantSeedBlock(
                            TreeGrower.MANGROVE,
                            BlockBehaviour.Properties
                                    .ofFullCopy(
                                            Blocks.MANGROVE_PROPAGULE
                                    )
                                    .setId(
                                            ModBlocks.RECHORUS_PLANT_SEED_KEY
                                    )
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
                ENDERMAN_VILLAGE_RETURN_GATEWAY.get(),
                RECHORUS_MELON.get(),
                RECHORUS_PLANT_CORE.get(),
                WATER_TRANSFER_MACHINE_A.get(),
                WATER_TRANSFER_MACHINE_B.get(),
                RECHORUS_ROOT.get(),
                RECHORUS_PLANT.get(),
                RECHORUS_FLOWER.get(),
                RECHORUS_MELON_STEM_PROTOTYPE.get(),
                RECHORUS_MELON_STEM.get(),
                RECHORUS_PLANT_SEED.get(),
                RECHORUS_PLANT_CORE_PREVIEW.get(),
                WATER_TRANSFER_MACHINE_B_PREVIEW.get(),
                RECHORUS_JUICE.get(),
                RECHORUS_FARMLAND.get()
        );
    }

    private NeoForgeModBlocks() {
    }
}