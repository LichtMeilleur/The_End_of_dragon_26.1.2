package com.licht_meilleur.the_end_of_dragon.mixin;

import com.licht_meilleur.the_end_of_dragon.world.phase
        .TedDifferentPhaseManager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback
        .CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerDifferentPhaseMixin {

    /**
     * 別位相中のプレイヤーを、
     * 標準Mobの索敵対象から除外する。
     *
     * スペクテイターそのものにはしないため、
     * 歩行・重力・ブロックとの衝突は通常どおり残る。
     */
    @Inject(
            method = "canBeSeenAsEnemy",
            at = @At("HEAD"),
            cancellable = true
    )
    private void ted$hideDifferentPhasePlayerFromEnemies(
            CallbackInfoReturnable<Boolean> callbackInfo
    ) {
        Player player =
                (Player) (Object) this;

        if (TedDifferentPhaseManager
                .isInDifferentPhase(
                        player
                )) {

            callbackInfo.setReturnValue(
                    false
            );
        }
    }

}