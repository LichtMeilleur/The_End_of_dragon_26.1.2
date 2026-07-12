package com.licht_meilleur.the_end_of_dragon.entity.ai;

import com.licht_meilleur.the_end_of_dragon.entity.DragonState;
import com.licht_meilleur.the_end_of_dragon.entity.TheEndOfDragonCoreEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class DragonRecoveryGoal extends Goal {
    private final TheEndOfDragonCoreEntity dragon;

    public DragonRecoveryGoal(
            TheEndOfDragonCoreEntity dragon
    ) {
        this.dragon = dragon;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (dragon.isDebugFrozen()) {
            return false;
        }

        if (!(dragon.level() instanceof ServerLevel level)) {
            return false;
        }

        if (!dragon.isAlive()) {
            return false;
        }

        if (dragon.isIntroStateNow()) {
            return false;
        }

        /*
         * Core側の位置監視や通常空中攻撃から、
         * すでにRecovery状態へ入れられた場合にもGoalを開始する。
         */
        if (isRecoveryState()) {
            return true;
        }

        return dragon.shouldEmergencyRecover(level);
    }

    @Override
    public void start() {
        /*
         * Core側ですでにRecoveryを開始済みなら、
         * ここで最初から開始し直さない。
         */
        if (!isRecoveryState()) {
            dragon.startEmergencyRecovery();
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (!(dragon.level() instanceof ServerLevel)) {
            return false;
        }

        if (!dragon.isAlive()) {
            return false;
        }

        if (dragon.isDebugFrozen()) {
            return false;
        }

        if (dragon.isIntroStateNow()) {
            return false;
        }

        return isRecoveryState();
    }

    @Override
    public void tick() {
        if (!(dragon.level() instanceof ServerLevel level)) {
            return;
        }

        dragon.tickEmergencyRecoveryMove(level);
    }

    @Override
    public void stop() {
        /*
         * 正常終了後に移動ロックを解除する処理は、
         * SUPER_LANDING終了側で行う。
         *
         * Goalの中断時にここでIDLEへ戻すと、
         * 着地演出を途中終了させる可能性があるので何もしない。
         */
    }

    private boolean isRecoveryState() {
        return switch (dragon.getDragonState()) {
            case RECOVERY_ASCEND,
                 RECOVERY_RETURN,
                 RECOVERY_DIVE,
                 SUPER_LANDING -> true;

            default -> false;
        };
    }
}