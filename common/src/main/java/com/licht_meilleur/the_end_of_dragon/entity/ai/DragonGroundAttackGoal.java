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

        if (dragon.isDebugFrozen()) {
            return false;
        }
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

    private void chooseGroundAttack(
            ServerLevel level,
            LivingEntity target
    ) {
        Vec3 toTarget =
                target.position().subtract(dragon.position());

        dragon.setBossYawOnly(toTarget);

        // 不可壊・超高耐久装備への対策
        if (dragon.shouldPunishOverpoweredEquipment(target)) {
            dragon.setDragonState(
                    DragonState.ROAR_OF_OBLITERATION
            );
            return;
        }

        // 高防御プレイヤーへのOrb優先
        LivingEntity highDefenseTarget =
                dragon.findHighDefenseTarget(level);

        if (highDefenseTarget != null
                && dragon.getRandom().nextFloat() < 0.45F) {

            Vec3 toHighDefenseTarget =
                    highDefenseTarget.position()
                            .subtract(dragon.position());

            dragon.setTarget(highDefenseTarget);
            dragon.setBossYawOnly(toHighDefenseTarget);

            dragon.setDragonState(
                    DragonState.ORB_OF_ANNIHILATION
            );
            return;
        }

        double distance =
                dragon.distanceTo(target);

        double targetAbove =
                target.getEyeY() - dragon.getY();

        // 極端な高所籠城時のみ低確率Judgment Ray
        if (targetAbove >= 24.0D
                && dragon.getRandom().nextFloat() < 0.20F) {
            dragon.startJudgmentRaySequence();
            return;
        }

        // 通常の高低差は踏みつけ
        if (Math.abs(targetAbove) >= 6.0D) {
            dragon.startDiveStompSequence();
            return;
        }

        // 近距離
        if (distance < 18.0D) {
            if (dragon.getRandom().nextBoolean()) {
                dragon.setDragonState(
                        DragonState.ROAR_OF_OBLITERATION
                );
            } else {
                dragon.setDragonState(
                        DragonState.BLASTER_TACKLE
                );
            }
            return;
        }

        // 通常抽選
        switch (dragon.getRandom().nextInt(6)) {
            case 0 -> dragon.setDragonState(
                    DragonState.ORB_OF_ANNIHILATION
            );
            case 1 -> dragon.setDragonState(
                    DragonState.ROAR_OF_OBLITERATION
            );
            case 2 -> dragon.setDragonState(
                    DragonState.PHOTON_BLASTER
            );
            case 3 -> dragon.setDragonState(
                    DragonState.LIGHT_OF_DESTRUCTION
            );
            case 4 -> dragon.setDragonState(
                    DragonState.BLASTER_TACKLE
            );
            case 5 -> dragon.setDragonState(
                    DragonState.PHOTON_BUSTER
            );
        }
    }
}