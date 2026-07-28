package com.licht_meilleur.the_end_of_dragon.fabric.network;

import com.licht_meilleur.the_end_of_dragon.network.*;
import com.licht_meilleur.the_end_of_dragon.world.phase.TedDifferentPhaseManager;
import com.licht_meilleur.the_end_of_dragon.world.village.quest.TedVillageQuestManager;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class TedFabricNetwork {
    private TedFabricNetwork() {
    }

    public static void init() {

        PayloadTypeRegistry.clientboundPlay()
                .register(
                        TedBgmPayload.TYPE,
                        TedBgmPayload.STREAM_CODEC
                );

        PayloadTypeRegistry.clientboundPlay()
                .register(
                        TedDifferentPhaseSyncPayload.TYPE,
                        TedDifferentPhaseSyncPayload
                                .STREAM_CODEC
                );

        TedDifferentPhaseNetwork.bindSender(
                (player, payload) ->
                        ServerPlayNetworking.send(
                                player,
                                payload
                        )
        );

        PayloadTypeRegistry.clientboundPlay()
                .register(
                        TedOpenQuestLetterPayload.TYPE,
                        TedOpenQuestLetterPayload.STREAM_CODEC
                );

        PayloadTypeRegistry.serverboundPlay()
                .register(
                        TedSubmitQuestPayload.TYPE,
                        TedSubmitQuestPayload.STREAM_CODEC
                );

        PayloadTypeRegistry.clientboundPlay()
                .register(
                        TedOpenWaterTransferScreenPayload.TYPE,
                        TedOpenWaterTransferScreenPayload.STREAM_CODEC
                );

        PayloadTypeRegistry.serverboundPlay()
                .register(
                        TedSetWaterTransferChannelPayload.TYPE,
                        TedSetWaterTransferChannelPayload.STREAM_CODEC
                );

        TedNetwork.setSender(
                (player, command) ->
                        ServerPlayNetworking.send(
                                player,
                                new TedBgmPayload(command)
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

        TedWaterTransferNetwork.bindOpenScreenSender(
                (player, payload) ->
                        ServerPlayNetworking.send(
                                player,
                                payload
                        )
        );

        ServerPlayNetworking.registerGlobalReceiver(
                TedSubmitQuestPayload.TYPE,
                (payload, context) ->
                        context.server()
                                .execute(
                                        () ->
                                                TedVillageQuestManager
                                                        .submitQuest(
                                                                context.player(),
                                                                payload.questId()
                                                        )
                                )
        );

        ServerPlayNetworking.registerGlobalReceiver(
                TedSetWaterTransferChannelPayload.TYPE,
                (payload, context) ->
                        context.server()
                                .execute(
                                        () ->
                                                TedWaterTransferServerHandler
                                                        .handleSetChannel(
                                                                context.player(),
                                                                payload
                                                        )
                                )
        );

        ServerPlayConnectionEvents.JOIN.register(
                (handler, sender, server) ->
                        TedDifferentPhaseManager
                                .synchronizeAllTo(
                                        handler.player
                                )
        );
    }
}