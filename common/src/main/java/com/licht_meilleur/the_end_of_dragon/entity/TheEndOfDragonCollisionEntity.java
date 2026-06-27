package com.licht_meilleur.the_end_of_dragon.entity;

import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.licht_meilleur.the_end_of_dragon.entity.collision.DragonCollisionBox;
import com.licht_meilleur.the_end_of_dragon.entity.collision.DragonCollisionPart;
import com.licht_meilleur.the_end_of_dragon.entity.collision.DragonCollisionSampler;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

public class TheEndOfDragonCollisionEntity extends TheEndOfDragonEntity {
    public TheEndOfDragonCollisionEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }


    @Override
    public void tick() {
        super.tick();

        syncRotationFromParent();

        if (!this.level().isClientSide()) {
            for (DragonCollisionBox collisionBox : this.getCollisionBoxes()) {
                var players = this.level().getEntitiesOfClass(
                        net.minecraft.world.entity.player.Player.class,
                        collisionBox.box()
                );

                for (var player : players) {
                    if (!collisionBox.obb().intersects(player.getBoundingBox())) {
                        continue;
                    }

                    player.hurtServer(
                            (net.minecraft.server.level.ServerLevel) this.level(),
                            this.damageSources().mobAttack(this),
                            1.0F
                    );

                    //System.out.println("[TED HIT OBB] part=" + collisionBox.part() + " player=" + player.getName().getString());
                }
            }
        }

        if (!this.level().isClientSide()) {
            this.debugDrawCollisionBoxes();
            this.pushPlayersOutOfCollisionBoxes();
        }
    }

    private void syncRotationFromParent() {
        if (!(this.getVehicle() instanceof TheEndOfDragonCoreEntity parent)) {
            return;
        }

        float yaw = parent.getYRot();

        this.setYRot(yaw);
        this.setYBodyRot(yaw);
        this.setYHeadRot(yaw);

        this.yRotO = parent.yRotO;
        this.yBodyRotO = parent.yBodyRotO;
        this.yHeadRotO = parent.yHeadRotO;
    }

    public List<DragonCollisionBox> getCollisionBoxes() {
        return DragonCollisionSampler.buildBoxes(this);
    }

    private void pushPlayersOutOfCollisionBoxes() {
        if (this.level().isClientSide()) {
            return;
        }

        for (DragonCollisionBox collisionBox : this.getCollisionBoxes()) {
            AABB box = collisionBox.box();

            var players = this.level().getEntitiesOfClass(
                    net.minecraft.world.entity.player.Player.class,
                    box.inflate(0.05D)
            );

            for (var player : players) {
                AABB playerBox = player.getBoundingBox();

                if (!collisionBox.obb().intersects(playerBox)) {
                    continue;
                }

                double pushX1 = Math.abs(playerBox.maxX - box.minX);
                double pushX2 = Math.abs(box.maxX - playerBox.minX);
                double pushZ1 = Math.abs(playerBox.maxZ - box.minZ);
                double pushZ2 = Math.abs(box.maxZ - playerBox.minZ);

                double minPush = Math.min(
                        Math.min(pushX1, pushX2),
                        Math.min(pushZ1, pushZ2)
                );

                double pushX = 0.0D;
                double pushZ = 0.0D;

                if (minPush == pushX1) {
                    pushX = -(pushX1 + 0.05D);
                } else if (minPush == pushX2) {
                    pushX = pushX2 + 0.05D;
                } else if (minPush == pushZ1) {
                    pushZ = -(pushZ1 + 0.05D);
                } else {
                    pushZ = pushZ2 + 0.05D;
                }

                player.setDeltaMovement(
                        player.getDeltaMovement().add(pushX * 0.35D, 0.0D, pushZ * 0.35D)
                );

                player.hurtMarked = true;

                //System.out.println("[TED PUSH] part=" + collisionBox.part());
            }
        }
    }

    private void debugDrawCollisionBoxes() {
        if (!(this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
            return;
        }

        if (this.tickCount % 2 != 0) {
            return;
        }

        var boxes = this.getCollisionBoxes();

        if (this.tickCount % 40 == 0) {
            //System.out.println("[TED COLLISION DEBUG] boxes=" + boxes.size());
        }

        for (var collisionBox : boxes) {
            drawAabb(serverLevel, collisionBox.box());
        }
    }

    private void drawAabb(net.minecraft.server.level.ServerLevel level, net.minecraft.world.phys.AABB box) {
        double step = 0.35D;

        drawLine(level, box.minX, box.minY, box.minZ, box.maxX, box.minY, box.minZ, step);
        drawLine(level, box.minX, box.minY, box.maxZ, box.maxX, box.minY, box.maxZ, step);
        drawLine(level, box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.minZ, step);
        drawLine(level, box.minX, box.maxY, box.maxZ, box.maxX, box.maxY, box.maxZ, step);

        drawLine(level, box.minX, box.minY, box.minZ, box.minX, box.maxY, box.minZ, step);
        drawLine(level, box.maxX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ, step);
        drawLine(level, box.minX, box.minY, box.maxZ, box.minX, box.maxY, box.maxZ, step);
        drawLine(level, box.maxX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ, step);

        drawLine(level, box.minX, box.minY, box.minZ, box.minX, box.minY, box.maxZ, step);
        drawLine(level, box.maxX, box.minY, box.minZ, box.maxX, box.minY, box.maxZ, step);
        drawLine(level, box.minX, box.maxY, box.minZ, box.minX, box.maxY, box.maxZ, step);
        drawLine(level, box.maxX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ, step);
    }

    private void drawLine(
            net.minecraft.server.level.ServerLevel level,
            double x1, double y1, double z1,
            double x2, double y2, double z2,
            double step
    ) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;

        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        int count = Math.max(1, (int) (length / step));

        for (int i = 0; i <= count; i++) {
            double t = (double) i / (double) count;

            level.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.END_ROD,
                    x1 + dx * t,
                    y1 + dy * t,
                    z1 + dz * t,
                    1,
                    0.0D,
                    0.0D,
                    0.0D,
                    0.0D
            );
        }
    }

}