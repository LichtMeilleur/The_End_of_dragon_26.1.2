package com.licht_meilleur.the_end_of_dragon.world.enderman;

import com.licht_meilleur.the_end_of_dragon.entity.enderman
        .TedAllyEndermanEntity;
import com.licht_meilleur.the_end_of_dragon.world
        .TedBattleWorldState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public final class TedEndermanFriendship {

    private static final int CLEANUP_INTERVAL =
            20;

    /**
     * TED討伐済みなら、通常エンダーマンは
     * プレイヤーと友好状態になる。
     *
     * 討伐状態は必ずEnd側のSavedDataを参照する。
     */
    public static boolean isFriendshipUnlocked(
            MinecraftServer server
    ) {
        if (server == null) {
            return false;
        }

        ServerLevel endLevel =
                server.getLevel(
                        Level.END
                );

        if (endLevel == null) {
            return false;
        }

        TedBattleWorldState battleState =
                TedBattleWorldState.get(
                        endLevel
                );

        return battleState.isBattleCompleted();
    }

    public static boolean shouldPreventTarget(
            LivingEntity attacker,
            LivingEntity target
    ) {
        if (!(attacker instanceof EnderMan)) {
            return false;
        }

        /*
         * 味方エンダーマンはEnderManを継承していませんが、
         * 将来の継承変更に備えて明示的に除外。
         */
        if (attacker
                instanceof TedAllyEndermanEntity) {
            return false;
        }

        if (!(target instanceof Player)) {
            return false;
        }

        if (!(attacker.level()
                instanceof ServerLevel serverLevel)) {
            return false;
        }

        return isFriendshipUnlocked(
                serverLevel.getServer()
        );
    }

    /**
     * すでに敵対中の通常エンダーマンを解除する。
     *
     * 各ディメンションで1秒に1回呼び出す。
     */
    public static void tick(
            ServerLevel level
    ) {
        if (level.getGameTime()
                % CLEANUP_INTERVAL != 0L) {
            return;
        }

        if (!isFriendshipUnlocked(
                level.getServer()
        )) {
            return;
        }

        /*
         * ロード済みEntityだけを対象にする。
         *
         * level.getAllEntities()を使わず、
         * 各プレイヤー周辺だけ処理する。
         */
        for (Player player : level.players()) {
            if (!player.isAlive()
                    || player.isSpectator()) {
                continue;
            }

            AABB area =
                    player.getBoundingBox()
                            .inflate(
                                    128.0D,
                                    96.0D,
                                    128.0D
                            );

            List<EnderMan> endermen =
                    level.getEntitiesOfClass(
                            EnderMan.class,
                            area,
                            enderman ->
                                    enderman.isAlive()
                                            && !enderman.isRemoved()
                    );

            for (EnderMan enderman : endermen) {
                clearHostility(
                        enderman
                );
            }
        }
    }

    private static void clearHostility(
            EnderMan enderman
    ) {
        /*
         * 現在の攻撃対象を解除。
         */
        if (enderman.getTarget()
                instanceof Player) {
            enderman.setTarget(
                    null
            );
        }

        /*
         * プレイヤーから攻撃された記録も解除。
         */
        if (enderman.getLastHurtByMob()
                instanceof Player) {
            enderman.setLastHurtByMob(
                    null
            );
        }

        /*
         * NeutralMob側の永続的な怒りを解除。
         */
        enderman.stopBeingAngry();
    }

    private TedEndermanFriendship() {
    }
}