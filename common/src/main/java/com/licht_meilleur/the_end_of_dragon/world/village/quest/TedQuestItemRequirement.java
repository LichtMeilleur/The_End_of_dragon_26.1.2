package com.licht_meilleur.the_end_of_dragon.world.village.quest;

import net.minecraft.world.item.Item;

public record TedQuestItemRequirement(
        Item item,
        int count,
        boolean hidden
) {

    public TedQuestItemRequirement(
            Item item,
            int count
    ) {
        this(
                item,
                count,
                false
        );
    }

    public TedQuestItemRequirement {
        if (item == null) {
            throw new IllegalArgumentException(
                    "Quest requirement item cannot be null"
            );
        }

        if (count <= 0) {
            throw new IllegalArgumentException(
                    "Quest requirement count must be positive"
            );
        }
    }
}