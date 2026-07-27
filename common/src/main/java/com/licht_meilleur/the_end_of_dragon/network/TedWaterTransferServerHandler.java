package com.licht_meilleur.the_end_of_dragon.network;

import com.licht_meilleur.the_end_of_dragon.world.block.entity
        .WaterTransferChannelBlockEntity;
import com.licht_meilleur.the_end_of_dragon.world.village
        .TedWaterTransferNetworkState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class TedWaterTransferServerHandler {

    private static final double MAX_INTERACTION_DISTANCE_SQR =
            64.0D;

    public static void handleSetChannel(
            ServerPlayer player,
            TedSetWaterTransferChannelPayload payload
    ) {
        if (player == null
                || payload == null) {
            return;
        }

        BlockPos machinePosition =
                payload.machinePosition();

        /*
         * 遠隔から他人の装置を書き換えられないようにする。
         */
        if (player.distanceToSqr(
                machinePosition.getX() + 0.5D,
                machinePosition.getY() + 0.5D,
                machinePosition.getZ() + 0.5D
        ) > MAX_INTERACTION_DISTANCE_SQR) {
            return;
        }

        if (!player.level().isLoaded(
                machinePosition
        )) {
            return;
        }

        BlockEntity blockEntity =
                player.level()
                        .getBlockEntity(
                                machinePosition
                        );

        if (!(blockEntity
                instanceof WaterTransferChannelBlockEntity
                machine)) {
            return;
        }

        String normalizedChannel =
                TedWaterTransferNetworkState
                        .normalizeChannelName(
                                payload.channelName()
                        );

        machine.setChannelName(
                normalizedChannel
        );

    }

    private TedWaterTransferServerHandler() {
    }
}