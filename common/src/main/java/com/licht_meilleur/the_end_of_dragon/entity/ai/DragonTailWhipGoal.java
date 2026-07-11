package com.licht_meilleur.the_end_of_dragon.entity.ai;

import com.licht_meilleur.the_end_of_dragon.entity.DragonState;
import com.licht_meilleur.the_end_of_dragon.entity.TheEndOfDragonCoreEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;

public class DragonTailWhipGoal extends Goal {
    private final TheEndOfDragonCoreEntity dragon;
    private int cooldown = 40;

    public DragonTailWhipGoal(TheEndOfDragonCoreEntity dragon) {
        this.dragon = dragon;
        this.setFlags(EnumSet.noneOf(Flag.class));
    }

    @Override
    public boolean canUse() {

        if (dragon.isDebugFrozen()) {
            return false;
        }
        if (!(dragon.level() instanceof ServerLevel level)) return false;
        if (!dragon.isAlive()) return false;
        if (!dragon.isCombatStarted()) return false;
        if (dragon.isIntroStateNow()) return false;
        if (dragon.isCombatLocked()) return false;
        if (dragon.isAirborneBoss(level)) return false;

        if (cooldown > 0) {
            cooldown--;
            return false;
        }

        int count = countNearbyThreats(level);

        if (count >= 3) {
            cooldown = 70 + dragon.getRandom().nextInt(40);
            return true;
        }

        return false;
    }

    @Override
    public void start() {
        dragon.setDragonState(DragonState.TAIL_WHIP);
    }

    private int countNearbyThreats(ServerLevel level) {
        AABB area = dragon.getBoundingBox().inflate(13.0D);

        return level.getEntitiesOfClass(
                LivingEntity.class,
                area,
                entity -> entity.isAlive()
                        && entity != dragon
                        && !(entity instanceof TheEndOfDragonCoreEntity)
                        && !(entity instanceof com.licht_meilleur.the_end_of_dragon.entity.TheEndOfDragonEntity)
        ).size();
    }
}