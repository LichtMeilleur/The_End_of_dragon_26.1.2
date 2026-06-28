package com.licht_meilleur.the_end_of_dragon.entity;

import com.licht_meilleur.the_end_of_dragon.entity.collision.DragonCollisionBox;
import com.licht_meilleur.the_end_of_dragon.entity.collision.DragonCollisionPart;
import com.licht_meilleur.the_end_of_dragon.entity.hitbox.DragonLocatorSampler;
import com.licht_meilleur.the_end_of_dragon.entity.hitbox.DragonLocators;
import com.licht_meilleur.the_end_of_dragon.entity.vfx.TedVfxSpawner;
import com.licht_meilleur.the_end_of_dragon.entity.vfx.TedVfxType;
import com.licht_meilleur.the_end_of_dragon.registry.ModEntities;
import com.licht_meilleur.the_end_of_dragon.world.EndPortalSealHandler;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
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

    private void debugDrawLaserRay(
            ServerLevel serverLevel,
            DragonCollisionPart part,
            double rightOffset,
            double upOffset,
            double forwardOffset,
            double maxLength
    ) {
        DragonCollisionBox box = getCollisionPartBox(part);
        if (box == null || box.obb() == null) {
            return;
        }

        Vec3 center = box.obb().center();

        Vec3 posForward = box.obb().axisZ().normalize().scale(-1.0D);
        Vec3 up = box.obb().axisY().normalize();
        Vec3 right = box.obb().axisX().normalize();

        Vec3 start = center
                .add(right.scale(rightOffset))
                .add(up.scale(upOffset))
                .add(posForward.scale(forwardOffset));

        // ここを変えて方向テスト
        Vec3 dir = box.obb().axisY().normalize().scale(-1.0D);

        drawDebugLine(serverLevel, start, start.add(dir.scale(maxLength)));
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
                if (age == 1) {
                    spawnLaserFromMouth(serverLevel, 0.0D, -0.1D, 2.2D, 64.0D);

                    spawnLaserFromPart(serverLevel, DragonCollisionPart.FRONT_LEFT_HAND,  0.0D, 2.0D, -0.0D, 64.0D);
                    spawnLaserFromPart(serverLevel, DragonCollisionPart.FRONT_RIGHT_HAND, 0.0D, 2.0D, -0.0D, 64.0D);
                    spawnLaserFromPart(serverLevel, DragonCollisionPart.BACK_LEFT_HAND,   0.0D, 2.0D, -0.8D, 64.0D);
                    spawnLaserFromPart(serverLevel, DragonCollisionPart.BACK_RIGHT_HAND,  0.0D, 2.0D, -0.0D, 64.0D);
                }
            }

            case FLAMES_OF_RAGNAROK -> {
                if (age == 1) {
                    spawnLaserFromPart(serverLevel, DragonCollisionPart.FRONT_LEFT_HAND,  0.0D, 2.0D, -0.0D, 64.0D);
                    spawnLaserFromPart(serverLevel, DragonCollisionPart.FRONT_RIGHT_HAND, 0.0D, 2.0D, -0.0D, 64.0D);
                    spawnLaserFromPart(serverLevel, DragonCollisionPart.BACK_LEFT_HAND,   0.0D, 2.0D, -0.8D, 64.0D);
                    spawnLaserFromPart(serverLevel, DragonCollisionPart.BACK_RIGHT_HAND,  0.0D, 2.0D, -0.0D, 64.0D);
                }
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
            }
        }
    }


    private void spawnLaserFromPart(
            ServerLevel serverLevel,
            DragonCollisionPart part,
            double rightOffset,
            double upOffset,
            double forwardOffset,
            double maxLength
    ) {
        DragonCollisionBox box = getCollisionPartBox(part);
        if (box == null || box.obb() == null) {
            return;
        }

        Vec3 center = box.obb().center();

        Vec3 posForward = box.obb().axisZ().normalize().scale(-1.0D);
        Vec3 renderForward = this.getViewVector(1.0F).normalize();
        Vec3 up = box.obb().axisY().normalize();
        Vec3 right = box.obb().axisX().normalize();

        Vec3 start = center
                .add(right.scale(rightOffset))
                .add(up.scale(upOffset))
                .add(posForward.scale(forwardOffset));

        double length = raycastLaserLength(serverLevel, start, renderForward, maxLength);

        float yaw = (float) Math.toDegrees(Math.atan2(-renderForward.x, renderForward.z));
        float pitch = (float) -Math.toDegrees(Math.asin(renderForward.y));

        TedVfxSpawner.spawnAt(
                serverLevel,
                start,
                yaw,
                pitch,
                TedVfxType.TED_LASER_BEAM,
                1.5F,
                (float) length,
                20
        );
    }

    private void spawnLaserFromMouth(
            ServerLevel serverLevel,
            double rightOffset,
            double upOffset,
            double forwardOffset,
            double maxLength
    ) {
        DragonCollisionBox box = getCollisionPartBox(DragonCollisionPart.HEAD);
        if (box == null || box.obb() == null) {
            return;
        }

        Vec3 center = box.obb().center();

        Vec3 forward = getFrontLeftLaserForward(box);
        Vec3 up = new Vec3(0.0D, 1.0D, 0.0D);
        Vec3 right = up.cross(forward).normalize();

        Vec3 start = center
                .add(right.scale(rightOffset))
                .add(up.scale(upOffset))
                .add(forward.scale(forwardOffset));

        double length = raycastLaserLength(serverLevel, start, forward, maxLength);

        float yaw = (float) Math.toDegrees(Math.atan2(-forward.x, forward.z));
        float pitch = (float) -Math.toDegrees(Math.asin(forward.y));

        TedVfxSpawner.spawnAt(
                serverLevel,
                start,
                yaw,
                pitch,
                TedVfxType.TED_LASER_BEAM,
                1.5F,
                (float) length,
                20
        );
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



    private void debugDrawRayFromHandTip(
            ServerLevel serverLevel,
            DragonCollisionPart part,
            Vec3 axis,
            boolean reverse,
            double maxLength
    ) {
        DragonCollisionBox box = getCollisionPartBox(part);
        if (box == null || box.obb() == null) return;

        Vec3 root = endpointAlongAxis(box, axis, reverse);
        Vec3 tip = endpointAlongAxis(box, axis, !reverse);

        Vec3 dir = tip.subtract(root);
        if (dir.lengthSqr() < 1.0E-6D) return;

        Vec3 forward = getFrontLeftLaserForward(box);

        drawDebugLine(serverLevel, tip, tip.add(forward.scale(maxLength)));
    }

    private Vec3 getFrontLeftLaserForward(DragonCollisionBox box) {
        return box.obb().axisY().normalize();
    }

    private Vec3 rotateDirectionYawPitch(Vec3 dir, double yawDeg, double pitchDeg) {
        Vec3 forward = dir.normalize();
        Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);
        Vec3 right = worldUp.cross(forward).normalize();

        if (right.lengthSqr() < 1.0E-6D) {
            right = new Vec3(1.0D, 0.0D, 0.0D);
        }

        // yaw: 水平方向に少し振る
        double yaw = Math.toRadians(yawDeg);
        Vec3 yawed = forward.scale(Math.cos(yaw)).add(right.scale(Math.sin(yaw))).normalize();

        // pitch: 上下に少し振る
        double pitch = Math.toRadians(pitchDeg);
        Vec3 pitched = yawed.scale(Math.cos(pitch)).add(worldUp.scale(Math.sin(pitch))).normalize();

        return pitched;
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

    private Vec3 getCollisionPartCenter(com.licht_meilleur.the_end_of_dragon.entity.collision.DragonCollisionPart part) {
        TheEndOfDragonCollisionEntity collision =
                this.getChild(this.collisionEntityId, TheEndOfDragonCollisionEntity.class);

        if (collision == null) {
            return this.position();
        }

        for (var box : collision.getCollisionBoxes()) {
            if (box.part() == part) {
                var aabb = box.box();

                return new Vec3(
                        (aabb.minX + aabb.maxX) * 0.5D,
                        (aabb.minY + aabb.maxY) * 0.5D,
                        (aabb.minZ + aabb.maxZ) * 0.5D
                );
            }
        }

        return this.position();
    }

    private Vec3 offsetFromPart(
            com.licht_meilleur.the_end_of_dragon.entity.collision.DragonCollisionPart part,
            double rightOffset,
            double upOffset,
            double forwardOffset
    ) {
        Vec3 center = getCollisionPartCenter(part);

        Vec3 forward = this.getViewVector(1.0F).normalize();
        Vec3 up = new Vec3(0.0D, 1.0D, 0.0D);
        Vec3 right = up.cross(forward).normalize();

        return center
                .add(right.scale(rightOffset))
                .add(up.scale(upOffset))
                .add(forward.scale(forwardOffset));
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
}