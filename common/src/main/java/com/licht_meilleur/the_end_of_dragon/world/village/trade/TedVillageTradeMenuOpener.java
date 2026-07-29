package com.licht_meilleur.the_end_of_dragon.world.village.trade;

import com.licht_meilleur.the_end_of_dragon.entity.enderman.village.TedTechEndermanEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;

public final class TedVillageTradeMenuOpener {

    private TedVillageTradeMenuOpener() {
    }

    public static void open(
            ServerPlayer player,
            TedTechEndermanEntity technician
    ) {
        if (player == null
                || technician == null) {

            return;
        }

        if (player.isRemoved()
                || !player.isAlive()
                || technician.isRemoved()
                || !technician.isAlive()) {

            return;
        }

        if (player.level()
                != technician.level()) {

            return;
        }

        if (player.distanceToSqr(technician)
                > 64.0D) {

            return;
        }

        int technicianEntityId =
                technician.getId();

        player.openMenu(
                new SimpleMenuProvider(
                        (
                                containerId,
                                playerInventory,
                                ignoredPlayer
                        ) ->
                                new TedVillageTradeMenu(
                                        containerId,
                                        playerInventory,
                                        technicianEntityId
                                ),
                        Component.translatable(
                                "gui.the_end_of_dragon.village_trade"
                        )
                )
        );

        technician.beginMenuInteraction(
                player
        );
    }
}