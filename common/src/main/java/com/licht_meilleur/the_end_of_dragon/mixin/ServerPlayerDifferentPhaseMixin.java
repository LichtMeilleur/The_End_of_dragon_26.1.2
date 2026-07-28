package com.licht_meilleur.the_end_of_dragon.mixin;

import com.licht_meilleur.the_end_of_dragon.world.phase
        .TedDifferentPhaseManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback
        .CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerDifferentPhaseMixin {

    /**
     * 死亡した時点で、
     * 永続位相と一時位相の両方を解除する。
     */
    @Inject(
            method = "die",
            at = @At("HEAD")
    )
    private void ted$leaveDifferentPhaseOnDeath(
            DamageSource damageSource,
            CallbackInfo callbackInfo
    ) {
        ServerPlayer player =
                (ServerPlayer) (Object) this;

        TedDifferentPhaseManager
                .leaveAllPhases(
                        player
                );
    }
}