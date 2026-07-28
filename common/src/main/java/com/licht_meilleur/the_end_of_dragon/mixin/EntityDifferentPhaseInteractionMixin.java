package com.licht_meilleur.the_end_of_dragon.mixin;

import com.licht_meilleur.the_end_of_dragon.world.phase
        .TedDifferentPhaseManager;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback
        .CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityDifferentPhaseInteractionMixin {

    /**
     * 位相の異なるエンティティへの
     * 右クリック干渉を禁止する。
     */
    @Inject(
            method = "interact",
            at = @At("HEAD"),
            cancellable = true
    )
    private void ted$preventDifferentPhaseInteraction(
            Player player,
            InteractionHand hand,
            Vec3 location,
            CallbackInfoReturnable<InteractionResult> callbackInfo
    ) {
        Entity target =
                (Entity) (Object) this;

        if (!TedDifferentPhaseManager
                .canInteractWithEntity(
                        player,
                        target
                )) {

            callbackInfo.setReturnValue(
                    InteractionResult.FAIL
            );
        }
    }
}