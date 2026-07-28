package com.licht_meilleur.the_end_of_dragon.world.crop;

public final class RechorusMutationChances {

    private static final float
            PROTOTYPE_WITH_WATER =
            0.20F;

    private static final float
            PROTOTYPE_WITH_RECHORUS_JUICE =
            0.40F;

    private static final float
            PROTOTYPE_WITHOUT_IRRIGATION =
            0.0F;

    private static final float
            NORMAL_WITH_RECHORUS_JUICE =
            0.05F;

    private RechorusMutationChances() {
    }

    public static float getChance(
            RechorusSeedType seedType,
            RechorusIrrigationType irrigationType
    ) {
        return switch (seedType) {
            case NORMAL ->
                    irrigationType
                            == RechorusIrrigationType.RECHORUS_JUICE
                            ? NORMAL_WITH_RECHORUS_JUICE
                            : 0.0F;

            case PROTOTYPE ->
                    switch (irrigationType) {
                        case RECHORUS_JUICE ->
                                PROTOTYPE_WITH_RECHORUS_JUICE;

                        case WATER ->
                                PROTOTYPE_WITH_WATER;

                        case NONE ->
                                PROTOTYPE_WITHOUT_IRRIGATION;
                    };

            case STABLE_MUTANT ->
                    1.0F;
        };
    }
}