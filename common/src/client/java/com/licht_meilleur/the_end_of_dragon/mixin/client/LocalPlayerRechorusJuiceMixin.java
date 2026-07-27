package com.licht_meilleur.the_end_of_dragon.mixin.client;

import com.licht_meilleur.the_end_of_dragon.access
        .RechorusJuiceEntityAccess;
import com.llamalad7.mixinextras.injector
        .ModifyExpressionValue;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerRechorusJuiceMixin {

    /*
     * LocalPlayer#canStartSprinting()内にある
     * isUnderWater()の結果へ、
     * 果汁水に目が入っている判定を追加する。
     *
     * このメソッド内にはisUnderWater()が2か所あり、
     * ordinalを指定しないため両方が対象になる。
     */
    @ModifyExpressionValue(
            method = "canStartSprinting",
            at = @At(
                    value = "INVOKE",
                    target =
                            "Lnet/minecraft/client/player/LocalPlayer;"
                                    + "isUnderWater()Z"
            )
    )
    private boolean ted$allowSprintingInRechorusJuice(
            boolean vanillaUnderWater
    ) {
        RechorusJuiceEntityAccess access =
                (RechorusJuiceEntityAccess) (Object) this;

        return vanillaUnderWater
                || access.ted$isEyeInRechorusJuice();
    }
}