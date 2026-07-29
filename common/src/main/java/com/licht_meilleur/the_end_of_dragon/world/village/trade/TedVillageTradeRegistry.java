package com.licht_meilleur.the_end_of_dragon.world.village.trade;

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

    /*
     * 正式な商品内容が決まったら、
     * ここからregister(...)を呼ぶ。
     */
    public static void bootstrap() {
    }
}