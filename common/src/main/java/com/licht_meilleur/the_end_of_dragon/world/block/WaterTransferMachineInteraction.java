package com.licht_meilleur.the_end_of_dragon.world.block;

import com.licht_meilleur.the_end_of_dragon.network
        .TedOpenWaterTransferScreenPayload;
import com.licht_meilleur.the_end_of_dragon.network
        .TedWaterTransferNetwork;
import com.licht_meilleur.the_end_of_dragon.world.block.entity
        .WaterTransferChannelBlockEntity;
import com.licht_meilleur.the_end_of_dragon.world.block.entity
        .WaterTransferMachineABlockEntity;
import com.licht_meilleur.the_end_of_dragon.world.village
        .TedWaterTransferNetworkState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class WaterTransferMachineInteraction {

    public static InteractionResult interact(
            Level level,
            BlockPos position,
            Player player,
            InteractionHand hand
    ) {
        /*
         * メインハンド操作だけ受け付ける。
         */
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        if (!(level.getBlockEntity(
                position
        ) instanceof WaterTransferChannelBlockEntity
                machine)) {
            return InteractionResult.PASS;
        }

        /*
         * クライアント側では操作を成功扱いにし、
         * 実際の画面送信はサーバー側で行う。
         */
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!(level instanceof ServerLevel
                serverLevel)
                || !(player instanceof ServerPlayer
                serverPlayer)) {
            return InteractionResult.PASS;
        }

        TedWaterTransferNetworkState network =
                TedWaterTransferNetworkState.get(
                        serverLevel
                );

        boolean machineA =
                machine
                        instanceof WaterTransferMachineABlockEntity;

        String channelName =
                machine.getChannelName();

        long storedAmount =
                network.getStoredAmount(
                        channelName
                );

        TedWaterTransferNetwork.sendOpenScreen(
                serverPlayer,
                new TedOpenWaterTransferScreenPayload(
                        position.immutable(),
                        machineA,
                        channelName,
                        storedAmount
                )
        );

        return InteractionResult.SUCCESS;
    }

    private WaterTransferMachineInteraction() {
    }
}