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

    private static final int BASE_WEIGHT = 100;
    private static final int WEIGHT_INCREASE = 25;
    private static final int MAX_WEIGHT = 275;

    private int orbWeight = BASE_WEIGHT;
    private int roarWeight = BASE_WEIGHT;
    private int photonBlasterWeight = BASE_WEIGHT;
    private int lightWeight = BASE_WEIGHT;
    private int tackleWeight = BASE_WEIGHT;
    private int photonBusterWeight = BASE_WEIGHT;

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

        /*
         * 不可壊・超高耐久装備への対策。
         *
         * これは最優先の特殊対策なので、
         * 通常抽選より先に判定する。
         */
        if (dragon.shouldPunishOverpoweredEquipment(target)) {
            dragon.setDragonState(
                    DragonState.ROAR_OF_OBLITERATION
            );
            return;
        }

        /*
         * 高防御プレイヤーへのOrb優先。
         */
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

        /*
         * ジェットパック・飛行MODなどを含む空中判定。
         *
         * onGroundだけだとジャンプ中も該当するため、
         * 高低差4以上も条件に入れる。
         */
        boolean targetIsAirborne =
                !target.onGround()
                        && targetAbove >= 4.0D;

        /*
         * 空中に浮き続けるプレイヤーへの専用抽選。
         *
         * 踏みつけはほぼ使わず、
         * Judgment RayとPhoton Busterを優先する。
         */
        if (targetIsAirborne) {
            int airborneRoll =
                    dragon.getRandom().nextInt(100);

            if (airborneRoll < 40) {
                dragon.startJudgmentRaySequence();
                return;
            }

            if (airborneRoll < 75) {
                dragon.setDragonState(
                        DragonState.PHOTON_BUSTER
                );
                return;
            }

            if (airborneRoll < 90) {
                dragon.setDragonState(
                        DragonState.PHOTON_BLASTER
                );
                return;
            }

            dragon.setDragonState(
                    DragonState.ORB_OF_ANNIHILATION
            );
            return;
        }

        /*
         * 極端な高所にいる地上プレイヤー。
         *
         * Judgment RayかPhoton Busterを優先し、
         * 踏みつけ固定にはしない。
         */
        if (targetAbove >= 24.0D) {
            int highGroundRoll =
                    dragon.getRandom().nextInt(100);

            if (highGroundRoll < 35) {
                dragon.startJudgmentRaySequence();
                return;
            }

            if (highGroundRoll < 70) {
                dragon.setDragonState(
                        DragonState.PHOTON_BUSTER
                );
                return;
            }

            dragon.startDiveStompSequence();
            return;
        }

        /*
         * 通常の崖・谷・段差。
         *
         * 以前は必ず踏みつけだったが、
         * Photon BusterやOrbも混ぜる。
         */
        if (Math.abs(targetAbove) >= 6.0D) {
            int heightRoll =
                    dragon.getRandom().nextInt(100);

            if (heightRoll < 50) {
                dragon.startDiveStompSequence();
                return;
            }

            if (heightRoll < 75) {
                dragon.setDragonState(
                        DragonState.PHOTON_BUSTER
                );
                return;
            }

            if (heightRoll < 90) {
                dragon.setDragonState(
                        DragonState.ORB_OF_ANNIHILATION
                );
                return;
            }

            dragon.setDragonState(
                    DragonState.PHOTON_BLASTER
            );
            return;
        }
        /*
         * 優先条件に該当しなかった場合は、
         * 使用されていない攻撃ほど選ばれやすくなる通常抽選。
         */
        chooseWeightedGroundAttack();

    }

    private enum GroundAttack {
        ORB,
        ROAR,
        PHOTON_BLASTER,
        LIGHT,
        TACKLE,
        PHOTON_BUSTER
    }

    private void chooseWeightedGroundAttack() {
        int totalWeight =
                this.orbWeight
                        + this.roarWeight
                        + this.photonBlasterWeight
                        + this.lightWeight
                        + this.tackleWeight
                        + this.photonBusterWeight;

        int roll =
                dragon.getRandom().nextInt(totalWeight);

        if (roll < this.orbWeight) {
            dragon.setDragonState(
                    DragonState.ORB_OF_ANNIHILATION
            );
            updateGroundWeights(GroundAttack.ORB);
            return;
        }

        roll -= this.orbWeight;

        if (roll < this.roarWeight) {
            dragon.setDragonState(
                    DragonState.ROAR_OF_OBLITERATION
            );
            updateGroundWeights(GroundAttack.ROAR);
            return;
        }

        roll -= this.roarWeight;

        if (roll < this.photonBlasterWeight) {
            dragon.setDragonState(
                    DragonState.PHOTON_BLASTER
            );
            updateGroundWeights(
                    GroundAttack.PHOTON_BLASTER
            );
            return;
        }

        roll -= this.photonBlasterWeight;

        if (roll < this.lightWeight) {
            dragon.setDragonState(
                    DragonState.LIGHT_OF_DESTRUCTION
            );
            updateGroundWeights(GroundAttack.LIGHT);
            return;
        }

        roll -= this.lightWeight;

        if (roll < this.tackleWeight) {
            dragon.setDragonState(
                    DragonState.BLASTER_TACKLE
            );
            updateGroundWeights(GroundAttack.TACKLE);
            return;
        }

        dragon.setDragonState(
                DragonState.PHOTON_BUSTER
        );
        updateGroundWeights(
                GroundAttack.PHOTON_BUSTER
        );
    }

    private void updateGroundWeights(
            GroundAttack selected
    ) {
        this.orbWeight =
                updateWeight(
                        this.orbWeight,
                        selected == GroundAttack.ORB
                );

        this.roarWeight =
                updateWeight(
                        this.roarWeight,
                        selected == GroundAttack.ROAR
                );

        this.photonBlasterWeight =
                updateWeight(
                        this.photonBlasterWeight,
                        selected
                                == GroundAttack.PHOTON_BLASTER
                );

        this.lightWeight =
                updateWeight(
                        this.lightWeight,
                        selected == GroundAttack.LIGHT
                );

        this.tackleWeight =
                updateWeight(
                        this.tackleWeight,
                        selected == GroundAttack.TACKLE
                );

        this.photonBusterWeight =
                updateWeight(
                        this.photonBusterWeight,
                        selected
                                == GroundAttack.PHOTON_BUSTER
                );
    }

    private int updateWeight(
            int currentWeight,
            boolean selected
    ) {
        if (selected) {
            return BASE_WEIGHT;
        }

        return Math.min(
                MAX_WEIGHT,
                currentWeight + WEIGHT_INCREASE
        );
    }
}