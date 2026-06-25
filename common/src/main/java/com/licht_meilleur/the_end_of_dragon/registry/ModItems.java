package com.licht_meilleur.the_end_of_dragon.registry;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.item.TedDebugBowItem;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.TypedEntityData;

public final class ModItems {
    public static final ResourceKey<Item> THE_END_OF_DRAGON_SPAWN_EGG_KEY =
            ResourceKey.create(
                    Registries.ITEM,
                    Identifier.fromNamespaceAndPath(TheEndOfDragon.MOD_ID, "the_end_of_dragon_spawn_egg")
            );

    public static final Item THE_END_OF_DRAGON_SPAWN_EGG =
            Registry.register(
                    BuiltInRegistries.ITEM,
                    THE_END_OF_DRAGON_SPAWN_EGG_KEY.identifier(),
                    new SpawnEggItem(
                            new Item.Properties()
                                    .setId(THE_END_OF_DRAGON_SPAWN_EGG_KEY)
                                    .component(
                                            DataComponents.ENTITY_DATA,
                                            TypedEntityData.of(ModEntities.THE_END_OF_DRAGON, new CompoundTag())
                                    )
                    )
            );

    public static final ResourceKey<Item> TED_DEBUG_BOW_KEY =
            ResourceKey.create(
                    Registries.ITEM,
                    Identifier.fromNamespaceAndPath(TheEndOfDragon.MOD_ID, "ted_debug_bow")
            );

    public static final Item TED_DEBUG_BOW =
            Registry.register(
                    BuiltInRegistries.ITEM,
                    TED_DEBUG_BOW_KEY.identifier(),
                    new TedDebugBowItem(
                            new Item.Properties()
                                    .setId(TED_DEBUG_BOW_KEY)
                                    .stacksTo(1)
                    )
            );

    public static void init() {
    }

    private ModItems() {
    }
}