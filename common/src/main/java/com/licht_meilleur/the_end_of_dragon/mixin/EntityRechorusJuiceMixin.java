package com.licht_meilleur.the_end_of_dragon.mixin;

import com.licht_meilleur.the_end_of_dragon.access.RechorusJuiceEntityAccess;
import com.licht_meilleur.the_end_of_dragon.registry.ModFluidTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityFluidInteraction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(Entity.class)
public abstract class EntityRechorusJuiceMixin
        implements RechorusJuiceEntityAccess {

    /*
     * バニラでは最初に
     *
     * WATER
     * LAVA
     *
     * だけが登録されている。
     */
    @Shadow
    @Final
    @Mutable
    private EntityFluidInteraction fluidInteraction;

    /*
     * Entityの生成完了後、追跡対象へ
     * Rechorus Juiceを追加する。
     */
    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    private void ted$addRechorusJuiceTracker(
            CallbackInfo callbackInfo
    ) {
        this.fluidInteraction =
                new EntityFluidInteraction(
                        Set.of(
                                FluidTags.WATER,
                                FluidTags.LAVA,
                                ModFluidTags.RECHORUS_JUICE
                        )
                );
    }

    /*
     * バニラの水・溶岩更新が終わったあと、
     * 果汁水の流れをEntityへ適用する。
     */
    @Inject(
            method = "updateFluidInteraction",
            at = @At("TAIL")
    )
    private void ted$applyRechorusJuiceCurrent(
            CallbackInfoReturnable<Boolean> callbackInfo
    ) {
        Entity self =
                (Entity) (Object) this;

        if (!ted$isInRechorusJuice()) {
            return;
        }

        /*
         * 果汁水に触れている間は、
         * 落下距離を蓄積しない。
         */
        self.resetFallDistance();

        if (!self.isPushedByFluid()) {
            return;
        }

        /*
         * 通常水と同じ流れの強さ。
         *
         * Entity.WATER_FLOW_SCALE = 0.014
         */
        this.fluidInteraction.applyCurrentTo(
                ModFluidTags.RECHORUS_JUICE,
                self,
                0.006D
        );
    }



    @Override
    @Unique
    public boolean ted$isInRechorusJuice() {
        return this.fluidInteraction.isInFluid(
                ModFluidTags.RECHORUS_JUICE
        );
    }

    @Override
    @Unique
    public boolean ted$isEyeInRechorusJuice() {
        return this.fluidInteraction.isEyeInFluid(
                ModFluidTags.RECHORUS_JUICE
        );
    }

    @Override
    @Unique
    public double ted$getRechorusJuiceHeight() {
        return this.fluidInteraction.getFluidHeight(
                ModFluidTags.RECHORUS_JUICE
        );
    }

    @Inject(
            method = "updateSwimming",
            at = @At("HEAD"),
            cancellable = true
    )
    private void ted$updateRechorusJuiceSwimming(
            CallbackInfo callbackInfo
    ) {
        Entity self =
                (Entity) (Object) this;

        boolean inJuice =
                ted$isInRechorusJuice();

        boolean eyeInJuice =
                ted$isEyeInRechorusJuice();

        if (!inJuice && !eyeInJuice) {
            return;
        }

        /*
         * 動作確認用：
         * 目まで果汁水に入ったら必ず泳ぎ姿勢にする。
         */
        self.setSwimming(
                eyeInJuice
                        && inJuice
                        && !self.isPassenger()
        );

        callbackInfo.cancel();
    }
}