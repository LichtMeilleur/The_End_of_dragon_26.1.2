package com.licht_meilleur.the_end_of_dragon.entity.ai;

import com.licht_meilleur.the_end_of_dragon.entity.TheEndOfDragonCoreEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class DragonStallRecoveryGoal extends Goal {
    private static final int STALL_TICKS = 20 * 12;

    private final TheEndOfDragonCoreEntity dragon;

    public DragonStallRecoveryGoal(
            TheEndOfDragonCoreEntity dragon
    ) {
        this.dragon = dragon;

        this.setFlags(
                EnumSet.of(
                        Flag.MOVE,
                        Flag.LOOK
                )
        );
    }

    @Override
    public boolean canUse() {
        if (dragon.isDebugFrozen()) {
            return false;
        }

        if (!(dragon.level() instanceof ServerLevel)) {
            return false;
        }

        if (!dragon.isAlive()) {
            return false;
        }

        if (!dragon.isCombatStarted()) {
            return false;
        }

        if (dragon.isIntroStateNow()) {
            return false;
        }

        if (dragon.isRecoveryStateNow()) {
            return false;
        }

        /*
         * 12秒間、攻撃を開始できていない場合に復帰。
         */
        return dragon.getTicksSinceLastAttack()
                >= STALL_TICKS;
    }

    @Override
    public void start() {
        dragon.startRecoveryDiveSequence();
    }

    @Override
    public boolean canContinueToUse() {
        return dragon.isAlive()
                && dragon.isRecoveryStateNow();
    }

    @Override
    public void tick() {
        if (!(dragon.level()
                instanceof ServerLevel level)) {
            return;
        }

        dragon.tickEmergencyRecoveryMove(level);
    }

    @Override
    public void stop() {
        /*
         * 正常終了はSUPER_LANDING後の
         * StateMachineに任せる。
         */
    }
}