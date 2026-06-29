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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class TedVfxEntity extends Entity implements GeoEntity {
    private static final EntityDataAccessor<Integer> DATA_VFX_TYPE =
            SynchedEntityData.defineId(TedVfxEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Float> DATA_SCALE =
            SynchedEntityData.defineId(TedVfxEntity.class, EntityDataSerializers.FLOAT);

    private static final EntityDataAccessor<Float> DATA_LENGTH =
            SynchedEntityData.defineId(TedVfxEntity.class, EntityDataSerializers.FLOAT);

    private static final EntityDataAccessor<Integer> DATA_MAX_AGE =
            SynchedEntityData.defineId(TedVfxEntity.class, EntityDataSerializers.INT);

    // direction型：光弾/光球など
    private static final EntityDataAccessor<Float> DATA_ROT_X =
            SynchedEntityData.defineId(TedVfxEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_ROT_Y =
            SynchedEntityData.defineId(TedVfxEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_ROT_Z =
            SynchedEntityData.defineId(TedVfxEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_ROT_W =
            SynchedEntityData.defineId(TedVfxEntity.class, EntityDataSerializers.FLOAT);

    // basis型：ジェット/レーザーなど。親OBBの回転継承用
    private static final EntityDataAccessor<Float> DATA_FORWARD_X =
            SynchedEntityData.defineId(TedVfxEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_FORWARD_Y =
            SynchedEntityData.defineId(TedVfxEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_FORWARD_Z =
            SynchedEntityData.defineId(TedVfxEntity.class, EntityDataSerializers.FLOAT);

    private static final EntityDataAccessor<Float> DATA_UP_X =
            SynchedEntityData.defineId(TedVfxEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_UP_Y =
            SynchedEntityData.defineId(TedVfxEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_UP_Z =
            SynchedEntityData.defineId(TedVfxEntity.class, EntityDataSerializers.FLOAT);

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

        builder.define(DATA_ROT_X, 0.0F);
        builder.define(DATA_ROT_Y, 0.0F);
        builder.define(DATA_ROT_Z, 0.0F);
        builder.define(DATA_ROT_W, 1.0F);

        builder.define(DATA_FORWARD_X, 0.0F);
        builder.define(DATA_FORWARD_Y, 0.0F);
        builder.define(DATA_FORWARD_Z, 1.0F);

        builder.define(DATA_UP_X, 0.0F);
        builder.define(DATA_UP_Y, 1.0F);
        builder.define(DATA_UP_Z, 0.0F);
    }

    public void setup(TedVfxType type, float scale, float length, int maxAge) {
        this.entityData.set(DATA_VFX_TYPE, type.ordinal());
        this.entityData.set(DATA_SCALE, scale);
        this.entityData.set(DATA_LENGTH, length);
        this.entityData.set(DATA_MAX_AGE, maxAge);
    }

    public void updateVfx(float scale, float length) {
        this.entityData.set(DATA_SCALE, scale);
        this.entityData.set(DATA_LENGTH, length);
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

    public void setVfxRotationQuat(org.joml.Quaternionf q) {
        this.entityData.set(DATA_ROT_X, q.x);
        this.entityData.set(DATA_ROT_Y, q.y);
        this.entityData.set(DATA_ROT_Z, q.z);
        this.entityData.set(DATA_ROT_W, q.w);
    }

    public org.joml.Quaternionf getVfxRotationQuat() {
        return new org.joml.Quaternionf(
                this.entityData.get(DATA_ROT_X),
                this.entityData.get(DATA_ROT_Y),
                this.entityData.get(DATA_ROT_Z),
                this.entityData.get(DATA_ROT_W)
        );
    }



    public void setBasis(Vec3 forward, Vec3 up) {
        Vec3 f = safeNormalize(forward, new Vec3(0, 0, 1));
        Vec3 u = safeNormalize(up, new Vec3(0, 1, 0));

        this.entityData.set(DATA_FORWARD_X, (float) f.x);
        this.entityData.set(DATA_FORWARD_Y, (float) f.y);
        this.entityData.set(DATA_FORWARD_Z, (float) f.z);

        this.entityData.set(DATA_UP_X, (float) u.x);
        this.entityData.set(DATA_UP_Y, (float) u.y);
        this.entityData.set(DATA_UP_Z, (float) u.z);
    }

    public Vec3 getForward() {
        return new Vec3(
                this.entityData.get(DATA_FORWARD_X),
                this.entityData.get(DATA_FORWARD_Y),
                this.entityData.get(DATA_FORWARD_Z)
        );
    }

    public Vec3 getUp() {
        return new Vec3(
                this.entityData.get(DATA_UP_X),
                this.entityData.get(DATA_UP_Y),
                this.entityData.get(DATA_UP_Z)
        );
    }

    private static Vec3 safeNormalize(Vec3 v, Vec3 fallback) {
        if (v == null || v.lengthSqr() < 1.0E-6D) {
            return fallback;
        }
        return v.normalize();
    }

    @Override
    public void tick() {
        super.tick();

        this.noPhysics = true;
        this.setNoGravity(true);

        if (!this.level().isClientSide() && this.tickCount >= this.getMaxAge()) {
            this.discard();
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

        output.putFloat("RotX", this.entityData.get(DATA_ROT_X));
        output.putFloat("RotY", this.entityData.get(DATA_ROT_Y));
        output.putFloat("RotZ", this.entityData.get(DATA_ROT_Z));
        output.putFloat("RotW", this.entityData.get(DATA_ROT_W));

        output.putFloat("ForwardX", this.entityData.get(DATA_FORWARD_X));
        output.putFloat("ForwardY", this.entityData.get(DATA_FORWARD_Y));
        output.putFloat("ForwardZ", this.entityData.get(DATA_FORWARD_Z));

        output.putFloat("UpX", this.entityData.get(DATA_UP_X));
        output.putFloat("UpY", this.entityData.get(DATA_UP_Y));
        output.putFloat("UpZ", this.entityData.get(DATA_UP_Z));


    }

    @Override
    protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput input) {
        this.entityData.set(DATA_VFX_TYPE, input.getIntOr("VfxType", TedVfxType.LIGHT_PROJECTILE.ordinal()));
        this.entityData.set(DATA_SCALE, input.getFloatOr("VfxScale", 1.0F));
        this.entityData.set(DATA_LENGTH, input.getFloatOr("VfxLength", 1.0F));
        this.entityData.set(DATA_MAX_AGE, input.getIntOr("MaxAge", 40));

        this.entityData.set(DATA_ROT_X, input.getFloatOr("RotX", 0.0F));
        this.entityData.set(DATA_ROT_Y, input.getFloatOr("RotY", 0.0F));
        this.entityData.set(DATA_ROT_Z, input.getFloatOr("RotZ", 0.0F));
        this.entityData.set(DATA_ROT_W, input.getFloatOr("RotW", 1.0F));

        this.entityData.set(DATA_FORWARD_X, input.getFloatOr("ForwardX", 0.0F));
        this.entityData.set(DATA_FORWARD_Y, input.getFloatOr("ForwardY", 0.0F));
        this.entityData.set(DATA_FORWARD_Z, input.getFloatOr("ForwardZ", 1.0F));

        this.entityData.set(DATA_UP_X, input.getFloatOr("UpX", 0.0F));
        this.entityData.set(DATA_UP_Y, input.getFloatOr("UpY", 1.0F));
        this.entityData.set(DATA_UP_Z, input.getFloatOr("UpZ", 0.0F));
    }

    @Override
    public boolean hurtServer(
            net.minecraft.server.level.ServerLevel level,
            net.minecraft.world.damagesource.DamageSource source,
            float amount
    ) {
        return false;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 256.0D * 256.0D;
    }


}