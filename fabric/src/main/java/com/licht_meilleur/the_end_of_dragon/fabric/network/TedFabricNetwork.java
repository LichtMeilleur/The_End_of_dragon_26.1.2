package com.licht_meilleur.the_end_of_dragon.fabric.network;

import com.licht_meilleur.the_end_of_dragon.network.TedBgmPayload;
import com.licht_meilleur.the_end_of_dragon.network.TedNetwork;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class TedFabricNetwork {
    private TedFabricNetwork() {
    }

    public static void init() {
        /*
         * サーバーからクライアントへ送るPayload型を登録。
         */
        PayloadTypeRegistry.clientboundPlay()
                .register(
                        TedBgmPayload.TYPE,
                        TedBgmPayload.STREAM_CODEC
                );

        /*
         * Common側の送信窓口へFabric実装を設定。
         */
        TedNetwork.setSender(
                (player, command) ->
                        ServerPlayNetworking.send(
                                player,
                                new TedBgmPayload(command)
                        )
        );
    }
}