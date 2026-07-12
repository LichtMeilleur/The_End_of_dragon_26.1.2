package com.licht_meilleur.the_end_of_dragon.entity.ai;

import com.licht_meilleur.the_end_of_dragon.entity.TheEndOfDragonCoreEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class DragonAirAttackGoal extends Goal {
    private final TheEndOfDragonCoreEntity dragon;
    private int cooldown = 120;

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

        if (cooldown > 0) {
            cooldown--;
            return false;
        }

        return dragon.tryConsumeAirAttackCooldown();
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

        LivingEntity target =
                dragon.findBossTarget(level);

        if (target == null || !target.isAlive()) {
            return;
        }

        cooldown =
                220 + dragon.getRandom().nextInt(160);

        double targetAbove =
                target.getEyeY() - dragon.getY();

        /*
         * 極端な高所にいるときだけ、
         * 25%程度でJudgment Rayを選ぶ。
         */
        if (targetAbove >= 24.0D
                && dragon.getRandom().nextFloat() < 0.25F) {
            dragon.startJudgmentRaySequence();
            return;
        }

        /*
         * 通常は3択。
         * 踏みつけを位置修正兼攻撃として採用。
         */
        int roll = dragon.getRandom().nextInt(3);

        switch (roll) {
            case 0 -> dragon.startRagnarokSequence();
            case 1 -> dragon.startFigureEightSequence();
            case 2 -> dragon.startDiveStompSequence();
        }
    }
}