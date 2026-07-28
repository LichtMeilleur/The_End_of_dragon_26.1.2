package com.licht_meilleur.the_end_of_dragon.mixin;

import com.licht_meilleur.the_end_of_dragon.registry.ModItemTags;
import com.licht_meilleur.the_end_of_dragon.registry.ModItems;
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
        BlockPos position =
                hitResult.getBlockPos();

        BlockState blockState =
                level.getBlockState(
                        position
                );

        if (itemStack.is(
                ModItemTags.DIFFERENT_PHASE_USABLE
        )) {
            /*
             * 位相関連アイテムは、
             * ブロックを向いていても通常処理を続ける。
             */
            return;
        }

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