package com.licht_meilleur.the_end_of_dragon.world.village.quest;

import net.minecraft.world.item.Item;

public record ItemDeliveryObjective(
        Item item,
        int count
) implements TedVillageQuestObjective {

    @Override
    public boolean isComplete(
            net.minecraft.server.level.ServerPlayer player,
            TedVillageQuestContext context
    ) {
        return player.getInventory()
                .countItem(this.item)
                >= this.count;
    }
}