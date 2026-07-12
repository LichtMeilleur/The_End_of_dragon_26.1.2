package com.licht_meilleur.the_end_of_dragon.client.sound;

import com.licht_meilleur.the_end_of_dragon.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public final class TedBgmManager {
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

        if (currentBgm != null
                && minecraft.getSoundManager()
                .isActive(currentBgm)) {
            return;
        }

        stop();

        RandomSource random =
                minecraft.level.getRandom();

        currentBgm = new SimpleSoundInstance(
                ModSounds.SYUUMATU_NO_LAEVATEIN.location(),
                SoundSource.MUSIC,
                1.0F,
                1.0F,
                random,
                true, // loop
                0,
                SimpleSoundInstance.Attenuation.NONE,
                0.0D,
                0.0D,
                0.0D,
                true // relative
        );

        minecraft.getSoundManager()
                .play(currentBgm);
    }

    public static void stop() {
        if (currentBgm == null) {
            return;
        }

        Minecraft.getInstance()
                .getSoundManager()
                .stop(currentBgm);

        currentBgm = null;
    }

    public static boolean isPlaying() {
        return currentBgm != null
                && Minecraft.getInstance()
                .getSoundManager()
                .isActive(currentBgm);
    }
}