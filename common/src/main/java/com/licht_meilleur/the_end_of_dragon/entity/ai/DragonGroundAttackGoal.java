package com.licht_meilleur.the_end_of_dragon.entity.ai;

import com.licht_meilleur.the_end_of_dragon.entity.DragonState;
import com.licht_meilleur.the_end_of_dragon.entity.TheEndOfDragonCoreEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class DragonGroundAttackGoal extends Goal {
    private final TheEndOfDragonCoreEntity dragon;
    private int cooldown = 20;

    public DragonGroundAttackGoal(TheEndOfDragonCoreEntity dragon) {
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
        if (dragon.isAirborneBoss(level)) return false;

        return dragon.findBossTarget(level) != null;
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

        cooldown = 35 + dragon.getRandom().nextInt(25);

        chooseGroundAttack(level, target);
    }

    private void chooseGroundAttack(ServerLevel level, LivingEntity target) {
        Vec3 toTarget = target.position().subtract(dragon.position());
        dragon.setBossYawOnly(toTarget);

        if (dragon.shouldPunishOverpoweredEquipment(target)) {
            dragon.setDragonState(DragonState.ROAR_OF_OBLITERATION);
            return;
        }

        double distance = dragon.distanceTo(target);
        double yDiff = Math.abs(target.getY() - dragon.getY());

        if (distance < 18.0D) {
            if (dragon.getRandom().nextBoolean()) {
                dragon.setDragonState(DragonState.ROAR_OF_OBLITERATION);
            } else {
                dragon.setDragonState(DragonState.BLASTER_TACKLE);
            }
            return;
        }

        if (yDiff > 8.0D) {
            if (dragon.getRandom().nextBoolean()) {
                dragon.setDragonState(DragonState.ORB_OF_ANNIHILATION);
            } else {
                dragon.setDragonState(DragonState.BLASTER_TACKLE);
            }
            return;
        }

        int roll = dragon.getRandom().nextInt(5);

        switch (roll) {
            case 0 -> dragon.setDragonState(DragonState.ORB_OF_ANNIHILATION);
            case 1 -> dragon.setDragonState(DragonState.ROAR_OF_OBLITERATION);
            case 2 -> dragon.setDragonState(DragonState.PHOTON_BLASTER);
            case 3 -> dragon.setDragonState(DragonState.LIGHT_OF_DESTRUCTION);
            case 4 -> dragon.setDragonState(DragonState.BLASTER_TACKLE);
        }
    }
}