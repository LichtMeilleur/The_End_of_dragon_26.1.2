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
    /**
     * 異なる位相間のダメージを禁止する。
     *
     * 別位相側から通常位相への攻撃と、
     * 通常位相側から別位相への攻撃の両方を防ぐ。
     *
     * 別位相中の対象については、
     * 独自酸素消費に使用する溺水ダメージだけ許可する。
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
         * getEntity()は、矢などであれば通常は
         * 発射したプレイヤーやMobを返す。
         *
         * 所有者が取得できない場合は、
         * 直接ダメージを発生させたEntityを確認する。
         */
        if (attacker == null) {
            attacker =
                    damageSource.getDirectEntity();
        }

        /*
         * 攻撃元Entityが存在し、
         * 攻撃元と対象の位相が異なる場合は
         * 双方向ともダメージを禁止する。
         *
         * これにより、
         * ・別位相プレイヤー → 通常Mob
         * ・通常Mob → 別位相プレイヤー
         * の両方が無効になる。
         */
        if (attacker != null
                && !TedDifferentPhaseManager.canInteract(
                attacker,
                target
        )) {

            callbackInfo.setReturnValue(
                    false
            );
            return;
        }

        /*
         * 対象が通常位相なら、ここから先の
         * 環境ダメージ制御は不要。
         *
         * 攻撃元との位相比較はすでに上で済んでいる。
         */
        if (!TedDifferentPhaseManager
                .isInDifferentPhase(
                        target
                )) {
            return;
        }

        /*
         * 別位相中の酸素切れとして使用する
         * 溺水ダメージだけは許可する。
         */
        if (damageSource.is(
                DamageTypes.DROWN
        )) {
            return;
        }

        /*
         * 攻撃元Entityを持たないダメージは、
         * 炎、溶岩、落下、窒息、毒などの
         * 環境ダメージとして無効化する。
         */
        if (attacker == null) {
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