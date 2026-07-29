package com.licht_meilleur.the_end_of_dragon.neoforge.client.network;

import com.licht_meilleur.the_end_of_dragon.client.TedWaterTransferClientHandler;
import com.licht_meilleur.the_end_of_dragon.client.phase.TedDifferentPhaseClientState;
import com.licht_meilleur.the_end_of_dragon.client.quest
        .TedVillageQuestClientHandler;
import com.licht_meilleur.the_end_of_dragon.client.screen.TedVillageTradeScreen;
import com.licht_meilleur.the_end_of_dragon.client.sound
        .TedBgmManager;
import com.licht_meilleur.the_end_of_dragon.network.*;
import com.licht_meilleur.the_end_of_dragon.registry.ModMenus;
import net.neoforged.neoforge.client.network.event
        .RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public final class TedNeoForgeClientNetwork {

    private TedNeoForgeClientNetwork() {
    }

    public static void registerClientPayloads(
            RegisterClientPayloadHandlersEvent event
    ) {
        event.register(
                TedBgmPayload.TYPE,
                (payload, context) -> {
                    if (payload.command()
                            == TedBgmCommand.START) {

                        TedBgmManager.start();
                    } else {
                        TedBgmManager.stop();
                    }
                }
        );

        event.register(
                TedDifferentPhaseSyncPayload.TYPE,
                (payload, context) ->
                        TedDifferentPhaseClientState
                                .update(
                                        payload.playerId(),
                                        payload.persistent(),
                                        payload.temporaryTicks()
                                )
        );


        event.register(
                TedOpenQuestLetterPayload.TYPE,
                (payload, context) ->
                        TedVillageQuestClientHandler
                                .openQuestLetter(
                                        payload
                                )
        );

        TedQuestClientNetwork.setSubmitSender(
                questId ->
                        ClientPacketDistributor.sendToServer(
                                new TedSubmitQuestPayload(
                                        questId
                                )
                        )
        );

        event.register(
                TedOpenQuestListPayload.TYPE,
                (payload, context) ->
                        TedVillageQuestClientHandler
                                .openQuestList(
                                        payload
                                )
        );

        TedQuestClientNetwork.setSelectSender(
                questId ->
                        ClientPacketDistributor.sendToServer(
                                new TedSelectQuestPayload(
                                        questId
                                )
                        )
        );

        event.register(
                TedOpenWaterTransferScreenPayload.TYPE,
                (payload, context) ->
                        TedWaterTransferClientHandler
                                .openScreen(
                                        payload
                                )
        );

        TedWaterTransferNetwork.bindSetChannelSender(
                payload ->
                        ClientPacketDistributor.sendToServer(
                                payload
                        )
        );

        TedVillageTradeNetwork
                .bindExecuteTradeSender(
                        payload ->
                                ClientPacketDistributor
                                        .sendToServer(
                                                payload
                                        )
                );
    }
}