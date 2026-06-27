package com.licht_meilleur.the_end_of_dragon.entity;

import com.licht_meilleur.the_end_of_dragon.entity.hitbox.DragonLocatorSampler;
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

    private void tickAttackVfx() {
        if (!(this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
            return;
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
                    spawnLaser(serverLevel, com.licht_meilleur.the_end_of_dragon.entity.hitbox.DragonLocators.MOUTH);

                    spawnLaser(serverLevel, com.licht_meilleur.the_end_of_dragon.entity.hitbox.DragonLocators.FRONT_LEFT_JET);
                    spawnLaser(serverLevel, com.licht_meilleur.the_end_of_dragon.entity.hitbox.DragonLocators.FRONT_RIGHT_JET);
                    spawnLaser(serverLevel, com.licht_meilleur.the_end_of_dragon.entity.hitbox.DragonLocators.BACK_LEFT_JET);
                    spawnLaser(serverLevel, com.licht_meilleur.the_end_of_dragon.entity.hitbox.DragonLocators.BACK_RIGHT_JET);
                }
            }

            case FLAMES_OF_RAGNAROK -> {


                // レーザー部分
                if (age == 120) {
                    spawnLaser(serverLevel, com.licht_meilleur.the_end_of_dragon.entity.hitbox.DragonLocators.FRONT_LEFT_JET);
                    spawnLaser(serverLevel, com.licht_meilleur.the_end_of_dragon.entity.hitbox.DragonLocators.FRONT_RIGHT_JET);
                    spawnLaser(serverLevel, com.licht_meilleur.the_end_of_dragon.entity.hitbox.DragonLocators.BACK_LEFT_JET);
                    spawnLaser(serverLevel, com.licht_meilleur.the_end_of_dragon.entity.hitbox.DragonLocators.BACK_RIGHT_JET);
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
                    spawnJet(serverLevel, com.licht_meilleur.the_end_of_dragon.entity.hitbox.DragonLocators.FRONT_LEFT_JET);
                    spawnJet(serverLevel, com.licht_meilleur.the_end_of_dragon.entity.hitbox.DragonLocators.FRONT_RIGHT_JET);
                    spawnJet(serverLevel, com.licht_meilleur.the_end_of_dragon.entity.hitbox.DragonLocators.BACK_LEFT_JET);
                    spawnJet(serverLevel, com.licht_meilleur.the_end_of_dragon.entity.hitbox.DragonLocators.BACK_RIGHT_JET);
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
                    spawnJet(serverLevel, com.licht_meilleur.the_end_of_dragon.entity.hitbox.DragonLocators.FRONT_LEFT_JET);
                    spawnJet(serverLevel, com.licht_meilleur.the_end_of_dragon.entity.hitbox.DragonLocators.FRONT_RIGHT_JET);
                    spawnJet(serverLevel, com.licht_meilleur.the_end_of_dragon.entity.hitbox.DragonLocators.BACK_LEFT_JET);
                    spawnJet(serverLevel, com.licht_meilleur.the_end_of_dragon.entity.hitbox.DragonLocators.BACK_RIGHT_JET);
                }

            }
            case FLY_LEFT -> {
                // ジェットは攻撃中ずっと短寿命VFXを出し続ける
                if (age % 2 == 0) {
                    spawnJet(serverLevel, com.licht_meilleur.the_end_of_dragon.entity.hitbox.DragonLocators.FRONT_LEFT_JET);
                    spawnJet(serverLevel, com.licht_meilleur.the_end_of_dragon.entity.hitbox.DragonLocators.FRONT_RIGHT_JET);
                    spawnJet(serverLevel, com.licht_meilleur.the_end_of_dragon.entity.hitbox.DragonLocators.BACK_LEFT_JET);
                    spawnJet(serverLevel, com.licht_meilleur.the_end_of_dragon.entity.hitbox.DragonLocators.BACK_RIGHT_JET);
                }

            }
            case FLY_RIGHT -> {
                // ジェットは攻撃中ずっと短寿命VFXを出し続ける
                if (age % 2 == 0) {
                    spawnJet(serverLevel, com.licht_meilleur.the_end_of_dragon.entity.hitbox.DragonLocators.FRONT_LEFT_JET);
                    spawnJet(serverLevel, com.licht_meilleur.the_end_of_dragon.entity.hitbox.DragonLocators.FRONT_RIGHT_JET);
                    spawnJet(serverLevel, com.licht_meilleur.the_end_of_dragon.entity.hitbox.DragonLocators.BACK_LEFT_JET);
                    spawnJet(serverLevel, com.licht_meilleur.the_end_of_dragon.entity.hitbox.DragonLocators.BACK_RIGHT_JET);
                }

            }

            default -> {
            }
        }
    }

    private void spawnLaser(ServerLevel level, String locator) {
        Vec3 start = DragonLocatorSampler.worldPos(this, locator);
        Vec3 dir = DragonLocatorSampler.forward(this);

        double maxLength = 64.0D;
        Vec3 end = start.add(dir.scale(maxLength));

        var hit = level.clip(new net.minecraft.world.level.ClipContext(
                start,
                end,
                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE,
                this
        ));

        double length = hit.getLocation().distanceTo(start);

        TedVfxSpawner.spawnAt(
                level,
                start.add(dir.scale(length * 0.5D)),
                this.getYRot(),
                this.getXRot(),
                TedVfxType.TED_LASER_BEAM,
                1.0F,
                (float) length,
                20
        );
    }

    private void spawnJet(net.minecraft.server.level.ServerLevel serverLevel, String locator) {
        var pos = com.licht_meilleur.the_end_of_dragon.entity.hitbox.DragonLocatorSampler.worldPos(this, locator);

        System.out.println("[TED VFX JET] locator=" + locator + " pos=" + pos);

        com.licht_meilleur.the_end_of_dragon.entity.vfx.TedVfxSpawner.spawnAt(
                serverLevel,
                pos,
                this.getYRot(),
                this.getXRot(),
                com.licht_meilleur.the_end_of_dragon.entity.vfx.TedVfxType.TED_JET,
                1.5F,
                5.0F,
                20
        );
    }
    private void tickAttackStateTimeout() {
        int age = this.getDragonStateAgeTicks();

        switch (this.getDragonState()) {
            case ORB_OF_ANNIHILATION -> {
                if (age > 33) this.setDragonState(DragonState.IDLE);
            }
            case ROAR_OF_OBLITERATION -> {
                if (age > 36) this.setDragonState(DragonState.IDLE);
            }
            case FLAMES_OF_RAGNAROK -> {
                if (age > 120) this.setDragonState(DragonState.IDLE);
            }
            case LIGHT_OF_DESTRUCTION -> {
                if (age > 30) this.setDragonState(DragonState.IDLE);
            }
            case PHOTON_BLASTER -> {
                if (age > 36) this.setDragonState(DragonState.IDLE);
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