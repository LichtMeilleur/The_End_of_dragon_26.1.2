package com.licht_meilleur.the_end_of_dragon.network;

import java.util.Objects;
import java.util.function.Consumer;

public final class TedQuestClientNetwork {

    private static Consumer<String> submitSender =
            questId -> {
            };

    private static Consumer<String> selectSender =
            questId -> {
            };

    public static void setSubmitSender(
            Consumer<String> implementation
    ) {
        submitSender =
                Objects.requireNonNull(
                        implementation,
                        "TED quest submit sender"
                );
    }

    public static void setSelectSender(
            Consumer<String> implementation
    ) {
        selectSender =
                Objects.requireNonNull(
                        implementation,
                        "TED quest select sender"
                );
    }

    public static void submitQuest(
            String questId
    ) {
        if (questId == null
                || questId.isBlank()) {
            return;
        }

        submitSender.accept(
                questId
        );
    }

    public static void selectQuest(
            String questId
    ) {
        if (questId == null
                || questId.isBlank()) {
            return;
        }

        selectSender.accept(
                questId
        );
    }

    private TedQuestClientNetwork() {
    }
}