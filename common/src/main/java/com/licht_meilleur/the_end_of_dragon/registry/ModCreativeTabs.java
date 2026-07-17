package com.licht_meilleur.the_end_of_dragon.registry;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class ModCreativeTabs {

    public static final ResourceKey<CreativeModeTab> TAB_KEY =
            ResourceKey.create(
                    Registries.CREATIVE_MODE_TAB,
                    TheEndOfDragon.id("main")
            );

    public static CreativeModeTab MAIN_TAB;

    public static void registerFabric() {
        MAIN_TAB = Registry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                TAB_KEY.identifier(),
                CreativeModeTab.builder(
                                CreativeModeTab.Row.TOP,
                                0
                        )
                        .title(
                                Component.translatable(
                                        "itemGroup.the_end_of_dragon"
                                )
                        )
                        .icon(() ->
                                new ItemStack(
                                        ModItems.THE_END_OF_DRAGON_ICON
                                )
                        )
                        .displayItems((parameters, output) -> {
                            output.accept(
                                    ModItems.THE_END_PIECE
                            );

                            output.accept(
                                    ModItems.THE_END_OF_DRAGON_SPAWN_EGG
                            );
                            output.accept(
                                    ModItems.ENDERMAN_VILLAGE_GATEWAY
                            );
                        })
                        .build()


        );
    }

    public static void bindNeoForge(
            CreativeModeTab tab
    ) {
        MAIN_TAB = tab;
    }

    private ModCreativeTabs() {
    }
}