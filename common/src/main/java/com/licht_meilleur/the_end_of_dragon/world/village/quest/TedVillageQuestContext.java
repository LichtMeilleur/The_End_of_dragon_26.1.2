package com.licht_meilleur.the_end_of_dragon.world.village.quest;

import com.licht_meilleur.the_end_of_dragon.world.village
        .TedVillageWorldState;
import net.minecraft.server.level.ServerLevel;

public record TedVillageQuestContext(
        ServerLevel villageLevel,
        TedVillageWorldState villageState
) {
}