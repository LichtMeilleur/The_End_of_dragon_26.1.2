package com.licht_meilleur.the_end_of_dragon.entity;

import com.licht_meilleur.the_end_of_dragon.entity.collision.DragonCollisionBox;
import com.licht_meilleur.the_end_of_dragon.entity.collision.DragonCollisionPart;
import com.licht_meilleur.the_end_of_dragon.entity.hitbox.DragonLocatorSampler;
import com.licht_meilleur.the_end_of_dragon.entity.hitbox.DragonLocators;
import com.licht_meilleur.the_end_of_dragon.entity.vfx.*;
import com.licht_meilleur.the_end_of_dragon.registry.ModEntities;
import com.licht_meilleur.the_end_of_dragon.world.EndPortalSealHandler;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class TheEndOfDragonCoreEntity extends Monster {
    private static final EntityDataAccessor<Integer> DATA_STATE =
            SynchedEntityData.defineId(TheEndOfDragonCoreEntity.class, EntityDataSerializers.INT);

    private int displayEntityId = -1;
    private int collisionEntityId = -1;

    private int frontLeftLaserVfxId = -1;
    private int frontRightLaserVfxId = -1;
    private int backLeftLaserVfxId = -1;
    private int backRightLaserVfxId = -1;

    public TheEndOfDragonCoreEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 500;
        this.setPersistenceRequired();
        this.setInvisible(true);
        this.noPhysics = true;
        this.setNoGravity(true);

        float yaw = 0.0F;

        this.setYRot(yaw);
        this.setYBodyRot(yaw);
        this.setYHeadRot(yaw);
        this.yRotO = yaw;
        this.yBodyRotO = yaw;
        this.yHeadRotO = yaw;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_STATE, DragonState.IDLE.ordinal());
    }

    private int stateStartTick = 0;

    public void setDragonState(DragonState state) {
        if (this.getDragonState() == state) {
            return;
        }

        this.entityData.set(DATA_STATE, state.ordinal());
        this.stateStartTick = this.tickCount;
    }

    public int getDragonStateAgeTicks() {
        return Math.max(0, this.tickCount - this.stateStartTick);
    }

    public DragonState getDragonState() {
        int id = this.entityData.get(DATA_STATE);
        DragonState[] values = DragonState.values();
        if (id < 0 || id >= values.length) {
            return DragonState.IDLE;
        }
        return values[id];
    }

    @Override
    public void tick() {
        super.tick();

        this.setInvisible(true);
        this.noPhysics = true;
        this.setNoGravity(true);



        if (!this.level().isClientSide()) {
            this.tickChildren();
            this.tickAttackVfx();
            this.tickAttackStateTimeout();
        }
    }

    private void tickChildren() {
        TheEndOfDragonDisplayEntity display =
                this.getChild(this.displayEntityId, TheEndOfDragonDisplayEntity.class);

        TheEndOfDragonCollisionEntity collision =
                this.getChild(this.collisionEntityId, TheEndOfDragonCollisionEntity.class);

        if (display == null) {
            display = new TheEndOfDragonDisplayEntity(
                    ModEntities.THE_END_OF_DRAGON_DISPLAY,
                    this.level()
            );
            display.syncFromCore(this);
            this.level().addFreshEntity(display);
            this.displayEntityId = display.getId();
        }

        if (collision == null) {
            collision = new TheEndOfDragonCollisionEntity(
                    ModEntities.THE_END_OF_DRAGON_COLLISION,
                    this.level()
            );
            collision.syncFromCore(this);
            this.level().addFreshEntity(collision);
            this.collisionEntityId = collision.getId();
        }

        display.syncFromCore(this);
        collision.syncFromCore(this);
    }

    private <T extends Entity> T getChild(int entityId, Class<T> type) {
        if (entityId == -1) {
            return null;
        }

        Entity entity = this.level().getEntity(entityId);
        if (type.isInstance(entity) && entity.isAlive()) {
            return type.cast(entity);
        }

        return null;
    }

    @Override
    protected void registerGoals() {
        // Goal式AIは使わない。大型ボス用ステートマシンをtickに書く。
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!this.level().isClientSide()) {
            Entity display = this.level().getEntity(this.displayEntityId);
            if (display != null) {
                display.discard();
            }

            Entity collision = this.level().getEntity(this.collisionEntityId);
            if (collision != null) {
                collision.discard();
            }
        }

        super.remove(reason);
    }

    @Override
    public void die(net.minecraft.world.damagesource.DamageSource damageSource) {
        super.die(damageSource);

        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            EndPortalSealHandler.unseal(serverLevel);
        }
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
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



    private void drawDebugLine(ServerLevel serverLevel, Vec3 start, Vec3 end) {
        int count = 80;

        for (int i = 0; i <= count; i++) {
            double t = (double) i / (double) count;
            Vec3 p = start.lerp(end, t);

            serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.FLAME,
                    p.x,
                    p.y,
                    p.z,
                    1,
                    0.0D,
                    0.0D,
                    0.0D,
                    0.0D
            );
        }
    }

    private void tickAttackVfx() {
        if (!(this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
            return;
        }

        com.licht_meilleur.the_end_of_dragon.entity.hitbox.DragonLocatorSampler.debugDrawLocator(
                serverLevel,
                this,
                com.licht_meilleur.the_end_of_dragon.entity.hitbox.DragonLocators.FRONT_LEFT_JET
        );
        /*
        //レイキャスト確認
        DragonCollisionBox box = getCollisionPartBox(DragonCollisionPart.FRONT_LEFT_HAND);
        if (box != null && box.obb() != null) {
            debugDrawRayFromHandTip(
                    serverLevel,
                    DragonCollisionPart.FRONT_LEFT_HAND,
                    box.obb().axisY(),
                    false, // false から true に変更
                    64.0D
            );
        }

         */
        // DEBUG: idleでも常時レーザー表示
        if (this.tickCount % 2 == 0) {
            updateAttachedVfx(
                    serverLevel,
                    TedVfxSpecs.FRONT_LEFT_LASER,
                    true
            );
            updateAttachedVfx(serverLevel, TedVfxSpecs.FRONT_RIGHT_LASER, true);
            updateAttachedVfx(serverLevel, TedVfxSpecs.BACK_LEFT_LASER, true);
            updateAttachedVfx(serverLevel, TedVfxSpecs.BACK_RIGHT_LASER, true);
        }



        int age = this.getDragonStateAgeTicks();

        switch (this.getDragonState()) {
            case ORB_OF_ANNIHILATION -> {
                if (age == 1) {
                    com.licht_meilleur.the_end_of_dragon.entity.vfx.TedVfxSpawner.spawnForwardFromLocator(
                            serverLevel,
                            this,
                            com.licht_meilleur.the_end_of_dragon.entity.hitbox.DragonLocators.CHEST_CRYSTAL,
                            3.0D,
                            com.licht_meilleur.the_end_of_dragon.entity.vfx.TedVfxType.ORB_OF_ANIHILATION,
                            1.5F,
                            1.0F,
                            80
                    );
                }
            }

            case PHOTON_BLASTER -> {
                boolean active = age >= 1 && age <= 36;

                updateAttachedVfx(serverLevel, TedVfxSpecs.FRONT_LEFT_LASER, active);
                updateAttachedVfx(serverLevel, TedVfxSpecs.FRONT_RIGHT_LASER, active);
                updateAttachedVfx(serverLevel, TedVfxSpecs.BACK_LEFT_LASER, active);
                updateAttachedVfx(serverLevel, TedVfxSpecs.BACK_RIGHT_LASER, active);
            }

            case FLAMES_OF_RAGNAROK -> {
                boolean active = age >= 1 && age <= 80;

                updateAttachedVfx(serverLevel, TedVfxSpecs.FRONT_LEFT_LASER, active);
                updateAttachedVfx(serverLevel, TedVfxSpecs.FRONT_RIGHT_LASER, active);
                updateAttachedVfx(serverLevel, TedVfxSpecs.BACK_LEFT_LASER, active);
                updateAttachedVfx(serverLevel, TedVfxSpecs.BACK_RIGHT_LASER, active);
            }

            case LIGHT_OF_DESTRUCTION -> {
                if (age == 1) {
                    com.licht_meilleur.the_end_of_dragon.entity.vfx.TedVfxSpawner.spawnAtLocator(
                            serverLevel,
                            this,
                            com.licht_meilleur.the_end_of_dragon.entity.hitbox.DragonLocators.CHEST_CRYSTAL,
                            com.licht_meilleur.the_end_of_dragon.entity.vfx.TedVfxType.LIGHT_OF_DESTRUCTION,
                            2.0F,
                            1.0F,
                            60
                    );
                }
            }

            case FLY_SHOT -> {
                // ジェットは攻撃中ずっと短寿命VFXを出し続ける
                if (age % 2 == 0) {
                    spawnJet(serverLevel, DragonCollisionPart.FRONT_LEFT_HAND, 0.0D, 2.0D, -0.0D);
                    spawnJet(serverLevel, DragonCollisionPart.FRONT_RIGHT_HAND, 0.0D, 2.0D, -0.0D);
                    spawnJet(serverLevel, DragonCollisionPart.BACK_LEFT_HAND, 0.0D, 2.0D, -0.8D);
                    spawnJet(serverLevel, DragonCollisionPart.BACK_RIGHT_HAND, 0.0D, 2.0D, -0.0D);
                }



                if (age % 12 == 1) {
                    com.licht_meilleur.the_end_of_dragon.entity.vfx.TedVfxSpawner.spawnForwardFromLocator(
                            serverLevel,
                            this,
                            com.licht_meilleur.the_end_of_dragon.entity.hitbox.DragonLocators.MOUTH,
                            2.0D,
                            com.licht_meilleur.the_end_of_dragon.entity.vfx.TedVfxType.LIGHT_PROJECTILE,
                            1.0F,
                            1.0F,
                            80
                    );
                }
            }

            case FLY -> {
                // ジェットは攻撃中ずっと短寿命VFXを出し続ける
                if (age % 2 == 0) {
                    spawnJet(serverLevel, DragonCollisionPart.FRONT_LEFT_HAND, 0.0D, 2.0D, -0.0D);
                    spawnJet(serverLevel, DragonCollisionPart.FRONT_RIGHT_HAND, 0.0D, 2.0D, -0.0D);
                    spawnJet(serverLevel, DragonCollisionPart.BACK_LEFT_HAND, 0.0D, 2.0D, -0.8D);
                    spawnJet(serverLevel, DragonCollisionPart.BACK_RIGHT_HAND, 0.0D, 2.0D, -0.0D);
                }

            }
            case FLY_LEFT -> {
                // ジェットは攻撃中ずっと短寿命VFXを出し続ける
                if (age % 2 == 0) {
                    spawnJet(serverLevel, DragonCollisionPart.FRONT_LEFT_HAND, 0.0D, 2.0D, -0.0D);
                    spawnJet(serverLevel, DragonCollisionPart.FRONT_RIGHT_HAND, 0.0D, 2.0D, -0.0D);
                    spawnJet(serverLevel, DragonCollisionPart.BACK_LEFT_HAND, 0.0D, 2.0D, -0.8D);
                    spawnJet(serverLevel, DragonCollisionPart.BACK_RIGHT_HAND, -1.0D, 2.0D, -0.0D);
                }

            }
            case FLY_RIGHT -> {
                // ジェットは攻撃中ずっと短寿命VFXを出し続ける
                if (age % 2 == 0) {
                    spawnJet(serverLevel, DragonCollisionPart.FRONT_LEFT_HAND, 0.0D, 2.0D, -0.0D);
                    spawnJet(serverLevel, DragonCollisionPart.FRONT_RIGHT_HAND, 0.0D, 2.0D, -0.0D);
                    spawnJet(serverLevel, DragonCollisionPart.BACK_LEFT_HAND, 0.0D, 2.0D, -0.8D);
                    spawnJet(serverLevel, DragonCollisionPart.BACK_RIGHT_HAND, 0.0D, 2.0D, -0.0D);
                }

            }

            default -> {
                //hideAllLaserVfx(serverLevel);
            }
        }
    }

    private org.joml.Quaternionf quaternionFromBasis(Vec3 right, Vec3 up, Vec3 forward) {
        Vec3 f = forward.normalize();

        // レーザーモデルは -Z 方向へ伸びる想定
        Vec3 back = f.scale(-1.0D).normalize();

        Vec3 u = up.normalize();
        Vec3 r = u.cross(back).normalize();
        u = back.cross(r).normalize();

        float m00 = (float) r.x;
        float m01 = (float) u.x;
        float m02 = (float) back.x;

        float m10 = (float) r.y;
        float m11 = (float) u.y;
        float m12 = (float) back.y;

        float m20 = (float) r.z;
        float m21 = (float) u.z;
        float m22 = (float) back.z;

        float trace = m00 + m11 + m22;

        float x, y, z, w;

        if (trace > 0.0F) {
            float s = (float) Math.sqrt(trace + 1.0F) * 2.0F;
            w = 0.25F * s;
            x = (m21 - m12) / s;
            y = (m02 - m20) / s;
            z = (m10 - m01) / s;
        } else if (m00 > m11 && m00 > m22) {
            float s = (float) Math.sqrt(1.0F + m00 - m11 - m22) * 2.0F;
            w = (m21 - m12) / s;
            x = 0.25F * s;
            y = (m01 + m10) / s;
            z = (m02 + m20) / s;
        } else if (m11 > m22) {
            float s = (float) Math.sqrt(1.0F + m11 - m00 - m22) * 2.0F;
            w = (m02 - m20) / s;
            x = (m01 + m10) / s;
            y = 0.25F * s;
            z = (m12 + m21) / s;
        } else {
            float s = (float) Math.sqrt(1.0F + m22 - m00 - m11) * 2.0F;
            w = (m10 - m01) / s;
            x = (m02 + m20) / s;
            y = (m12 + m21) / s;
            z = 0.25F * s;
        }

        return new org.joml.Quaternionf(x, y, z, w).normalize();
    }

    private Vec3 offsetFromObb(
            DragonCollisionBox box,
            Vec3 base,
            double x,
            double y,
            double z
    ) {
        return base
                .add(box.obb().axisX().normalize().scale(x))
                .add(box.obb().axisY().normalize().scale(y))
                .add(box.obb().axisZ().normalize().scale(z));
    }

    private void hideAllLaserVfx(ServerLevel serverLevel) {
        updateLaserAttachedToHand(serverLevel, DragonCollisionPart.FRONT_LEFT_HAND, false, 0, 0, 0, 1);
        updateLaserAttachedToHand(serverLevel, DragonCollisionPart.FRONT_RIGHT_HAND, false, 0, 0, 0, 1);
        updateLaserAttachedToHand(serverLevel, DragonCollisionPart.BACK_LEFT_HAND, false, 0, 0, 0, 1);
        updateLaserAttachedToHand(serverLevel, DragonCollisionPart.BACK_RIGHT_HAND, false, 0, 0, 0, 1);
    }

    private TedVfxEntity getOrCreateLaserVfx(
            ServerLevel serverLevel,
            int currentId
    ) {
        if (currentId != -1) {
            Entity entity = this.level().getEntity(currentId);
            if (entity instanceof TedVfxEntity vfx && vfx.isAlive()) {
                return vfx;
            }
        }

        TedVfxEntity vfx = ModEntities.TED_VFX.create(serverLevel, EntitySpawnReason.EVENT);
        if (vfx == null) {
            return null;
        }

        vfx.setup(TedVfxType.TED_LASER_BEAM, 1.5F, 0.0F, 999999);
        vfx.snapTo(this.getX(), this.getY(), this.getZ(), 0.0F, 0.0F);

        vfx.setBasis(new Vec3(0, 0, 1), new Vec3(0, 1, 0));

        serverLevel.addFreshEntity(vfx);
        return vfx;
    }

    private TedVfxEntity getLaserVfxForPart(ServerLevel serverLevel, DragonCollisionPart part) {
        TedVfxEntity vfx;

        switch (part) {
            case FRONT_LEFT_HAND -> {
                vfx = getOrCreateLaserVfx(serverLevel, frontLeftLaserVfxId);
                if (vfx != null) frontLeftLaserVfxId = vfx.getId();
                return vfx;
            }
            case FRONT_RIGHT_HAND -> {
                vfx = getOrCreateLaserVfx(serverLevel, frontRightLaserVfxId);
                if (vfx != null) frontRightLaserVfxId = vfx.getId();
                return vfx;
            }
            case BACK_LEFT_HAND -> {
                vfx = getOrCreateLaserVfx(serverLevel, backLeftLaserVfxId);
                if (vfx != null) backLeftLaserVfxId = vfx.getId();
                return vfx;
            }
            case BACK_RIGHT_HAND -> {
                vfx = getOrCreateLaserVfx(serverLevel, backRightLaserVfxId);
                if (vfx != null) backRightLaserVfxId = vfx.getId();
                return vfx;
            }
            default -> {
                return null;
            }
        }
    }


    private Vec3 endpointAlongAxis(DragonCollisionBox box, Vec3 axis, boolean maxSide) {
        Vec3 dir = axis.normalize();

        Vec3[] points = box.points();
        if (points == null || points.length == 0) {
            return box.obb().center();
        }

        double best = maxSide ? -Double.MAX_VALUE : Double.MAX_VALUE;
        Vec3 sum = Vec3.ZERO;
        int count = 0;

        for (Vec3 p : points) {
            double d = p.dot(dir);

            boolean better = maxSide ? d > best + 0.0001D : d < best - 0.0001D;

            if (better) {
                best = d;
                sum = p;
                count = 1;
            } else if (Math.abs(d - best) < 0.0001D) {
                sum = sum.add(p);
                count++;
            }
        }

        return count <= 0 ? box.obb().center() : sum.scale(1.0D / count);
    }









    private double raycastLaserLength(
            ServerLevel serverLevel,
            Vec3 start,
            Vec3 direction,
            double maxLength
    ) {
        Vec3 dir = direction.normalize();
        Vec3 end = start.add(dir.scale(maxLength));

        // レイキャスト方向デバッグ
        for (int i = 0; i <= 64; i++) {
            double t = i / 64.0D;
            Vec3 p = start.lerp(end, t);

            serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.FLAME,
                    p.x,
                    p.y,
                    p.z,
                    1,
                    0.0D,
                    0.0D,
                    0.0D,
                    0.0D
            );
        }

        var hit = serverLevel.clip(new net.minecraft.world.level.ClipContext(
                start,
                end,
                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE,
                this
        ));

        double length = hit.getLocation().distanceTo(start);
        return Math.max(1.0D, Math.min(maxLength, length));
    }

    private void spawnJet(
            ServerLevel serverLevel,
            DragonCollisionPart part,
            double rightOffset,
            double upOffset,
            double forwardOffset
    ) {
        DragonCollisionBox box = getCollisionPartBox(part);
        if (box == null || box.obb() == null) {
            return;
        }

        Vec3 center = box.obb().center();

        Vec3 posForward = box.obb().axisZ().normalize().scale(-1.0D);
        Vec3 renderForward = box.obb().axisY().normalize().scale(-1.0D);
        Vec3 up = box.obb().axisY().normalize();
        Vec3 right = box.obb().axisX().normalize();

        Vec3 pos = center
                .add(right.scale(rightOffset))
                .add(up.scale(upOffset))
                .add(posForward.scale(forwardOffset));

        float yaw = (float) Math.toDegrees(Math.atan2(-renderForward.x, renderForward.z));
        float pitch = (float) -Math.toDegrees(Math.asin(renderForward.y));

        TedVfxSpawner.spawnAt(
                serverLevel,
                pos,
                yaw,
                pitch,
                TedVfxType.TED_JET,
                1.0F,
                5.0F,
                20
        );
    }





    private DragonCollisionBox getCollisionPartBox(DragonCollisionPart part) {
        TheEndOfDragonCollisionEntity collision =
                this.getChild(this.collisionEntityId, TheEndOfDragonCollisionEntity.class);

        if (collision == null) {
            return null;
        }

        for (var box : collision.getCollisionBoxes()) {
            if (box.part() == part) {
                return box;
            }
        }

        return null;
    }




    private void tickAttackStateTimeout() {
        int age = this.getDragonStateAgeTicks();

        switch (this.getDragonState()) {
            case ORB_OF_ANNIHILATION -> {
                if (age > 78) this.setDragonState(DragonState.IDLE);
            }
            case ROAR_OF_OBLITERATION -> {
                if (age > 48) this.setDragonState(DragonState.IDLE);
            }
            case FLAMES_OF_RAGNAROK -> {
                if (age > 120) this.setDragonState(DragonState.IDLE);
            }
            case LIGHT_OF_DESTRUCTION -> {
                if (age > 36) this.setDragonState(DragonState.IDLE);
            }
            case PHOTON_BLASTER -> {
                if (age > 84) this.setDragonState(DragonState.IDLE);
            }
            case BLASTER_TACKLE -> {
                if (age > 24) this.setDragonState(DragonState.IDLE);
            }
            case FLY_SHOT -> {
                if (age > 12) this.setDragonState(DragonState.FLY);
            }
            default -> {
            }
        }
    }
    private void updateLaserAttachedToHand(
            ServerLevel serverLevel,
            DragonCollisionPart part,
            boolean active,
            double offsetX,
            double offsetY,
            double offsetZ,
            double maxLength
    ) {
        DragonCollisionBox box = getCollisionPartBox(part);
        if (box == null || box.obb() == null) {
            return;
        }

        TedVfxEntity vfx = getLaserVfxForPart(serverLevel, part);
        if (vfx == null) {
            return;
        }

        if (!active) {
            vfx.updateVfx(0.0F, 0.0F);
            return;
        }

        Vec3 axisX = box.obb().axisX().normalize();
        Vec3 axisY = box.obb().axisY().normalize();
        Vec3 axisZ = box.obb().axisZ().normalize();

        Vec3 forward = axisY;
        Vec3 up = axisZ;

        Vec3 start = box.obb().center()
                .add(axisX.scale(offsetX))
                .add(axisY.scale(offsetY))
                .add(axisZ.scale(offsetZ));

        double length = raycastLaserLength(serverLevel, start, forward, maxLength);

        org.joml.Quaternionf q = quaternionFromBasis(axisX, up, forward);

        vfx.updateVfx(1.5F, (float) length);
        vfx.setVfxRotationQuat(q);
        vfx.snapTo(start.x, start.y, start.z, 0.0F, 0.0F);
    }


    private void updateAttachedVfx(
            ServerLevel serverLevel,
            TedVfxSpec spec,
            boolean active
    ) {
        DragonCollisionBox box = getCollisionPartBox(spec.part());
        if (box == null || box.obb() == null) {
            return;
        }

        TedVfxEntity vfx = getLaserVfxForPart(serverLevel, spec.part());
        if (vfx == null) {
            return;
        }

        if (!active) {
            vfx.updateVfx(0.0F, 0.0F);
            return;
        }

        Vec3 axisX = box.obb().axisX().normalize();
        Vec3 axisY = box.obb().axisY().normalize();
        Vec3 axisZ = box.obb().axisZ().normalize();

        Vec3 pos = box.obb().center()
                .add(axisX.scale(spec.offsetX()))
                .add(axisY.scale(spec.offsetY()))
                .add(axisZ.scale(spec.offsetZ()));

// レイキャスト方向。ここが炎パーティクルと同じ方向になる
        Vec3 rayDir = axisY.normalize();

        float length = spec.length();

        if (spec.type() == TedVfxType.TED_LASER_BEAM) {
            length = (float) raycastLaserLength(serverLevel, pos, rayDir, spec.length());
        }

        vfx.updateVfx(spec.scale(), length);
        vfx.snapTo(pos.x, pos.y, pos.z, 0.0F, 0.0F);

// 親回転は腕OBBではなく、レイキャスト方向をY軸として作る
        if (spec.mode() == TedVfxAttachMode.PART_BASIS) {
            Vec3 upForRoll = axisZ;
            vfx.setBasis(rayDir, upForRoll);
        }
    }
}