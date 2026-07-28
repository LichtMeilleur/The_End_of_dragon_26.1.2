package com.licht_meilleur.the_end_of_dragon.client.phase;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public final class TedDifferentPhaseClientState {

    private static final Map<
            UUID,
            PhaseState> PLAYER_STATES =
            new HashMap<>();

    private TedDifferentPhaseClientState() {
    }

    public static void update(
            UUID playerId,
            boolean persistent,
            int temporaryTicks
    ) {
        if (!persistent
                && temporaryTicks <= 0) {

            PLAYER_STATES.remove(
                    playerId
            );

            return;
        }

        PLAYER_STATES.put(
                playerId,
                new PhaseState(
                        persistent,
                        Math.max(
                                0,
                                temporaryTicks
                        )
                )
        );
    }

    public static boolean isInDifferentPhase(
            Entity entity
    ) {
        if (!(entity instanceof Player player)) {
            return false;
        }

        PhaseState state =
                PLAYER_STATES.get(
                        player.getUUID()
                );

        return state != null
                && state.active();
    }

    public static boolean isLocalPlayerInDifferentPhase() {
        Player player =
                Minecraft.getInstance().player;

        return player != null
                && isInDifferentPhase(
                player
        );
    }

    public static boolean canSee(
            Entity viewer,
            Entity target
    ) {
        return isInDifferentPhase(viewer)
                == isInDifferentPhase(target);
    }

    public static void clear() {
        PLAYER_STATES.clear();
    }

    private record PhaseState(
            boolean persistent,
            int temporaryTicks
    ) {
        private boolean active() {
            return this.persistent
                    || this.temporaryTicks > 0;
        }
    }
}