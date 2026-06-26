package com.licht_meilleur.the_end_of_dragon.entity;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.util.GeckoLibUtil;


import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;


public class OldTheEndOfDragonEntity extends Monster implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public static final String ANIM_ORB_OF_ANNIHILATION = "animation.model.orb_of_annihilation_6tick_start_24tick_fire";
    public static final String ANIM_ROAR_OF_OBLITERATION = "animation.model.roar_of_obliteration_3tick_start";
    public static final String ANIM_FLAMES_OF_RAGNAROK = "animation.model.flames_of_ragnarok";
    public static final String ANIM_LIGHT_OF_DESTRUCTION = "animation.model.light_of_destruction_24tick_start";
    public static final String ANIM_PHOTON_BLASTER = "animation.model.photon_blaster_6tick_start";
    public static final String ANIM_BLASTER_TACKLE = "animation.model.blaster_tackle_12tick_start";
    public static final String ANIM_FLY_START = "animation.model.fly_start_12tick_start";
    public static final String ANIM_FLY = "animation.model.fly";
    public static final String ANIM_FLY_SHOT = "animation.model.fly_shot_6tick_start";
    public static final String ANIM_FLY_LEFT = "animation.model.fly_left";
    public static final String ANIM_FLY_RIGHT = "animation.model.fly_right";
    public static final String ANIM_FALL = "animation.model.fall";
    public static final String ANIM_LANDING = "animation.model.landing";
    public static final String ANIM_IDLE = "animation.model.idle";
    public static final String ANIM_WALK = "animation.model.walk";
    public static final String ANIM_SUPER_LANDING = "animation.model.super_landing";

    private int displayEntityId = -1;
    private int collisionEntityId = -1;

    private boolean spawnedHitBoxes;

    public OldTheEndOfDragonEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 500;
        this.setPersistenceRequired();
    }


    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {

        }
    }
/*
    private void tickChildEntities() {
        TheEndOfDragonDisplayEntity display = this.getChild(this.displayEntityId, TheEndOfDragonDisplayEntity.class);
        TheEndOfDragonCollisionEntity collision = this.getChild(this.collisionEntityId, TheEndOfDragonCollisionEntity.class);

        if (display == null) {
            display = new TheEndOfDragonDisplayEntity(
                    com.licht_meilleur.the_end_of_dragon.registry.ModEntities.THE_END_OF_DRAGON_DISPLAY,
                    this.level()
            );
            display.syncFromParent(this);
            this.level().addFreshEntity(display);
            this.displayEntityId = display.getId();
        }

        if (collision == null) {
            collision = new TheEndOfDragonCollisionEntity(
                    com.licht_meilleur.the_end_of_dragon.registry.ModEntities.THE_END_OF_DRAGON_COLLISION,
                    this.level()
            );
            collision.syncFromParent(this);
            this.level().addFreshEntity(collision);
            this.collisionEntityId = collision.getId();
        }

        display.syncFromParent(this);
        collision.syncFromParent(this);
    }

 */

    private <T> T getChild(int id, Class<T> type) {
        if (id == -1) {
            return null;
        }

        var entity = this.level().getEntity(id);
        if (type.isInstance(entity) && entity.isAlive()) {
            return type.cast(entity);
        }

        return null;
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!this.level().isClientSide()) {
            var display = this.level().getEntity(this.displayEntityId);
            if (display != null) display.discard();

            var collision = this.level().getEntity(this.collisionEntityId);
            if (collision != null) collision.discard();
        }

        super.remove(reason);
    }



    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 600.0D)
                .add(Attributes.ATTACK_DAMAGE, 20.0D)
                .add(Attributes.ARMOR, 20.0D)
                .add(Attributes.FOLLOW_RANGE, 128.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(
                "main_controller",
                0,
                state -> {
                    state.setAnimation(
                            RawAnimation.begin().thenLoop(this.getDebugAnimation())
                    );
                    return PlayState.CONTINUE;
                }
        ));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void die(net.minecraft.world.damagesource.DamageSource damageSource) {
        super.die(damageSource);

        if (!this.level().isClientSide() && this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            com.licht_meilleur.the_end_of_dragon.world.EndPortalSealHandler.unseal(serverLevel);
        }
    }

    private boolean allowCollisionPartDamage = false;

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(net.minecraft.world.entity.Entity entity) {
    }

    @Override
    protected void actuallyHurt(
            net.minecraft.server.level.ServerLevel serverLevel,
            net.minecraft.world.damagesource.DamageSource damageSource,
            float amount
    ) {
        if (!this.allowCollisionPartDamage) {
            return;
        }

        super.actuallyHurt(serverLevel, damageSource, amount);
    }

    public boolean hurtFromCollisionPart(
            net.minecraft.server.level.ServerLevel serverLevel,
            net.minecraft.world.damagesource.DamageSource source,
            float amount
    ) {
        this.allowCollisionPartDamage = true;

        try {
            this.actuallyHurt(serverLevel, source, amount);
            return true;
        } finally {
            this.allowCollisionPartDamage = false;
        }
    }

    private static final EntityDataAccessor<String> DATA_DEBUG_ANIMATION =
            SynchedEntityData.defineId(OldTheEndOfDragonEntity.class, EntityDataSerializers.STRING);

    public void setDebugAnimation(String animationName) {
        this.entityData.set(DATA_DEBUG_ANIMATION, animationName);
    }

    public String getDebugAnimation() {
        return this.entityData.get(DATA_DEBUG_ANIMATION);
    }
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_DEBUG_ANIMATION, ANIM_IDLE);
    }
}