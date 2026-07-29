package com.licht_meilleur.the_end_of_dragon.client.sound;

import com.licht_meilleur.the_end_of_dragon.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public final class TedBgmManager {

    /*
     * nullでなければ、再生要求済み。
     *
     * SoundManager#isActive()は再生開始直前に
     * falseを返すことがあるため、
     * 多重開始防止には使用しない。
     */
    private static SimpleSoundInstance currentBgm;

    private TedBgmManager() {
    }

    public static void start() {
        Minecraft minecraft =
                Minecraft.getInstance();

        if (minecraft.level == null
                || minecraft.player == null) {
            return;
        }

        /*
         * 再生中かどうかではなく、
         * すでに再生要求を出したかで判定する。
         */
        if (currentBgm != null) {
            return;
        }

        RandomSource random =
                minecraft.level.getRandom();

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
         * play()より先に保存する。
         * 同じクライアントtick中に再度start()されても
         * 新しい音源を作らせない。
         */
        currentBgm = newBgm;

        minecraft.getSoundManager()
                .play(newBgm);
    }

    public static void stop() {
        SimpleSoundInstance stoppingBgm =
                currentBgm;

        /*
         * stop()処理中に再度呼ばれても
         * 同じインスタンスを扱わないよう先にnullへ戻す。
         */
        currentBgm = null;

        if (stoppingBgm == null) {
            return;
        }

        Minecraft.getInstance()
                .getSoundManager()
                .stop(stoppingBgm);
    }

    public static boolean isPlaying() {
        return currentBgm != null
                && Minecraft.getInstance()
                .getSoundManager()
                .isActive(currentBgm);
    }

    /**
     * 再生要求済みか。
     *
     * 多重開始防止の判定にはこちらを使用する。
     */
    public static boolean isStartRequested() {
        return currentBgm != null;
    }
}