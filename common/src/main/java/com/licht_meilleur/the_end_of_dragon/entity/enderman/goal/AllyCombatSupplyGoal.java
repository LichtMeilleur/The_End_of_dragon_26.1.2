package com.licht_meilleur.the_end_of_dragon.entity.enderman.goal;

import com.licht_meilleur.the_end_of_dragon.entity.enderman.AllyEndermanState;
import com.licht_meilleur.the_end_of_dragon.entity.enderman.TedAllyEndermanEntity;
import com.licht_meilleur.the_end_of_dragon.world.TedAllyEndermanMessageHandler;
import com.licht_meilleur.the_end_of_dragon.world.TedBattleWorldState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class AllyCombatSupplyGoal extends Goal {

    /*
     * プレイヤーへ近づく距離。
     */
    private static final double HAND_OVER_DISTANCE_SQR =
            3.5D * 3.5D;

    /*
     * hand_overアニメーション中、
     * アイテムを渡すtick。
     */
    private static final int GIVE_TICK =
            18;

    /*
     * 手渡し終了tick。
     */
    private static final int END_TICK =
            42;

    private static final int ARROW_COUNT =
            32;

    private final TedAllyEndermanEntity ally;

    private ServerPlayer targetPlayer;

    private Phase phase =
            Phase.APPROACH;

    private int phaseTicks;
    private boolean supplied;

    public AllyCombatSupplyGoal(
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

        if (!this.ally.isStoryAlly()) {
            return false;
        }

        if (this.ally.hasGivenCombatSupply()) {
            return false;
        }

        TedBattleWorldState worldState =
                TedBattleWorldState.get(
                        level
                );

        /*
         * TED戦闘中だけ実行。
         */
        if (!worldState.isBattleActive()) {
            return false;
        }

        ServerPlayer foundPlayer =
                findNearestPlayer(
                        level
                );

        if (foundPlayer == null) {
            return false;
        }

        this.targetPlayer =
                foundPlayer;

        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.targetPlayer == null
                || !this.targetPlayer.isAlive()
                || this.targetPlayer.isRemoved()) {
            return false;
        }

        if (!this.ally.isAlive()
                || this.ally.isRemoved()) {
            return false;
        }

        return this.phase
                != Phase.FINISHED;
    }

    @Override
    public void start() {
        this.phase =
                Phase.APPROACH;

        this.phaseTicks = 0;
        this.supplied = false;
    }

    @Override
    public void tick() {
        if (!(this.ally.level()
                instanceof ServerLevel level)) {
            this.phase =
                    Phase.FINISHED;

            return;
        }

        if (this.targetPlayer == null
                || !this.targetPlayer.isAlive()) {
            this.phase =
                    Phase.FINISHED;

            return;
        }

        switch (this.phase) {
            case APPROACH ->
                    tickApproach(level);

            case HAND_OVER ->
                    tickHandOver(level);

            case FINISHED -> {
            }
        }
    }

    private void tickApproach(
            ServerLevel level
    ) {
        this.ally.getLookControl()
                .setLookAt(
                        this.targetPlayer,
                        30.0F,
                        30.0F
                );

        double distanceSqr =
                this.ally.distanceToSqr(
                        this.targetPlayer
                );

        if (distanceSqr
                <= HAND_OVER_DISTANCE_SQR) {
            beginHandOver();
            return;
        }

        /*
         * 遠い場合はプレイヤーの少し横へワープ。
         */
        Vec3 playerLook =
                this.targetPlayer
                        .getLookAngle();

        Vec3 horizontalLook =
                new Vec3(
                        playerLook.x,
                        0.0D,
                        playerLook.z
                );

        if (horizontalLook
                .horizontalDistanceSqr()
                < 1.0E-6D) {
            horizontalLook =
                    new Vec3(
                            0.0D,
                            0.0D,
                            1.0D
                    );
        } else {
            horizontalLook =
                    horizontalLook.normalize();
        }

        Vec3 side =
                new Vec3(
                        -horizontalLook.z,
                        0.0D,
                        horizontalLook.x
                );

        Vec3 desiredCenter =
                this.targetPlayer
                        .position()
                        .add(
                                side.scale(
                                        2.0D
                                )
                        );

        Vec3 destination =
                AllyEndermanAiUtil
                        .findSafePositionAround(
                                this.ally,
                                level,
                                desiredCenter,
                                0.5D,
                                2.0D
                        );

        boolean teleported =
                AllyEndermanAiUtil.teleportAlly(
                        this.ally,
                        level,
                        destination
                );

        if (!teleported) {
            /*
             * ワープできなければ通常移動を試す。
             */
            this.ally.getNavigation()
                    .moveTo(
                            this.targetPlayer,
                            1.15D
                    );

            return;
        }

        beginHandOver();
    }

    private void beginHandOver() {
        this.phase =
                Phase.HAND_OVER;

        this.phaseTicks = 0;
        this.supplied = false;

        this.ally.getNavigation().stop();

        this.ally.setDeltaMovement(
                0.0D,
                this.ally.getDeltaMovement().y,
                0.0D
        );

        this.ally.setAllyState(
                AllyEndermanState
                        .COMBAT_SUPPLY_HAND_OVER
        );
    }

    private void tickHandOver(
            ServerLevel level
    ) {
        this.phaseTicks++;

        this.ally.getNavigation().stop();

        this.ally.setDeltaMovement(
                0.0D,
                this.ally.getDeltaMovement().y,
                0.0D
        );

        this.ally.getLookControl()
                .setLookAt(
                        this.targetPlayer,
                        30.0F,
                        30.0F
                );

        if (!this.supplied
                && this.phaseTicks
                >= GIVE_TICK) {

            this.supplied = true;

            giveCombatSupply(
                    level
            );
        }

        if (this.phaseTicks
                >= END_TICK) {
            this.phase =
                    Phase.FINISHED;
        }
    }

    private void giveCombatSupply(
            ServerLevel level
    ) {
        if (this.targetPlayer == null
                || !this.targetPlayer.isAlive()) {
            return;
        }

        ItemStack bow =
                new ItemStack(
                        Items.BOW
                );

        ItemStack arrows =
                new ItemStack(
                        Items.ARROW,
                        ARROW_COUNT
                );

        giveOrDrop(
                this.targetPlayer,
                bow
        );

        giveOrDrop(
                this.targetPlayer,
                arrows
        );

        this.ally.setCombatSupplyGiven(
                true
        );

        TedAllyEndermanMessageHandler
                .sendCombatSupplyMessage(
                        level,
                        this.ally,
                        this.targetPlayer
                );
    }

    private void giveOrDrop(
            ServerPlayer player,
            ItemStack stack
    ) {
        if (player.getInventory()
                .add(stack)) {
            return;
        }

        player.drop(
                stack,
                false
        );
    }

    private ServerPlayer findNearestPlayer(
            ServerLevel level
    ) {
        ServerPlayer nearest = null;
        double nearestDistanceSqr =
                Double.MAX_VALUE;

        for (ServerPlayer player :
                level.getServer()
                        .getPlayerList()
                        .getPlayers()) {

            if (player.level() != level
                    || !player.isAlive()
                    || player.isSpectator()) {
                continue;
            }

            double distanceSqr =
                    this.ally.distanceToSqr(
                            player
                    );

            if (distanceSqr
                    >= nearestDistanceSqr) {
                continue;
            }

            nearest =
                    player;

            nearestDistanceSqr =
                    distanceSqr;
        }

        return nearest;
    }

    @Override
    public void stop() {
        this.ally.getNavigation().stop();

        if (this.ally.isAlive()
                && !this.ally.isRemoved()) {
            this.ally.setAllyState(
                    AllyEndermanState.SUPPORT_IDLE
            );
        }

        this.targetPlayer = null;

        this.phase =
                Phase.APPROACH;

        this.phaseTicks = 0;
        this.supplied = false;
    }

    private enum Phase {
        APPROACH,
        HAND_OVER,
        FINISHED
    }
}