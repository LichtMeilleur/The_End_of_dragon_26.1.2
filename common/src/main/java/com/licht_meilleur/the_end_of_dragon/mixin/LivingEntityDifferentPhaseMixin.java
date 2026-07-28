package com.licht_meilleur.the_end_of_dragon.mixin;

import com.licht_meilleur.the_end_of_dragon.world.phase
        .TedDifferentPhaseManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback
        .CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityDifferentPhaseMixin {

    /**
     * 攻撃元と攻撃対象の位相が異なる場合、
     * ダメージ処理そのものを無効化する。
     */
    @Inject(
            method = "hurtServer",
            at = @At("HEAD"),
            cancellable = true
    )
    private void ted$preventDifferentPhaseDamage(
            ServerLevel level,
            DamageSource damageSource,
            float damageAmount,
            CallbackInfoReturnable<Boolean> callbackInfo
    ) {
        LivingEntity target =
                (LivingEntity) (Object) this;

        Entity attacker =
                damageSource.getEntity();

        /*
         * getEntity()には通常、飛び道具の所有者が入る。
         *
         * ディスペンサーなど所有者が存在しない場合は、
         * 矢などの直接攻撃元を使って判定する。
         */
        if (attacker == null) {
            attacker =
                    damageSource.getDirectEntity();
        }

        /*
         * 落下、窒息、溺水、炎など、
         * Entityを攻撃元に持たない環境ダメージは
         * ここでは通常どおり処理する。
         */
        if (attacker == null) {
            return;
        }

        if (!TedDifferentPhaseManager.canInteract(
                attacker,
                target
        )) {
            callbackInfo.setReturnValue(
                    false
            );
        }
    }

    /**
     * 別位相中は、空気中にいても
     * 酸素ゲージを自然回復させない。
     */
    @Inject(
            method = "increaseAirSupply",
            at = @At("HEAD"),
            cancellable = true
    )
    private void ted$preventAirRecoveryInDifferentPhase(
            int currentAir,
            CallbackInfoReturnable<Integer> callbackInfo
    ) {
        LivingEntity livingEntity =
                (LivingEntity) (Object) this;

        if (livingEntity instanceof Player player
                && TedDifferentPhaseManager
                .isInDifferentPhase(
                        player
                )) {

            callbackInfo.setReturnValue(
                    currentAir
            );
        }
    }
}