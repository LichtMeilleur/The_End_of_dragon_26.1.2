package com.licht_meilleur.the_end_of_dragon.entity.ai;

import com.licht_meilleur.the_end_of_dragon.entity.DragonState;
import com.licht_meilleur.the_end_of_dragon.entity.TheEndOfDragonCoreEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class DragonMoveGoal extends Goal {
    private final TheEndOfDragonCoreEntity dragon;

    public DragonMoveGoal(TheEndOfDragonCoreEntity dragon) {
        this.dragon = dragon;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {

        if (dragon.isDebugFrozen()) {
            return false;
        }
        if (dragon.isAttackMovementLocked()) return false;
        if (!(dragon.level() instanceof ServerLevel level)) return false;
        if (dragon.isIntroStateNow()) return false;
        if (dragon.isCombatLocked()) return false;

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

        // 攻撃・イントロ・飛行遷移中は絶対にIDLEへ戻さない
        if (dragon.isCombatLocked()
                || dragon.isIntroStateNow()
                || dragon.isAttackMovementLocked()) {
            dragon.getNavigation().stop();
            return;
        }

        LivingEntity target = dragon.findBossTarget(level);
        if (target == null || !target.isAlive()) {
            if (!dragon.isCombatLocked()) {
                dragon.setDragonState(DragonState.IDLE);
            }
            return;
        }

        dragon.getNavigation().stop();

        if (dragon.getDragonState() != DragonState.IDLE) {
            dragon.setDragonState(DragonState.IDLE);
        }

        // 通常時は何もしない
    }
}