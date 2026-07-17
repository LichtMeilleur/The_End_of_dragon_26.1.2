package com.licht_meilleur.the_end_of_dragon.entity.ai;

import com.licht_meilleur.the_end_of_dragon.entity.TheEndOfDragonCoreEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class DragonAirAttackGoal extends Goal {
    private final TheEndOfDragonCoreEntity dragon;

    private static final int BASE_WEIGHT = 100;
    private static final int WEIGHT_INCREASE = 35;
    private static final int MAX_WEIGHT = 300;

    private int ragnarokWeight = BASE_WEIGHT;
    private int figureEightWeight = BASE_WEIGHT;
    private int judgmentRayWeight = 60;
    private int diveStompWeight = 70;

    public DragonAirAttackGoal(TheEndOfDragonCoreEntity dragon) {
        this.dragon = dragon;
        this.setFlags(EnumSet.noneOf(Flag.class));
    }

    @Override
    public boolean canUse() {

        if (dragon.isDebugFrozen()) {
            return false;
        }
        if (dragon.level().isClientSide()) return false;
        if (!dragon.isAlive()) return false;
        if (!dragon.isCombatStarted()) return false;
        if (!(dragon.level() instanceof ServerLevel level)) return false;
        if (dragon.isIntroStateNow()) return false;
        if (dragon.isCombatLocked()) return false;
        if (dragon.isAttackMovementLocked()) return false;
        if (dragon.isAirborneBoss(level)) return false;
        if (dragon.findBossTarget(level) == null) return false;



        return dragon.tryConsumeAirAttackCooldown();
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public void tick() {
        if (!(dragon.level() instanceof ServerLevel level)) {
            return;
        }

        LivingEntity target =
                dragon.findBossTarget(level);

        if (target == null || !target.isAlive()) {
            return;
        }


        double targetAbove =
                target.getEyeY() - dragon.getY();

        boolean targetIsAirborne =
                !target.onGround()
                        && targetAbove >= 4.0D;

        /*
         * 空中滞在者へのメタ対策。
         */
        if (targetIsAirborne) {
            chooseAirborneTargetAttack();
            return;
        }

        /*
         * 極端な高所へのメタ対策。
         */
        if (targetAbove >= 24.0D) {
            int roll =
                    dragon.getRandom().nextInt(100);

            if (roll < 45) {
                dragon.startJudgmentRaySequence();
                updateAirWeights(
                        AirAttack.JUDGMENT_RAY
                );
                return;
            }

            if (roll < 75) {
                dragon.startFigureEightSequence();
                updateAirWeights(
                        AirAttack.FIGURE_EIGHT
                );
                return;
            }

            dragon.startRagnarokSequence();
            updateAirWeights(
                    AirAttack.RAGNAROK
            );
            return;
        }

        /*
         * 優先条件がない通常ロール。
         */
        chooseWeightedAirAttack();
    }

    private void chooseWeightedAirAttack() {
        int totalWeight =
                this.ragnarokWeight
                        + this.figureEightWeight
                        + this.judgmentRayWeight
                        + this.diveStompWeight;

        int roll = dragon.getRandom().nextInt(totalWeight);

        if (roll < this.ragnarokWeight) {
            dragon.startRagnarokSequence();
            updateAirWeights(AirAttack.RAGNAROK);
            return;
        }

        roll -= this.ragnarokWeight;

        if (roll < this.figureEightWeight) {
            dragon.startFigureEightSequence();
            updateAirWeights(AirAttack.FIGURE_EIGHT);
            return;
        }

        roll -= this.figureEightWeight;

        if (roll < this.judgmentRayWeight) {
            dragon.startJudgmentRaySequence();
            updateAirWeights(AirAttack.JUDGMENT_RAY);
            return;
        }

        dragon.startDiveStompSequence();
        updateAirWeights(AirAttack.DIVE_STOMP);
    }

    private enum AirAttack {
        RAGNAROK,
        FIGURE_EIGHT,
        JUDGMENT_RAY,
        DIVE_STOMP
    }

    private void updateAirWeights(AirAttack selected) {
        this.ragnarokWeight =
                updateWeight(
                        this.ragnarokWeight,
                        selected == AirAttack.RAGNAROK,
                        BASE_WEIGHT
                );

        this.figureEightWeight =
                updateWeight(
                        this.figureEightWeight,
                        selected == AirAttack.FIGURE_EIGHT,
                        BASE_WEIGHT
                );

        this.judgmentRayWeight =
                updateWeight(
                        this.judgmentRayWeight,
                        selected == AirAttack.JUDGMENT_RAY,
                        60
                );

        this.diveStompWeight =
                updateWeight(
                        this.diveStompWeight,
                        selected == AirAttack.DIVE_STOMP,
                        70
                );
    }

    private int updateWeight(
            int currentWeight,
            boolean selected,
            int resetWeight
    ) {
        if (selected) {
            return resetWeight;
        }

        return Math.min(
                MAX_WEIGHT,
                currentWeight + WEIGHT_INCREASE
        );
    }

    private void chooseAirborneTargetAttack() {
        int ragnarok =
                Math.max(80, this.ragnarokWeight);

        int figureEight =
                Math.max(100, this.figureEightWeight);

        int judgment =
                Math.max(160, this.judgmentRayWeight);

        int total =
                ragnarok
                        + figureEight
                        + judgment;

        int roll =
                dragon.getRandom().nextInt(total);

        if (roll < judgment) {
            dragon.startJudgmentRaySequence();
            updateAirWeights(AirAttack.JUDGMENT_RAY);
            return;
        }

        roll -= judgment;

        if (roll < figureEight) {
            dragon.startFigureEightSequence();
            updateAirWeights(AirAttack.FIGURE_EIGHT);
            return;
        }

        dragon.startRagnarokSequence();
        updateAirWeights(AirAttack.RAGNAROK);
    }
}