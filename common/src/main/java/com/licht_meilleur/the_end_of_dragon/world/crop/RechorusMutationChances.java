package com.licht_meilleur.the_end_of_dragon.world.crop;

public final class RechorusMutationChances {

    private static final float
            PROTOTYPE_WITH_WATER =
            0.20F;

    private static final float
            PROTOTYPE_WITH_JUICE =
            0.40F;

    private static final float
            NORMAL_WITH_JUICE =
            0.05F;

    private RechorusMutationChances() {
    }

    public static float getChance(
            RechorusSeedType seedType,
            RechorusIrrigationType irrigationType
    ) {
        return switch (seedType) {

            case STABLE_MUTANT ->
                    1.0F;

            case PROTOTYPE ->
                    switch (irrigationType) {
                        case WATER ->
                                PROTOTYPE_WITH_WATER;

                        case RECHORUS_JUICE ->
                                PROTOTYPE_WITH_JUICE;

                        case NONE ->
                                0.0F;
                    };

            case NORMAL ->
                    irrigationType
                            == RechorusIrrigationType.RECHORUS_JUICE
                            ? NORMAL_WITH_JUICE
                            : 0.0F;
        };
    }
}