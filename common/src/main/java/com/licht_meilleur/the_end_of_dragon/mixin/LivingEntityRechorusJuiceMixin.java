package com.licht_meilleur.the_end_of_dragon.mixin;

import com.licht_meilleur.the_end_of_dragon.access.RechorusJuiceEntityAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityRechorusJuiceMixin {


    @Shadow
    protected abstract int decreaseAirSupply(
            int air
    );

    @Shadow
    protected abstract int increaseAirSupply(
            int air
    );
    @Shadow
    protected abstract double getEffectiveGravity();

    @Shadow
    protected abstract void travelInWater(
            Vec3 input,
            double baseGravity,
            boolean isFalling,
            double oldY
    );

    /*
     * 果汁水に入っている場合だけ、
     * バニラのtravelInWater()を直接使用する。
     */
    @Inject(
            method = "travel",
            at = @At("HEAD"),
            cancellable = true
    )
    private void ted$travelInRechorusJuice(
            Vec3 input,
            CallbackInfo callbackInfo
    ) {
        LivingEntity self =
                (LivingEntity) (Object) this;

        RechorusJuiceEntityAccess access =
                (RechorusJuiceEntityAccess) self;

        if (!access.ted$isInRechorusJuice()) {
            return;
        }

        /*
         * 念のため、バニラ水や溶岩処理と
         * 同時には実行しない。
         */
        if (self.isInWater()
                || self.isInLava()) {
            return;
        }

        boolean isFalling =
                self.getDeltaMovement().y
                        <= 0.0D;

        double oldY =
                self.getY();

        double gravity =
                this.getEffectiveGravity();

        this.travelInWater(
                input,
                gravity,
                isFalling,
                oldY
        );

        callbackInfo.cancel();
    }

    @Shadow
    protected boolean jumping;

    @Inject(
            method = "aiStep",
            at = @At("HEAD")
    )
    private void ted$jumpInRechorusJuice(
            CallbackInfo callbackInfo
    ) {
        LivingEntity self =
                (LivingEntity) (Object) this;

        RechorusJuiceEntityAccess access =
                (RechorusJuiceEntityAccess) self;

        if (!this.jumping
                || !access.ted$isInRechorusJuice()
                || !self.isAffectedByFluids()) {
            return;
        }

        /*
         * LivingEntity#jumpInLiquid()と同じ
         * 基本上昇量。
         */
        self.setDeltaMovement(
                self.getDeltaMovement()
                        .add(
                                0.0D,
                                0.04D,
                                0.0D
                        )
        );

    }
    @Inject(
            method = "aiStep",
            at = @At("TAIL")
    )
    private void ted$handleRechorusJuiceBreathing(
            CallbackInfo callbackInfo
    ) {
        LivingEntity self =
                (LivingEntity) (Object) this;

        RechorusJuiceEntityAccess access =
                (RechorusJuiceEntityAccess) self;

        /*
         * エンダーマンは果汁水に適応しているため、
         * 酸素を消費しない。
         */
        if (self instanceof EnderMan) {
            return;
        }

        boolean eyeInJuice =
                access.ted$isEyeInRechorusJuice();

        /*
         * 果汁水へ目まで浸かっている場合。
         */
        if (eyeInJuice) {

            /*
             * 水中呼吸がある間は酸素を減らさない。
             */
            if (self.hasEffect(
                    MobEffects.WATER_BREATHING
            )) {
                self.setAirSupply(
                        self.getMaxAirSupply()
                );

                return;
            }

            int nextAir =
                    this.decreaseAirSupply(
                            self.getAirSupply()
                    );

            self.setAirSupply(
                    nextAir
            );

            /*
             * バニラ同様、-20まで行ったら0へ戻し、
             * 2ダメージを与える。
             */
            if (nextAir <= -20
                    && self.level()
                    instanceof ServerLevel serverLevel) {

                self.setAirSupply(0);

                self.hurtServer(
                        serverLevel,
                        self.damageSources()
                                .drown(),
                        2.0F
                );
            }

            return;
        }

        /*
         * 通常水にも入っていないときだけ回復。
         *
         * 通常水中の呼吸はバニラ側へ任せる。
         */
        if (!self.isInWater()
                && self.getAirSupply()
                < self.getMaxAirSupply()) {

            self.setAirSupply(
                    this.increaseAirSupply(
                            self.getAirSupply()
                    )
            );
        }
    }

}