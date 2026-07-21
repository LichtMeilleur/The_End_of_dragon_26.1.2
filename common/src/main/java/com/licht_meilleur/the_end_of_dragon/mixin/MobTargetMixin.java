package com.licht_meilleur.the_end_of_dragon.mixin;

import com.licht_meilleur.the_end_of_dragon.world.enderman
        .TedEndermanFriendship;
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
     * TED討伐後、通常エンダーマンが
     * プレイヤーを攻撃対象に設定するのを阻止する。
     *
     * Mob全体へMixinするが、
     * 判定内部でEnderManだけに限定している。
     */
    @Inject(
            method = "setTarget",
            at = @At("HEAD"),
            cancellable = true
    )
    private void ted$preventFriendlyEndermanTarget(
            @Nullable LivingEntity target,
            CallbackInfo callbackInfo
    ) {
        if (target == null) {
            return;
        }

        Mob self =
                (Mob) (Object) this;

        if (TedEndermanFriendship
                .shouldPreventTarget(
                        self,
                        target
                )) {

            callbackInfo.cancel();
        }
    }
}