package com.licht_meilleur.the_end_of_dragon.entity.vfx;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.util.GeckoLibUtil;
import com.licht_meilleur.the_end_of_dragon.entity.projectile.TedProjectileSpec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
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

    private double hitRadius = 1.0D;
    private float damage = 1.0F;
    private boolean destroyBlocks = false;
    private double destroyRadius = 0.0D;

    public TedVfxEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    private int ownerEntityId = -1;
    private int noHitTicks = 5;

    public void setProjectileOwner(Entity owner) {
        this.ownerEntityId = owner.getId();
    }

    private boolean isOwnerOrDragonPart(Entity entity) {
        if (entity == this) return true;
        if (entity.getId() == this.ownerEntityId) return true;

        return entity instanceof com.licht_meilleur.the_end_of_dragon.entity.TheEndOfDragonCoreEntity
                || entity instanceof com.licht_meilleur.the_end_of_dragon.entity.TheEndOfDragonEntity
                || entity instanceof com.licht_meilleur.the_end_of_dragon.entity.vfx.TedVfxEntity;
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


    //爆発判定
    private boolean createExplosion = false;
    private float explosionRadius = 0.0F;
    private int explosionParticleCount = 0;


    public void setup(TedProjectileSpec spec) {
        setup(
                spec.type(),
                spec.scale(),
                1.0F,
                spec.maxAge()
        );

        this.hitRadius = spec.hitRadius();
        this.damage = spec.damage();
        this.destroyBlocks = spec.destroyBlocks();
        this.destroyRadius = spec.destroyRadius();
        this.createExplosion = spec.createExplosion();
        this.explosionRadius = spec.explosionRadius();
        this.explosionParticleCount = spec.explosionParticleCount();
    }

    public double getHitRadius() {
        return hitRadius;
    }

    public float getDamage() {
        return damage;
    }

    public boolean isDestroyBlocks() {
        return destroyBlocks;
    }

    public double getDestroyRadius() {
        return destroyRadius;
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

        if (!this.level().isClientSide()) {
            Vec3 motion = this.getDeltaMovement();

            if (motion.lengthSqr() > 1.0E-7D) {
                this.setPos(
                        this.getX() + motion.x,
                        this.getY() + motion.y,
                        this.getZ() + motion.z
                );
            }
            tickProjectileCollision();
            tickOrbCollision();

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

    private void tickOrbCollision() {
        if (this.level().isClientSide()) {
            return;
        }

        Vec3 motion = this.getDeltaMovement();
        if (motion.lengthSqr() < 1.0E-7D) {
            return; // チャージ中の静止オーブは攻撃判定なし
        }

        if (this.getVfxType() != TedVfxType.ORB_OF_ANIHILATION) {
            return;
        }



        var level = (net.minecraft.server.level.ServerLevel) this.level();

        double hitRadius = this.getHitRadius();

        var entities = level.getEntities(
                this,
                this.getBoundingBox().inflate(hitRadius),
                entity -> entity instanceof net.minecraft.world.entity.LivingEntity
                        && entity.isAlive()
                        && entity != this
                        && !(entity instanceof com.licht_meilleur.the_end_of_dragon.entity.TheEndOfDragonCoreEntity)
                        && !(entity instanceof com.licht_meilleur.the_end_of_dragon.entity.TheEndOfDragonEntity)
                        && !(entity instanceof TedVfxEntity)
        );

        for (Entity entity : entities) {
            entity.hurtServer(
                    level,
                    level.damageSources().magic(),
                    this.getDamage()
            );
        }

        if (this.isDestroyBlocks()) {
            destroyBlocksAround(level, this.getDestroyRadius());
        }
        spawnOrbTrail(level);

        if (this.tickCount >= this.getMaxAge() - 20) {
            spawnOrbFadeParticles(level);
        }
    }

    private void destroyBlocksAround(
            net.minecraft.server.level.ServerLevel level,
            double radius
    ) {
        int r = (int) Math.ceil(radius);

        net.minecraft.core.BlockPos center = this.blockPosition();

        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    double distSqr = x * x + y * y + z * z;

                    if (distSqr > radius * radius) {
                        continue;
                    }

                    net.minecraft.core.BlockPos pos = center.offset(x, y, z);

                    var state = level.getBlockState(pos);

                    if (state.isAir()) {
                        continue;
                    }

                    if (state.getDestroySpeed(level, pos) < 0.0F) {
                        continue;
                    }

                    level.destroyBlock(pos, false, this);
                }
            }
        }
    }

    private void spawnOrbTrail(ServerLevel level) {

        if ((this.tickCount & 1) != 0) {
            return;
        }

        level.sendParticles(
                ParticleTypes.END_ROD,
                getX(), getY(), getZ(),
                4,
                0.25D,0.25D,0.25D,
                0.01D
        );

        level.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,
                getX(), getY(), getZ(),
                3,
                0.35D,0.35D,0.35D,
                0.03D
        );
    }


    private void spawnOrbFadeParticles(net.minecraft.server.level.ServerLevel level) {
        level.sendParticles(
                net.minecraft.core.particles.ParticleTypes.END_ROD,
                this.getX(), this.getY(), this.getZ(),
                20,
                1.8D, 1.8D, 1.8D,
                0.02D
        );

        level.sendParticles(
                net.minecraft.core.particles.ParticleTypes.PORTAL,
                this.getX(), this.getY(), this.getZ(),
                30,
                2.0D, 2.0D, 2.0D,
                0.04D
        );
    }

    private void tickProjectileCollision() {
        if (this.level().isClientSide()) {
            return;
        }

        if (this.getVfxType() != TedVfxType.LIGHT_PROJECTILE
                && this.getVfxType() != TedVfxType.ORB_OF_ANIHILATION) {
            return;
        }

        if (this.tickCount < this.noHitTicks) {
            return;
        }

        var level = (net.minecraft.server.level.ServerLevel) this.level();

        double radius = this.getHitRadius();

        var entities = level.getEntities(
                this,
                this.getBoundingBox().inflate(radius),
                entity -> entity instanceof net.minecraft.world.entity.LivingEntity
                        && entity.isAlive()
                        && !isOwnerOrDragonPart(entity)
        );

        for (Entity entity : entities) {
            entity.hurtServer(
                    level,
                    level.damageSources().magic(),
                    this.getDamage()
            );
        }

        boolean hitBlock = !level.noCollision(this, this.getBoundingBox().inflate(0.1D));

        if (this.getVfxType() == TedVfxType.LIGHT_PROJECTILE && hitBlock) {
            onLightProjectileHit(level);
            return;
        }

        if (this.getVfxType() == TedVfxType.LIGHT_PROJECTILE && !entities.isEmpty()) {
            onLightProjectileHit(level);
            return;
        }

        if (this.getVfxType() == TedVfxType.ORB_OF_ANIHILATION) {
            if (this.isDestroyBlocks()) {
                destroyBlocksAround(level, this.getDestroyRadius());
            }

            spawnOrbTrail(level);
        }
    }

    private void onLightProjectileHit(ServerLevel level) {
        if (this.createExplosion) {
            spawnSmallExplosion(level, this.position());
        }

        if (this.isDestroyBlocks()) {
            breakWeakBlocks(level, this.position(), this.getDestroyRadius());
        }

        this.discard();
    }

    private void spawnSmallExplosion(ServerLevel level, Vec3 pos) {
        level.sendParticles(
                ParticleTypes.EXPLOSION,
                pos.x, pos.y, pos.z,
                2,
                0.15D, 0.15D, 0.15D,
                0.0D
        );

        level.sendParticles(
                ParticleTypes.END_ROD,
                pos.x, pos.y, pos.z,
                this.explosionParticleCount,
                this.explosionRadius, this.explosionRadius, this.explosionRadius,
                0.08D
        );

        level.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,
                pos.x, pos.y, pos.z,
                this.explosionParticleCount,
                this.explosionRadius * 0.7D,
                this.explosionRadius * 0.7D,
                this.explosionRadius * 0.7D,
                0.12D
        );

        level.sendParticles(
                ParticleTypes.SMOKE,
                pos.x, pos.y, pos.z,
                10,
                this.explosionRadius * 0.45D,
                this.explosionRadius * 0.45D,
                this.explosionRadius * 0.45D,
                0.02D
        );
    }

    private void breakWeakBlocks(ServerLevel level, Vec3 center, double radius) {
        int r = (int) Math.ceil(radius);
        BlockPos base = BlockPos.containing(center);

        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    if (x * x + y * y + z * z > radius * radius) {
                        continue;
                    }

                    BlockPos pos = base.offset(x, y, z);
                    BlockState state = level.getBlockState(pos);

                    if (state.isAir()) {
                        continue;
                    }

                    if (!canProjectileDestroyBlock(level, pos, state)) {
                        continue;
                    }

                    level.destroyBlock(pos, true, this);
                }
            }
        }
    }

    private boolean canProjectileDestroyBlock(ServerLevel level, BlockPos pos, BlockState state) {
        Block block = state.getBlock();

        // 壊せないブロックは除外
        if (state.getDestroySpeed(level, pos) < 0.0F) {
            return false;
        }

        // 強すぎる/守りたいブロックは除外
        if (block == Blocks.OBSIDIAN) return false;
        if (block == Blocks.CRYING_OBSIDIAN) return false;
        if (block == Blocks.END_PORTAL) return false;
        if (block == Blocks.END_PORTAL_FRAME) return false;
        if (block == Blocks.BARRIER) return false;
        if (block == Blocks.COMMAND_BLOCK) return false;
        if (block == Blocks.CHAIN_COMMAND_BLOCK) return false;
        if (block == Blocks.REPEATING_COMMAND_BLOCK) return false;
        if (block == Blocks.STRUCTURE_BLOCK) return false;
        if (block == Blocks.JIGSAW) return false;

        return true;
    }





}