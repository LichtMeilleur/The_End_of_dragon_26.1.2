package com.licht_meilleur.the_end_of_dragon.mixin;

import com.licht_meilleur.the_end_of_dragon.world.phase
        .TedDifferentPhaseManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback
        .CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeDifferentPhaseMixin {

    @Shadow
    protected ServerPlayer player;

    /**
     * 別位相中はブロック破壊を成立させない。
     *
     * 道具を持つことや振ること、
     * 武器・弓・盾などの使用には影響しない。
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

            /*
             * クライアント側に正しいブロック状態を
             * 再送して、見かけ上消えたままになるのを防ぐ。
             */
            this.player.connection
                    .send(
                            new net.minecraft.network.protocol.game
                                    .ClientboundBlockUpdatePacket(
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