package com.licht_meilleur.the_end_of_dragon.client;

import com.licht_meilleur.the_end_of_dragon.client.screen
        .TedVillageTradeScreen;
import com.licht_meilleur.the_end_of_dragon.network
        .TedOpenTradeScreenPayload;
import net.minecraft.client.Minecraft;

public final class TedVillageTradeClientHandler {

    private static TedOpenTradeScreenPayload
            pendingPayload;

    private TedVillageTradeClientHandler() {
    }

    public static void handleOpenScreen(
            TedOpenTradeScreenPayload payload
    ) {
        Minecraft minecraft =
                Minecraft.getInstance();

        minecraft.execute(
                () -> {
                    if (minecraft.screen
                            instanceof TedVillageTradeScreen screen) {

                        int menuEntityId =
                                screen.getMenu()
                                        .getTechnicianEntityId();

                        if (menuEntityId < 0
                                || menuEntityId
                                == payload
                                .technicianEntityId()) {

                            pendingPayload =
                                    null;

                            screen.applyPayload(
                                    payload
                            );

                            return;
                        }
                    }

                    pendingPayload =
                            payload;
                }
        );
    }

    public static TedOpenTradeScreenPayload
    takePendingPayload(
            int technicianEntityId
    ) {
        TedOpenTradeScreenPayload payload =
                pendingPayload;

        if (payload == null) {
            return null;
        }

        if (technicianEntityId >= 0
                && payload.technicianEntityId()
                != technicianEntityId) {
            return null;
        }

        pendingPayload =
                null;

        return payload;
    }
}