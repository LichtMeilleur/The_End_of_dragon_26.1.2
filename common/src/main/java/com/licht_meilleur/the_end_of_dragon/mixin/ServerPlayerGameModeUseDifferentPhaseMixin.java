package com.licht_meilleur.the_end_of_dragon.mixin;

import com.licht_meilleur.the_end_of_dragon.registry.ModItemTags;
import com.licht_meilleur.the_end_of_dragon.world.phase
        .TedDifferentPhaseManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback
        .CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeUseDifferentPhaseMixin {

    @Inject(
            method = "useItemOn",
            at = @At("HEAD"),
            cancellable = true
    )
    private void ted$preventDifferentPhaseBlockInteraction(
            ServerPlayer player,
            Level level,
            ItemStack itemStack,
            InteractionHand hand,
            BlockHitResult hitResult,
            CallbackInfoReturnable<InteractionResult> callbackInfo
    ) {
        /*
         * 通常位相ではバニラ処理へ一切干渉しない。
         */
        if (!TedDifferentPhaseManager
                .isInDifferentPhase(player)) {
            return;
        }

        /*
         * 使用許可タグのアイテムは、
         * ブロック使用経路ではなく
         * アイテム自身のuse()を実行する。
         */
        if (itemStack.is(
                ModItemTags.DIFFERENT_PHASE_USABLE
        )) {
            callbackInfo.setReturnValue(
                    itemStack.use(
                            level,
                            player,
                            hand
                    )
            );
            return;
        }

        BlockPos position =
                hitResult.getBlockPos();

        BlockState blockState =
                level.getBlockState(position);

        /*
         * それ以外のブロック操作・設置を禁止する。
         */
        if (!TedDifferentPhaseManager
                .canModifyBlock(
                        player,
                        blockState
                )) {

            callbackInfo.setReturnValue(
                    InteractionResult.FAIL
            );
        }
    }
}