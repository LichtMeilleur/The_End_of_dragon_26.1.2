package com.licht_meilleur.the_end_of_dragon.neoforge.registry;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.item.TedDebugBowItem;
import com.licht_meilleur.the_end_of_dragon.item.TrueEnderPearlItem;
import com.licht_meilleur.the_end_of_dragon.registry.ModDataComponents;
import com.licht_meilleur.the_end_of_dragon.registry.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.TypedEntityData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NeoForgeModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(
                    Registries.ITEM,
                    TheEndOfDragon.MOD_ID
            );

    public static final DeferredHolder<Item, Item> THE_END_OF_DRAGON_ICON =
            ITEMS.register(
                    "the_end_of_dragon_icon",
                    () -> new Item(
                            new Item.Properties()
                                    .setId(ModItems.THE_END_OF_DRAGON_ICON_KEY)
                    )
            );

    public static final DeferredHolder<Item, Item>
            THE_END_OF_DRAGON_SPAWN_EGG =
            ITEMS.register(
                    "the_end_of_dragon_spawn_egg",
                    () -> new SpawnEggItem(
                            new Item.Properties()
                                    .setId(
                                            ModItems
                                                    .THE_END_OF_DRAGON_SPAWN_EGG_KEY
                                    )
                                    .component(
                                            DataComponents.ENTITY_DATA,
                                            TypedEntityData.of(
                                                    NeoForgeModEntities
                                                            .THE_END_OF_DRAGON
                                                            .get(),
                                                    new CompoundTag()
                                            )
                                    )
                    )
            );

    public static final DeferredHolder<Item, Item> THE_END_PIECE =
            ITEMS.register(
                    "the_end_piece",
                    () -> new Item(
                            new Item.Properties()
                                    .setId(ModItems.THE_END_PIECE_KEY)
                    )
            );

    public static final DeferredHolder<Item, Item> TED_DEBUG_BOW =
            ITEMS.register(
                    "ted_debug_bow",
                    () -> new TedDebugBowItem(
                            new Item.Properties()
                                    .setId(ModItems.TED_DEBUG_BOW_KEY)
                                    .stacksTo(1)
                    )
            );

    public static final DeferredHolder<Item, Item>
            ENDERMAN_VILLAGE_GATEWAY =
            ITEMS.register(
                    "enderman_village_gateway",
                    () -> new BlockItem(
                            NeoForgeModBlocks
                                    .ENDERMAN_VILLAGE_GATEWAY
                                    .get(),
                            new Item.Properties()
                                    .setId(
                                            ModItems
                                                    .ENDERMAN_VILLAGE_GATEWAY_KEY
                                    )
                                    .stacksTo(1)
                                    .fireResistant()
                                    .useBlockDescriptionPrefix()
                    )
            );

    public static final DeferredHolder<Item, Item>
            ENDER_PEARL_GUIDE_BOOK =
            ITEMS.register(
                    "ender_pearl_guide_book",
                    () -> new Item(
                            new Item.Properties()
                                    .setId(
                                            ModItems.ENDER_PEARL_GUIDE_BOOK_KEY
                                    )
                                    .stacksTo(1)
                                    .fireResistant()
                    )
            );

    public static final DeferredHolder<Item, Item>
            ENDER_PEARL_APPLICATION_BOOK =
            ITEMS.register(
                    "ender_pearl_application_book",
                    () -> new Item(
                            new Item.Properties()
                                    .setId(
                                            ModItems
                                                    .ENDER_PEARL_APPLICATION_BOOK_KEY
                                    )
                                    .stacksTo(1)
                                    .fireResistant()
                    )
            );

    public static final DeferredHolder<Item, Item>
            ENDER_PEARL_ADVANCED_BOOK =
            ITEMS.register(
                    "ender_pearl_advanced_book",
                    () -> new Item(
                            new Item.Properties()
                                    .setId(
                                            ModItems
                                                    .ENDER_PEARL_ADVANCED_BOOK_KEY
                                    )
                                    .stacksTo(1)
                                    .fireResistant()
                    )
            );

    public static final DeferredHolder<Item, Item>
            ENDER_PEARL_MASTERY_BOOK =
            ITEMS.register(
                    "ender_pearl_mastery_book",
                    () -> new Item(
                            new Item.Properties()
                                    .setId(
                                            ModItems
                                                    .ENDER_PEARL_MASTERY_BOOK_KEY
                                    )
                                    .stacksTo(1)
                                    .fireResistant()
                    )
            );

    public static final DeferredHolder<Item, Item>
            ENDER_PEARL_SECRET_BOOK =
            ITEMS.register(
                    "ender_pearl_secret_book",
                    () -> new Item(
                            new Item.Properties()
                                    .setId(
                                            ModItems
                                                    .ENDER_PEARL_SECRET_BOOK_KEY
                                    )
                                    .stacksTo(1)
                                    .fireResistant()
                    )
            );

    public static final DeferredHolder<
            Item,
            TrueEnderPearlItem>
            TRUE_ENDER_PEARL =
            ITEMS.register(
                    "true_ender_pearl",
                    () ->
                            new TrueEnderPearlItem(
                                    new Item.Properties()
                                            .setId(
                                                    ModItems
                                                            .TRUE_ENDER_PEARL_KEY
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

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }

    public static void bindCommonReferences() {
        ModItems.bindNeoForge(
                THE_END_OF_DRAGON_ICON.get(),
                THE_END_OF_DRAGON_SPAWN_EGG.get(),
                THE_END_PIECE.get(),
                TED_DEBUG_BOW.get(),
                ENDERMAN_VILLAGE_GATEWAY.get(),
                ENDER_PEARL_GUIDE_BOOK.get(),
                ENDER_PEARL_APPLICATION_BOOK.get(),
                ENDER_PEARL_ADVANCED_BOOK.get(),
                ENDER_PEARL_MASTERY_BOOK.get(),
                ENDER_PEARL_SECRET_BOOK.get(),
                TRUE_ENDER_PEARL.get()
        );
    }

    private NeoForgeModItems() {
    }
}