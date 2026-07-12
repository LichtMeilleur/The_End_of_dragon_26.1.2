package com.licht_meilleur.the_end_of_dragon.fabric.client.network;

import com.licht_meilleur.the_end_of_dragon.client.sound.TedBgmManager;
import com.licht_meilleur.the_end_of_dragon.network.TedBgmCommand;
import com.licht_meilleur.the_end_of_dragon.network.TedBgmPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class TedFabricClientNetwork {
    private TedFabricClientNetwork() {
    }

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(
                TedBgmPayload.TYPE,
                (payload, context) ->
                        context.client().execute(() -> {
                            if (payload.command()
                                    == TedBgmCommand.START) {
                                TedBgmManager.start();
                            } else {
                                TedBgmManager.stop();
                            }
                        })
        );
    }
}