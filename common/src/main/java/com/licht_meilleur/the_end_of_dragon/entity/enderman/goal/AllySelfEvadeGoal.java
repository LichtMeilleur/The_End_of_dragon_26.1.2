package com.licht_meilleur.the_end_of_dragon.entity.enderman.goal;

import com.licht_meilleur.the_end_of_dragon.entity.DragonState;
import com.licht_meilleur.the_end_of_dragon.entity.TheEndOfDragonCoreEntity;
import com.licht_meilleur.the_end_of_dragon.entity.enderman.TedAllyEndermanEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class AllySelfEvadeGoal extends Goal {

    private final TedAllyEndermanEntity ally;

    private TheEndOfDragonCoreEntity dragon;
    private int reactedAttackStartTick =
            Integer.MIN_VALUE;

    public AllySelfEvadeGoal(
            TedAllyEndermanEntity ally
    ) {
        this.ally = ally;

        this.setFlags(
                EnumSet.of(
                        Flag.MOVE,
                        Flag.LOOK
                )
        );
    }

    @Override
    public boolean canUse() {
        if (!(this.ally.level()
                instanceof ServerLevel level)) {
            return false;
        }

        if (!this.ally.canRunSupportAi()) {
            return false;
        }

        if (this.ally.getSelfEvadeCooldown()
                > 0) {
            return false;
        }

        TheEndOfDragonCoreEntity foundDragon =
                AllyEndermanAiUtil.findDragon(
                        this.ally,
                        level
                );

        if (foundDragon == null) {
            return false;
        }

        if (!isDangerWindow(foundDragon)) {
            return false;
        }

        int attackStartTick =
                foundDragon.tickCount
                        - foundDragon
                        .getDragonStateAgeTicks();

        if (attackStartTick
                == this.reactedAttackStartTick) {
            return false;
        }

        this.reactedAttackStartTick =
                attackStartTick;

        float chance =
                getEvadeChance(
                        foundDragon.getDragonState()
                );

        if (this.ally.getRandom()
                .nextFloat() > chance) {
            return false;
        }

        this.dragon = foundDragon;

        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public void start() {
        if (!(this.ally.level()
                instanceof ServerLevel level)) {
            return;
        }

        if (this.dragon == null) {
            return;
        }

        Vec3 destination =
                AllyEndermanAiUtil
                        .findPositionAwayFromDragon(
                                this.ally,
                                level,
                                this.dragon,
                                this.ally.position(),
                                getMinimumEvadeDistance(
                                        this.dragon
                                                .getDragonState()
                                ),
                                getMaximumEvadeDistance(
                                        this.dragon
                                                .getDragonState()
                                )
                        );

        if (AllyEndermanAiUtil.teleportAlly(
                this.ally,
                level,
                destination
        )) {
            /*
             * 連続攻撃へ反応できるよう短め。
             */
            this.ally.setSelfEvadeCooldown(
                    10
            );
        }
    }

    @Override
    public void stop() {
        this.dragon = null;
    }

    private boolean isDangerWindow(
            TheEndOfDragonCoreEntity dragon
    ) {
        DragonState state =
                dragon.getDragonState();

        int age =
                dragon.getDragonStateAgeTicks();

        return switch (state) {
            case ROAR_OF_OBLITERATION ->
                    between(age, 3, 8);

            case PHOTON_BLASTER ->
                    between(age, 18, 26);

            case PHOTON_BUSTER ->
                    between(age, 2, 12);

            case BLASTER_TACKLE ->
                    between(age, 3, 9);

            case ORB_OF_ANNIHILATION ->
                    between(age, 45, 54);

            case FLAMES_OF_RAGNAROK ->
                    between(age, 1, 12);

            case JUDGMENT_RAY ->
                    between(age, 12, 24);

            case TAIL_WHIP ->
                    between(age, 2, 10);

            case SUPER_LANDING ->
                    between(age, 1, 18);

            case FLY_SHOT ->
                    between(age, 1, 5);

            default -> false;
        };
    }

    private float getEvadeChance(
            DragonState state
    ) {
        return switch (state) {
            case PHOTON_BLASTER,
                 PHOTON_BUSTER,
                 FLAMES_OF_RAGNAROK,
                 JUDGMENT_RAY,
                 SUPER_LANDING ->
                    0.98F;

            case ROAR_OF_OBLITERATION ->
                    0.95F;

            case ORB_OF_ANNIHILATION,
                 FLY_SHOT ->
                    0.90F;

            default ->
                    0.94F;
        };
    }

    private double getMinimumEvadeDistance(
            DragonState state
    ) {
        return switch (state) {
            case ROAR_OF_OBLITERATION ->
                    34.0D;

            case FLAMES_OF_RAGNAROK,
                 PHOTON_BLASTER,
                 PHOTON_BUSTER,
                 JUDGMENT_RAY ->
                    22.0D;

            default ->
                    15.0D;
        };
    }

    private double getMaximumEvadeDistance(
            DragonState state
    ) {
        return switch (state) {
            case ROAR_OF_OBLITERATION ->
                    46.0D;

            case FLAMES_OF_RAGNAROK,
                 PHOTON_BLASTER,
                 PHOTON_BUSTER,
                 JUDGMENT_RAY ->
                    32.0D;

            default ->
                    24.0D;
        };
    }

    private static boolean between(
            int value,
            int minimum,
            int maximum
    ) {
        return value >= minimum
                && value <= maximum;
    }
}