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

    /*
     * 最初の予備動作時間。
     */
    private static final int PREPARE_TICKS = 6;

    /*
     * 各攻撃アニメーション中、
     * ダメージを発生させるtick。
     */
    private static final int PUNCH_HIT_TICK = 7;
    private static final int KICK_HIT_TICK = 8;
    private static final int SMASH_HIT_TICK = 10;

    /*
     * 各攻撃フェーズの終了tick。
     */
    private static final int PUNCH_END_TICK = 13;
    private static final int KICK_END_TICK = 15;
    private static final int SMASH_END_TICK = 18;

    /*
     * 攻撃間の短いワープ準備時間。
     */
    private static final int COMBO_WARP_DELAY = 3;

    /*
     * エンダーマンは補助役なので低火力。
     *
     * 合計:
     * 2 + 2.5 + 4 = 8.5
     */
    private static final float PUNCH_DAMAGE = 2.0F;
    private static final float KICK_DAMAGE = 2.5F;
    private static final float SMASH_DAMAGE = 4.0F;

    /*
     * 龍は大型なので、
     * 攻撃成立距離は少し広くする。
     */
    private static final double ATTACK_RANGE_SQR =
            10.0D * 10.0D;

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

        return this.phase
                != Phase.FINISHED;
    }

    @Override
    public void start() {
        this.phase =
                Phase.PREPARE;

        this.phaseTicks = 0;
        this.hitDone = false;

        this.ally.getNavigation().stop();

        this.ally.setDeltaMovement(
                Vec3.ZERO
        );

        this.ally.setAllyState(
                AllyEndermanState
                        .ATTACK_WARP_PREPARE
        );
    }

    @Override
    public void tick() {
        if (!(this.ally.level()
                instanceof ServerLevel level)) {
            this.phase =
                    Phase.FINISHED;

            return;
        }

        if (this.dragon == null
                || !this.dragon.isAlive()
                || this.dragon.isRemoved()) {
            this.phase =
                    Phase.FINISHED;

            return;
        }

        /*
         * 龍が危険攻撃へ入ったら、
         * コンボを中断して即離脱。
         */
        if (isUnsafeAttackState(
                this.dragon.getDragonState()
        )) {
            performRetreat(level);

            this.phase =
                    Phase.FINISHED;

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

            case PUNCH ->
                    tickPunch(level);

            case WARP_TO_KICK ->
                    tickWarpToKick(level);

            case KICK ->
                    tickKick(level);

            case WARP_TO_SMASH ->
                    tickWarpToSmash(level);

            case SMASH ->
                    tickSmash(level);

            case RETREAT ->
                    tickRetreat(level);

            case FINISHED -> {
            }
        }
    }

    /*
     * 最初の攻撃位置へワープ。
     */
    private void tickPrepare(
            ServerLevel level
    ) {
        this.ally.getNavigation().stop();

        this.ally.setDeltaMovement(
                Vec3.ZERO
        );

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
            performRetreat(level);

            this.phase =
                    Phase.FINISHED;

            return;
        }

        AllyEndermanAiUtil.faceTarget(
                this.ally,
                this.dragon
        );

        beginPhase(
                Phase.PUNCH,
                AllyEndermanState.WARP_PUNCH
        );
    }

    private void tickPunch(
            ServerLevel level
    ) {
        holdAttackPosition();

        if (!this.hitDone
                && this.phaseTicks
                >= PUNCH_HIT_TICK) {

            this.hitDone = true;

            damageDragon(
                    level,
                    PUNCH_DAMAGE
            );
        }

        if (this.phaseTicks
                >= PUNCH_END_TICK) {

            beginPhase(
                    Phase.WARP_TO_KICK,
                    AllyEndermanState
                            .ATTACK_WARP_PREPARE
            );
        }
    }

    private void tickWarpToKick(
            ServerLevel level
    ) {
        holdAttackPosition();

        if (this.phaseTicks
                < COMBO_WARP_DELAY) {
            return;
        }

        Vec3 kickPosition =
                findSideAttackPosition(
                        level,
                        false
                );

        if (!AllyEndermanAiUtil.teleportAlly(
                this.ally,
                level,
                kickPosition
        )) {
            performRetreat(level);

            this.phase =
                    Phase.FINISHED;

            return;
        }

        AllyEndermanAiUtil.faceTarget(
                this.ally,
                this.dragon
        );

        beginPhase(
                Phase.KICK,
                AllyEndermanState.WARP_KICK
        );
    }

    private void tickKick(
            ServerLevel level
    ) {
        holdAttackPosition();

        if (!this.hitDone
                && this.phaseTicks
                >= KICK_HIT_TICK) {

            this.hitDone = true;

            damageDragon(
                    level,
                    KICK_DAMAGE
            );
        }

        if (this.phaseTicks
                >= KICK_END_TICK) {

            beginPhase(
                    Phase.WARP_TO_SMASH,
                    AllyEndermanState
                            .ATTACK_WARP_PREPARE
            );
        }
    }

    private void tickWarpToSmash(
            ServerLevel level
    ) {
        holdAttackPosition();

        if (this.phaseTicks
                < COMBO_WARP_DELAY) {
            return;
        }

        Vec3 smashPosition =
                findSmashPosition(
                        level
                );

        if (!AllyEndermanAiUtil.teleportAlly(
                this.ally,
                level,
                smashPosition
        )) {
            performRetreat(level);

            this.phase =
                    Phase.FINISHED;

            return;
        }

        AllyEndermanAiUtil.faceTarget(
                this.ally,
                this.dragon
        );

        beginPhase(
                Phase.SMASH,
                AllyEndermanState.WARP_SMASH
        );
    }

    private void tickSmash(
            ServerLevel level
    ) {
        holdAttackPosition();

        if (!this.hitDone
                && this.phaseTicks
                >= SMASH_HIT_TICK) {

            this.hitDone = true;

            damageDragon(
                    level,
                    SMASH_DAMAGE
            );
        }

        if (this.phaseTicks
                >= SMASH_END_TICK) {

            beginPhase(
                    Phase.RETREAT,
                    AllyEndermanState
                            .ATTACK_WARP_PREPARE
            );
        }
    }

    private void tickRetreat(
            ServerLevel level
    ) {
        performRetreat(level);

        this.phase =
                Phase.FINISHED;
    }

    private void beginPhase(
            Phase nextPhase,
            AllyEndermanState state
    ) {
        this.phase =
                nextPhase;

        this.phaseTicks = 0;
        this.hitDone = false;

        this.ally.getNavigation().stop();

        this.ally.setDeltaMovement(
                Vec3.ZERO
        );

        this.ally.setAllyState(
                state
        );
    }

    private void holdAttackPosition() {
        this.ally.getNavigation().stop();

        this.ally.setDeltaMovement(
                Vec3.ZERO
        );

        AllyEndermanAiUtil.faceTarget(
                this.ally,
                this.dragon
        );
    }

    private void damageDragon(
            ServerLevel level,
            float damage
    ) {
        if (this.dragon == null
                || !this.dragon.isAlive()) {
            return;
        }

        if (this.ally.distanceToSqr(
                this.dragon
        ) > ATTACK_RANGE_SQR) {
            return;
        }

        this.dragon.hurtServer(
                level,
                level.damageSources()
                        .mobAttack(this.ally),
                damage
        );
    }

    /*
     * 2段目は、
     * 現在位置とは反対側を優先して探す。
     */
    private Vec3 findSideAttackPosition(
            ServerLevel level,
            boolean clockwise
    ) {
        Vec3 dragonPos =
                this.dragon.position();

        Vec3 fromDragon =
                this.ally.position()
                        .subtract(
                                dragonPos
                        );

        Vec3 horizontal =
                new Vec3(
                        fromDragon.x,
                        0.0D,
                        fromDragon.z
                );

        if (horizontal.horizontalDistanceSqr()
                < 1.0E-6D) {
            horizontal =
                    new Vec3(
                            1.0D,
                            0.0D,
                            0.0D
                    );
        } else {
            horizontal =
                    horizontal.normalize();
        }

        /*
         * 現在位置の反対側。
         */
        Vec3 opposite =
                horizontal.scale(
                        -1.0D
                );

        /*
         * 少し横へずらすことで、
         * 同じ直線上だけのワープを避ける。
         */
        Vec3 sideOffset;

        if (clockwise) {
            sideOffset =
                    new Vec3(
                            -opposite.z,
                            0.0D,
                            opposite.x
                    );
        } else {
            sideOffset =
                    new Vec3(
                            opposite.z,
                            0.0D,
                            -opposite.x
                    );
        }

        Vec3 desiredCenter =
                dragonPos
                        .add(
                                opposite.scale(
                                        4.5D
                                )
                        )
                        .add(
                                sideOffset.scale(
                                        2.0D
                                )
                        );

        return AllyEndermanAiUtil
                .findSafePositionAround(
                        this.ally,
                        level,
                        desiredCenter,
                        1.0D,
                        3.0D
                );
    }

    /*
     * 3段目は、
     * 龍の少し高い位置または背後から狙う。
     */
    private Vec3 findSmashPosition(
            ServerLevel level
    ) {
        Vec3 dragonPos =
                this.dragon.position();

        Vec3 away =
                this.ally.position()
                        .subtract(
                                dragonPos
                        );

        away =
                new Vec3(
                        away.x,
                        0.0D,
                        away.z
                );

        if (away.horizontalDistanceSqr()
                < 1.0E-6D) {
            away =
                    new Vec3(
                            0.0D,
                            0.0D,
                            1.0D
                    );
        } else {
            away =
                    away.normalize();
        }

        Vec3 desiredCenter =
                dragonPos
                        .add(
                                away.scale(
                                        -3.5D
                                )
                        )
                        .add(
                                0.0D,
                                2.0D,
                                0.0D
                        );

        return AllyEndermanAiUtil
                .findSafePositionAround(
                        this.ally,
                        level,
                        desiredCenter,
                        1.0D,
                        3.0D
                );
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
                                    away.scale(
                                            14.0D
                                    )
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
                        + this.ally
                        .getRandom()
                        .nextInt(61)
        );

        this.ally.getNavigation().stop();

        if (this.ally.isAlive()) {
            this.ally.setAllyState(
                    AllyEndermanState
                            .SUPPORT_IDLE
            );
        }

        this.dragon = null;
        this.player = null;

        this.phase =
                Phase.PREPARE;

        this.phaseTicks = 0;
        this.hitDone = false;
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
        PUNCH,
        WARP_TO_KICK,
        KICK,
        WARP_TO_SMASH,
        SMASH,
        RETREAT,
        FINISHED
    }
}