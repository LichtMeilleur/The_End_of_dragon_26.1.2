package com.licht_meilleur.the_end_of_dragon.registry;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.world.block.*;
import com.licht_meilleur.the_end_of_dragon.world.crop.RechorusMelonStemBlock;
import com.licht_meilleur.the_end_of_dragon.world.crop.RechorusPlantSeedBlock;
import com.licht_meilleur.the_end_of_dragon.world.crop.RechorusSeedType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.grower.TreeGrower;
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




    public static final ResourceKey<Block>
            RECHORUS_ROOT_KEY =
            ResourceKey.create(
                    Registries.BLOCK,
                    TheEndOfDragon.id(
                            "rechorus_root"
                    )
            );

    public static final ResourceKey<Block>
            RECHORUS_PLANT_KEY =
            ResourceKey.create(
                    Registries.BLOCK,
                    TheEndOfDragon.id(
                            "rechorus_plant"
                    )
            );

    public static final ResourceKey<Block>
            RECHORUS_FLOWER_KEY =
            ResourceKey.create(
                    Registries.BLOCK,
                    TheEndOfDragon.id(
                            "rechorus_flower"
                    )
            );


    public static final ResourceKey<Block>
            RECHORUS_PLANT_CORE_PREVIEW_KEY =
            ResourceKey.create(
                    Registries.BLOCK,
                    TheEndOfDragon.id(
                            "rechorus_plant_core_preview"
                    )
            );

    public static final ResourceKey<Block>
            WATER_TRANSFER_MACHINE_B_PREVIEW_KEY =
            ResourceKey.create(
                    Registries.BLOCK,
                    TheEndOfDragon.id(
                            "water_transfer_machine_b_preview"
                    )
            );

    public static final ResourceKey<Block>
            RECHORUS_JUICE_KEY =
            ResourceKey.create(
                    Registries.BLOCK,
                    TheEndOfDragon.id(
                            "rechorus_juice"
                    )
            );

    public static final ResourceKey<Block>
            RECHORUS_FARMLAND_KEY =
            ResourceKey.create(
                    Registries.BLOCK,
                    TheEndOfDragon.id(
                            "rechorus_farmland"
                    )
            );

    public static final ResourceKey<Block>
            RECHORUS_MELON_STEM_PROTOTYPE_KEY =
            ResourceKey.create(
                    Registries.BLOCK,
                    TheEndOfDragon.id(
                            "rechorus_melon_stem_prototype"
                    )
            );

    public static final ResourceKey<Block>
            RECHORUS_MELON_STEM_KEY =
            ResourceKey.create(
                    Registries.BLOCK,
                    TheEndOfDragon.id(
                            "rechorus_melon_stem"
                    )
            );

    public static final ResourceKey<Block>
            RECHORUS_PLANT_SEED_KEY =
            ResourceKey.create(
                    Registries.BLOCK,
                    TheEndOfDragon.id(
                            "rechorus_plant_seed"
                    )
            );




    public static Block ENDERMAN_VILLAGE_GATEWAY;
    public static Block ENDERMAN_VILLAGE_RETURN_GATEWAY;
    public static Block RECHORUS_MELON;
    public static Block RECHORUS_PLANT_CORE;
    public static Block WATER_TRANSFER_MACHINE_A;
    public static Block WATER_TRANSFER_MACHINE_B;
    public static Block RECHORUS_ROOT;
    public static Block RECHORUS_PLANT;
    public static Block RECHORUS_FLOWER;

    public static Block RECHORUS_PLANT_CORE_PREVIEW;
    public static Block WATER_TRANSFER_MACHINE_B_PREVIEW;

    public static Block RECHORUS_JUICE;

    public static Block RECHORUS_FARMLAND;

    public static Block RECHORUS_MELON_STEM_PROTOTYPE;

    public static Block RECHORUS_MELON_STEM;


    public static Block RECHORUS_PLANT_SEED;

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



        RECHORUS_PLANT_CORE =
                Registry.register(
                        BuiltInRegistries.BLOCK,
                        RECHORUS_PLANT_CORE_KEY.identifier(),
                        new RechorusPlantCoreBlock(
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
                        new WaterTransferMachineABlock(
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
                        new WaterTransferMachineBBlock(
                                BlockBehaviour.Properties
                                        .of()
                                        .setId(WATER_TRANSFER_MACHINE_B_KEY)
                                        .strength(3.0F, 12.0F)
                                        .sound(SoundType.METAL)
                                        .lightLevel(state -> 4)
                                        .noOcclusion()
                        )
                );

        RECHORUS_PLANT_CORE_PREVIEW =
                Registry.register(
                        BuiltInRegistries.BLOCK,
                        RECHORUS_PLANT_CORE_PREVIEW_KEY
                                .identifier(),
                        new Block(
                                BlockBehaviour.Properties
                                        .of()
                                        .setId(
                                                RECHORUS_PLANT_CORE_PREVIEW_KEY
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

        WATER_TRANSFER_MACHINE_B_PREVIEW =
                Registry.register(
                        BuiltInRegistries.BLOCK,
                        WATER_TRANSFER_MACHINE_B_PREVIEW_KEY
                                .identifier(),
                        new Block(
                                BlockBehaviour.Properties
                                        .of()
                                        .setId(
                                                WATER_TRANSFER_MACHINE_B_PREVIEW_KEY
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

        RECHORUS_ROOT =
                Registry.register(
                        BuiltInRegistries.BLOCK,
                        RECHORUS_ROOT_KEY.identifier(),
                        new Block(
                                BlockBehaviour.Properties
                                        .of()
                                        .setId(
                                                RECHORUS_ROOT_KEY
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

        RECHORUS_PLANT =
                Registry.register(
                        BuiltInRegistries.BLOCK,
                        RECHORUS_PLANT_KEY.identifier(),
                        new Block(
                                BlockBehaviour.Properties
                                        .of()
                                        .setId(
                                                RECHORUS_PLANT_KEY
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

        RECHORUS_FLOWER =
                Registry.register(
                        BuiltInRegistries.BLOCK,
                        RECHORUS_FLOWER_KEY.identifier(),
                        new RechorusFlowerBlock(
                                BlockBehaviour.Properties
                                        .of()
                                        .setId(
                                                RECHORUS_FLOWER_KEY
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



        RECHORUS_JUICE =
                Registry.register(
                        BuiltInRegistries.BLOCK,
                        RECHORUS_JUICE_KEY.identifier(),
                        new LiquidBlock(
                                ModFluids.RECHORUS_JUICE_SOURCE,
                                BlockBehaviour.Properties
                                        .ofFullCopy(
                                                Blocks.WATER
                                        )
                                        .setId(
                                                RECHORUS_JUICE_KEY
                                        )
                                        .noLootTable()
                                        .lightLevel(
                                                state -> 6
                                        )
                        )
                );

        RECHORUS_FARMLAND =
                Registry.register(
                        BuiltInRegistries.BLOCK,
                        RECHORUS_FARMLAND_KEY.identifier(),
                        new RechorusFarmlandBlock(
                                BlockBehaviour.Properties
                                        .ofFullCopy(
                                                Blocks.FARMLAND
                                        )
                                        .setId(
                                                RECHORUS_FARMLAND_KEY
                                        )
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

        RECHORUS_MELON_STEM_PROTOTYPE =
                Registry.register(
                        BuiltInRegistries.BLOCK,
                        RECHORUS_MELON_STEM_PROTOTYPE_KEY
                                .identifier(),
                        new RechorusMelonStemBlock(
                                RechorusSeedType.PROTOTYPE,
                                () -> ModItems
                                        .RECHORUS_MELON_SEED_PROTOTYPE,
                                () -> ModBlocks
                                        .RECHORUS_MELON,
                                BlockBehaviour.Properties
                                        .ofFullCopy(
                                                Blocks.MELON_STEM
                                        )
                                        .setId(
                                                RECHORUS_MELON_STEM_PROTOTYPE_KEY
                                        )
                                        .randomTicks()
                        )
                );

        RECHORUS_MELON_STEM =
                Registry.register(
                        BuiltInRegistries.BLOCK,
                        RECHORUS_MELON_STEM_KEY
                                .identifier(),
                        new RechorusMelonStemBlock(
                                RechorusSeedType.STABLE_MUTANT,
                                () -> ModItems
                                        .RECHORUS_MELON_SEED,
                                () -> ModBlocks
                                        .RECHORUS_MELON,
                                BlockBehaviour.Properties
                                        .ofFullCopy(
                                                Blocks.MELON_STEM
                                        )
                                        .setId(
                                                RECHORUS_MELON_STEM_KEY
                                        )
                                        .randomTicks()
                        )
                );

        RECHORUS_PLANT_SEED =
                Registry.register(
                        BuiltInRegistries.BLOCK,
                        RECHORUS_PLANT_SEED_KEY.identifier(),
                        new RechorusPlantSeedBlock(
                                TreeGrower.MANGROVE,
                                BlockBehaviour.Properties
                                        .ofFullCopy(
                                                Blocks.MANGROVE_PROPAGULE
                                        )
                                        .setId(
                                                RECHORUS_PLANT_SEED_KEY
                                        )
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
            Block waterTransferMachineB,
            Block rechorusRoot,
            Block rechorusPlant,
            Block rechorusFlower,
            Block rechorusMelonStemPrototype,
            Block rechorusMelonStem,
            Block rechorusPlantSeed,
            Block rechorusPlantCorePreview,
            Block waterTransferMachineBPreview,
            Block rechorusJuice,
            Block rechorusFarmland


    ) {
        {
            {
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



                RECHORUS_ROOT =
                        rechorusRoot;

                RECHORUS_PLANT =
                        rechorusPlant;

                RECHORUS_FLOWER =
                        rechorusFlower;

                RECHORUS_MELON_STEM_PROTOTYPE =
                        rechorusMelonStemPrototype;

                RECHORUS_MELON_STEM =
                        rechorusMelonStem;

                RECHORUS_PLANT_SEED =
                        rechorusPlantSeed;

                RECHORUS_PLANT_CORE_PREVIEW =
                        rechorusPlantCorePreview;

                WATER_TRANSFER_MACHINE_B_PREVIEW =
                        waterTransferMachineBPreview;

                RECHORUS_JUICE =
                        rechorusJuice;

                RECHORUS_FARMLAND =
                        rechorusFarmland;




                TheEndOfDragon.LOGGER.info(
                        "Bound NeoForge blocks to common registry references"
                );
            }


        }

    }
}