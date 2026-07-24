package com.licht_meilleur.the_end_of_dragon.world.village.quest;

import com.licht_meilleur.the_end_of_dragon.world.village
        .TedVillageQuestStage;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;

public record TedVillageQuest(
        TedVillageQuestId id,
        TedVillageQuestStage requiredStage,
        TedVillageQuestStage completionStage,
        Component title,
        Component description,
        Identifier illustrationTexture,
        List<TedQuestItemRequirement> requirements,
        List<TedVillageQuestObjective> objectives,
        List<TedQuestItemReward> rewards,
        boolean automaticallyAccept,
        int cooldownTicks
) {
    public TedVillageQuest {
        requirements =
                requirements == null
                        ? List.of()
                        : List.copyOf(requirements);

        objectives =
                objectives == null
                        ? List.of()
                        : List.copyOf(objectives);

        rewards =
                rewards == null
                        ? List.of()
                        : List.copyOf(rewards);
    }

    public boolean isRepeatable() {
        return this.id != null
                && this.id.isRepeatable();
    }
}