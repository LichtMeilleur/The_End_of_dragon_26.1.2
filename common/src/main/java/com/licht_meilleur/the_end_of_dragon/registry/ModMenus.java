package com.licht_meilleur.the_end_of_dragon.registry;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.world.village.trade.TedVillageTradeMenu;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.MenuType;

public final class ModMenus {

    public static final ResourceKey<MenuType<?>>
            TED_VILLAGE_TRADE_KEY =
            ResourceKey.create(
                    BuiltInRegistries.MENU.key(),
                    TheEndOfDragon.id(
                            "village_trade"
                    )
            );

    /*
     * Fabricでは、このフィールドへ直接登録したMenuTypeを格納する。
     * NeoForgeではcommonSetup中にDeferredHolderの値を渡す。
     */
    public static MenuType<TedVillageTradeMenu>
            TED_VILLAGE_TRADE;

    private ModMenus() {
    }

    public static void registerFabric() {
        TED_VILLAGE_TRADE =
                Registry.register(
                        BuiltInRegistries.MENU,
                        TED_VILLAGE_TRADE_KEY.identifier(),
                        new MenuType<>(
                                (containerId, inventory) ->
                                        new TedVillageTradeMenu(
                                                containerId,
                                                inventory
                                        ),
                                net.minecraft.world.flag
                                        .FeatureFlags.DEFAULT_FLAGS
                        )
                );
    }

    public static void bindNeoForge(
            MenuType<TedVillageTradeMenu> menuType
    ) {
        TED_VILLAGE_TRADE = menuType;
    }
}