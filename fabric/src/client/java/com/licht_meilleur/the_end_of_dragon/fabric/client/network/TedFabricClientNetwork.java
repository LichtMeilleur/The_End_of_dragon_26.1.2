package com.licht_meilleur.the_end_of_dragon.fabric.client.network;

import com.licht_meilleur.the_end_of_dragon.client.TedVillageTradeClientHandler;
import com.licht_meilleur.the_end_of_dragon.client.TedWaterTransferClientHandler;
import com.licht_meilleur.the_end_of_dragon.client.phase.TedDifferentPhaseClientState;
import com.licht_meilleur.the_end_of_dragon.client.quest.TedVillageQuestClientHandler;
import com.licht_meilleur.the_end_of_dragon.client.sound.TedBgmManager;
import com.licht_meilleur.the_end_of_dragon.network.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class TedFabricClientNetwork {

    private TedFabricClientNetwork() {
    }

    public static void init() {

        ClientPlayNetworking.registerGlobalReceiver(
                TedBgmPayload.TYPE,
                (payload, context) ->
                        context.client().execute(
                                () -> {
                                    if (payload.command()
                                            == TedBgmCommand.START) {
                                        TedBgmManager.start();
                                    } else {
                                        TedBgmManager.stop();
                                    }
                                }
                        )
        );

        ClientPlayNetworking.registerGlobalReceiver(
                TedDifferentPhaseSyncPayload.TYPE,
                (payload, context) ->
                        context.client().execute(
                                () ->
                                        TedDifferentPhaseClientState.update(
                                                payload.playerId(),
                                                payload.persistent(),
                                                payload.temporaryTicks()
                                        )
                        )
        );

        ClientPlayNetworking.registerGlobalReceiver(
                TedOpenTradeScreenPayload.TYPE,
                (payload, context) ->
                        context.client().execute(
                                () ->
                                        TedVillageTradeClientHandler
                                                .handleOpenScreen(
                                                        payload
                                                )
                        )
        );

        ClientPlayNetworking.registerGlobalReceiver(
                TedOpenQuestLetterPayload.TYPE,
                (payload, context) ->
                        context.client().execute(
                                () ->
                                        TedVillageQuestClientHandler
                                                .openQuestLetter(
                                                        payload
                                                )
                        )
        );

        ClientPlayNetworking.registerGlobalReceiver(
                TedOpenQuestListPayload.TYPE,
                (payload, context) ->
                        context.client().execute(
                                () ->
                                        TedVillageQuestClientHandler
                                                .openQuestList(
                                                        payload
                                                )
                        )
        );

        ClientPlayNetworking.registerGlobalReceiver(
                TedOpenWaterTransferScreenPayload.TYPE,
                (payload, context) ->
                        context.client().execute(
                                () ->
                                        TedWaterTransferClientHandler
                                                .openScreen(
                                                        payload
                                                )
                        )
        );

        TedVillageTradeNetwork
                .bindExecuteTradeSender(
                        ClientPlayNetworking::send
                );


        TedQuestClientNetwork.setSubmitSender(
                questId ->
                        ClientPlayNetworking.send(
                                new TedSubmitQuestPayload(
                                        questId
                                )
                        )
        );

        TedQuestClientNetwork.setSelectSender(
                questId ->
                        ClientPlayNetworking.send(
                                new TedSelectQuestPayload(
                                        questId
                                )
                        )
        );

        TedWaterTransferNetwork.bindSetChannelSender(
                ClientPlayNetworking::send
        );
    }
}