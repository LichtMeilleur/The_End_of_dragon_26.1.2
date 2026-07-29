package com.licht_meilleur.the_end_of_dragon.neoforge.registry;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.world.village.trade.TedVillageTradeMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NeoForgeModMenus {

    private static final DeferredRegister<MenuType<?>>
            MENUS =
            DeferredRegister.create(
                    net.minecraft.core.registries
                            .Registries.MENU,
                    TheEndOfDragon.MOD_ID
            );

    public static final DeferredHolder<
            MenuType<?>,
            MenuType<TedVillageTradeMenu>>
            TED_VILLAGE_TRADE =
            MENUS.register(
                    "village_trade",
                    () ->
                            new MenuType<>(
                                    TedVillageTradeMenu::new,
                                    net.minecraft.world.flag
                                            .FeatureFlags.DEFAULT_FLAGS
                            )
            );

    private NeoForgeModMenus() {
    }

    public static void register(
            IEventBus modBus
    ) {
        MENUS.register(modBus);
    }

    public static void bindCommonReferences() {
        com.licht_meilleur.the_end_of_dragon
                .registry.ModMenus
                .bindNeoForge(
                        TED_VILLAGE_TRADE.get()
                );
    }
}