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
        if (!(dragon.level() instanceof ServerLevel level)) return;

        LivingEntity target = dragon.findBossTarget(level);
        if (target == null || !target.isAlive()) return;

        cooldown = 220 + dragon.getRandom().nextInt(160);

        if (dragon.getRandom().nextBoolean()) {
            dragon.startRagnarokSequence();
        } else {
            dragon.startFigureEightSequence();
        }
    }
}