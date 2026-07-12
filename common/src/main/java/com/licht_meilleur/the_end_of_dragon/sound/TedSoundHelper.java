package com.licht_meilleur.the_end_of_dragon.sound;

import com.licht_meilleur.the_end_of_dragon.registry.ModSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class TedSoundHelper {

    private static final float DEFAULT_BOSS_VOLUME = 2.0F;
    private static final float DEFAULT_PITCH = 1.0F;

    /*
     * The End Of Dragon全効果音の共通倍率。
     *
     * 1.0F = 元の音量
     * 0.5F = 半分
     * 0.25F = かなり控えめ
     */
    private static final float MASTER_VOLUME = 0.5F;


    private TedSoundHelper() {
    }

    public static void playBossSound(
            ServerLevel level,
            Entity source,
            SoundEvent sound
    ) {
        playBossSound(
                level,
                source,
                sound,
                DEFAULT_BOSS_VOLUME,
                DEFAULT_PITCH
        );
    }

    public static void playBossSound(
            ServerLevel level,
            Entity source,
            SoundEvent sound,
            float volume,
            float pitch
    ) {
        play(
                level,
                source.position(),
                sound,
                SoundSource.HOSTILE,
                volume,
                pitch
        );
    }

    public static void playAt(
            ServerLevel level,
            Vec3 position,
            SoundEvent sound,
            float volume,
            float pitch
    ) {
        play(
                level,
                position,
                sound,
                SoundSource.HOSTILE,
                volume,
                pitch
        );
    }

    /**
     * 通常飛行中のジェット音。
     *
     * 毎tick呼ばず、Core側から一定間隔で呼ぶ。
     */
    public static void playJet(
            ServerLevel level,
            Entity source
    ) {
        playBossSound(
                level,
                source,
                ModSounds.TED_JET,
                0.7F,
                1.0F
        );
    }

    /**
     * Blaster Tackle用の短い噴射音。
     */
    public static void playTackleJet(
            ServerLevel level,
            Entity source
    ) {
        playBossSound(
                level,
                source,
                ModSounds.TED_TACKLE,
                5.0F,
                1.0F
        );
    }

    /**
     * 小型光弾の発射音。
     */
    public static void playShot(
            ServerLevel level,
            Vec3 position
    ) {
        float pitch =
                0.95F + level.getRandom().nextFloat() * 0.10F;

        playAt(
                level,
                position,
                ModSounds.TED_SHOT,
                1.4F,
                pitch
        );
    }

    /**
     * 結晶が砕けてEntityが消える瞬間。
     */
    public static void playDeathShatter(
            ServerLevel level,
            Entity source
    ) {
        playBossSound(
                level,
                source,
                ModSounds.TED_DEAD,
                8.0F,
                1.0F
        );
    }

    private static void play(
            ServerLevel level,
            Vec3 position,
            SoundEvent sound,
            SoundSource source,
            float volume,
            float pitch
    ) {
        level.playSound(
                null,
                position.x,
                position.y,
                position.z,
                sound,
                source,
                volume * MASTER_VOLUME,
                pitch
        );
    }

    public static void playEventBgm(
            ServerLevel level,
            Entity source
    ) {
        /*
         * BGMには効果音用MASTER_VOLUMEを適用しない。
         * 音楽カテゴリなので、Minecraftの「音楽」音量で調節可能。
         */
        level.playSound(
                null,
                source.getX(),
                source.getY(),
                source.getZ(),
                ModSounds.SYUUMATU_NO_LAEVATEIN,
                SoundSource.MUSIC,
                0.7F,
                1.0F
        );
    }
}