package com.licht_meilleur.the_end_of_dragon.network;

import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;
import java.util.function.BiConsumer;

public final class TedDifferentPhaseNetwork {

    private static BiConsumer<
            ServerPlayer,
            TedDifferentPhaseSyncPayload> sender;

    private TedDifferentPhaseNetwork() {
    }

    public static void bindSender(
            BiConsumer<
                    ServerPlayer,
                    TedDifferentPhaseSyncPayload> newSender
    ) {
        sender = Objects.requireNonNull(
                newSender
        );
    }

    public static void sendState(
            ServerPlayer changedPlayer,
            boolean persistent,
            int temporaryTicks
    ) {
        if (sender == null) {
            return;
        }

        TedDifferentPhaseSyncPayload payload =
                new TedDifferentPhaseSyncPayload(
                        changedPlayer.getUUID(),
                        persistent,
                        temporaryTicks
                );

        for (ServerPlayer receiver :
                changedPlayer.level()
                        .getServer()
                        .getPlayerList()
                        .getPlayers()) {

            sender.accept(
                    receiver,
                    payload
            );
        }
    }

    public static void sendStateTo(
            ServerPlayer receiver,
            ServerPlayer changedPlayer,
            boolean persistent,
            int temporaryTicks
    ) {
        if (sender == null) {
            return;
        }

        sender.accept(
                receiver,
                new TedDifferentPhaseSyncPayload(
                        changedPlayer.getUUID(),
                        persistent,
                        temporaryTicks
                )
        );
    }
}