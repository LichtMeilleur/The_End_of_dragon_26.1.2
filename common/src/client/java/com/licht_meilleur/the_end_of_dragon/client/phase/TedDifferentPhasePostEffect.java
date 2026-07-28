package com.licht_meilleur.the_end_of_dragon.client.phase;

import net.minecraft.resources.Identifier;

public final class TedDifferentPhasePostEffect {

    private static final float FADE_SECONDS =
            0.4F;

    private static final int STAGE_COUNT =
            4;

    private static float currentStrength;

    private static long previousTimeNanos =
            -1L;

    private TedDifferentPhasePostEffect() {
    }

    public static Identifier updateAndGetEffect() {
        long currentTime =
                System.nanoTime();

        if (previousTimeNanos < 0L) {
            previousTimeNanos =
                    currentTime;
        }

        float elapsedSeconds =
                Math.min(
                        0.1F,
                        (currentTime - previousTimeNanos)
                                / 1_000_000_000.0F
                );

        previousTimeNanos =
                currentTime;

        boolean active =
                TedDifferentPhaseClientState
                        .isLocalPlayerInDifferentPhase();

        float targetStrength =
                active ? 1.0F : 0.0F;

        float change =
                elapsedSeconds
                        / FADE_SECONDS;

        if (currentStrength < targetStrength) {
            currentStrength =
                    Math.min(
                            targetStrength,
                            currentStrength + change
                    );
        } else if (currentStrength > targetStrength) {
            currentStrength =
                    Math.max(
                            targetStrength,
                            currentStrength - change
                    );
        }

        int stage =
                Math.min(
                        STAGE_COUNT,
                        Math.round(
                                currentStrength
                                        * STAGE_COUNT
                        )
                );

        if (stage <= 0) {
            return null;
        }

        return Identifier.fromNamespaceAndPath(
                "the_end_of_dragon",
                "different_phase_" + stage
        );
    }

    public static void reset() {
        currentStrength = 0.0F;
        previousTimeNanos = -1L;
    }
}