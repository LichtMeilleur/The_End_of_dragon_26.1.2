package com.licht_meilleur.the_end_of_dragon.world.village.quest;

import net.minecraft.server.level.ServerPlayer;

public final class RechorusFacilityObjective
        implements TedVillageQuestObjective {

    @Override
    public boolean isComplete(
            ServerPlayer player,
            TedVillageQuestContext context
    ) {
        if (context == null
                || context.villageState() == null) {
            return false;
        }

        return context.villageState()
                .isWaterTransferMachineBInstalled()
                && context.villageState()
                .isRechorusPlantBuilt();
    }
}