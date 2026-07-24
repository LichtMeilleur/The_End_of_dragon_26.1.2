package com.licht_meilleur.the_end_of_dragon.world.village.quest;

public enum TedVillageQuestType {

    MAIN(
            "main"
    ),

    SIDE(
            "side"
    ),

    DAILY(
            "daily"
    ),

    REPEATABLE(
            "repeatable"
    ),

    EVENT(
            "event"
    );

    private final String folderName;

    TedVillageQuestType(
            String folderName
    ) {
        this.folderName =
                folderName;
    }

    public String getFolderName() {
        return this.folderName;
    }
}