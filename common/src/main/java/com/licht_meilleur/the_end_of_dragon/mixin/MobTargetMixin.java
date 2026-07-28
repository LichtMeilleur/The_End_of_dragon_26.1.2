package com.licht_meilleur.the_end_of_dragon.mixin;

import com.licht_meilleur.the_end_of_dragon.world.enderman
        .TedEndermanFriendship;
import com.licht_meilleur.the_end_of_dragon.world.phase
        .TedDifferentPhaseManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback
        .CallbackInfo;

@Mixin(Mob.class)
public abstract class MobTargetMixin {

    /**
     * Mobが攻撃対象を設定する直前に、
     * エンダーマン友好化と位相の両方を確認する。
     */
    @Inject(
            method = "setTarget",
            at = @At("HEAD"),
            cancellable = true
    )
    private void ted$preventInvalidTarget(
            @Nullable LivingEntity target,
            CallbackInfo callbackInfo
    ) {
        if (target == null) {
            return;
        }

        Mob self =
                (Mob) (Object) this;

        /*
         * TED討伐後、通常エンダーマンが
         * プレイヤーを攻撃対象にするのを防ぐ。
         */
        if (TedEndermanFriendship
                .shouldPreventTarget(
                        self,
                        target
                )) {

            callbackInfo.cancel();
            return;
        }

        /*
         * Mobは現時点では通常位相として扱う。
         * そのため、別位相プレイヤーを
         * 攻撃対象に設定できない。
         */
        if (!TedDifferentPhaseManager
                .canInteract(
                        self,
                        target
                )) {

            callbackInfo.cancel();
        }
    }

    @Inject(
            method = "serverAiStep",
            at = @At("HEAD")
    )
    private void ted$clearDifferentPhaseTarget(
            CallbackInfo callbackInfo
    ) {
        Mob self =
                (Mob) (Object) this;

        LivingEntity target =
                self.getTarget();

        if (target == null) {
            return;
        }

        if (!TedDifferentPhaseManager
                .canInteract(
                        self,
                        target
                )) {

            self.setTarget(
                    null
            );
        }
    }
}