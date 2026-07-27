package com.licht_meilleur.the_end_of_dragon.neoforge.registry;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.registry.ModCreativeTabs;
import com.licht_meilleur.the_end_of_dragon.registry.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NeoForgeCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(
                    Registries.CREATIVE_MODE_TAB,
                    TheEndOfDragon.MOD_ID
            );

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN =
            TABS.register("main", () ->

                    CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup.the_end_of_dragon"))
                            .icon(() ->
                                    new ItemStack(
                                            NeoForgeModItems
                                                    .THE_END_OF_DRAGON_ICON
                                                    .get()
                                    )
                            )
                            .displayItems((params, output) -> {

                                output.accept(
                                        NeoForgeModItems
                                                .THE_END_PIECE
                                                .get()
                                );

                                output.accept(
                                        NeoForgeModItems
                                                .THE_END_OF_DRAGON_SPAWN_EGG
                                                .get()
                                );

                                output.accept(
                                        NeoForgeModItems
                                                .ENDERMAN_VILLAGE_GATEWAY
                                                .get()
                                );

                                output.accept(
                                        NeoForgeModItems
                                                .ENDER_PEARL_GUIDE_BOOK
                                                .get()
                                );

                                output.accept(
                                        NeoForgeModItems
                                                .ENDER_PEARL_APPLICATION_BOOK
                                                .get()
                                );

                                output.accept(
                                        NeoForgeModItems
                                                .ENDER_PEARL_ADVANCED_BOOK
                                                .get()
                                );

                                output.accept(
                                        NeoForgeModItems
                                                .ENDER_PEARL_MASTERY_BOOK
                                                .get()
                                );

                                output.accept(
                                        NeoForgeModItems
                                                .ENDER_PEARL_SECRET_BOOK
                                                .get()
                                );

                                output.accept(
                                        NeoForgeModItems
                                                .TRUE_ENDER_PEARL
                                                .get()
                                );

                                output.accept(
                                        NeoForgeModItems
                                                .RECHORUS_MELON
                                                .get()
                                );

                                output.accept(
                                        NeoForgeModItems
                                                .RECHORUS_MELON_CUT
                                                .get()
                                );

                                output.accept(
                                        NeoForgeModItems
                                                .RECHORUS_MELON_SEED_PROTOTYPE
                                                .get()
                                );

                                output.accept(
                                        NeoForgeModItems
                                                .RECHORUS_MELON_SEED
                                                .get()
                                );

                                output.accept(
                                        NeoForgeModItems
                                                .RECHORUS_PLANT_CORE
                                                .get()
                                );

                                output.accept(
                                        NeoForgeModItems
                                                .RECHORUS_PLANT_SEED
                                                .get()
                                );

                                output.accept(
                                        NeoForgeModItems
                                                .WATER_TRANSFER_MACHINE_A
                                                .get()
                                );

                                output.accept(
                                        NeoForgeModItems
                                                .WATER_TRANSFER_MACHINE_B
                                                .get()
                                );

                                output.accept(
                                        NeoForgeModItems
                                                .RECHORUS_JUICE_BUCKET
                                                .get()
                                );

                                output.accept(
                                        NeoForgeModItems
                                                .RECHORUS_JUICE_BOTTLE
                                                .get()
                                );
                            })
                            .build()

            );

    public static void register(IEventBus bus) {
        TABS.register(bus);
    }

    public static void bindCommon() {
        ModCreativeTabs.bindNeoForge(MAIN.get());
    }

    private NeoForgeCreativeTabs() {
    }
}