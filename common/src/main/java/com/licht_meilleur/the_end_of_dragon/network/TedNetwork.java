package com.licht_meilleur.the_end_of_dragon.network;

import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;

public final class TedNetwork {
    private static Sender sender =
            (player, command) -> {
                // ローダー側の初期化前は何もしない
            };

    private static QuestSender questSender =
            (player, questId, completable) -> {
                /*
                 * ローダー側初期化前は何もしない。
                 */
            };

    private static QuestListSender questListSender =
            (player, quests) -> {
            };

    private TedNetwork() {
    }

    public static void setQuestSender(
            QuestSender implementation
    ) {
        questSender =
                Objects.requireNonNull(
                        implementation,
                        "TED quest network sender"
                );
    }

    public static void setSender(Sender implementation) {
        sender = Objects.requireNonNull(
                implementation,
                "TED network sender"
        );
    }

    public static void sendBgmStart(
            ServerPlayer player
    ) {
        sender.send(
                player,
                TedBgmCommand.START
        );
    }

    public static void sendBgmStop(
            ServerPlayer player
    ) {
        sender.send(
                player,
                TedBgmCommand.STOP
        );
    }

    public static void sendOpenQuestLetter(
            ServerPlayer player,
            String questId,
            boolean completable
    ) {
        if (player == null
                || questId == null
                || questId.isBlank()) {
            return;
        }

        questSender.send(
                player,
                questId,
                completable
        );
    }

    public static void setQuestListSender(
            QuestListSender implementation
    ) {
        questListSender =
                Objects.requireNonNull(
                        implementation,
                        "TED quest list sender"
                );
    }

    @FunctionalInterface
    public interface Sender {
        void send(
                ServerPlayer player,
                TedBgmCommand command
        );
    }

    @FunctionalInterface
    public interface QuestSender {

        void send(
                ServerPlayer player,
                String questId,
                boolean completable
        );
    }

    public static void sendOpenQuestList(
            ServerPlayer player,
            java.util.List<
                    TedQuestListEntryData> quests
    ) {
        if (player == null) {
            return;
        }

        questListSender.send(
                player,
                quests == null
                        ? java.util.List.of()
                        : java.util.List.copyOf(quests)
        );
    }

    @FunctionalInterface
    public interface QuestListSender {

        void send(
                ServerPlayer player,
                java.util.List<
                        TedQuestListEntryData> quests
        );
    }
}