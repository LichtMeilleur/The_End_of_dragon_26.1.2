package com.licht_meilleur.the_end_of_dragon.neoforge.client.network;

import com.licht_meilleur.the_end_of_dragon.client.sound.TedBgmManager;
import com.licht_meilleur.the_end_of_dragon.network.TedBgmCommand;
import com.licht_meilleur.the_end_of_dragon.network.TedBgmPayload;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;

public final class TedNeoForgeClientNetwork {
    private TedNeoForgeClientNetwork() {
    }

    public static void registerClientPayloads(
            RegisterClientPayloadHandlersEvent event
    ) {
        event.register(
                TedBgmPayload.TYPE,
                (payload, context) -> {
                    if (payload.command() == TedBgmCommand.START) {
                        TedBgmManager.start();
                    } else {
                        TedBgmManager.stop();
                    }
                }
        );
    }
}