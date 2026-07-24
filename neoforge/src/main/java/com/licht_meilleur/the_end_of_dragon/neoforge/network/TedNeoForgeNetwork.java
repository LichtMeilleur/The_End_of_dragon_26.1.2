package com.licht_meilleur.the_end_of_dragon.neoforge.network;

import com.licht_meilleur.the_end_of_dragon.network.*;
import com.licht_meilleur.the_end_of_dragon.world.village.quest.TedVillageQuestManager;
import net.minecraft.server.level.ServerPlayer;
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

        TedNetwork.setQuestSender(
                (player, questId, completable) ->
                        PacketDistributor.sendToPlayer(
                                player,
                                new TedOpenQuestLetterPayload(
                                        questId,
                                        completable
                                )
                        )
        );

        TedNetwork.setQuestListSender(
                (player, quests) ->
                        PacketDistributor.sendToPlayer(
                                player,
                                new TedOpenQuestListPayload(
                                        quests
                                )
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

        registrar.playToClient(
                TedOpenQuestLetterPayload.TYPE,
                TedOpenQuestLetterPayload.STREAM_CODEC
        );

        registrar.playToServer(
                TedSubmitQuestPayload.TYPE,
                TedSubmitQuestPayload.STREAM_CODEC,
                (payload, context) ->
                        context.enqueueWork(
                                () -> {
                                    if (context.player()
                                            instanceof ServerPlayer player) {

                                        TedVillageQuestManager
                                                .submitQuest(
                                                        player,
                                                        payload.questId()
                                                );
                                    }
                                }
                        )
        );

        registrar.playToClient(
                TedOpenQuestListPayload.TYPE,
                TedOpenQuestListPayload.STREAM_CODEC
        );

        registrar.playToServer(
                TedSelectQuestPayload.TYPE,
                TedSelectQuestPayload.STREAM_CODEC,
                (payload, context) ->
                        context.enqueueWork(
                                () -> {
                                    if (context.player()
                                            instanceof ServerPlayer player) {

                                        TedVillageQuestManager
                                                .selectQuest(
                                                        player,
                                                        payload.questId()
                                                );
                                    }
                                }
                        )
        );
    }
}