package com.licht_meilleur.the_end_of_dragon.registry;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.item.TedDebugBowItem;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
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

    public static final ResourceKey<Item> ENDERPEAL_GUIDE_BOOK_KEY =
            ResourceKey.create(
                    Registries.ITEM,
                    TheEndOfDragon.id("enderpeal_guide_book")
            );

    public static final ResourceKey<Item> TRUE_ENDERPEAL_KEY =
            ResourceKey.create(
                    Registries.ITEM,
                    TheEndOfDragon.id("true_enderpeal")
            );

    public static Item THE_END_OF_DRAGON_ICON;
    public static Item THE_END_OF_DRAGON_SPAWN_EGG;
    public static Item THE_END_PIECE;
    public static Item TED_DEBUG_BOW;
    public static Item ENDERMAN_VILLAGE_GATEWAY;

    public static Item ENDERPEAL_GUIDE_BOOK;
    public static Item TRUE_ENDERPEAL;

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
        ENDERMAN_VILLAGE_GATEWAY =
                Registry.register(
                        BuiltInRegistries.ITEM,
                        ENDERMAN_VILLAGE_GATEWAY_KEY.identifier(),
                        new Item(
                                new Item.Properties()
                                        .setId(
                                                ENDERMAN_VILLAGE_GATEWAY_KEY
                                        )
                                        .stacksTo(1)
                                        .fireResistant()
                        )
                );

        ENDERPEAL_GUIDE_BOOK =
                Registry.register(
                        BuiltInRegistries.ITEM,
                        ENDERPEAL_GUIDE_BOOK_KEY.identifier(),
                        new Item(
                                new Item.Properties()
                                        .setId(
                                                ENDERPEAL_GUIDE_BOOK_KEY
                                        )
                                        .stacksTo(1)
                                        .fireResistant()
                        )
                );

        TRUE_ENDERPEAL =
                Registry.register(
                        BuiltInRegistries.ITEM,
                        TRUE_ENDERPEAL_KEY.identifier(),
                        new Item(
                                new Item.Properties()
                                        .setId(
                                                TRUE_ENDERPEAL_KEY
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
            Item enderpealGUIDEBook,
            Item trueEnderpeal
    ) {
        THE_END_OF_DRAGON_ICON = icon;
        THE_END_OF_DRAGON_SPAWN_EGG = spawnEgg;
        THE_END_PIECE = endPiece;
        TED_DEBUG_BOW = debugBow;
        ENDERMAN_VILLAGE_GATEWAY = endermanVillageGateway;
        ENDERPEAL_GUIDE_BOOK = enderpealGUIDEBook;
        TRUE_ENDERPEAL = trueEnderpeal;

        TheEndOfDragon.LOGGER.info(
                "Bound NeoForge items to common registry references"
        );
    }

    private ModItems() {
    }
}