package com.licht_meilleur.the_end_of_dragon.entity.ai;

import com.licht_meilleur.the_end_of_dragon.entity.DragonState;
import com.licht_meilleur.the_end_of_dragon.entity.TheEndOfDragonCoreEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class DragonRecoveryGoal extends Goal {
    private final TheEndOfDragonCoreEntity dragon;

    public DragonRecoveryGoal(TheEndOfDragonCoreEntity dragon) {
        this.dragon = dragon;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {

        if (dragon.isDebugFrozen()) {
            return false;
        }
        if (!(dragon.level() instanceof ServerLevel level)) return false;
        return dragon.shouldEmergencyRecover(level);
    }

    @Override
    public void start() {
        dragon.startEmergencyRecovery();
    }

    @Override
    public boolean canContinueToUse() {
        if (!(dragon.level() instanceof ServerLevel level)) {
            return false;
        }

        if (!dragon.isAlive()) {
            return false;
        }

        if (dragon.isIntroStateNow()) {
            return false;
        }

        DragonState state = dragon.getDragonState();

        // Recoveryシーケンス中は継続
        if (state == DragonState.RECOVERY_ASCEND
                || state == DragonState.RECOVERY_RETURN
                || state == DragonState.FLY_DESCEND
                || state == DragonState.SUPER_LANDING) {
            return true;
        }

        if (dragon.isCombatLocked()) {
            return false;
        }

        return dragon.shouldEmergencyRecover(level);
    }
    @Override
    public void tick() {
        if (!(dragon.level() instanceof ServerLevel level)) return;

        dragon.tickEmergencyRecoveryMove(level);
    }
}