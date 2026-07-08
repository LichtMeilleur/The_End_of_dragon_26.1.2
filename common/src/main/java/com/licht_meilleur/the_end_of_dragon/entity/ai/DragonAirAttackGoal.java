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
        return false; // 空中攻撃はコマンドテスト専用にする
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

        if (--cooldown > 0) return;

        cooldown = 160 + dragon.getRandom().nextInt(120);

        if (dragon.getRandom().nextBoolean()) {
            dragon.startRagnarokSequence();
        } else {
            dragon.startFigureEightSequence();
        }
    }
}