package com.licht_meilleur.the_end_of_dragon.entity.enderman.goal;

import com.licht_meilleur.the_end_of_dragon.entity.DragonState;
import com.licht_meilleur.the_end_of_dragon.entity.TheEndOfDragonCoreEntity;
import com.licht_meilleur.the_end_of_dragon.entity.enderman.AllyEndermanState;
import com.licht_meilleur.the_end_of_dragon.entity.enderman.TedAllyEndermanEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class AllyHitAndAwayGoal extends Goal {

    private static final int PREPARE_TICKS = 6;
    private static final int ATTACK_HIT_TICK = 10;
    private static final int RETREAT_TICK = 18;

    /*
     * エンダーマンは低火力サポート役。
     */
    private static final float ATTACK_DAMAGE = 3.0F;

    private final TedAllyEndermanEntity ally;

    private TheEndOfDragonCoreEntity dragon;
    private Player player;

    private Phase phase =
            Phase.PREPARE;

    private int phaseTicks;
    private boolean hitDone;

    public AllyHitAndAwayGoal(
            TedAllyEndermanEntity ally
    ) {
        this.ally = ally;

        this.setFlags(
                EnumSet.of(
                        Flag.MOVE,
                        Flag.LOOK
                )
        );
    }

    @Override
    public boolean canUse() {
        if (!(this.ally.level()
                instanceof ServerLevel level)) {
            return false;
        }

        if (!this.ally.canRunSupportAi()) {
            return false;
        }

        if (this.ally.getSupportAttackCooldown()
                > 0) {
            return false;
        }

        TheEndOfDragonCoreEntity foundDragon =
                AllyEndermanAiUtil.findDragon(
                        this.ally,
                        level
                );

        if (foundDragon == null
                || isUnsafeAttackState(
                foundDragon.getDragonState()
        )) {
            return false;
        }

        this.dragon = foundDragon;

        this.player =
                AllyEndermanAiUtil.findPlayer(
                        this.ally,
                        level
                );

        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.dragon == null
                || !this.dragon.isAlive()
                || this.dragon.isRemoved()) {
            return false;
        }

        return switch (this.phase) {
            case PREPARE ->
                    this.phaseTicks
                            < PREPARE_TICKS;

            case ATTACK ->
                    this.phaseTicks
                            < RETREAT_TICK;

            case RETREAT ->
                    false;
        };
    }

    @Override
    public void start() {
        this.phase =
                Phase.PREPARE;

        this.phaseTicks = 0;
        this.hitDone = false;

        this.ally.getNavigation().stop();
        this.ally.setDeltaMovement(Vec3.ZERO);

        this.ally.setAllyState(
                AllyEndermanState
                        .ATTACK_WARP_PREPARE
        );
    }

    @Override
    public void tick() {
        if (!(this.ally.level()
                instanceof ServerLevel level)) {
            return;
        }

        if (this.dragon == null) {
            return;
        }

        this.phaseTicks++;

        AllyEndermanAiUtil.faceTarget(
                this.ally,
                this.dragon
        );

        switch (this.phase) {
            case PREPARE ->
                    tickPrepare(level);

            case ATTACK ->
                    tickAttack(level);

            case RETREAT -> {
            }
        }
    }

    private void tickPrepare(
            ServerLevel level
    ) {
        if (this.phaseTicks
                < PREPARE_TICKS) {
            return;
        }

        Vec3 attackPosition =
                AllyEndermanAiUtil
                        .findAttackPosition(
                                this.ally,
                                level,
                                this.dragon
                        );

        if (!AllyEndermanAiUtil.teleportAlly(
                this.ally,
                level,
                attackPosition
        )) {
            /*
             * 位置が見つからなければ中断。
             */
            this.phase =
                    Phase.RETREAT;

            return;
        }

        AllyEndermanAiUtil.faceTarget(
                this.ally,
                this.dragon
        );

        this.ally.setAllyState(
                chooseAttackState()
        );

        this.phase =
                Phase.ATTACK;

        this.phaseTicks = 0;
    }

    private void tickAttack(
            ServerLevel level
    ) {
        this.ally.getNavigation().stop();
        this.ally.setDeltaMovement(Vec3.ZERO);

        AllyEndermanAiUtil.faceTarget(
                this.ally,
                this.dragon
        );

        if (!this.hitDone
                && this.phaseTicks
                >= ATTACK_HIT_TICK) {

            this.hitDone = true;

            /*
             * 龍の大型判定を考慮して少し広め。
             */
            if (this.ally.distanceToSqr(
                    this.dragon
            ) <= 10.0D * 10.0D) {

                this.dragon.hurtServer(
                        level,
                        level.damageSources()
                                .mobAttack(this.ally),
                        ATTACK_DAMAGE
                );
            }
        }

        if (this.phaseTicks
                >= RETREAT_TICK) {

            performRetreat(
                    level
            );

            this.phase =
                    Phase.RETREAT;
        }
    }

    private void performRetreat(
            ServerLevel level
    ) {
        Vec3 retreatCenter;

        if (this.player != null
                && this.player.isAlive()) {

            retreatCenter =
                    this.player.position();

        } else {
            Vec3 away =
                    this.ally.position()
                            .subtract(
                                    this.dragon.position()
                            );

            if (away.horizontalDistanceSqr()
                    < 1.0E-6D) {
                away =
                        new Vec3(
                                1.0D,
                                0.0D,
                                0.0D
                        );
            } else {
                away =
                        new Vec3(
                                away.x,
                                0.0D,
                                away.z
                        ).normalize();
            }

            retreatCenter =
                    this.ally.position()
                            .add(
                                    away.scale(14.0D)
                            );
        }

        Vec3 destination =
                AllyEndermanAiUtil
                        .findSafePositionAround(
                                this.ally,
                                level,
                                retreatCenter,
                                4.0D,
                                9.0D
                        );

        AllyEndermanAiUtil.teleportAlly(
                this.ally,
                level,
                destination
        );
    }

    @Override
    public void stop() {
        /*
         * 約5～8秒後に再攻撃。
         */
        this.ally.setSupportAttackCooldown(
                100
                        + this.ally.getRandom()
                        .nextInt(61)
        );

        this.ally.getNavigation().stop();

        if (this.ally.isAlive()) {
            this.ally.setAllyState(
                    AllyEndermanState.SUPPORT_IDLE
            );
        }

        this.dragon = null;
        this.player = null;

        this.phase =
                Phase.PREPARE;

        this.phaseTicks = 0;
        this.hitDone = false;
    }

    private AllyEndermanState chooseAttackState() {
        return switch (
                this.ally.getRandom().nextInt(3)
                ) {
            case 0 ->
                    AllyEndermanState.WARP_PUNCH;

            case 1 ->
                    AllyEndermanState.WARP_KICK;

            default ->
                    AllyEndermanState.WARP_SMASH;
        };
    }

    private boolean isUnsafeAttackState(
            DragonState state
    ) {
        return switch (state) {
            case ROAR_OF_OBLITERATION,
                 PHOTON_BLASTER,
                 PHOTON_BUSTER,
                 BLASTER_TACKLE,
                 FLAMES_OF_RAGNAROK,
                 JUDGMENT_RAY,
                 TAIL_WHIP,
                 SUPER_LANDING,
                 ORB_OF_ANNIHILATION,
                 FLY_SHOT,
                 DEAD ->
                    true;

            default ->
                    false;
        };
    }

    private enum Phase {
        PREPARE,
        ATTACK,
        RETREAT
    }
}