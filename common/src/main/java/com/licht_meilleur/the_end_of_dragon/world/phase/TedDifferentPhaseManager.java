package com.licht_meilleur.the_end_of_dragon.world.phase;

import com.licht_meilleur.the_end_of_dragon.network.TedDifferentPhaseNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class TedDifferentPhaseManager {

    public static final int JUICE_BOTTLE_TICKS = 40;
    public static final int MELON_CUT_TICKS = 20;

    /*
     * パールによる、再使用まで継続する位相状態。
     */
    private static final Set<UUID>
            PERSISTENT_PLAYERS =
            new HashSet<>();

    /*
     * 食べ物・飲み物による一時的な位相時間。
     */
    private static final Map<UUID, Integer>
            TEMPORARY_TICKS =
            new HashMap<>();

    private TedDifferentPhaseManager() {
    }

    public static boolean isInDifferentPhase(
            Player player
    ) {
        return isPersistent(player)
                || getTemporaryTicks(player) > 0;
    }

    public static boolean isInDifferentPhase(
            Entity entity
    ) {
        if (!(entity instanceof Player player)) {
            /*
             * 現時点では、Mobなどは通常位相として扱う。
             * 後から別位相Mobを追加する場合はここを拡張する。
             */
            return false;
        }

        return isInDifferentPhase(
                player
        );
    }

    public static boolean canInteract(
            Entity first,
            Entity second
    ) {
        return isInDifferentPhase(first)
                == isInDifferentPhase(second);
    }

    public static boolean isPersistent(
            Player player
    ) {
        return PERSISTENT_PLAYERS.contains(
                player.getUUID()
        );
    }

    public static int getTemporaryTicks(
            Player player
    ) {
        return TEMPORARY_TICKS.getOrDefault(
                player.getUUID(),
                0
        );
    }

    public static void togglePersistent(
            ServerPlayer player
    ) {
        UUID playerId =
                player.getUUID();

        if (!PERSISTENT_PLAYERS.remove(
                playerId
        )) {
            PERSISTENT_PLAYERS.add(
                    playerId
            );
        }

        synchronize(
                player
        );
    }

    public static void enterTemporary(
            ServerPlayer player,
            int durationTicks
    ) {
        if (durationTicks <= 0) {
            return;
        }

        UUID playerId =
                player.getUUID();

        int currentTicks =
                TEMPORARY_TICKS.getOrDefault(
                        playerId,
                        0
                );

        TEMPORARY_TICKS.put(
                playerId,
                Math.max(
                        currentTicks,
                        durationTicks
                )
        );

        synchronize(
                player
        );
    }

    public static void leaveAllPhases(
            ServerPlayer player
    ) {
        UUID playerId =
                player.getUUID();

        PERSISTENT_PLAYERS.remove(
                playerId
        );

        TEMPORARY_TICKS.remove(
                playerId
        );

        synchronize(
                player
        );
    }

    public static void serverTick(
            ServerPlayer player
    ) {
        UUID playerId =
                player.getUUID();

        int temporaryTicks =
                TEMPORARY_TICKS.getOrDefault(
                        playerId,
                        0
                );

        if (temporaryTicks > 1) {
            TEMPORARY_TICKS.put(
                    playerId,
                    temporaryTicks - 1
            );
        } else if (temporaryTicks == 1) {
            TEMPORARY_TICKS.remove(
                    playerId
            );

            synchronize(
                    player
            );
        }

        if (!isInDifferentPhase(
                player
        )) {
            return;
        }

        tickAirSupply(
                player
        );
    }

    private static void tickAirSupply(
            ServerPlayer player
    ) {
        /*
         * クリエイティブとスペクテイターでは
         * 酸素を消費しない。
         */
        if (player.getAbilities().instabuild
                || player.isSpectator()) {
            return;
        }

        int nextAir =
                player.getAirSupply() - 1;

        /*
         * バニラの溺水と同様に、
         * 酸素が-20まで達したら0へ戻して
         * 2ダメージを与える。
         */
        if (nextAir <= -20) {
            player.setAirSupply(
                    0
            );

            player.hurtServer(
                    player.level(),
                    player.damageSources()
                            .drown(),
                    2.0F
            );

            return;
        }

        player.setAirSupply(
                nextAir
        );

        /*
         * 別位相中は炎上状態も解除する。
         *
         * ダメージだけ無効にすると、
         * 炎の表示が残り続けるため。
         */
        if (player.isOnFire()) {
            player.clearFire();
        }
    }

    private static void synchronize(
            ServerPlayer changedPlayer
    ) {
        boolean persistent =
                isPersistent(
                        changedPlayer
                );

        int temporaryTicks =
                getTemporaryTicks(
                        changedPlayer
                );

        for (ServerPlayer receiver :
                changedPlayer.level()
                        .getServer()
                        .getPlayerList()
                        .getPlayers()) {

            TedDifferentPhaseNetwork.sendStateTo(
                    receiver,
                    changedPlayer,
                    persistent,
                    temporaryTicks
            );
        }
    }

    public static void synchronizeAllTo(
            ServerPlayer receiver
    ) {
        for (ServerPlayer existingPlayer :
                receiver.level()
                        .getServer()
                        .getPlayerList()
                        .getPlayers()) {

            TedDifferentPhaseNetwork.sendStateTo(
                    receiver,
                    existingPlayer,
                    isPersistent(existingPlayer),
                    getTemporaryTicks(
                            existingPlayer
                    )
            );
        }
    }

    /**
     * プレイヤーがブロックを変更できるか確認する。
     *
     * 将来、別位相専用ブロックを追加した場合は、
     * ここで対象ブロックだけtrueにできる。
     */
    public static boolean canModifyBlock(
            Player player,
            BlockState blockState
    ) {
        if (!isInDifferentPhase(
                player
        )) {
            return true;
        }

        /*
         * 現在は別位相中に通常ブロックを
         * 一切変更できない。
         */
        return false;
    }

    /**
     * ブロックに対するアイテム使用が可能か確認する。
     */
    public static boolean canUseOnBlock(
            Player player,
            UseOnContext context
    ) {
        return canModifyBlock(
                player,
                context.getLevel()
                        .getBlockState(
                                context.getClickedPos()
                        )
        );
    }

    /**
     * エンティティへ右クリック干渉できるか確認する。
     */
    public static boolean canInteractWithEntity(
            Player player,
            Entity target
    ) {
        return canInteract(
                player,
                target
        );
    }
}