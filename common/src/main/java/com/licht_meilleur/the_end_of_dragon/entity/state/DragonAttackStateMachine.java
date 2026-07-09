package com.licht_meilleur.the_end_of_dragon.entity.state;

import com.licht_meilleur.the_end_of_dragon.entity.DragonState;
import com.licht_meilleur.the_end_of_dragon.entity.TheEndOfDragonCoreEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public class DragonAttackStateMachine {
    private final TheEndOfDragonCoreEntity dragon;

    private enum AirMode {
        NONE,
        RAGNAROK,
        FIGURE,
        INTRO
    }

    private AirMode airMode = AirMode.NONE;

    private int ascendTicks = 20;

    private int figureEightTicks = 0;
    private int figureEightShotCooldown = 0;
    private int figureEightStraightShots = 0;
    private boolean wasInShotWindow = false;

    public void startIntroAscend() {
        airMode = AirMode.INTRO;
        ascendTicks = 20;

        dragon.setAttackMovementLocked(true);
        dragon.setDragonState(DragonState.FLY_ASCEND);
    }

    public DragonAttackStateMachine(TheEndOfDragonCoreEntity dragon) {
        this.dragon = dragon;
    }

    public void startRagnarok() {
        airMode = AirMode.RAGNAROK;
        ascendTicks = 5;

        dragon.setAttackMovementLocked(true);
        dragon.setDragonState(DragonState.FLY_START);
    }

    public void startFigureEight() {
        airMode = AirMode.FIGURE;
        ascendTicks = 70;

        figureEightTicks = 0;
        figureEightShotCooldown = 12;
        figureEightStraightShots = 0;
        wasInShotWindow = false;

        dragon.setAttackMovementLocked(true);
        dragon.setDragonState(DragonState.FLY_START);
    }

    public void tick(ServerLevel level) {
        int age = dragon.getDragonStateAgeTicks();

        switch (dragon.getDragonState()) {
            case FLY_START -> {
                if (age > 20) {
                    dragon.setDragonState(DragonState.FLY_ASCEND);
                }
            }

            case FLY_ASCEND -> tickAscend(level);

            case FLAMES_OF_RAGNAROK -> {
                if (age > 120) {
                    dragon.setDragonState(DragonState.FALL);
                }
            }

            case FALL -> dragon.descendForRagnarok(level);

            case LANDING -> {
                if (age > 25) {
                    finishAirSequence();
                }
            }

            case FIGURE_EIGHT -> tickFigureEight(level);

            case FLY_DESCEND -> tickFigureDescend(level);

            case SUPER_LANDING -> {
                if (age > 50) {
                    finishAirSequence();
                }
            }

            case TAIL_WHIP -> {
                if (age > 20) {
                    dragon.setDragonState(DragonState.IDLE);
                }
            }


            case ORB_OF_ANNIHILATION -> {
                if (age > 65) dragon.setDragonState(DragonState.IDLE);
            }

            case ROAR_OF_OBLITERATION -> {
                if (age > 40) dragon.setDragonState(DragonState.IDLE);
            }

            case LIGHT_OF_DESTRUCTION -> {
                if (age > 30) dragon.setDragonState(DragonState.IDLE);
            }

            case PHOTON_BLASTER -> {
                if (age > 70) dragon.setDragonState(DragonState.IDLE);
            }

            case BLASTER_TACKLE -> {
                if (age > 20) dragon.setDragonState(DragonState.IDLE);
            }

            default -> {
            }
        }
    }

    private void tickAscend(ServerLevel level) {
        int age = dragon.getDragonStateAgeTicks();

        if (airMode == AirMode.INTRO) {
            dragon.setDragonState(DragonState.INTRO_WAIT_PORTAL);
            return;
        }

        if (age < ascendTicks) {
            dragon.moveBossByNoFace(level, new Vec3(0.0D, 15.0D, 0.0D));
            return;
        }

        if (airMode == AirMode.RAGNAROK) {
            dragon.setDragonState(DragonState.FLAMES_OF_RAGNAROK);
            return;
        }

        if (airMode == AirMode.FIGURE) {
            figureEightTicks = 0;
            dragon.setDragonState(DragonState.FIGURE_EIGHT);
            return;
        }

        finishAirSequence();
    }

    private void tickFigureEight(ServerLevel level) {
        figureEightTicks++;

        Vec3 center = dragon.arenaCenter(level).add(0.0D, 55.0D, 0.0D);

        double t = figureEightTicks * 0.075D;
        double x = center.x + Math.sin(t) * 360.0D;
        double z = center.z + Math.sin(t * 2.0D) * 230.0D;
        double y = center.y + Math.cos(t * 2.0D) * 40.0D;

        Vec3 move = new Vec3(x, y, z).subtract(dragon.position());

        if (move.lengthSqr() > 1.0E-6D) {
            dragon.moveBossBy(level, move.normalize().scale(Math.min(20.0D, move.length())));
        }

        Vec3 centerFlat = new Vec3(center.x, dragon.getY(), center.z);

        Vec3 pos = dragon.position();
        Vec3 target = new Vec3(x, y, z);

        Vec3 toCenter = centerFlat.subtract(pos);
        Vec3 moveDir = target.subtract(pos);

        boolean movingToCenter =
                toCenter.lengthSqr() > 1.0D
                        && moveDir.lengthSqr() > 1.0D
                        && moveDir.normalize().dot(toCenter.normalize()) > 0.65D;

// 中央を超える前だけ
        boolean beforeCenter = toCenter.length() > 35.0D;

// 中心へ向かう途中だけ
        boolean shotWindow =
                movingToCenter
                        && beforeCenter
                        && toCenter.length() < 170.0D
                        && toCenter.length() > 45.0D;

        if (shotWindow && !wasInShotWindow) {
            figureEightStraightShots = 0;
            figureEightShotCooldown = 0;
        }

        wasInShotWindow = shotWindow;

        if (shotWindow
                && figureEightStraightShots < 4
                && --figureEightShotCooldown <= 0) {
            figureEightStraightShots++;
            figureEightShotCooldown = 8;
            dragon.requestFlyShot(level);
        }

        if (figureEightTicks >= 340) {
            dragon.setDragonState(DragonState.FLY_DESCEND);
        }
    }

    private void tickFigureDescend(ServerLevel level) {
        dragon.moveBossByNoFace(level, new Vec3(0.0D, -15.0D, 0.0D));

        if (dragon.isNearGroundForSuperLandingPublic(level)) {
            dragon.setDragonState(DragonState.SUPER_LANDING);
        }
    }

    public void cancelAirSequence() {
        airMode = AirMode.NONE;

        figureEightTicks = 0;
        figureEightShotCooldown = 0;
        figureEightStraightShots = 0;
        wasInShotWindow = false;
    }

    private void finishAirSequence() {
        airMode = AirMode.NONE;

        figureEightTicks = 0;
        figureEightShotCooldown = 0;
        figureEightStraightShots = 0;
        wasInShotWindow = false;

        dragon.setAttackMovementLocked(false);
        dragon.setDragonState(DragonState.IDLE);
    }
}