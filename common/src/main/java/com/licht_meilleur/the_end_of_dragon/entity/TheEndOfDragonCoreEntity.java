package com.licht_meilleur.the_end_of_dragon.entity;

import com.licht_meilleur.the_end_of_dragon.entity.collision.DragonCollisionBox;
import com.licht_meilleur.the_end_of_dragon.entity.collision.DragonCollisionPart;
import com.licht_meilleur.the_end_of_dragon.entity.hitbox.DragonLocatorSampler;
import com.licht_meilleur.the_end_of_dragon.entity.hitbox.TedBeamHitbox;
import com.licht_meilleur.the_end_of_dragon.entity.projectile.TedProjectileSpec;
import com.licht_meilleur.the_end_of_dragon.entity.projectile.TedProjectileSpecs;
import com.licht_meilleur.the_end_of_dragon.entity.vfx.*;
import com.licht_meilleur.the_end_of_dragon.registry.ModEntities;
import com.licht_meilleur.the_end_of_dragon.world.EndPortalSealHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.phys.AABB;
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



    // Photon Blaster
    private static final int PHOTON_CHARGE_START = 1;
    private static final int PHOTON_FIRE_START = 27;
    private static final int PHOTON_FIRE_END = 70;

    // Flames of Ragnarok
    private static final int FLAMES_FIRE_START = 1;
    private static final int FLAMES_FIRE_END = 120;

    //Orb of Annihilation
    private static final int ORB_CHARGE_START = 6;
    private static final int ORB_FIRE_TICK = 55;

    //Fly Shot
    private static final int FLY_SHOT_FIRE_TICK = 5;


    private static boolean between(int age, int start, int end) {
        return age >= start && age <= end;
    }

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

        //頭の判定確認
        //debugDrawHeadCollision(serverLevel);


        // DEBUG: idleでも常時レーザー表示
        /*
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

         */



        int age = this.getDragonStateAgeTicks();

        switch (this.getDragonState()) {
            case ORB_OF_ANNIHILATION -> {
                if (between(age, ORB_CHARGE_START, ORB_FIRE_TICK - 1)) {
                    updateOrbCharge(serverLevel, age);
                }

                if (age == ORB_FIRE_TICK) {
                    fireOrbOfAnnihilation(serverLevel);
                }
            }

            case PHOTON_BLASTER -> {
                boolean firing = between(age, PHOTON_FIRE_START, PHOTON_FIRE_END);

                if (firing) {
                    updatePhotonLasers(serverLevel);
                }
            }

            case FLAMES_OF_RAGNAROK -> {
                boolean firing = between(age, FLAMES_FIRE_START, FLAMES_FIRE_END);

                updateAttachedVfx(serverLevel, TedVfxSpecs.FRONT_LEFT_LASER, firing);
                updateAttachedVfx(serverLevel, TedVfxSpecs.FRONT_RIGHT_LASER, firing);
                updateAttachedVfx(serverLevel, TedVfxSpecs.BACK_LEFT_LASER, firing);
                updateAttachedVfx(serverLevel, TedVfxSpecs.BACK_RIGHT_LASER, firing);
            }

            case LIGHT_OF_DESTRUCTION -> {
                if (age >= 20 && age <= 30) {
                    updateLightOfDestruction(serverLevel, age);
                }
            }

            case FLY_SHOT -> {
                updateFlightJets(serverLevel);

                if (age == FLY_SHOT_FIRE_TICK) {
                    fireLightProjectile(serverLevel);
                }
            }

            case FLY, FLY_LEFT, FLY_RIGHT -> {
                updateFlightJets(serverLevel);
            }

            case ROAR_OF_OBLITERATION -> {
                if (age == 10) {
                    applyRoarOfObliteration(serverLevel);
                }

                spawnRoarParticles(serverLevel, age);
            }

            default -> {
                //hideAllLaserVfx(serverLevel);
            }
        }
    }

    private void applyRoarOfObliteration(ServerLevel level) {
        double radius = 48.0D;

        AABB area = this.getBoundingBox().inflate(radius);

        var entities = level.getEntitiesOfClass(
                LivingEntity.class,
                area,
                entity -> entity.isAlive()
                        && entity != this
                        && !(entity instanceof TheEndOfDragonCoreEntity)
                        && !(entity instanceof TheEndOfDragonEntity)
        );

        for (LivingEntity entity : entities) {
            damageEquipmentByRoar(level, entity, 40);

            Vec3 away = entity.position().subtract(this.position());
            if (away.lengthSqr() > 1.0E-6D) {
                away = away.normalize();
                entity.push(away.x * 1.6D, 0.35D, away.z * 1.6D);
                entity.hurtMarked = true;
            }

            entity.hurtServer(
                    level,
                    level.damageSources().mobAttack(this),
                    6.0F
            );
        }
    }

    private void damageEquipmentByRoar(ServerLevel level, LivingEntity entity, int amount) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = entity.getItemBySlot(slot);

            if (stack.isEmpty()) {
                continue;
            }

            if (!stack.isDamageableItem()) {
                // 耐久値がないものは即時破壊
                entity.setItemSlot(slot, ItemStack.EMPTY);
                continue;
            }

            // 高耐久装備は即破壊
            if (stack.getMaxDamage() >= 1000) {
                entity.setItemSlot(slot, ItemStack.EMPTY);
                continue;
            }


            int nextDamage = stack.getDamageValue() + amount;

            if (nextDamage >= stack.getMaxDamage()) {
                entity.setItemSlot(slot, ItemStack.EMPTY);
            } else {
                stack.setDamageValue(nextDamage);
            }
        }
        for (ItemStack stack : com.licht_meilleur.the_end_of_dragon.compat.TedAccessories.getAccessories(entity)) {
            damageAccessoryStackByRoar(stack, amount);
        }
    }

    private void damageAccessoryStackByRoar(ItemStack stack, int amount) {
        if (stack.isEmpty()) {
            return;
        }

        if (!stack.isDamageableItem()) {
            stack.shrink(1);
            return;
        }

        if (stack.getMaxDamage() >= 1000) {
            stack.shrink(1);
            return;
        }

        int nextDamage = stack.getDamageValue() + amount;

        if (nextDamage >= stack.getMaxDamage()) {
            stack.shrink(1);
        } else {
            stack.setDamageValue(nextDamage);
        }
    }

    private void spawnRoarParticles(ServerLevel level, int age) {
        if (age < 1 || age > 30) return;

        Vec3 center = this.position().add(0.0D, 4.0D, 0.0D);

        level.sendParticles(
                ParticleTypes.SONIC_BOOM,
                center.x, center.y, center.z,
                1,
                0.0D, 0.0D, 0.0D,
                0.0D
        );

        level.sendParticles(
                ParticleTypes.END_ROD,
                center.x, center.y, center.z,
                20,
                age * 0.2D, age * 0.1D, age * 0.2D,
                0.03D
        );
    }

    private void updateLightOfDestruction(ServerLevel serverLevel, int age) {
        Vec3 center = getChestCrystalCenter();
        if (center == null) {
            return;
        }

        // 20～30tickを0～1へ
        float t = (age - 20) / 10.0F;
        t = Math.min(1.0F, Math.max(0.0F, t));

        float scale = 2.0F + t * 4000.0F;

        TedVfxSpawner.spawnAt(
                serverLevel,
                center,
                this.getYRot(),
                this.getXRot(),
                TedVfxType.LIGHT_OF_DESTRUCTION,
                scale,
                1.0F,
                2
        );

        serverLevel.sendParticles(
                ParticleTypes.END_ROD,
                center.x,
                center.y,
                center.z,
                20,
                1.0D + scale * 0.25D,
                1.0D + scale * 0.25D,
                1.0D + scale * 0.25D,
                0.02D
        );

        // 最大まで広がった瞬間
        if (age == 30) {
            clearBuffsByLight(serverLevel, center, scale * 2.5D);


        }
    }

    private Vec3 getChestCrystalCenter() {
        DragonCollisionBox box = getCollisionPartBox(DragonCollisionPart.CHEST_CRYSTAL);
        if (box == null || box.obb() == null) {
            return null;
        }

        return box.obb().center();
    }

    private void clearBuffsByLight(ServerLevel level, Vec3 center, double radius) {
        AABB area = new AABB(center, center).inflate(radius);

        var entities = level.getEntitiesOfClass(
                LivingEntity.class,
                area,
                entity -> entity.isAlive()
                        && entity != this
                        && !(entity instanceof TheEndOfDragonCoreEntity)
                        && !(entity instanceof TheEndOfDragonEntity)
        );

        for (LivingEntity entity : entities) {
            entity.removeAllEffects();

            level.sendParticles(
                    ParticleTypes.END_ROD,
                    entity.getX(),
                    entity.getY() + entity.getBbHeight() * 0.5D,
                    entity.getZ(),
                    20,
                    0.5D, 0.8D, 0.5D,
                    0.08D
            );
        }
    }

    private void fireLightProjectile(ServerLevel serverLevel) {
        DragonCollisionBox head = getCollisionPartBox(DragonCollisionPart.HEAD);
        if (head == null || head.obb() == null) {
            return;
        }

        Vec3 axisY = head.obb().axisY().normalize();

        // 頭の少し前から発射
        Vec3 start = head.obb().center()
                .add(axisY.scale(2.5D));

        Vec3 shotDir = axisY;

        net.minecraft.world.entity.player.Player target =
                serverLevel.getNearestPlayer(this, 128.0D);

        if (target != null) {
            Vec3 toTarget = target.getEyePosition().subtract(start).normalize();

            double dot = axisY.dot(toTarget);
            double maxAngleCos = Math.cos(Math.toRadians(60.0D));

            if (dot > maxAngleCos) {
                shotDir = axisY.lerp(toTarget, 0.75D).normalize();
            }
        }

        spawnProjectile(
                serverLevel,
                TedProjectileSpecs.LIGHT_PROJECTILE,
                start,
                shotDir
        );
    }

    private void updateJetBeam(
            ServerLevel serverLevel,
            DragonCollisionPart part,
            double offsetX,
            double offsetY,
            double offsetZ
    ) {
        DragonCollisionBox box = getCollisionPartBox(part);
        if (box == null || box.obb() == null) {
            return;
        }

        Vec3 axisX = box.obb().axisX().normalize();
        Vec3 axisY = box.obb().axisY().normalize();
        Vec3 axisZ = box.obb().axisZ().normalize();

        Vec3 start = box.obb().center()
                .add(axisX.scale(offsetX))
                .add(axisY.scale(offsetY))
                .add(axisZ.scale(offsetZ));

        Vec3 direction = axisY.normalize();

        TedBeamHitbox beam = new TedBeamHitbox(
                start,
                direction,
                5.0D,
                1.2D
        );

        beam.spawnJetParticles(serverLevel);
    }

    private void updateOrbCharge(ServerLevel serverLevel, int age) {
        float t = Math.min(1.0F, age / 55.0F);
        float scale = 0.3F + t * 8F;

        Vec3 pos = orbChargePos();

        TedVfxSpawner.spawnAt(
                serverLevel,
                pos,
                this.getYRot(),
                this.getXRot(),
                TedVfxType.ORB_OF_ANIHILATION,
                scale,
                1.0F,
                2
        );
    }

    private Vec3 orbChargePos() {
        Vec3 forward = DragonLocatorSampler.forward(this).normalize();

        return this.position()
                .add(0.0D, 5.0D, 0.0D)
                .add(forward.scale(6.0D));
    }

    private void fireOrbOfAnnihilation(ServerLevel serverLevel) {
        Vec3 start = orbChargePos();
        Vec3 direction = DragonLocatorSampler.forward(this).normalize();

        spawnProjectile(
                serverLevel,
                TedProjectileSpecs.ORB_OF_ANNIHILATION,
                start,
                direction
        );
    }

    private void spawnProjectile(
            ServerLevel serverLevel,
            TedProjectileSpec spec,
            Vec3 start,
            Vec3 direction
    ) {
        TedVfxEntity projectile = ModEntities.TED_VFX.create(serverLevel, EntitySpawnReason.EVENT);
        if (projectile == null) {
            return;
        }

        projectile.setup(spec);
        projectile.setProjectileOwner(this);

        Vec3 dir = direction.normalize();

        float yaw = (float) Math.toDegrees(Math.atan2(-dir.x, dir.z));
        float pitch = (float) -Math.toDegrees(Math.asin(dir.y));

        projectile.snapTo(
                start.x,
                start.y,
                start.z,
                yaw,
                pitch
        );

        projectile.setDeltaMovement(dir.scale(spec.speed()));


        serverLevel.addFreshEntity(projectile);
    }

    private void updatePhotonLasers(ServerLevel serverLevel) {
        updateHandLaserBeam(serverLevel, DragonCollisionPart.FRONT_LEFT_HAND, 0.0D, 0.0D, 0.0D);
        updateHandLaserBeam(serverLevel, DragonCollisionPart.FRONT_RIGHT_HAND, 0.0D, 0.0D, 0.0D);
        updateHandLaserBeam(serverLevel, DragonCollisionPart.BACK_LEFT_HAND, 0.0D, 0.0D, 0.0D);
        updateHandLaserBeam(serverLevel, DragonCollisionPart.BACK_RIGHT_HAND, 0.0D, 0.0D, 0.0D);

        updateHeadLaserBeam(serverLevel, true);
    }

    private void updateHandLaserBeam(
            ServerLevel serverLevel,
            DragonCollisionPart part,
            double offsetX,
            double offsetY,
            double offsetZ
    ) {
        DragonCollisionBox box = getCollisionPartBox(part);
        if (box == null || box.obb() == null) {
            return;
        }

        Vec3 axisX = box.obb().axisX().normalize();
        Vec3 axisY = box.obb().axisY().normalize();
        Vec3 axisZ = box.obb().axisZ().normalize();

        Vec3 start = box.obb().center()
                .add(axisX.scale(offsetX))
                .add(axisY.scale(offsetY))
                .add(axisZ.scale(offsetZ));

        Vec3 direction = axisY.normalize();

        double length = raycastLaserLength(
                serverLevel,
                start,
                direction,
                64.0D
        );

        TedBeamHitbox beam = new TedBeamHitbox(
                start,
                direction,
                length,
                1.5D
        );

        beam.spawnLaserParticles(serverLevel);

        beam.damageEntities(
                serverLevel,
                this,
                8.0F,
                entity -> entity != this
        );
    }

    private void updateFlightJets(ServerLevel serverLevel) {
        updateJetBeam(serverLevel, DragonCollisionPart.FRONT_LEFT_HAND, 0.0D, 2.0D, 0.0D);
        updateJetBeam(serverLevel, DragonCollisionPart.FRONT_RIGHT_HAND, 0.0D, 2.0D, 0.0D);
        updateJetBeam(serverLevel, DragonCollisionPart.BACK_LEFT_HAND, 0.0D, 2.0D, 0.0D);
        updateJetBeam(serverLevel, DragonCollisionPart.BACK_RIGHT_HAND, 0.0D, 2.0D, 0.0D);
    }

    private void updateHeadLaserBeam(
            ServerLevel serverLevel,
            boolean active
    ) {
        if (!active) {
            return;
        }

        DragonCollisionBox box = getCollisionPartBox(DragonCollisionPart.HEAD);
        if (box == null || box.obb() == null) {
            return;
        }

        Vec3 axisX = box.obb().axisX().normalize();
        Vec3 axisY = box.obb().axisY().normalize();
        Vec3 axisZ = box.obb().axisZ().normalize();

        // まずは手レーザーと同じく axisY を正方向として使う
        Vec3 direction = axisY;

        // 頭の中心から少し前へ出す
        Vec3 start = box.obb().center()
                .add(direction.scale(0.8D));

        double maxLength = 64.0D;

        double length = raycastLaserLength(
                serverLevel,
                start,
                direction,
                maxLength
        );

        TedBeamHitbox beam = new TedBeamHitbox(
                start,
                direction,
                length,
                1.0D
        );

        beam.spawnLaserParticles(serverLevel);

        beam.damageEntities(
                serverLevel,
                this,
                8.0F,
                entity -> entity != this
        );
    }


    private void debugDrawHeadCollision(ServerLevel level) {
        DragonCollisionBox box = getCollisionPartBox(DragonCollisionPart.HEAD);
        if (box == null || box.points() == null || box.points().length < 8) {
            return;
        }

        Vec3[] p = box.points();

        // 8頂点
        for (Vec3 v : p) {
            level.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.FLAME,
                    v.x, v.y, v.z,
                    4,
                    0.03D, 0.03D, 0.03D,
                    0.0D
            );
        }

        // 面と辺
        debugLine(level, p[0], p[1]);
        debugLine(level, p[1], p[2]);
        debugLine(level, p[2], p[3]);
        debugLine(level, p[3], p[0]);

        debugLine(level, p[4], p[5]);
        debugLine(level, p[5], p[6]);
        debugLine(level, p[6], p[7]);
        debugLine(level, p[7], p[4]);

        debugLine(level, p[0], p[4]);
        debugLine(level, p[1], p[5]);
        debugLine(level, p[2], p[6]);
        debugLine(level, p[3], p[7]);
    }

    private void debugLine(ServerLevel level, Vec3 a, Vec3 b) {
        int count = 24;

        for (int i = 0; i <= count; i++) {
            double t = i / (double) count;
            Vec3 p = a.lerp(b, t);

            level.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.END_ROD,
                    p.x, p.y, p.z,
                    1,
                    0.0D, 0.0D, 0.0D,
                    0.0D
            );
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
                if (age > 65) this.setDragonState(DragonState.IDLE);
            }
            case ROAR_OF_OBLITERATION -> {
                if (age > 40) this.setDragonState(DragonState.IDLE);
            }
            case FLAMES_OF_RAGNAROK -> {
                if (age > 120) this.setDragonState(DragonState.IDLE);
            }
            case LIGHT_OF_DESTRUCTION -> {
                if (age > 30) this.setDragonState(DragonState.IDLE);
            }
            case PHOTON_BLASTER -> {
                if (age > 70) this.setDragonState(DragonState.IDLE);
            }
            case BLASTER_TACKLE -> {
                if (age > 20) this.setDragonState(DragonState.IDLE);
            }
            case FLY_SHOT -> {
                if (age > 10) this.setDragonState(DragonState.FLY);
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

        if (!active) {
            if (vfx != null) {
                vfx.updateVfx(0.0F, 0.0F);
            }
            return;
        }

        Vec3 axisX = box.obb().axisX().normalize();
        Vec3 axisY = box.obb().axisY().normalize();
        Vec3 axisZ = box.obb().axisZ().normalize();

        Vec3 start = box.obb().center()
                .add(axisX.scale(spec.offsetX()))
                .add(axisY.scale(spec.offsetY()))
                .add(axisZ.scale(spec.offsetZ()));

        Vec3 rayDir = axisY.normalize();

        float length = spec.length();

        if (spec.type() == TedVfxType.TED_LASER_BEAM) {
            length = (float) raycastLaserLength(
                    serverLevel,
                    start,
                    rayDir,
                    spec.length()
            );
        }

        TedBeamHitbox beam = new TedBeamHitbox(
                start,
                rayDir,
                length,
                spec.radius()
        );

        beam.damageEntities(
                serverLevel,
                this,
                spec.damage(),
                entity -> entity != this
        );

        switch (spec.type()) {
            case TED_LASER_BEAM ->{
                System.out.println("LASER");
                    beam.spawnLaserParticles(serverLevel);
            }
            case TED_JET -> {
                beam.spawnJetParticles(serverLevel);
                placeTemporaryLight(serverLevel, start);
            }
        }

        if (vfx != null) {
            vfx.updateVfx(0.0F, 0.0F);
        }
    }

    private void placeTemporaryLight(ServerLevel level, Vec3 pos) {
        BlockPos blockPos = BlockPos.containing(pos);

        level.setBlock(
                blockPos,
                Blocks.LIGHT.defaultBlockState()
                        .setValue(LightBlock.LEVEL, 15),
                3
        );
    }


}