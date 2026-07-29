package com.licht_meilleur.the_end_of_dragon.registry;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.item.*;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TypedEntityData;

public final class ModItems {

    public static final ResourceKey<Item> THE_END_OF_DRAGON_ICON_KEY =
            ResourceKey.create(
                    Registries.ITEM,
                    TheEndOfDragon.id("the_end_of_dragon_icon")
            );

    public static final ResourceKey<Item> THE_END_OF_DRAGON_SPAWN_EGG_KEY =
            ResourceKey.create(
                    Registries.ITEM,
                    TheEndOfDragon.id("the_end_of_dragon_spawn_egg")
            );

    public static final ResourceKey<Item> THE_END_PIECE_KEY =
            ResourceKey.create(
                    Registries.ITEM,
                    TheEndOfDragon.id("the_end_piece")
            );

    public static final ResourceKey<Item> TED_DEBUG_BOW_KEY =
            ResourceKey.create(
                    Registries.ITEM,
                    TheEndOfDragon.id("ted_debug_bow")
            );

    public static final ResourceKey<Item> ENDERMAN_VILLAGE_GATEWAY_KEY =
            ResourceKey.create(
                    Registries.ITEM,
                    TheEndOfDragon.id("enderman_village_gateway")
            );

    public static final ResourceKey<Item>
            ENDER_PEARL_GUIDE_BOOK_KEY =
            ResourceKey.create(
                    Registries.ITEM,
                    TheEndOfDragon.id("ender_pearl_guide_book")
            );

    public static final ResourceKey<Item>
            ENDER_PEARL_APPLICATION_BOOK_KEY =
            ResourceKey.create(
                    Registries.ITEM,
                    TheEndOfDragon.id(
                            "ender_pearl_application_book"
                    )
            );

    public static final ResourceKey<Item>
            ENDER_PEARL_ADVANCED_BOOK_KEY =
            ResourceKey.create(
                    Registries.ITEM,
                    TheEndOfDragon.id(
                            "ender_pearl_advanced_book"
                    )
            );

    public static final ResourceKey<Item>
            ENDER_PEARL_MASTERY_BOOK_KEY =
            ResourceKey.create(
                    Registries.ITEM,
                    TheEndOfDragon.id(
                            "ender_pearl_mastery_book"
                    )
            );

    public static final ResourceKey<Item>
            ENDER_PEARL_SECRET_BOOK_KEY =
            ResourceKey.create(
                    Registries.ITEM,
                    TheEndOfDragon.id(
                            "ender_pearl_secret_book"
                    )
            );

    public static final ResourceKey<Item> TRUE_ENDER_PEARL_KEY =
            ResourceKey.create(
                    Registries.ITEM,
                    TheEndOfDragon.id("true_ender_pearl")
            );

    public static final ResourceKey<Item> RECHORUS_MELON_KEY =
            ResourceKey.create(
                    Registries.ITEM,
                    TheEndOfDragon.id("rechorus_melon")
            );

    public static final ResourceKey<Item> RECHORUS_MELON_CUT_KEY =
            ResourceKey.create(
                    Registries.ITEM,
                    TheEndOfDragon.id("rechorus_melon_cut")
            );

    public static final ResourceKey<Item>
            RECHORUS_MELON_SEED_KEY =
            ResourceKey.create(
                    Registries.ITEM,
                    TheEndOfDragon.id("rechorus_melon_seed")
            );

    public static final ResourceKey<Item>
            RECHORUS_MELON_SEED_PROTOTYPE_KEY =
            ResourceKey.create(
                    Registries.ITEM,
                    TheEndOfDragon.id(
                            "rechorus_melon_seed_prototype"
                    )
            );

    public static final ResourceKey<Item>
            RECHORUS_PLANT_CORE_KEY =
            ResourceKey.create(
                    Registries.ITEM,
                    TheEndOfDragon.id("rechorus_plant_core")
            );

    public static final ResourceKey<Item>
            RECHORUS_PLANT_SEED_KEY =
            ResourceKey.create(
                    Registries.ITEM,
                    TheEndOfDragon.id("rechorus_plant_seed")
            );

    public static final ResourceKey<Item>
            WATER_TRANSFER_MACHINE_A_KEY =
            ResourceKey.create(
                    Registries.ITEM,
                    TheEndOfDragon.id("water_transfer_machine_a")
            );

    public static final ResourceKey<Item>
            WATER_TRANSFER_MACHINE_B_KEY =
            ResourceKey.create(
                    Registries.ITEM,
                    TheEndOfDragon.id("water_transfer_machine_b")
            );

    public static final ResourceKey<Item>
            RECHORUS_JUICE_BUCKET_KEY =
            ResourceKey.create(
                    Registries.ITEM,
                    TheEndOfDragon.id(
                            "rechorus_juice_bucket"
                    )
            );

    public static final ResourceKey<Item>
            RECHORUS_JUICE_BOTTLE_KEY =
            ResourceKey.create(
                    Registries.ITEM,
                    TheEndOfDragon.id(
                            "rechorus_juice_bottle"
                    )
            );

    public static final ResourceKey<Item>
            DIFFERENT_PHASE_PEARL_KEY =
            ResourceKey.create(
                    Registries.ITEM,
                    TheEndOfDragon.id(
                            "different_phase_pearl"
                    )
            );

    public static Item THE_END_OF_DRAGON_ICON;
    public static Item THE_END_OF_DRAGON_SPAWN_EGG;
    public static Item THE_END_PIECE;
    public static Item TED_DEBUG_BOW;
    public static Item ENDERMAN_VILLAGE_GATEWAY;

    public static Item ENDER_PEARL_GUIDE_BOOK;
    public static Item ENDER_PEARL_APPLICATION_BOOK;
    public static Item ENDER_PEARL_ADVANCED_BOOK;
    public static Item ENDER_PEARL_MASTERY_BOOK;
    public static Item ENDER_PEARL_SECRET_BOOK;

    public static Item TRUE_ENDER_PEARL;

    public static Item RECHORUS_MELON;
    public static Item RECHORUS_MELON_CUT;
    public static Item RECHORUS_MELON_SEED;
    public static Item RECHORUS_MELON_SEED_PROTOTYPE;

    public static Item RECHORUS_PLANT_CORE;
    public static Item RECHORUS_PLANT_SEED;

    public static Item WATER_TRANSFER_MACHINE_A;
    public static Item WATER_TRANSFER_MACHINE_B;

    public static Item RECHORUS_JUICE_BUCKET;
    public static Item RECHORUS_JUICE_BOTTLE;

    public static Item DIFFERENT_PHASE_PEARL;

    private static boolean fabricRegistered = false;

    public static void registerFabric() {
        if (fabricRegistered) {
            return;
        }

        fabricRegistered = true;

        if (ModEntities.THE_END_OF_DRAGON == null) {
            throw new IllegalStateException(
                    "ModEntities must be registered before ModItems"
            );
        }
        THE_END_OF_DRAGON_ICON =
                Registry.register(
                        BuiltInRegistries.ITEM,
                        THE_END_OF_DRAGON_ICON_KEY.identifier(),
                        new Item(
                                new Item.Properties()
                                        .setId(THE_END_OF_DRAGON_ICON_KEY)
                        )
                );

        THE_END_OF_DRAGON_SPAWN_EGG =
                Registry.register(
                        BuiltInRegistries.ITEM,
                        THE_END_OF_DRAGON_SPAWN_EGG_KEY.identifier(),
                        new SpawnEggItem(
                                new Item.Properties()
                                        .setId(
                                                THE_END_OF_DRAGON_SPAWN_EGG_KEY
                                        )
                                        .component(
                                                DataComponents.ENTITY_DATA,
                                                TypedEntityData.of(
                                                        ModEntities
                                                                .THE_END_OF_DRAGON,
                                                        new CompoundTag()
                                                )
                                        )
                        )
                );

        THE_END_PIECE =
                Registry.register(
                        BuiltInRegistries.ITEM,
                        THE_END_PIECE_KEY.identifier(),
                        new Item(
                                new Item.Properties()
                                        .setId(THE_END_PIECE_KEY)
                        )
                );

        TED_DEBUG_BOW =
                Registry.register(
                        BuiltInRegistries.ITEM,
                        TED_DEBUG_BOW_KEY.identifier(),
                        new TedDebugBowItem(
                                new Item.Properties()
                                        .setId(TED_DEBUG_BOW_KEY)
                                        .stacksTo(1)
                        )
                );
        if (ModBlocks.ENDERMAN_VILLAGE_GATEWAY
                == null) {
            throw new IllegalStateException(
                    "ModBlocks must be registered before ModItems"
            );
        }

        ENDERMAN_VILLAGE_GATEWAY =
                Registry.register(
                        BuiltInRegistries.ITEM,
                        ENDERMAN_VILLAGE_GATEWAY_KEY
                                .identifier(),
                        new BlockItem(
                                ModBlocks
                                        .ENDERMAN_VILLAGE_GATEWAY,
                                new Item.Properties()
                                        .setId(
                                                ENDERMAN_VILLAGE_GATEWAY_KEY
                                        )
                                        .stacksTo(1)
                                        .fireResistant()
                                        .useBlockDescriptionPrefix()
                        )
                );

        ENDER_PEARL_GUIDE_BOOK =
                Registry.register(
                        BuiltInRegistries.ITEM,
                        ENDER_PEARL_GUIDE_BOOK_KEY.identifier(),
                        new Item(
                                new Item.Properties()
                                        .setId(
                                                ENDER_PEARL_GUIDE_BOOK_KEY
                                        )
                                        .stacksTo(1)
                                        .fireResistant()
                        )
                );

        ENDER_PEARL_APPLICATION_BOOK =
                Registry.register(
                        BuiltInRegistries.ITEM,
                        ENDER_PEARL_APPLICATION_BOOK_KEY
                                .identifier(),
                        new Item(
                                new Item.Properties()
                                        .setId(
                                                ENDER_PEARL_APPLICATION_BOOK_KEY
                                        )
                                        .stacksTo(1)
                                        .fireResistant()
                        )
                );

        ENDER_PEARL_ADVANCED_BOOK =
                Registry.register(
                        BuiltInRegistries.ITEM,
                        ENDER_PEARL_ADVANCED_BOOK_KEY
                                .identifier(),
                        new Item(
                                new Item.Properties()
                                        .setId(
                                                ENDER_PEARL_ADVANCED_BOOK_KEY
                                        )
                                        .stacksTo(1)
                                        .fireResistant()
                        )
                );

        ENDER_PEARL_MASTERY_BOOK =
                Registry.register(
                        BuiltInRegistries.ITEM,
                        ENDER_PEARL_MASTERY_BOOK_KEY
                                .identifier(),
                        new Item(
                                new Item.Properties()
                                        .setId(
                                                ENDER_PEARL_MASTERY_BOOK_KEY
                                        )
                                        .stacksTo(1)
                                        .fireResistant()
                        )
                );

        ENDER_PEARL_SECRET_BOOK =
                Registry.register(
                        BuiltInRegistries.ITEM,
                        ENDER_PEARL_SECRET_BOOK_KEY
                                .identifier(),
                        new Item(
                                new Item.Properties()
                                        .setId(
                                                ENDER_PEARL_SECRET_BOOK_KEY
                                        )
                                        .stacksTo(1)
                                        .fireResistant()
                        )
                );

        TRUE_ENDER_PEARL =
                Registry.register(
                        BuiltInRegistries.ITEM,
                        TRUE_ENDER_PEARL_KEY
                                .identifier(),
                        new TrueEnderPearlItem(
                                new Item.Properties()
                                        .setId(
                                                TRUE_ENDER_PEARL_KEY
                                        )
                                        .stacksTo(1)
                                        .fireResistant()
                                        .component(
                                                ModDataComponents
                                                        .TRUE_ENDER_PEARL_LEVEL,
                                                1
                                        )
                                        .component(
                                                DataComponents.ITEM_MODEL,
                                                TheEndOfDragon.id(
                                                        "true_ender_pearl_1"
                                                )
                                        )
                        )
                );

        RECHORUS_MELON =
                Registry.register(
                        BuiltInRegistries.ITEM,
                        RECHORUS_MELON_KEY.identifier(),
                        new BlockItem(
                                ModBlocks.RECHORUS_MELON,
                                new Item.Properties()
                                        .setId(RECHORUS_MELON_KEY)
                                        .useBlockDescriptionPrefix()
                        )
                );

        RECHORUS_MELON_CUT =
                Registry.register(
                        BuiltInRegistries.ITEM,
                        RECHORUS_MELON_CUT_KEY.identifier(),
                        new RechorusMelonCutItem(
                                new Item.Properties()
                                        .setId(
                                                RECHORUS_MELON_CUT_KEY
                                        )
                                        .food(
                                                new FoodProperties.Builder()
                                                        .nutrition(2)
                                                        .saturationModifier(
                                                                0.3F
                                                        )
                                                        .build()
                                        )
                        )
                );

        RECHORUS_MELON_SEED =
                Registry.register(
                        BuiltInRegistries.ITEM,
                        RECHORUS_MELON_SEED_KEY
                                .identifier(),
                        new BlockItem(
                                ModBlocks.RECHORUS_MELON_STEM,
                                new Item.Properties()
                                        .setId(
                                                RECHORUS_MELON_SEED_KEY
                                        )
                                        .useItemDescriptionPrefix()
                        )
                );

        RECHORUS_MELON_SEED_PROTOTYPE =
                Registry.register(
                        BuiltInRegistries.ITEM,
                        RECHORUS_MELON_SEED_PROTOTYPE_KEY
                                .identifier(),
                        new BlockItem(
                                ModBlocks
                                        .RECHORUS_MELON_STEM_PROTOTYPE,
                                new Item.Properties()
                                        .setId(
                                                RECHORUS_MELON_SEED_PROTOTYPE_KEY
                                        )
                                        .useItemDescriptionPrefix()
                        )
                );

        RECHORUS_PLANT_CORE =
                Registry.register(
                        BuiltInRegistries.ITEM,
                        RECHORUS_PLANT_CORE_KEY.identifier(),
                        new BlockItem(
                                ModBlocks.RECHORUS_PLANT_CORE,
                                new Item.Properties()
                                        .setId(RECHORUS_PLANT_CORE_KEY)
                                        .fireResistant()
                                        .useBlockDescriptionPrefix()
                        )
                );

        RECHORUS_PLANT_SEED =
                Registry.register(
                        BuiltInRegistries.ITEM,
                        RECHORUS_PLANT_SEED_KEY.identifier(),
                        new BlockItem(
                                ModBlocks.RECHORUS_PLANT_SEED,
                                new Item.Properties()
                                        .setId(
                                                RECHORUS_PLANT_SEED_KEY
                                        )
                                        .useBlockDescriptionPrefix()
                        )
                );

        WATER_TRANSFER_MACHINE_A =
                Registry.register(
                        BuiltInRegistries.ITEM,
                        WATER_TRANSFER_MACHINE_A_KEY.identifier(),
                        new BlockItem(
                                ModBlocks.WATER_TRANSFER_MACHINE_A,
                                new Item.Properties()
                                        .setId(WATER_TRANSFER_MACHINE_A_KEY)
                                        .stacksTo(1)
                                        .fireResistant()
                                        .useBlockDescriptionPrefix()
                        )
                );

        WATER_TRANSFER_MACHINE_B =
                Registry.register(
                        BuiltInRegistries.ITEM,
                        WATER_TRANSFER_MACHINE_B_KEY.identifier(),
                        new BlockItem(
                                ModBlocks.WATER_TRANSFER_MACHINE_B,
                                new Item.Properties()
                                        .setId(WATER_TRANSFER_MACHINE_B_KEY)
                                        .stacksTo(1)
                                        .fireResistant()
                                        .useBlockDescriptionPrefix()
                        )
                );

        RECHORUS_JUICE_BUCKET =
                Registry.register(
                        BuiltInRegistries.ITEM,
                        RECHORUS_JUICE_BUCKET_KEY.identifier(),
                        new BucketItem(
                                ModFluids.RECHORUS_JUICE_SOURCE,
                                new Item.Properties()
                                        .setId(
                                                RECHORUS_JUICE_BUCKET_KEY
                                        )
                                        .craftRemainder(
                                                Items.BUCKET
                                        )
                                        .stacksTo(1)
                        )
                );

        RECHORUS_JUICE_BOTTLE =
                Registry.register(
                        BuiltInRegistries.ITEM,
                        RECHORUS_JUICE_BOTTLE_KEY.identifier(),
                        new RechorusJuiceBottleItem(
                                new Item.Properties()
                                        .setId(
                                                RECHORUS_JUICE_BOTTLE_KEY
                                        )
                                        .stacksTo(16)
                        )
                );

        DIFFERENT_PHASE_PEARL =
                Registry.register(
                        BuiltInRegistries.ITEM,
                        DIFFERENT_PHASE_PEARL_KEY.identifier(),
                        new DifferentPhasePearlItem(
                                new Item.Properties()
                                        .setId(
                                                DIFFERENT_PHASE_PEARL_KEY
                                        )
                                        .stacksTo(1)
                                        .fireResistant()
                        )
                );

        TheEndOfDragon.LOGGER.info(
                "Registered The End Of Dragon items for Fabric"
        );
    }

    public static void bindNeoForge(
            Item icon,
            Item spawnEgg,
            Item endPiece,
            Item debugBow,
            Item endermanVillageGateway,
            Item enderPearlGuideBook,
            Item enderPearlApplicationBook,
            Item enderPearlAdvancedBook,
            Item enderPearlMasteryBook,
            Item enderPearlSecretBook,
            Item trueEnderPearl,
            Item rechorusMelon,
            Item rechorusMelonCut,
            Item rechorusMelonSeed,
            Item rechorusMelonSeedPrototype,
            Item rechorusPlantCore,
            Item rechorusPlantSeed,
            Item waterTransferMachineA,
            Item waterTransferMachineB,
            Item rechorusJuiceBucket,
            Item rechorusJuiceBottle,
            Item diffrentPhasePearl
    )  {
        THE_END_OF_DRAGON_ICON =
                icon;

        THE_END_OF_DRAGON_SPAWN_EGG =
                spawnEgg;

        THE_END_PIECE =
                endPiece;

        TED_DEBUG_BOW =
                debugBow;

        ENDERMAN_VILLAGE_GATEWAY =
                endermanVillageGateway;

        ENDER_PEARL_GUIDE_BOOK =
                enderPearlGuideBook;

        ENDER_PEARL_APPLICATION_BOOK =
                enderPearlApplicationBook;

        ENDER_PEARL_ADVANCED_BOOK =
                enderPearlAdvancedBook;

        ENDER_PEARL_MASTERY_BOOK =
                enderPearlMasteryBook;

        ENDER_PEARL_SECRET_BOOK =
                enderPearlSecretBook;

        TRUE_ENDER_PEARL =
                trueEnderPearl;

        RECHORUS_MELON =
                rechorusMelon;

        RECHORUS_MELON_CUT =
                rechorusMelonCut;

        RECHORUS_MELON_SEED =
                rechorusMelonSeed;

        RECHORUS_MELON_SEED_PROTOTYPE =
                rechorusMelonSeedPrototype;

        RECHORUS_PLANT_CORE =
                rechorusPlantCore;

        RECHORUS_PLANT_SEED =
                rechorusPlantSeed;

        WATER_TRANSFER_MACHINE_A =
                waterTransferMachineA;

        WATER_TRANSFER_MACHINE_B =
                waterTransferMachineB;

        RECHORUS_JUICE_BUCKET =
                rechorusJuiceBucket;

        RECHORUS_JUICE_BOTTLE =
                rechorusJuiceBottle;

        DIFFERENT_PHASE_PEARL =
                diffrentPhasePearl;

        TheEndOfDragon.LOGGER.info(
                "Bound NeoForge items to common registry references"
        );
    }

    private ModItems() {
    }
}