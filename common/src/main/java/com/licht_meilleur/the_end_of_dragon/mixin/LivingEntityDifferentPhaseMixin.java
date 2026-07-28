package com.licht_meilleur.the_end_of_dragon.mixin;

import com.licht_meilleur.the_end_of_dragon.world.phase
        .TedDifferentPhaseManager;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
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
     * 別位相中のダメージを制御する。
     *
     * 許可するもの：
     * ・別位相にいる攻撃者からのダメージ
     * ・独自酸素消費による溺水ダメージ
     *
     * 無効にするもの：
     * ・通常位相のEntityからの攻撃
     * ・炎、溶岩、落下、窒息、毒などの環境ダメージ
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

        /*
         * 対象が別位相でなければ、
         * バニラの処理をそのまま続行する。
         */
        if (!TedDifferentPhaseManager
                .isInDifferentPhase(
                        target
                )) {
            return;
        }

        /*
         * 位相中の酸素切れとして使用する
         * 溺水ダメージだけは許可する。
         */
        if (damageSource.is(
                DamageTypes.DROWN
        )) {
            return;
        }

        Entity attacker =
                damageSource.getEntity();

        /*
         * 飛び道具に所有者が存在しない場合は
         * 矢などの直接Entityを確認する。
         */
        if (attacker == null) {
            attacker =
                    damageSource.getDirectEntity();
        }

        /*
         * 攻撃元Entityを持たないものは、
         * 炎、落下、毒、窒息などの環境ダメージとして
         * 別位相中は無効化する。
         */
        if (attacker == null) {
            callbackInfo.setReturnValue(
                    false
            );
            return;
        }

        /*
         * 攻撃元と対象の位相が異なる場合も
         * ダメージを無効化する。
         */
        if (!TedDifferentPhaseManager
                .canInteract(
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