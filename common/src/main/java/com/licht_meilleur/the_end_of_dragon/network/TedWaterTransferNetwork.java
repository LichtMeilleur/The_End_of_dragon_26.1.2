package com.licht_meilleur.the_end_of_dragon.network;

import net.minecraft.server.level.ServerPlayer;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class TedWaterTransferNetwork {

    /*
     * サーバーからクライアントへ、
     * 転送装置GUIを開くPayloadを送る窓口。
     */
    private static BiConsumer<
            ServerPlayer,
            TedOpenWaterTransferScreenPayload>
            openScreenSender;

    /*
     * クライアントからサーバーへ、
     * チャンネル名変更Payloadを送る窓口。
     */
    private static Consumer<
            TedSetWaterTransferChannelPayload>
            setChannelSender;

    public static void bindOpenScreenSender(
            BiConsumer<
                    ServerPlayer,
                    TedOpenWaterTransferScreenPayload>
                    sender
    ) {
        openScreenSender =
                sender;
    }

    public static void bindSetChannelSender(
            Consumer<
                    TedSetWaterTransferChannelPayload>
                    sender
    ) {
        setChannelSender =
                sender;
    }

    public static void sendOpenScreen(
            ServerPlayer player,
            TedOpenWaterTransferScreenPayload payload
    ) {
        if (player == null
                || payload == null
                || openScreenSender == null) {
            return;
        }

        openScreenSender.accept(
                player,
                payload
        );
    }

    public static void sendSetChannel(
            TedSetWaterTransferChannelPayload payload
    ) {
        if (payload == null
                || setChannelSender == null) {
            return;
        }

        setChannelSender.accept(
                payload
        );
    }

    private TedWaterTransferNetwork() {
    }
}