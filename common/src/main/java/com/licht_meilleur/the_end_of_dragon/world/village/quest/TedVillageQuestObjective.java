package com.licht_meilleur.the_end_of_dragon.world.village.quest;

import net.minecraft.server.level.ServerPlayer;

public interface TedVillageQuestObjective {

    boolean isComplete(
            ServerPlayer player,
            TedVillageQuestContext context
    );
}