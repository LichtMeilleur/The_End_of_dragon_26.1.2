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
        if (!(dragon.level() instanceof ServerLevel level)) {
            return false;
        }

        if (dragon.isIntroStateNow()) {
            return false;
        }

        return dragon.isRecoveringNeeded(level);
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

        return dragon.isRecoveringNeeded(level);
    }

    @Override
    public void tick() {
        if (!(dragon.level() instanceof ServerLevel level)) {
            return;
        }

        Vec3 target = dragon.arenaCenter(level).add(0.0D, 45.0D, 0.0D);
        Vec3 move = target.subtract(dragon.position());

        dragon.setDragonState(DragonState.FLY);

        if (move.lengthSqr() > 1.0E-6D) {
            dragon.moveBossBy(level, move.normalize().scale(Math.min(18.0D, move.length())));
        }
    }
}