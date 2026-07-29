package com.licht_meilleur.the_end_of_dragon.entity.enderman.village;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.UUID;

public abstract class TedVillageEndermanEntity
        extends PathfinderMob
        implements GeoEntity {

    private static final RawAnimation IDLE_ANIMATION =
            RawAnimation.begin()
                    .thenLoop(
                            "animation.model.idle"
                    );

    private static final RawAnimation WALK_ANIMATION =
            RawAnimation.begin()
                    .thenLoop(
                            "animation.model.walk"
                    );

    /*
     * 現在、このNPCの画面を開いているプレイヤー。
     * 保存する必要はないためNBTには書き込まない。
     */
    private UUID interactingPlayerUuid;

    private final AnimatableInstanceCache animationCache =
            GeckoLibUtil.createInstanceCache(this);

    protected TedVillageEndermanEntity(
            EntityType<? extends PathfinderMob> entityType,
            Level level
    ) {
        super(entityType, level);

        this.setPersistenceRequired();
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(
                0,
                new FloatGoal(this)
        );

        this.goalSelector.addGoal(
                5,
                new WaterAvoidingRandomStrollGoal(
                        this,
                        0.65D,
                        0.001F
                )
        );

        this.goalSelector.addGoal(
                6,
                new LookAtPlayerGoal(
                        this,
                        Player.class,
                        10.0F
                )
        );

        this.goalSelector.addGoal(
                7,
                new RandomLookAroundGoal(this)
        );
    }

    public static AttributeSupplier.Builder
    createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(
                        Attributes.MAX_HEALTH,
                        40.0D
                )
                .add(
                        Attributes.ARMOR,
                        6.0D
                )
                .add(
                        Attributes.MOVEMENT_SPEED,
                        0.25D
                )
                .add(
                        Attributes.FOLLOW_RANGE,
                        32.0D
                )
                .add(
                        Attributes.KNOCKBACK_RESISTANCE,
                        0.25D
                );
    }

    @Override
    public void registerControllers(
            AnimatableManager.ControllerRegistrar controllers
    ) {
        controllers.add(
                new AnimationController<>(
                        "movement",
                        4,
                        state -> {
                            if (state.isMoving()) {
                                state.controller()
                                        .setAnimation(
                                                WALK_ANIMATION
                                        );
                            } else {
                                state.controller()
                                        .setAnimation(
                                                IDLE_ANIMATION
                                        );
                            }

                            return PlayState.CONTINUE;
                        }
                )
        );
    }

    @Override
    public AnimatableInstanceCache
    getAnimatableInstanceCache() {
        return this.animationCache;
    }

    @Override
    public boolean removeWhenFarAway(
            double distanceToClosestPlayer
    ) {
        return false;
    }

    @Override
    public boolean isPersistenceRequired() {
        return true;
    }

    /**
     * NPCとの画面操作を開始する。
     */
    public void beginMenuInteraction(
            ServerPlayer player
    ) {
        if (player == null
                || player.isRemoved()
                || !player.isAlive()) {

            return;
        }

        this.interactingPlayerUuid =
                player.getUUID();

        this.getNavigation().stop();

        /*
         * すでに残っている水平方向の移動を止める。
         * 落下中の場合に備えてY速度は維持する。
         */
        this.setDeltaMovement(
                0.0D,
                this.getDeltaMovement().y,
                0.0D
        );
    }

    /**
     * NPCとの画面操作を終了する。
     */
    public void endMenuInteraction(
            Player player
    ) {
        if (player == null
                || this.interactingPlayerUuid == null) {

            return;
        }

        /*
         * このNPCを操作していた本人からの
         * 終了通知だけを受け付ける。
         */
        if (this.interactingPlayerUuid.equals(
                player.getUUID()
        )) {
            this.interactingPlayerUuid = null;
        }
    }

    /**
     * 現在、誰かがこのNPCの画面を開いているか。
     */
    public boolean isMenuInteractionActive() {
        return this.interactingPlayerUuid != null;
    }

    /**
     * 画面操作中のプレイヤーか。
     */
    public boolean isMenuInteractionPlayer(
            Player player
    ) {
        return player != null
                && this.interactingPlayerUuid != null
                && this.interactingPlayerUuid.equals(
                player.getUUID()
        );
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()
                || this.interactingPlayerUuid == null) {

            return;
        }

        if (!(this.level()
                instanceof ServerLevel serverLevel)) {

            this.interactingPlayerUuid = null;
            return;
        }

        Player foundPlayer =
                serverLevel.getPlayerByUUID(
                        this.interactingPlayerUuid
                );

        if (!(foundPlayer
                instanceof ServerPlayer player)) {

            this.interactingPlayerUuid = null;
            return;
        }

        /*
         * ログアウト、死亡、別ディメンションへの移動、
         * 距離超過時には自動解除する。
         */
        if (player == null
                || player.isRemoved()
                || !player.isAlive()
                || player.level() != this.level()
                || player.distanceToSqr(this)
                > 8.0D * 8.0D) {

            this.interactingPlayerUuid = null;
            return;
        }

        /*
         * 移動Goalが動き始めても毎tick停止させる。
         */
        this.getNavigation().stop();

        this.setDeltaMovement(
                0.0D,
                this.getDeltaMovement().y,
                0.0D
        );

        /*
         * 操作中のプレイヤーを見る。
         */
        this.getLookControl().setLookAt(
                player,
                30.0F,
                30.0F
        );
    }
}