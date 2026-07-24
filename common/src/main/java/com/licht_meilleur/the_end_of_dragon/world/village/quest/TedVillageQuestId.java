package com.licht_meilleur.the_end_of_dragon.world.village.quest;

public enum TedVillageQuestId {

    /*
     * メインクエスト
     */
    WATER_TRANSFER_RESEARCH(
            "water_transfer_research",
            QuestCategory.MAIN,
            false
    ),

    RECHORUS_MELON_PROTOTYPE(
            "rechorus_melon_prototype",
            QuestCategory.MAIN,
            false
    ),

    RECHORUS_MELON_SEED_DELIVERY(
            "rechorus_melon_seed_delivery",
            QuestCategory.MAIN,
            false
    ),

    RECHORUS_PLANT_PROTOTYPE(
            "rechorus_plant_prototype",
            QuestCategory.MAIN,
            false
    ),

    RECHORUS_PLANT_CORE_DELIVERY(
            "rechorus_plant_core_delivery",
            QuestCategory.MAIN,
            false
    ),

    RECHORUS_FACILITY_CONSTRUCTION(
            "rechorus_facility_construction",
            QuestCategory.MAIN,
            false
    ),

    /*
     * 将来の反復クエスト例
     */
    FLOWER_DELIVERY(
            "flower_delivery",
            QuestCategory.REPEATABLE,
            true
    ),

    SOIL_DELIVERY(
            "soil_delivery",
            QuestCategory.REPEATABLE,
            true
    );

    private final String serializedName;
    private final QuestCategory category;
    private final boolean repeatable;

    TedVillageQuestId(
            String serializedName,
            QuestCategory category,
            boolean repeatable
    ) {
        this.serializedName =
                serializedName;

        this.category =
                category;

        this.repeatable =
                repeatable;
    }

    public String getSerializedName() {
        return this.serializedName;
    }

    public QuestCategory getCategory() {
        return this.category;
    }

    public boolean isRepeatable() {
        return this.repeatable;
    }

    public static TedVillageQuestId fromSerializedName(
            String name
    ) {
        if (name == null
                || name.isBlank()) {
            return null;
        }

        for (TedVillageQuestId id : values()) {
            if (id.serializedName.equals(name)) {
                return id;
            }
        }

        return null;
    }

    public enum QuestCategory {
        MAIN,
        SIDE,
        REPEATABLE
    }
}