package com.licht_meilleur.the_end_of_dragon.registry;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.item.TedDebugBowItem;
import com.licht_meilleur.the_end_of_dragon.item.TrueEnderPearlItem;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
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
            Item trueEnderPearl
    ) {
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

        TheEndOfDragon.LOGGER.info(
                "Bound NeoForge items to common registry references"
        );
    }

    private ModItems() {
    }
}