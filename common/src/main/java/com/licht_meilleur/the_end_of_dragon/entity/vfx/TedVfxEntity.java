package com.licht_meilleur.the_end_of_dragon.entity.vfx;

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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class TedVfxEntity extends Entity implements GeoEntity {
    private static final EntityDataAccessor<Integer> DATA_VFX_TYPE =
            SynchedEntityData.defineId(TedVfxEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Float> DATA_SCALE =
            SynchedEntityData.defineId(TedVfxEntity.class, EntityDataSerializers.FLOAT);

    private static final EntityDataAccessor<Float> DATA_LENGTH =
            SynchedEntityData.defineId(TedVfxEntity.class, EntityDataSerializers.FLOAT);

    private static final EntityDataAccessor<Integer> DATA_MAX_AGE =
            SynchedEntityData.defineId(TedVfxEntity.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public TedVfxEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_VFX_TYPE, TedVfxType.LIGHT_PROJECTILE.ordinal());
        builder.define(DATA_SCALE, 1.0F);
        builder.define(DATA_LENGTH, 1.0F);
        builder.define(DATA_MAX_AGE, 40);
    }

    public void setup(TedVfxType type, float scale, float length, int maxAge) {
        this.entityData.set(DATA_VFX_TYPE, type.ordinal());
        this.entityData.set(DATA_SCALE, scale);
        this.entityData.set(DATA_LENGTH, length);
        this.entityData.set(DATA_MAX_AGE, maxAge);
    }

    public TedVfxType getVfxType() {
        return TedVfxType.byId(this.entityData.get(DATA_VFX_TYPE));
    }

    public float getVfxScale() {
        return this.entityData.get(DATA_SCALE);
    }

    public float getVfxLength() {
        return this.entityData.get(DATA_LENGTH);
    }

    public int getMaxAge() {
        return this.entityData.get(DATA_MAX_AGE);
    }

    public float getAlpha(float partialTick) {
        float age = this.tickCount + partialTick;
        float max = Math.max(1.0F, this.getMaxAge());

        float t = age / max;
        if (t < 0.15F) {
            return t / 0.15F;
        }

        return Math.max(0.0F, 1.0F - t);
    }

    @Override
    public void tick() {
        super.tick();

        this.noPhysics = true;
        this.setNoGravity(true);

        if (!this.level().isClientSide()) {
            if (this.tickCount >= this.getMaxAge()) {
                this.discard();
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(
                "main_controller",
                0,
                state -> {
                    state.setAnimation(
                            RawAnimation.begin().thenLoop(this.getVfxType().animationName)
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
    protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput output) {
        output.putInt("VfxType", this.entityData.get(DATA_VFX_TYPE));
        output.putFloat("VfxScale", this.entityData.get(DATA_SCALE));
        output.putFloat("VfxLength", this.entityData.get(DATA_LENGTH));
        output.putInt("MaxAge", this.entityData.get(DATA_MAX_AGE));
    }

    @Override
    protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput input) {
        this.entityData.set(DATA_VFX_TYPE, input.getIntOr("VfxType", TedVfxType.LIGHT_PROJECTILE.ordinal()));
        this.entityData.set(DATA_SCALE, input.getFloatOr("VfxScale", 1.0F));
        this.entityData.set(DATA_LENGTH, input.getFloatOr("VfxLength", 1.0F));
        this.entityData.set(DATA_MAX_AGE, input.getIntOr("MaxAge", 40));
    }

    @Override
    public boolean hurtServer(
            net.minecraft.server.level.ServerLevel level,
            net.minecraft.world.damagesource.DamageSource source,
            float amount
    ) {
        return false;
    }
}