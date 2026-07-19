package com.licht_meilleur.the_end_of_dragon.entity.enderman.goal;

import com.licht_meilleur.the_end_of_dragon.entity.enderman.AllyEndermanState;
import com.licht_meilleur.the_end_of_dragon.entity.enderman.TedAllyEndermanEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class AllyFollowPlayerGoal extends Goal {

    private static final double START_DISTANCE = 10.0D;
    private static final double STOP_DISTANCE = 6.0D;
    private static final double WARP_DISTANCE = 28.0D;

    private final TedAllyEndermanEntity ally;

    private Player player;

    public AllyFollowPlayerGoal(
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

        Player nearest =
                AllyEndermanAiUtil.findPlayer(
                        this.ally,
                        level
                );

        if (nearest == null
                || !nearest.isAlive()
                || nearest.isSpectator()) {
            return false;
        }

        if (this.ally.distanceToSqr(nearest)
                < START_DISTANCE
                * START_DISTANCE) {
            return false;
        }

        this.player = nearest;

        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.player == null
                || !this.player.isAlive()
                || this.player.isSpectator()) {
            return false;
        }

        if (!this.ally.canRunSupportAi()) {
            return false;
        }

        return this.ally.distanceToSqr(
                this.player
        ) > STOP_DISTANCE
                * STOP_DISTANCE;
    }

    @Override
    public void start() {
        this.ally.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (!(this.ally.level()
                instanceof ServerLevel level)) {
            return;
        }

        if (this.player == null) {
            return;
        }

        this.ally.getLookControl()
                .setLookAt(
                        this.player,
                        30.0F,
                        30.0F
                );

        double distanceSqr =
                this.ally.distanceToSqr(
                        this.player
                );

        if (distanceSqr
                >= WARP_DISTANCE
                * WARP_DISTANCE) {

            if (this.ally.getFollowWarpCooldown()
                    > 0) {
                return;
            }

            Vec3 destination =
                    AllyEndermanAiUtil
                            .findSafePositionAround(
                                    this.ally,
                                    level,
                                    this.player.position(),
                                    3.0D,
                                    7.0D
                            );

            if (AllyEndermanAiUtil
                    .teleportAlly(
                            this.ally,
                            level,
                            destination
                    )) {

                this.ally.setFollowWarpCooldown(
                        40
                );
            }

            return;
        }

        this.ally.getNavigation()
                .moveTo(
                        this.player,
                        1.1D
                );
    }

    @Override
    public void stop() {
        this.ally.getNavigation().stop();
        this.player = null;
    }
}