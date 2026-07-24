package com.licht_meilleur.the_end_of_dragon.client.quest;

import com.licht_meilleur.the_end_of_dragon.network
        .TedOpenQuestLetterPayload;
import com.licht_meilleur.the_end_of_dragon.network.TedOpenQuestListPayload;
import com.licht_meilleur.the_end_of_dragon.world.village.quest
        .TedVillageQuest;
import com.licht_meilleur.the_end_of_dragon.world.village.quest
        .TedVillageQuestRegistry;
import net.minecraft.client.Minecraft;

public final class TedVillageQuestClientHandler {

    public static void openQuestLetter(
            TedOpenQuestLetterPayload payload
    ) {
        if (payload == null) {
            return;
        }

        TedVillageQuest quest =
                TedVillageQuestRegistry
                        .getBySerializedName(
                                payload.questId()
                        );

        if (quest == null) {
            return;
        }

        Minecraft.getInstance()
                .setScreen(
                        new TedVillageQuestLetterScreen(
                                quest,
                                payload.completable()
                        )
                );
    }

    public static void openQuestList(
            TedOpenQuestListPayload payload
    ) {
        if (payload == null) {
            return;
        }

        Minecraft.getInstance()
                .setScreen(
                        new TedVillageQuestListScreen(
                                payload.quests()
                        )
                );
    }

    private TedVillageQuestClientHandler() {
    }
}