package com.licht_meilleur.the_end_of_dragon.mixin.client;

import com.licht_meilleur.the_end_of_dragon.client.phase
        .TedDifferentPhaseClientState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback
        .CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherDifferentPhaseMixin {

    @Inject(
            method = "shouldRender",
            at = @At("HEAD"),
            cancellable = true
    )
    private <E extends Entity>
    void ted$hidePlayerInOtherPhase(
            E entity,
            Frustum culler,
            double camX,
            double camY,
            double camZ,
            CallbackInfoReturnable<Boolean> callbackInfo
    ) {
        /*
         * プレイヤー以外の描画には干渉しない。
         */
        if (!(entity instanceof Player targetPlayer)) {
            return;
        }

        Minecraft minecraft =
                Minecraft.getInstance();

        Player viewer =
                minecraft.player;

        if (viewer == null) {
            return;
        }

        /*
         * 自分自身は常に描画する。
         *
         * 三人称では本人のモデルを表示し、
         * 一人称の腕と手持ちアイテムにも干渉しない。
         */
        if (targetPlayer == viewer
                || targetPlayer.getUUID()
                .equals(viewer.getUUID())) {
            return;
        }

        boolean viewerInDifferentPhase =
                TedDifferentPhaseClientState
                        .isInDifferentPhase(
                                viewer
                        );

        boolean targetInDifferentPhase =
                TedDifferentPhaseClientState
                        .isInDifferentPhase(
                                targetPlayer
                        );

        /*
         * 観察者と対象の位相が異なる場合は、
         * 対象を描画対象から除外する。
         */
        if (!viewerInDifferentPhase
                && targetInDifferentPhase) {

            callbackInfo.setReturnValue(
                    false
            );
        }
    }
}