package com.licht_meilleur.the_end_of_dragon.entity.hitbox;

import com.licht_meilleur.the_end_of_dragon.entity.TheEndOfDragonEntity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class DragonHitBoxEntity extends Entity {


    private static final EntityDataAccessor<Float> WIDTH =
            SynchedEntityData.defineId(DragonHitBoxEntity.class, EntityDataSerializers.FLOAT);

    private static final EntityDataAccessor<Float> HEIGHT =
            SynchedEntityData.defineId(DragonHitBoxEntity.class, EntityDataSerializers.FLOAT);


    private TheEndOfDragonEntity parent;
    private UUID parentUuid;

    private String partName = "unknown";
    private Vec3 localOffset = Vec3.ZERO;
    private double width = 1.0D;
    private double height = 1.0D;
    private double damageMultiplier = 1.0D;

    public DragonHitBoxEntity(EntityType<? extends DragonHitBoxEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public void setup(TheEndOfDragonEntity parent, String partName, Vec3 localOffset, double width, double height, double damageMultiplier) {
        this.parent = parent;
        this.parentUuid = parent.getUUID();
        this.partName = partName;
        this.localOffset = localOffset;
        this.width = width;
        this.height = height;
        this.damageMultiplier = damageMultiplier;
        this.setNoGravity(true);
        this.noPhysics = true;
        this.syncToParent();
        this.entityData.set(WIDTH, (float) width);
        this.entityData.set(HEIGHT, (float) height);
        this.refreshDimensions();

    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            return;
        }

        if (this.parent == null && this.parentUuid != null && this.level() instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(this.parentUuid);
            if (entity instanceof TheEndOfDragonEntity dragon) {
                this.parent = dragon;
            }
        }

        if (this.parent == null || !this.parent.isAlive()) {
            this.discard();
            return;
        }

        this.syncToParent();

        if (this.tickCount % 100 == 0) {
            System.out.println("[TED HITBOX] " + this.partName);
        }
    }

    private void syncToParent() {
        if (this.parent == null) {
            return;
        }

        Vec3 center = rotateYaw(this.localOffset, this.parent.getYRot()).add(this.parent.position());

        this.setPos(center.x, center.y, center.z);

        double w = this.entityData.get(WIDTH);
        double h = this.entityData.get(HEIGHT);
        double half = w / 2.0D;

        this.setBoundingBox(new AABB(
                center.x - half,
                center.y - h / 2.0D,
                center.z - half,
                center.x + half,
                center.y + h / 2.0D,
                center.z + half
        ));
    }

    private static Vec3 rotateYaw(Vec3 local, float yawDegrees) {
        double rad = Math.toRadians(yawDegrees);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);

        double x = local.x * cos - local.z * sin;
        double z = local.x * sin + local.z * cos;

        return new Vec3(x, local.y, z);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (this.parent == null || !this.parent.isAlive()) {
            return false;
        }

        float finalDamage = (float) (amount * this.damageMultiplier);
        System.out.println("[TED] hitbox hit: " + this.partName + " damage=" + finalDamage);

        return this.parent.hurtServer(level, source, finalDamage);
    }

    @Override
    public boolean isPickable() {
        return true;
    }


    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.fixed(
                this.entityData.get(WIDTH),
                this.entityData.get(HEIGHT)
        );
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(WIDTH, 1.0F);
        builder.define(HEIGHT, 1.0F);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
    }


}

