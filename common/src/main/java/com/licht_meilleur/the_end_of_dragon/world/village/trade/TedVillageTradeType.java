package com.licht_meilleur.the_end_of_dragon.world.village.trade;

public enum TedVillageTradeType {

    NORMAL(
            2
    ),

    WORK_BENCH(
            4
    );

    private final int inputSlotCount;

    TedVillageTradeType(
            int inputSlotCount
    ) {
        this.inputSlotCount =
                inputSlotCount;
    }

    public int getInputSlotCount() {
        return this.inputSlotCount;
    }

    public static TedVillageTradeType fromNetworkId(
            int networkId
    ) {
        TedVillageTradeType[] values =
                values();

        if (networkId < 0
                || networkId >= values.length) {
            return NORMAL;
        }

        return values[networkId];
    }
}