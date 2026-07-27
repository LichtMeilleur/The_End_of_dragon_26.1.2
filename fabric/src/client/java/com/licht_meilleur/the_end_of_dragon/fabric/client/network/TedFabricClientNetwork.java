package com.licht_meilleur.the_end_of_dragon.fabric.client.network;

import com.licht_meilleur.the_end_of_dragon.client.TedWaterTransferClientHandler;
import com.licht_meilleur.the_end_of_dragon.client.quest.TedVillageQuestClientHandler;
import com.licht_meilleur.the_end_of_dragon.client.sound.TedBgmManager;
import com.licht_meilleur.the_end_of_dragon.network.*;
import com.licht_meilleur.the_end_of_dragon.world.village.quest.TedVillageQuestManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

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

        ClientPlayNetworking.registerGlobalReceiver(
                TedOpenQuestLetterPayload.TYPE,
                (payload, context) ->
                        context.client()
                                .execute(
                                        () ->
                                                TedVillageQuestClientHandler
                                                        .openQuestLetter(
                                                                payload
                                                        )
                                )
        );

        TedQuestClientNetwork.setSubmitSender(
                questId ->
                        ClientPlayNetworking.send(
                                new TedSubmitQuestPayload(
                                        questId
                                )
                        )
        );

        PayloadTypeRegistry.clientboundPlay()
                .register(
                        TedOpenQuestListPayload.TYPE,
                        TedOpenQuestListPayload.STREAM_CODEC
                );

        PayloadTypeRegistry.serverboundPlay()
                .register(
                        TedSelectQuestPayload.TYPE,
                        TedSelectQuestPayload.STREAM_CODEC
                );

        TedNetwork.setQuestListSender(
                (player, quests) ->
                        ServerPlayNetworking.send(
                                player,
                                new TedOpenQuestListPayload(
                                        quests
                                )
                        )
        );

        ServerPlayNetworking.registerGlobalReceiver(
                TedSelectQuestPayload.TYPE,
                (payload, context) ->
                        context.server()
                                .execute(
                                        () ->
                                                TedVillageQuestManager
                                                        .selectQuest(
                                                                context.player(),
                                                                payload.questId()
                                                        )
                                )
        );
        ClientPlayNetworking.registerGlobalReceiver(
                TedOpenQuestListPayload.TYPE,
                (payload, context) ->
                        context.client()
                                .execute(
                                        () ->
                                                TedVillageQuestClientHandler
                                                        .openQuestList(
                                                                payload
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

        ClientPlayNetworking.registerGlobalReceiver(
                TedOpenWaterTransferScreenPayload.TYPE,
                (payload, context) ->
                        context.client()
                                .execute(
                                        () ->
                                                TedWaterTransferClientHandler
                                                        .openScreen(
                                                                payload
                                                        )
                                )
        );

        TedWaterTransferNetwork.bindSetChannelSender(
                payload ->
                        ClientPlayNetworking.send(
                                payload
                        )
        );
    }
}