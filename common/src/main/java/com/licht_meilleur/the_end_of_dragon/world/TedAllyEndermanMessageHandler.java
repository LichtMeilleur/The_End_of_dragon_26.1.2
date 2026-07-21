package com.licht_meilleur.the_end_of_dragon.world;

import com.licht_meilleur.the_end_of_dragon.entity.enderman.TedAllyEndermanEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class TedAllyEndermanMessageHandler {

    private static final double MESSAGE_RANGE = 256.0D;

    private TedAllyEndermanMessageHandler() {
    }

    public static void sendHelpMessage(
            ServerLevel level,
            TedAllyEndermanEntity ally
    ) {
        sendToNearbyPlayers(
                level,
                ally,
                "message.the_end_of_dragon.ally_enderman.help",
                false
        );
    }

    public static void sendDetectedMessage(
            ServerLevel level,
            TedAllyEndermanEntity ally
    ) {
        sendToNearbyPlayers(
                level,
                ally,
                "message.the_end_of_dragon.ally_enderman.detected",
                false
        );
    }

    public static void sendFeedMessage(
            ServerLevel level,
            TedAllyEndermanEntity ally
    ) {
        sendToNearbyPlayers(
                level,
                ally,
                "message.the_end_of_dragon.ally_enderman.feed",
                false
        );
    }

    public static void sendInteractHint(
            ServerPlayer player
    ) {
        player.sendSystemMessage(
                Component.translatable(
                        "message.the_end_of_dragon.ally_enderman.interact"
                ),
                true
        );
    }

    public static void sendFoodRequestMessage(
            ServerLevel level,
            TedAllyEndermanEntity ally
    ) {
        if (level == null
                || ally == null
                || !ally.isAlive()) {
            return;
        }

        /*
         * 周囲64ブロック以内のプレイヤーへ送信。
         */
        for (ServerPlayer player :
                level.getServer()
                        .getPlayerList()
                        .getPlayers()) {

            if (player.level() != level) {
                continue;
            }

            if (player.distanceToSqr(ally)
                    > 64.0D * 64.0D) {
                continue;
            }

            player.sendSystemMessage(
                    Component.translatable(
                            "message.the_end_of_dragon.ally_enderman.request_food"
                    )
            );
        }
    }

    public static void sendFoodReceivedMessage(
            ServerLevel level,
            TedAllyEndermanEntity ally
    ) {
        if (level == null
                || ally == null) {
            return;
        }

        for (ServerPlayer player :
                level.getServer()
                        .getPlayerList()
                        .getPlayers()) {

            if (player.level() != level) {
                continue;
            }

            if (player.distanceToSqr(ally)
                    > 64.0D * 64.0D) {
                continue;
            }

            player.sendSystemMessage(
                    Component.translatable(
                            "message.the_end_of_dragon.ally_enderman.food_received"
                    )
            );
        }
    }

    public static void sendCombatSupplyMessage(
            ServerLevel level,
            TedAllyEndermanEntity ally,
            ServerPlayer target
    ) {
        if (level == null
                || ally == null
                || target == null) {
            return;
        }

        target.sendSystemMessage(
                Component.translatable(
                        "message.the_end_of_dragon.ally_enderman.combat_supply"
                )
        );
    }

    private static void sendToNearbyPlayers(
            ServerLevel level,
            TedAllyEndermanEntity ally,
            String translationKey,
            boolean actionBar
    ) {
        double rangeSqr =
                MESSAGE_RANGE * MESSAGE_RANGE;

        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(ally)
                    > rangeSqr) {
                continue;
            }

            player.sendSystemMessage(
                    Component.translatable(
                            translationKey
                    ),
                    actionBar
            );
        }
    }
}