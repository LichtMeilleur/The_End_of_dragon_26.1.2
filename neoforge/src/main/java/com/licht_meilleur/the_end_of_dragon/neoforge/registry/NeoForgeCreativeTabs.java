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
                            .icon(() -> new ItemStack(ModItems.THE_END_OF_DRAGON_ICON))
                            .displayItems((params, output) -> {

                                output.accept(ModItems.THE_END_PIECE);
                                output.accept(ModItems.THE_END_OF_DRAGON_SPAWN_EGG);

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