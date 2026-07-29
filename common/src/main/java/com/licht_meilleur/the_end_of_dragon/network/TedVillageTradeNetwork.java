package com.licht_meilleur.the_end_of_dragon.network;

import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;

public final class TedVillageTradeNetwork {

    private static OpenScreenSender
            openScreenSender =
            (player, payload) -> {
                /*
                 * ローダー側の初期化前は何もしない。
                 */
            };

    private TedVillageTradeNetwork() {
    }

    public static void bindOpenScreenSender(
            OpenScreenSender sender
    ) {
        openScreenSender =
                Objects.requireNonNull(
                        sender,
                        "TED village trade screen sender"
                );
    }

    public static void sendOpenScreen(
            ServerPlayer player,
            TedOpenTradeScreenPayload payload
    ) {
        if (player == null
                || payload == null) {
            return;
        }

        openScreenSender.send(
                player,
                payload
        );
    }

    @FunctionalInterface
    public interface OpenScreenSender {

        void send(
                ServerPlayer player,
                TedOpenTradeScreenPayload payload
        );
    }

    private static java.util.function.Consumer<
            TedExecuteTradePayload
            > executeTradeSender;

    public static void bindExecuteTradeSender(
            java.util.function.Consumer<
                    TedExecuteTradePayload
                    > sender
    ) {
        executeTradeSender =
                java.util.Objects.requireNonNull(
                        sender
                );
    }

    public static void sendExecuteTrade(
            TedExecuteTradePayload payload
    ) {
        if (executeTradeSender == null) {
            throw new IllegalStateException(
                    "Execute trade sender is not bound"
            );
        }

        executeTradeSender.accept(
                payload
        );
    }
}