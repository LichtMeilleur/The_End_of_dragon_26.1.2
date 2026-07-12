package com.licht_meilleur.the_end_of_dragon.neoforge.registry;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.item.TedDebugBowItem;
import com.licht_meilleur.the_end_of_dragon.registry.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
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

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }

    public static void bindCommonReferences() {
        ModItems.bindNeoForge(
                THE_END_OF_DRAGON_ICON.get(),
                THE_END_OF_DRAGON_SPAWN_EGG.get(),
                THE_END_PIECE.get(),
                TED_DEBUG_BOW.get()
        );
    }

    private NeoForgeModItems() {
    }
}