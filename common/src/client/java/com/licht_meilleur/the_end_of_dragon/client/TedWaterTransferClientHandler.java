package com.licht_meilleur.the_end_of_dragon.client;

import com.licht_meilleur.the_end_of_dragon.client.screen
        .WaterTransferMachineScreen;
import com.licht_meilleur.the_end_of_dragon.network
        .TedOpenWaterTransferScreenPayload;
import net.minecraft.client.Minecraft;

public final class TedWaterTransferClientHandler {

    public static void openScreen(
            TedOpenWaterTransferScreenPayload payload
    ) {
        if (payload == null) {
            return;
        }

        Minecraft minecraft =
                Minecraft.getInstance();

        minecraft.setScreen(
                new WaterTransferMachineScreen(
                        payload.machinePosition(),
                        payload.machineA(),
                        payload.channelName(),
                        payload.storedWater()
                )
        );
    }

    private TedWaterTransferClientHandler() {
    }
}