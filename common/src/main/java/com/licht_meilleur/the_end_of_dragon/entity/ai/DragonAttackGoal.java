package com.licht_meilleur.the_end_of_dragon.entity.ai;

import com.licht_meilleur.the_end_of_dragon.entity.DragonState;
import com.licht_meilleur.the_end_of_dragon.entity.TheEndOfDragonCoreEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class DragonAttackGoal extends Goal {
    private final TheEndOfDragonCoreEntity dragon;

    private int cooldown = 40;

    private boolean figureEightActive = false;
    private int figureEightTicks = 0;
    private int figureEightShotCooldown = 0;

    private boolean ragnarokRequested = false;

    private int figureEightStraightShots = 0;
    private boolean wasInShotWindow = false;


    public DragonAttackGoal(TheEndOfDragonCoreEntity dragon) {
        this.dragon = dragon;
        this.setFlags(EnumSet.of(Flag.LOOK)); // MOVEを外す
    }

    @Override
    public boolean canUse() {
        if (dragon.level().isClientSide()) {
            return false;
        }

        return dragon.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        return dragon.isAlive();
    }

    @Override
    public void tick() {

        dragon.setAttackMovementLocked(figureEightActive || ragnarokRequested);

        if (!(dragon.level() instanceof ServerLevel level)) {
            return;
        }

        if (dragon.isIntroStateNow()) {
            return;
        }

        LivingEntity target = dragon.findBossTarget(level);

        if (target == null || !target.isAlive()) {
            dragon.setDragonState(DragonState.IDLE);
            return;
        }

        if (ragnarokRequested) {
            updateRagnarok(level);
            return;
        }

        if (figureEightActive) {
            updateFigureEight(level, target);
            return;
        }

        if (dragon.isAttackStateNow()) {
            return;
        }

        if (--cooldown > 0) {
            return;
        }

        cooldown = 120 + dragon.getRandom().nextInt(40);
        chooseAttack(level, target);
    }

    private void chooseAttack(ServerLevel level, LivingEntity target) {
        int roll = dragon.getRandom().nextInt(7);

        switch (roll) {
            case 0 -> dragon.setDragonState(DragonState.ORB_OF_ANNIHILATION);
            case 1 -> dragon.setDragonState(DragonState.ROAR_OF_OBLITERATION);
            case 2 -> dragon.setDragonState(DragonState.PHOTON_BLASTER);
            case 3 -> dragon.setDragonState(DragonState.LIGHT_OF_DESTRUCTION);
            case 4 -> startFigureEight();
            case 5 -> startRagnarok();
            case 6 -> dragon.setDragonState(DragonState.BLASTER_TACKLE);
        }
    }



    private void startFigureEight() {
        figureEightActive = true;
        figureEightTicks = 0;
        figureEightShotCooldown = 12;
        dragon.setDragonState(DragonState.FLY_START);
    }

    private void updateFigureEight(ServerLevel level, LivingEntity target) {
        figureEightTicks++;

        if (dragon.getDragonState() == DragonState.FLY_START) {
            return;
        }

        if (dragon.getDragonState() != DragonState.FLY
                && dragon.getDragonState() != DragonState.FLY_SHOT) {
            dragon.setDragonState(DragonState.FLY);
            return;
        }

        Vec3 center = dragon.arenaCenter(level).add(0.0D, 34.0D, 0.0D);

        double t = figureEightTicks * 0.075D;
        double x = center.x + Math.sin(t) * 360.0D;
        double z = center.z + Math.sin(t * 2.0D) * 230.0D;
        double y = center.y + Math.cos(t * 2.0D) * 40.0D;

        Vec3 move = new Vec3(x, y, z).subtract(dragon.position());

        if (move.lengthSqr() > 1.0E-6D) {
            dragon.moveBossBy(level, move.normalize().scale(Math.min(20.0D, move.length())));
        }

        double sin = Math.sin(t);
        double cos = Math.cos(t);

// 中心へ向かう直線気味の区間
        boolean shotWindow = Math.abs(sin) < 0.65D && cos > 0.0D;

        if (shotWindow && !wasInShotWindow) {
            figureEightStraightShots = 0;
        }

        wasInShotWindow = shotWindow;

        if (shotWindow
                && dragon.getDragonState() == DragonState.FLY
                && figureEightStraightShots < 4
                && --figureEightShotCooldown <= 0) {
            figureEightStraightShots++;
            figureEightShotCooldown = 8;
            dragon.setDragonState(DragonState.FLY_SHOT);
            return;
        }

        if (dragon.getDragonState() == DragonState.FLY && --figureEightShotCooldown <= 0) {
            figureEightShotCooldown = 20 + dragon.getRandom().nextInt(12);
            dragon.setDragonState(DragonState.FLY_SHOT);
            return;
        }

        if (figureEightTicks >= 340) {
            figureEightActive = false;
            figureEightTicks = 0;
            figureEightShotCooldown = 0;
            dragon.setDragonState(DragonState.IDLE);

            dragon.setAttackMovementLocked(false);
        }
    }

    private void startRagnarok() {
        ragnarokRequested = true;
        dragon.setDragonState(DragonState.FLY_START);
    }

    private void updateRagnarok(ServerLevel level) {
        if (dragon.getDragonState() == DragonState.FLY_START) {
            return;
        }

        if (dragon.getDragonState() != DragonState.FLY) {
            dragon.setDragonState(DragonState.FLY);
            return;
        }

        Vec3 target = dragon.arenaCenter(level).add(0.0D, 82.0D, 0.0D);
        Vec3 move = target.subtract(dragon.position());

        if (move.lengthSqr() > 4.0D) {
            dragon.moveBossBy(level, move.normalize().scale(Math.min(18.0D, move.length())));
            return;
        }

        ragnarokRequested = false;

        dragon.setAttackMovementLocked(false);

        figureEightStraightShots = 0;
        wasInShotWindow = false;

        dragon.setDragonState(DragonState.FLAMES_OF_RAGNAROK);
    }
}