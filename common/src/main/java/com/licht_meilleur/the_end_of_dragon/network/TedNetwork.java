package com.licht_meilleur.the_end_of_dragon.network;

import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;

public final class TedNetwork {
    private static Sender sender =
            (player, command) -> {
                // ローダー側の初期化前は何もしない
            };

    private TedNetwork() {
    }

    public static void setSender(Sender implementation) {
        sender = Objects.requireNonNull(
                implementation,
                "TED network sender"
        );
    }

    public static void sendBgmStart(
            ServerPlayer player
    ) {
        sender.send(
                player,
                TedBgmCommand.START
        );
    }

    public static void sendBgmStop(
            ServerPlayer player
    ) {
        sender.send(
                player,
                TedBgmCommand.STOP
        );
    }

    @FunctionalInterface
    public interface Sender {
        void send(
                ServerPlayer player,
                TedBgmCommand command
        );
    }
}