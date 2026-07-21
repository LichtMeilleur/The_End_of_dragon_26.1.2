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

        /*
         * 通常状態だけでなく、
         * 攻撃モーション中でも回避可能にする。
         */
        if (!this.ally.canRunEmergencySupportAi()) {
            return false;
        }

        if (this.ally.getSelfEvadeCooldown() > 0) {
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

        DragonState state =
                foundDragon.getDragonState();

        if (!isDangerState(state)) {
            return false;
        }

        float chance =
                getEvadeChance(state);

        /*
         * 失敗した場合はreact済みにしない。
         *
         * 次tick以降も危険状態が続いていれば
         * 再び回避判定を行う。
         */
        if (this.ally.getRandom().nextFloat()
                > chance) {
            return false;
        }

        this.dragon = foundDragon;

        return true;
    }

    @Override
    public boolean canContinueToUse() {
        /*
         * ワープはstart()で即時実行する。
         */
        return false;
    }

    @Override
    public void start() {
        if (!(this.ally.level()
                instanceof ServerLevel level)) {
            return;
        }

        if (this.dragon == null
                || !this.dragon.isAlive()
                || this.dragon.isRemoved()) {
            return;
        }

        DragonState state =
                this.dragon.getDragonState();

        this.ally.getNavigation().stop();
        this.ally.setDeltaMovement(Vec3.ZERO);

        Vec3 destination =
                AllyEndermanAiUtil
                        .findPositionAwayFromDragon(
                                this.ally,
                                level,
                                this.dragon,
                                this.ally.position(),
                                getMinimumEvadeDistance(state),
                                getMaximumEvadeDistance(state)
                        );

        if (!AllyEndermanAiUtil.teleportAlly(
                this.ally,
                level,
                destination
        )) {
            /*
             * ワープ先が見つからなければ、
             * クールタイムを付けず次tickに再試行する。
             */
            return;
        }

        /*
         * 継続攻撃では一定間隔で再びワープできる。
         */
        this.ally.setSelfEvadeCooldown(
                getEvadeCooldown(state)
        );
    }

    @Override
    public void stop() {
        this.dragon = null;
    }

    private boolean isDangerState(
            DragonState state
    ) {
        return switch (state) {
            case ROAR_OF_OBLITERATION,
                 PHOTON_BLASTER,
                 PHOTON_BUSTER,
                 BLASTER_TACKLE,
                 ORB_OF_ANNIHILATION,
                 FLAMES_OF_RAGNAROK,
                 JUDGMENT_RAY,
                 TAIL_WHIP,
                 SUPER_LANDING,
                 FLY_SHOT -> true;

            default -> false;
        };
    }

    private float getEvadeChance(
            DragonState state
    ) {
        return switch (state) {
            /*
             * 継続攻撃・広範囲攻撃は必ず逃げる。
             */
            case PHOTON_BLASTER,
                 PHOTON_BUSTER,
                 FLAMES_OF_RAGNAROK,
                 JUDGMENT_RAY,
                 SUPER_LANDING,
                 BLASTER_TACKLE -> 1.0F;

            case ROAR_OF_OBLITERATION -> 0.98F;

            case TAIL_WHIP -> 0.97F;

            case ORB_OF_ANNIHILATION,
                 FLY_SHOT -> 0.95F;

            default -> 1.0F;
        };
    }

    private int getEvadeCooldown(
            DragonState state
    ) {
        return switch (state) {
            /*
             * 長時間継続する攻撃。
             * 約0.4秒ごとに再回避可能。
             */
            case FLAMES_OF_RAGNAROK,
                 JUDGMENT_RAY,
                 PHOTON_BLASTER,
                 PHOTON_BUSTER -> 8;

            /*
             * 突進や着地の追撃を避ける。
             */
            case BLASTER_TACKLE,
                 SUPER_LANDING,
                 TAIL_WHIP -> 10;

            case ROAR_OF_OBLITERATION -> 12;

            case ORB_OF_ANNIHILATION,
                 FLY_SHOT -> 10;

            default -> 10;
        };
    }

    private double getMinimumEvadeDistance(
            DragonState state
    ) {
        return switch (state) {
            case ROAR_OF_OBLITERATION -> 42.0D;

            case FLAMES_OF_RAGNAROK,
                 PHOTON_BLASTER,
                 PHOTON_BUSTER,
                 JUDGMENT_RAY -> 28.0D;

            case SUPER_LANDING,
                 BLASTER_TACKLE -> 24.0D;

            case TAIL_WHIP -> 20.0D;

            default -> 18.0D;
        };
    }

    private double getMaximumEvadeDistance(
            DragonState state
    ) {
        return switch (state) {
            case ROAR_OF_OBLITERATION -> 56.0D;

            case FLAMES_OF_RAGNAROK,
                 PHOTON_BLASTER,
                 PHOTON_BUSTER,
                 JUDGMENT_RAY -> 42.0D;

            case SUPER_LANDING,
                 BLASTER_TACKLE -> 36.0D;

            case TAIL_WHIP -> 30.0D;

            default -> 28.0D;
        };
    }
}