package com.licht_meilleur.the_end_of_dragon.mixin;

import com.licht_meilleur.the_end_of_dragon.world.phase
        .TedDifferentPhaseManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game
        .ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game
        .ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game
        .ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback
        .CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeDifferentPhaseMixin {

    @Shadow
    protected ServerPlayer player;

    /**
     * 採掘開始・継続・中止パケットを処理する入口。
     *
     * START_DESTROY_BLOCKの時点で停止するため、
     * ブロックのひび割れが進行しない。
     */
    @Inject(
            method = "handleBlockBreakAction",
            at = @At("HEAD"),
            cancellable = true
    )
    private void ted$preventDifferentPhaseBlockBreakAction(
            BlockPos position,
            ServerboundPlayerActionPacket.Action action,
            Direction direction,
            int worldHeight,
            int sequence,
            CallbackInfo callbackInfo
    ) {
        BlockState blockState =
                this.player.level()
                        .getBlockState(
                                position
                        );

        if (TedDifferentPhaseManager
                .canModifyBlock(
                        this.player,
                        blockState
                )) {
            return;
        }

        /*
         * すでにクライアント側で表示された
         * ひび割れがあれば即座に消す。
         */
        this.player.connection.send(
                new ClientboundBlockDestructionPacket(
                        this.player.getId(),
                        position,
                        -1
                )
        );

        /*
         * 正しいブロック状態も再送する。
         */
        this.player.connection.send(
                new ClientboundBlockUpdatePacket(
                        this.player.level(),
                        position
                )
        );

        callbackInfo.cancel();
    }

    /**
     * 他Modや別経路からdestroyBlockが直接呼ばれた場合に備えた
     * 最終的な破壊防止。
     */
    @Inject(
            method = "destroyBlock",
            at = @At("HEAD"),
            cancellable = true
    )
    private void ted$preventDifferentPhaseBlockBreak(
            BlockPos position,
            CallbackInfoReturnable<Boolean> callbackInfo
    ) {
        BlockState blockState =
                this.player.level()
                        .getBlockState(
                                position
                        );

        if (!TedDifferentPhaseManager
                .canModifyBlock(
                        this.player,
                        blockState
                )) {

            this.player.connection.send(
                    new ClientboundBlockUpdatePacket(
                            this.player.level(),
                            position
                    )
            );

            callbackInfo.setReturnValue(
                    false
            );
        }
    }
}