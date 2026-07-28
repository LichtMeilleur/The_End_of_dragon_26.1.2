package com.licht_meilleur.the_end_of_dragon.mixin;

import com.licht_meilleur.the_end_of_dragon.world.phase
        .TedDifferentPhaseManager;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback
        .CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackDifferentPhaseMixin {

    /**
     * 別位相中のブロックに対するアイテム使用を禁止する。
     *
     * 対象例：
     * ・ブロック設置
     * ・火打石
     * ・クワ
     * ・シャベルによる道作成
     * ・骨粉
     * ・バケツの一部操作
     *
     * use()による弓、盾、食料、ポーション、
     * 位相パールなどは引き続き使用できる。
     */
    @Inject(
            method = "useOn",
            at = @At("HEAD"),
            cancellable = true
    )
    private void ted$preventDifferentPhaseUseOnBlock(
            UseOnContext context,
            CallbackInfoReturnable<InteractionResult> callbackInfo
    ) {
        Player player =
                context.getPlayer();

        if (player == null) {
            return;
        }

        if (!TedDifferentPhaseManager
                .canUseOnBlock(
                        player,
                        context
                )) {

            callbackInfo.setReturnValue(
                    InteractionResult.FAIL
            );
        }
    }
}