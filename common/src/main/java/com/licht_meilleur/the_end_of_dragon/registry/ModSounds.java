package com.licht_meilleur.the_end_of_dragon.registry;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public final class ModSounds {

    // 攻撃
    public static final SoundEvent TED_JUDGMENT_RAY =
            register("ted_judgment_ray");

    public static final SoundEvent TED_LIGHTING =
            register("ted_lighting");

    public static final SoundEvent TED_ORB_CHARGE =
            register("ted_orb_charge");

    public static final SoundEvent TED_ORB_SHOOTING =
            register("ted_orb_shooting");

    public static final SoundEvent TED_PHOTON_BUSTER =
            register("ted_photon_buster");

    public static final SoundEvent TED_PHOTON_BLASTER =
            register("ted_photon_blaster");

    public static final SoundEvent TED_RAGNAROK =
            register("ted_ragnarok");

    public static final SoundEvent TED_ROAR =
            register("ted_roar");

    public static final SoundEvent TED_SHOT =
            register("ted_shot");

    public static final SoundEvent TED_TACKLE =
            register("ted_tackle");

    // 移動・死亡
    public static final SoundEvent TED_DEAD =
            register("ted_dead");

    public static final SoundEvent TED_JET =
            register("ted_jet");

    // BGM
    public static final SoundEvent SYUUMATU_NO_LAEVATEIN =
            register("syuumatu_no_laevatein");

    private ModSounds() {
    }

    private static SoundEvent register(String name) {
        Identifier id = TheEndOfDragon.id(name);

        return Registry.register(
                BuiltInRegistries.SOUND_EVENT,
                id,
                SoundEvent.createVariableRangeEvent(id)
        );
    }

    /*
     * TheEndOfDragon.init()から呼び、クラスを読み込ませる。
     * SoundEvent自体はstaticフィールドの初期化時に登録される。
     */
    public static void init() {
        TheEndOfDragon.LOGGER.info(
                "Registering The End Of Dragon sounds"
        );
    }
}