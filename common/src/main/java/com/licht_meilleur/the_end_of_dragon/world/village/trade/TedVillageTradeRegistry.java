package com.licht_meilleur.the_end_of_dragon.world.village.trade;

import com.licht_meilleur.the_end_of_dragon.registry.ModItems;
import com.licht_meilleur.the_end_of_dragon.world.village.trust.TedVillageTrustStage;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class TedVillageTradeRegistry {

    private static final Map<
            String,
            TedVillageTradeDefinition
            > TRADES =
            new LinkedHashMap<>();

    private TedVillageTradeRegistry() {
    }

    public static void register(
            TedVillageTradeDefinition trade
    ) {
        if (trade == null) {
            throw new IllegalArgumentException(
                    "Trade must not be null"
            );
        }

        TedVillageTradeDefinition oldTrade =
                TRADES.putIfAbsent(
                        trade.id(),
                        trade
                );

        if (oldTrade != null) {
            throw new IllegalStateException(
                    "Duplicate village trade id: "
                            + trade.id()
            );
        }
    }

    public static TedVillageTradeDefinition get(
            String tradeId
    ) {
        if (tradeId == null
                || tradeId.isBlank()) {
            return null;
        }

        return TRADES.get(
                tradeId
        );
    }

    public static List<
            TedVillageTradeDefinition
            > getAll() {
        return List.copyOf(
                TRADES.values()
        );
    }

    public static TedVillageTradeDefinition getByIndex(
            int index
    ) {
        if (index < 0
                || index >= TRADES.size()) {
            return null;
        }

        return TRADES.values()
                .stream()
                .skip(index)
                .findFirst()
                .orElse(null);
    }

    public static int indexOf(
            String tradeId
    ) {
        if (tradeId == null
                || tradeId.isBlank()) {
            return -1;
        }

        int index = 0;

        for (String registeredId :
                TRADES.keySet()) {

            if (registeredId.equals(
                    tradeId
            )) {
                return index;
            }

            index++;
        }

        return -1;
    }

    /*
     * 正式な商品内容が決まったら、
     * ここからregister(...)を呼ぶ。
     */

    private static boolean bootstrapped =
            false;

    public static synchronized void bootstrap() {
        if (bootstrapped) {
            return;
        }

        /*
         * 実際の商品内容はCatalog側にまとめる。
         */
        TedVillageTradeCatalog.registerAll();

        bootstrapped =
                true;
    }
}