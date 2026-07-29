package com.licht_meilleur.the_end_of_dragon.neoforge.registry;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.item.*;
import com.licht_meilleur.the_end_of_dragon.registry.ModDataComponents;
import com.licht_meilleur.the_end_of_dragon.registry.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
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

    public static final DeferredHolder<Item, Item>
            RECHORUS_MELON =
            ITEMS.register(
                    "rechorus_melon",
                    () -> new BlockItem(
                            NeoForgeModBlocks.RECHORUS_MELON.get(),
                            new Item.Properties()
                                    .setId(ModItems.RECHORUS_MELON_KEY)
                                    .useBlockDescriptionPrefix()
                    )
            );

    public static final DeferredHolder<Item,
            RechorusMelonCutItem>

            RECHORUS_MELON_CUT =
            ITEMS.register(
                    "rechorus_melon_cut",
                    () -> new RechorusMelonCutItem(
                            new Item.Properties()
                                    .setId(
                                            ModItems.RECHORUS_MELON_CUT_KEY
                                    )
                                    .food(
                                            new FoodProperties.Builder()
                                                    .nutrition(2)
                                                    .saturationModifier(0.3F)
                                                    .build()
                                    )
                    )
            );

    public static final DeferredHolder<Item, Item>
            RECHORUS_MELON_SEED =
            ITEMS.register(
                    "rechorus_melon_seed",
                    () -> new BlockItem(
                            NeoForgeModBlocks
                                    .RECHORUS_MELON_STEM
                                    .get(),
                            new Item.Properties()
                                    .setId(
                                            ModItems
                                                    .RECHORUS_MELON_SEED_KEY
                                    )
                    )
            );

    public static final DeferredHolder<Item, Item>
            RECHORUS_MELON_SEED_PROTOTYPE =
            ITEMS.register(
                    "rechorus_melon_seed_prototype",
                    () -> new BlockItem(
                            NeoForgeModBlocks
                                    .RECHORUS_MELON_STEM_PROTOTYPE
                                    .get(),
                            new Item.Properties()
                                    .setId(
                                            ModItems
                                                    .RECHORUS_MELON_SEED_PROTOTYPE_KEY
                                    )
                    )
            );

    public static final DeferredHolder<Item, Item>
            RECHORUS_PLANT_CORE =
            ITEMS.register(
                    "rechorus_plant_core",
                    () -> new BlockItem(
                            NeoForgeModBlocks
                                    .RECHORUS_PLANT_CORE
                                    .get(),
                            new Item.Properties()
                                    .setId(
                                            ModItems.RECHORUS_PLANT_CORE_KEY
                                    )
                                    .fireResistant()
                                    .useBlockDescriptionPrefix()
                    )
            );

    public static final DeferredHolder<Item, Item>
            RECHORUS_PLANT_SEED =
            ITEMS.register(
                    "rechorus_plant_seed",
                    () -> new BlockItem(
                            NeoForgeModBlocks
                                    .RECHORUS_PLANT_SEED
                                    .get(),
                            new Item.Properties()
                                    .setId(
                                            ModItems
                                                    .RECHORUS_PLANT_SEED_KEY
                                    )
                                    .useBlockDescriptionPrefix()
                    )
            );

    public static final DeferredHolder<Item, Item>
            WATER_TRANSFER_MACHINE_A =
            ITEMS.register(
                    "water_transfer_machine_a",
                    () -> new BlockItem(
                            NeoForgeModBlocks
                                    .WATER_TRANSFER_MACHINE_A
                                    .get(),
                            new Item.Properties()
                                    .setId(
                                            ModItems.WATER_TRANSFER_MACHINE_A_KEY
                                    )
                                    .stacksTo(1)
                                    .fireResistant()
                                    .useBlockDescriptionPrefix()
                    )
            );

    public static final DeferredHolder<Item, Item>
            WATER_TRANSFER_MACHINE_B =
            ITEMS.register(
                    "water_transfer_machine_b",
                    () -> new BlockItem(
                            NeoForgeModBlocks
                                    .WATER_TRANSFER_MACHINE_B
                                    .get(),
                            new Item.Properties()
                                    .setId(
                                            ModItems.WATER_TRANSFER_MACHINE_B_KEY
                                    )
                                    .stacksTo(1)
                                    .fireResistant()
                                    .useBlockDescriptionPrefix()
                    )
            );

    public static final DeferredHolder<Item, Item>
            RECHORUS_JUICE_BUCKET =
            ITEMS.register(
                    "rechorus_juice_bucket",
                    () -> new BucketItem(
                            NeoForgeModFluids
                                    .RECHORUS_JUICE_SOURCE
                                    .get(),
                            new Item.Properties()
                                    .setId(
                                            ModItems
                                                    .RECHORUS_JUICE_BUCKET_KEY
                                    )
                                    .craftRemainder(
                                            Items.BUCKET
                                    )
                                    .stacksTo(1)
                    )
            );

    public static final DeferredHolder<
            Item,
            RechorusJuiceBottleItem>
            RECHORUS_JUICE_BOTTLE =
            ITEMS.register(
                    "rechorus_juice_bottle",
                    () ->
                            new RechorusJuiceBottleItem(
                                    new Item.Properties()
                                            .setId(
                                                    ModItems
                                                            .RECHORUS_JUICE_BOTTLE_KEY
                                            )
                                            .stacksTo(16)
                            )
            );

    public static final DeferredHolder<
            Item,
            DifferentPhasePearlItem>
            DIFFERENT_PHASE_PEARL =
            ITEMS.register(
                    "different_phase_pearl",
                    () ->
                            new DifferentPhasePearlItem(
                                    new Item.Properties()
                                            .setId(
                                                    ModItems
                                                            .DIFFERENT_PHASE_PEARL_KEY
                                            )
                                            .stacksTo(1)
                                            .fireResistant()
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
                TRUE_ENDER_PEARL.get(),
                RECHORUS_MELON.get(),
                RECHORUS_MELON_CUT.get(),
                RECHORUS_MELON_SEED.get(),
                RECHORUS_MELON_SEED_PROTOTYPE.get(),
                RECHORUS_PLANT_CORE.get(),
                RECHORUS_PLANT_SEED.get(),
                WATER_TRANSFER_MACHINE_A.get(),
                WATER_TRANSFER_MACHINE_B.get(),
                RECHORUS_JUICE_BUCKET.get(),
                RECHORUS_JUICE_BOTTLE.get(),
                DIFFERENT_PHASE_PEARL.get()
        );
    }

    private NeoForgeModItems() {
    }
}