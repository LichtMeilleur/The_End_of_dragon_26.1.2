package com.licht_meilleur.the_end_of_dragon.world.village.quest;

import net.minecraft.world.item.Item;

public record TedQuestItemReward(
        Item item,
        int count
) {
    public TedQuestItemReward {
        if (item == null) {
            throw new IllegalArgumentException(
                    "Quest reward item cannot be null"
            );
        }

        if (count <= 0) {
            throw new IllegalArgumentException(
                    "Quest reward count must be positive"
            );
        }
    }
}