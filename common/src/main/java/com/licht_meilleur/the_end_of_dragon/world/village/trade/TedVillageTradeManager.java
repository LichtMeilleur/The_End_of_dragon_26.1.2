package com.licht_meilleur.the_end_of_dragon.world.village.trade;

import com.licht_meilleur.the_end_of_dragon.entity.enderman.village
        .TedTechEndermanEntity;
import com.licht_meilleur.the_end_of_dragon.network
        .TedOpenTradeScreenPayload;
import com.licht_meilleur.the_end_of_dragon.network
        .TedTradeEntryData;
import com.licht_meilleur.the_end_of_dragon.network
        .TedVillageTradeNetwork;
import com.licht_meilleur.the_end_of_dragon.world.village
        .TedVillageWorldState;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.List;

public final class TedVillageTradeManager {

    private static final double
            MAX_INTERACTION_DISTANCE_SQUARED =
            8.0D * 8.0D;

    private TedVillageTradeManager() {
    }

    public static void open(
            ServerPlayer player,
            TedTechEndermanEntity technician
    ) {
        if (!isValidInteraction(
                player,
                technician
        )) {
            return;
        }

        ServerLevel villageLevel =
                (ServerLevel) technician.level();

        TedVillageWorldState villageState =
                TedVillageWorldState.get(
                        villageLevel
                );

        int trustPoints =
                villageState.getTrustPoints(
                        player.getUUID()
                );

        int trustCap =
                villageState.getTrustCap();

        int trustLevel =
                villageState.getTrustLevel(
                        player.getUUID()
                );

        List<TedTradeEntryData> trades =
                TedVillageTradeRegistry
                        .getAll()
                        .stream()
                        .map(
                                TedVillageTradeDefinition
                                        ::toNetworkData
                        )
                        .toList();

        TedVillageTradeNetwork.sendOpenScreen(
                player,
                new TedOpenTradeScreenPayload(
                        technician.getId(),
                        trustPoints,
                        trustCap,
                        trustLevel,
                        trades
                )
        );
    }

    public static void handleExecuteRequest(
            ServerPlayer player,
            int technicianEntityId,
            String tradeId
    ) {
        if (player == null
                || tradeId == null
                || tradeId.isBlank()) {
            return;
        }

        Entity entity =
                player.level()
                        .getEntity(
                                technicianEntityId
                        );

        if (!(entity
                instanceof TedTechEndermanEntity technician)) {
            return;
        }

        if (!isValidInteraction(
                player,
                technician
        )) {
            return;
        }

        TedVillageTradeDefinition trade =
                TedVillageTradeRegistry.get(
                        tradeId
                );

        if (trade == null) {
            return;
        }

        ServerLevel villageLevel =
                (ServerLevel) technician.level();

        TedVillageWorldState villageState =
                TedVillageWorldState.get(
                        villageLevel
                );

        int trustLevel =
                villageState.getTrustLevel(
                        player.getUUID()
                );

        if (trustLevel
                < trade.requiredTrustLevel()) {

            player.sendSystemMessage(
                    Component.translatable(
                            "message.the_end_of_dragon.trade.locked",
                            trade.requiredTrustLevel()
                    ),
                    false
            );

            return;
        }

        /*
         * 実際の素材確認・消費・結果付与は、
         * TradeMenuの交換スロットを参照して実行する。
         *
         * 現時点ではクライアントからの要求だけで
         * プレイヤーのインベントリを直接消費しない。
         */
    }

    private static boolean isValidInteraction(
            ServerPlayer player,
            TedTechEndermanEntity technician
    ) {
        if (player == null
                || technician == null
                || !technician.isAlive()
                || player.isSpectator()) {
            return false;
        }

        if (!(technician.level()
                instanceof ServerLevel)) {
            return false;
        }

        if (player.level()
                != technician.level()) {
            return false;
        }

        return player.distanceToSqr(
                technician
        ) <= MAX_INTERACTION_DISTANCE_SQUARED;
    }
}