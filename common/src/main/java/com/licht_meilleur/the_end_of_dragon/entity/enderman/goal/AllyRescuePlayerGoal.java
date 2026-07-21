package com.licht_meilleur.the_end_of_dragon.entity.enderman.goal;

import com.licht_meilleur.the_end_of_dragon.entity.DragonState;
import com.licht_meilleur.the_end_of_dragon.entity.TheEndOfDragonCoreEntity;
import com.licht_meilleur.the_end_of_dragon.entity.enderman.AllyEndermanState;
import com.licht_meilleur.the_end_of_dragon.entity.enderman.TedAllyEndermanEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class AllyRescuePlayerGoal extends Goal {

    private static final int WARP_TICK = 6;
    private static final int END_TICK = 14;

    private final TedAllyEndermanEntity ally;

    private TheEndOfDragonCoreEntity dragon;
    private ServerPlayer player;

    private int goalTicks;
    private int reactedAttackStartTick =
            Integer.MIN_VALUE;

    private boolean warped;

    public AllyRescuePlayerGoal(
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

        if (!this.ally.canRunEmergencySupportAi()) {
            return false;
        }

        if (this.ally.getPlayerRescueCooldown()
                > 0) {
            return false;
        }

        TheEndOfDragonCoreEntity foundDragon =
                AllyEndermanAiUtil.findDragon(
                        this.ally,
                        level
                );

        if (foundDragon == null
                || !isRescueWindow(foundDragon)) {
            return false;
        }

        Player foundPlayer =
                AllyEndermanAiUtil.findPlayer(
                        this.ally,
                        level
                );

        if (!(foundPlayer
                instanceof ServerPlayer serverPlayer)) {
            return false;
        }

        if (!isPlayerInDanger(
                foundDragon,
                serverPlayer
        )) {
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

        float rescueChance =
                getRescueChance(
                        foundDragon.getDragonState()
                );

        if (this.ally.getRandom().nextFloat()
                > rescueChance) {
            return false;
        }

        this.dragon = foundDragon;
        this.player = serverPlayer;

        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.goalTicks < END_TICK
                && this.player != null
                && this.player.isAlive()
                && this.dragon != null
                && this.dragon.isAlive();
    }

    @Override
    public void start() {
        this.goalTicks = 0;
        this.warped = false;

        this.ally.getNavigation().stop();
        this.ally.setDeltaMovement(Vec3.ZERO);

        this.ally.setAllyState(
                AllyEndermanState.WITH_PLAYER_WARP
        );
    }

    @Override
    public void tick() {
        if (!(this.ally.level()
                instanceof ServerLevel level)) {
            return;
        }

        this.goalTicks++;

        if (this.player == null
                || this.dragon == null) {
            return;
        }

        this.ally.getLookControl()
                .setLookAt(
                        this.player,
                        40.0F,
                        40.0F
                );

        /*
         * アニメーションを少し見せてから、
         * プレイヤーとエンダーマンを同時に移動。
         */
        if (!this.warped
                && this.goalTicks
                >= WARP_TICK) {

            this.warped = true;

            performRescueWarp(
                    level
            );
        }
    }

    @Override
    public void stop() {
        this.ally.getNavigation().stop();

        this.ally.setPlayerRescueCooldown(
                160
        );

        this.ally.setSelfEvadeCooldown(
                8
        );

        if (this.ally.isAlive()) {
            this.ally.setAllyState(
                    AllyEndermanState.SUPPORT_IDLE
            );
        }

        this.goalTicks = 0;
        this.warped = false;
        this.player = null;
        this.dragon = null;
    }

    private void performRescueWarp(
            ServerLevel level
    ) {
        Vec3 playerDestination =
                AllyEndermanAiUtil
                        .findPositionAwayFromDragon(
                                this.ally,
                                level,
                                this.dragon,
                                this.player.position(),
                                18.0D,
                                30.0D
                        );

        if (playerDestination == null) {
            return;
        }

        Vec3 allyDestination =
                AllyEndermanAiUtil
                        .findSafePositionAround(
                                this.ally,
                                level,
                                playerDestination,
                                2.0D,
                                4.0D
                        );

        AllyEndermanAiUtil.playTeleportEffect(
                level,
                this.player.position()
        );

        /*
         * ServerPlayer用のテレポート。
         *
         * 1.21.8環境で単純なteleportToが使用可能なら、
         * この形で動作します。
         */
        this.player.teleportTo(
                playerDestination.x,
                playerDestination.y,
                playerDestination.z
        );

        AllyEndermanAiUtil.playTeleportEffect(
                level,
                playerDestination
        );

        AllyEndermanAiUtil.teleportAlly(
                this.ally,
                level,
                allyDestination != null
                        ? allyDestination
                        : playerDestination
        );
    }

    private boolean isRescueWindow(
            TheEndOfDragonCoreEntity dragon
    ) {
        DragonState state =
                dragon.getDragonState();

        int age =
                dragon.getDragonStateAgeTicks();

        return switch (state) {
            case ROAR_OF_OBLITERATION ->
                    between(age, 2, 7);

            case PHOTON_BLASTER ->
                    between(age, 17, 25);

            case PHOTON_BUSTER ->
                    between(age, 1, 10);

            case BLASTER_TACKLE ->
                    between(age, 2, 8);

            case ORB_OF_ANNIHILATION ->
                    between(age, 44, 53);

            case FLAMES_OF_RAGNAROK ->
                    between(age, 1, 10);

            case JUDGMENT_RAY ->
                    between(age, 10, 22);

            case SUPER_LANDING ->
                    between(age, 1, 16);

            case TAIL_WHIP ->
                    between(age, 1, 8);

            case FLY_SHOT ->
                    between(age, 1, 4);

            default -> false;
        };
    }

    private boolean isPlayerInDanger(
            TheEndOfDragonCoreEntity dragon,
            ServerPlayer player
    ) {
        double distance =
                dragon.distanceTo(player);

        return switch (dragon.getDragonState()) {
            case ROAR_OF_OBLITERATION ->
                    distance <= 56.0D;

            case SUPER_LANDING ->
                    distance <= 24.0D;

            case TAIL_WHIP ->
                    distance <= 18.0D;

            case BLASTER_TACKLE ->
                    distance <= 32.0D;

            case ORB_OF_ANNIHILATION ->
                    distance <= 48.0D;

            case PHOTON_BLASTER,
                 PHOTON_BUSTER,
                 FLAMES_OF_RAGNAROK,
                 JUDGMENT_RAY,
                 FLY_SHOT ->
                    distance <= 72.0D;

            default -> false;
        };
    }

    private float getRescueChance(
            DragonState state
    ) {
        return switch (state) {
            case PHOTON_BLASTER,
                 PHOTON_BUSTER,
                 SUPER_LANDING,
                 BLASTER_TACKLE ->
                    0.85F;

            case ORB_OF_ANNIHILATION,
                 JUDGMENT_RAY,
                 FLY_SHOT ->
                    0.75F;

            /*
             * 咆哮とRagnarokは範囲が広く、
             * ワープしても避け切れない場合がある。
             */
            case ROAR_OF_OBLITERATION,
                 FLAMES_OF_RAGNAROK ->
                    0.65F;

            default ->
                    0.70F;
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