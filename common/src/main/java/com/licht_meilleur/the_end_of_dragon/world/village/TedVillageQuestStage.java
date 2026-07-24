package com.licht_meilleur.the_end_of_dragon.world.village;

public enum TedVillageQuestStage {

    NOT_STARTED(0),

    WATER_TRANSFER_RESEARCH(10),

    RECHORUS_MELON_PROTOTYPE(20),

    RECHORUS_MELON_SEED_DELIVERY(30),

    RECHORUS_PLANT_PROTOTYPE(40),

    RECHORUS_PLANT_CORE_DELIVERY(50),

    FACILITY_CONSTRUCTION_AVAILABLE(60),

    MACHINE_B_INSTALLED(70),

    PLANT_CORE_INSTALLED(80),

    RECHORUS_PLANT_BUILT(90),

    MACHINE_A_CONNECTED(100),

    JUICE_WATER_PRODUCED(110),

    COMPLETED(120);

    private final int id;

    TedVillageQuestStage(
            int id
    ) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public boolean isAtLeast(
            TedVillageQuestStage other
    ) {
        return this.id >= other.id;
    }

    public static TedVillageQuestStage fromId(
            int id
    ) {
        TedVillageQuestStage result =
                NOT_STARTED;

        for (TedVillageQuestStage stage : values()) {
            if (stage.id == id) {
                return stage;
            }

            if (stage.id <= id
                    && stage.id > result.id) {
                result = stage;
            }
        }

        return result;
    }
}