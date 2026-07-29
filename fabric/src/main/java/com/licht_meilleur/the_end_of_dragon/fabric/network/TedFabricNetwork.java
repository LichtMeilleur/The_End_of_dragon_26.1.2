package com.licht_meilleur.the_end_of_dragon.fabric.network;

import com.licht_meilleur.the_end_of_dragon.network.*;
import com.licht_meilleur.the_end_of_dragon.world.phase.TedDifferentPhaseManager;
import com.licht_meilleur.the_end_of_dragon.world.village.quest.TedVillageQuestManager;
import com.licht_meilleur.the_end_of_dragon.world.village.trade.TedVillageTradeManager;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class TedFabricNetwork {

    private TedFabricNetwork() {
    }

    public static void init() {

        /*
         * サーバー → クライアント
         */
        PayloadTypeRegistry.clientboundPlay()
                .register(
                        TedBgmPayload.TYPE,
                        TedBgmPayload.STREAM_CODEC
                );

        PayloadTypeRegistry.clientboundPlay()
                .register(
                        TedDifferentPhaseSyncPayload.TYPE,
                        TedDifferentPhaseSyncPayload.STREAM_CODEC
                );

        PayloadTypeRegistry.clientboundPlay()
                .register(
                        TedOpenQuestLetterPayload.TYPE,
                        TedOpenQuestLetterPayload.STREAM_CODEC
                );

        PayloadTypeRegistry.clientboundPlay()
                .register(
                        TedOpenQuestListPayload.TYPE,
                        TedOpenQuestListPayload.STREAM_CODEC
                );

        PayloadTypeRegistry.clientboundPlay()
                .register(
                        TedOpenWaterTransferScreenPayload.TYPE,
                        TedOpenWaterTransferScreenPayload.STREAM_CODEC
                );

        PayloadTypeRegistry.clientboundPlay()
                .register(
                        TedOpenTradeScreenPayload.TYPE,
                        TedOpenTradeScreenPayload.STREAM_CODEC
                );

        /*
         * クライアント → サーバー
         */
        PayloadTypeRegistry.serverboundPlay()
                .register(
                        TedSubmitQuestPayload.TYPE,
                        TedSubmitQuestPayload.STREAM_CODEC
                );

        PayloadTypeRegistry.serverboundPlay()
                .register(
                        TedSelectQuestPayload.TYPE,
                        TedSelectQuestPayload.STREAM_CODEC
                );

        PayloadTypeRegistry.serverboundPlay()
                .register(
                        TedSetWaterTransferChannelPayload.TYPE,
                        TedSetWaterTransferChannelPayload.STREAM_CODEC
                );

        PayloadTypeRegistry.serverboundPlay()
                .register(
                        TedExecuteTradePayload.TYPE,
                        TedExecuteTradePayload.STREAM_CODEC
                );

        /*
         * 共通コードからFabric通信を利用するための送信処理。
         */
        TedNetwork.setSender(
                (player, command) ->
                        ServerPlayNetworking.send(
                                player,
                                new TedBgmPayload(command)
                        )
        );

        TedDifferentPhaseNetwork.bindSender(
                (player, payload) ->
                        ServerPlayNetworking.send(
                                player,
                                payload
                        )
        );

        TedNetwork.setQuestSender(
                (player, questId, completable) ->
                        ServerPlayNetworking.send(
                                player,
                                new TedOpenQuestLetterPayload(
                                        questId,
                                        completable
                                )
                        )
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

        TedWaterTransferNetwork.bindOpenScreenSender(
                (player, payload) ->
                        ServerPlayNetworking.send(
                                player,
                                payload
                        )
        );


        TedVillageTradeNetwork
                .bindOpenScreenSender(
                        (player, payload) ->
                                ServerPlayNetworking.send(
                                        player,
                                        payload
                                )
                );



        /*
         * クエスト提出。
         */
        ServerPlayNetworking.registerGlobalReceiver(
                TedSubmitQuestPayload.TYPE,
                (payload, context) ->
                        context.server().execute(
                                () ->
                                        TedVillageQuestManager.submitQuest(
                                                context.player(),
                                                payload.questId()
                                        )
                        )
        );

        /*
         * クエスト選択。
         */
        ServerPlayNetworking.registerGlobalReceiver(
                TedSelectQuestPayload.TYPE,
                (payload, context) ->
                        context.server().execute(
                                () ->
                                        TedVillageQuestManager.selectQuest(
                                                context.player(),
                                                payload.questId()
                                        )
                        )
        );

        /*
         * 水転送装置のチャンネル変更。
         */
        ServerPlayNetworking.registerGlobalReceiver(
                TedSetWaterTransferChannelPayload.TYPE,
                (payload, context) ->
                        context.server().execute(
                                () ->
                                        TedWaterTransferServerHandler
                                                .handleSetChannel(
                                                        context.player(),
                                                        payload
                                                )
                        )
        );

        ServerPlayNetworking.registerGlobalReceiver(
                TedExecuteTradePayload.TYPE,
                (payload, context) ->
                        context.server().execute(
                                () ->
                                        TedVillageTradeManager
                                                .handleExecuteRequest(
                                                        context.player(),
                                                        payload.technicianEntityId(),
                                                        payload.tradeId()
                                                )
                        )
        );



        ServerPlayConnectionEvents.JOIN.register(
                (handler, sender, server) ->
                        TedDifferentPhaseManager.synchronizeAllTo(
                                handler.player
                        )
        );
    }
}