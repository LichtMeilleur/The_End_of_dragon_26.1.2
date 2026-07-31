package com.licht_meilleur.the_end_of_dragon.client.sound;

import com.licht_meilleur.the_end_of_dragon.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class TedBgmManager {

    /*
     * play()直後はSoundManager#isActive()が
     * 一時的にfalseを返す可能性があるため、
     * その間に再生成しないための猶予。
     */
    private static final long
            START_PENDING_GRACE_TICKS = 5L;

    private static SimpleSoundInstance currentBgm;

    /*
     * 再生要求を出したときのクライアント環境。
     *
     * 死亡後はLocalPlayerが作り直され、
     * ディメンション移動時はLevelが作り直されるため、
     * 古い再生要求かどうかを判別できる。
     */
    private static Player requestedPlayer;

    private static Level requestedLevel;

    private static long requestedGameTime =
            Long.MIN_VALUE;

    private TedBgmManager() {
    }

    public static void start() {
        Minecraft minecraft =
                Minecraft.getInstance();

        Player player =
                minecraft.player;

        Level level =
                minecraft.level;

        if (player == null
                || level == null) {
            return;
        }

        SoundManager soundManager =
                minecraft.getSoundManager();

        long currentGameTime =
                level.getGameTime();

        if (currentBgm != null) {

            /*
             * 実際に再生中なら新しく開始しない。
             */
            if (soundManager.isActive(
                    currentBgm
            )) {
                return;
            }

            boolean sameClientContext =
                    requestedPlayer == player
                            && requestedLevel == level;

            long elapsedTicks =
                    currentGameTime
                            - requestedGameTime;

            /*
             * play()直後で、まだSoundManagerが
             * activeとして認識していない期間だけは、
             * 同じ再生要求を重複させない。
             */
            if (sameClientContext
                    && elapsedTicks >= 0L
                    && elapsedTicks
                    <= START_PENDING_GRACE_TICKS) {
                return;
            }

            /*
             * ここへ来た場合は、
             * 死亡・ディメンション移動・何らかの停止により
             * 古いインスタンスだけ残っている状態。
             */
            soundManager.stop(
                    currentBgm
            );

            clearCurrentRequest();
        }

        RandomSource random =
                level.getRandom();

        SimpleSoundInstance newBgm =
                new SimpleSoundInstance(
                        ModSounds
                                .SYUUMATU_NO_LAEVATEIN
                                .location(),
                        SoundSource.MUSIC,
                        1.0F,
                        1.0F,
                        random,
                        true,
                        0,
                        SimpleSoundInstance
                                .Attenuation
                                .NONE,
                        0.0D,
                        0.0D,
                        0.0D,
                        true
                );

        /*
         * play()より先に保存して、
         * 同一tick内の重複要求を防ぐ。
         */
        currentBgm =
                newBgm;

        requestedPlayer =
                player;

        requestedLevel =
                level;

        requestedGameTime =
                currentGameTime;

        soundManager.play(
                newBgm
        );
    }

    public static void stop() {
        SimpleSoundInstance stoppingBgm =
                currentBgm;

        clearCurrentRequest();

        if (stoppingBgm == null) {
            return;
        }

        Minecraft.getInstance()
                .getSoundManager()
                .stop(
                        stoppingBgm
                );
    }

    public static boolean isPlaying() {
        return currentBgm != null
                && Minecraft.getInstance()
                .getSoundManager()
                .isActive(
                        currentBgm
                );
    }

    /**
     * 現在のクライアント環境で、
     * 有効な再生要求を保持しているか。
     */
    public static boolean isStartRequested() {
        Minecraft minecraft =
                Minecraft.getInstance();

        return currentBgm != null
                && requestedPlayer
                == minecraft.player
                && requestedLevel
                == minecraft.level;
    }

    private static void clearCurrentRequest() {
        currentBgm = null;
        requestedPlayer = null;
        requestedLevel = null;
        requestedGameTime =
                Long.MIN_VALUE;
    }
}