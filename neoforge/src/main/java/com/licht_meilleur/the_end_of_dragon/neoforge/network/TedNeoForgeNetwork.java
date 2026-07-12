package com.licht_meilleur.the_end_of_dragon.neoforge.network;

import com.licht_meilleur.the_end_of_dragon.network.TedBgmPayload;
import com.licht_meilleur.the_end_of_dragon.network.TedNetwork;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class TedNeoForgeNetwork {
    private static final String NETWORK_VERSION = "1";

    private TedNeoForgeNetwork() {
    }

    public static void initSender() {
        TedNetwork.setSender(
                (player, command) ->
                        PacketDistributor.sendToPlayer(
                                player,
                                new TedBgmPayload(command)
                        )
        );
    }

    public static void registerPayloads(
            RegisterPayloadHandlersEvent event
    ) {
        PayloadRegistrar registrar =
                event.registrar(NETWORK_VERSION);

        /*
         * クライアント向けPayloadの型とCodecを登録。
         * クライアント側ハンドラは
         * RegisterClientPayloadHandlersEventで登録する。
         */
        registrar.playToClient(
                TedBgmPayload.TYPE,
                TedBgmPayload.STREAM_CODEC
        );
    }
}