package com.licht_meilleur.the_end_of_dragon.entity.ai;

import com.licht_meilleur.the_end_of_dragon.entity.DragonState;
import com.licht_meilleur.the_end_of_dragon.entity.TheEndOfDragonCoreEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class DragonMoveGoal extends Goal {
    private final TheEndOfDragonCoreEntity dragon;

    private int flyShotCooldown = 30;

    public DragonMoveGoal(TheEndOfDragonCoreEntity dragon) {
        this.dragon = dragon;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {

        if (dragon.isAttackMovementLocked()) {
            return false;
        }

        if (!(dragon.level() instanceof ServerLevel level)) {
            return false;
        }

        if (dragon.isIntroStateNow()) {
            return false;
        }

        if (dragon.isAttackStateNow()) {
            return false;
        }

        if (dragon.getDragonState() == DragonState.FLY_START
                || dragon.getDragonState() == DragonState.FLY_SHOT
                || dragon.getDragonState() == DragonState.FALL
                || dragon.getDragonState() == DragonState.LANDING) {
            return false;
        }

        return dragon.findBossTarget(level) != null;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void tick() {
        if (!(dragon.level() instanceof ServerLevel level)) {
            return;
        }

        LivingEntity target = dragon.findBossTarget(level);
        if (target == null || !target.isAlive()) {
            dragon.setDragonState(DragonState.IDLE);
            return;
        }

        Vec3 toTarget = target.position().subtract(dragon.position());
        double distance = toTarget.length();

        if (distance > 42.0D) {
            dragon.setDragonState(DragonState.FLY);

            tryShootWhileFlying(level, target);

            Vec3 desired = target.position().add(0.0D, 20.0D, 0.0D);
            Vec3 move = desired.subtract(dragon.position());

            if (move.lengthSqr() > 1.0E-6D) {
                dragon.moveBossBy(level, move.normalize().scale(Math.min(10.0D, move.length())));
            }
            return;
        }

        if (distance < 18.0D) {
            // distance < 18 のところ
            dragon.setDragonState(DragonState.FLY);

            Vec3 away = dragon.position().subtract(target.position());
            Vec3 move = new Vec3(away.x, 0.0D, away.z);

            if (move.lengthSqr() > 1.0E-6D) {
                dragon.moveBossBy(level, move.normalize().scale(0.35D));
            }
            return;
        }

        // 近すぎず遠すぎずなら横移動
        dragon.setDragonState(DragonState.FLY);

        Vec3 side = new Vec3(-toTarget.z, 0.0D, toTarget.x);

        if (side.lengthSqr() > 1.0E-6D) {
            side = side.normalize();

            double sign = (dragon.tickCount / 80) % 2 == 0 ? 1.0D : -1.0D;
            Vec3 move = side.scale(0.30D * sign);

            dragon.moveBossBy(level, move);
            dragon.setBossYawOnly(toTarget);
        }
    }

    private void tryShootWhileFlying(ServerLevel level, LivingEntity target) {
        if (dragon.getDragonState() != DragonState.FLY) {
            return;
        }

        if (--flyShotCooldown > 0) {
            return;
        }

        flyShotCooldown = 25 + dragon.getRandom().nextInt(20);
        dragon.setDragonState(DragonState.FLY_SHOT);
    }
}