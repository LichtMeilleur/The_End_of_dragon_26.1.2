package com.licht_meilleur.the_end_of_dragon.world.village.trust;

public enum TedVillageTrustStage {

    LEVEL_1(
            1,
            0
    ),

    LEVEL_2(
            2,
            1
    ),

    LEVEL_3(
            3,
            2
    ),

    LEVEL_4(
            4,
            3
    ),

    LEVEL_5(
            5,
            4
    ),

    LEVEL_6(
            6,
            5
    );

    private final int displayLevel;
    private final int requiredInternalLevel;

    TedVillageTrustStage(
            int displayLevel,
            int requiredInternalLevel
    ) {
        this.displayLevel =
                displayLevel;

        this.requiredInternalLevel =
                requiredInternalLevel;
    }

    public int getDisplayLevel() {
        return this.displayLevel;
    }

    public int getRequiredInternalLevel() {
        return this.requiredInternalLevel;
    }

    public boolean isUnlocked(
            int currentInternalLevel
    ) {
        return currentInternalLevel
                >= this.requiredInternalLevel;
    }
}