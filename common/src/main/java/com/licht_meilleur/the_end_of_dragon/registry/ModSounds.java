package com.licht_meilleur.the_end_of_dragon.registry;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public final class ModSounds {

    public static SoundEvent TED_JUDGMENT_RAY;
    public static SoundEvent TED_LIGHTING;
    public static SoundEvent TED_ORB_CHARGE;
    public static SoundEvent TED_ORB_SHOOTING;
    public static SoundEvent TED_PHOTON_BUSTER;
    public static SoundEvent TED_PHOTON_BLASTER;
    public static SoundEvent TED_RAGNAROK;
    public static SoundEvent TED_ROAR;
    public static SoundEvent TED_SHOT;
    public static SoundEvent TED_TACKLE;

    public static SoundEvent TED_DEAD;
    public static SoundEvent TED_JET;

    public static SoundEvent SYUUMATU_NO_LAEVATEIN;

    private static boolean fabricRegistered = false;

    public static void registerFabric() {
        if (fabricRegistered) {
            return;
        }

        fabricRegistered = true;

        TED_JUDGMENT_RAY = registerFabricSound("ted_judgment_ray");
        TED_LIGHTING = registerFabricSound("ted_lighting");
        TED_ORB_CHARGE = registerFabricSound("ted_orb_charge");
        TED_ORB_SHOOTING = registerFabricSound("ted_orb_shooting");
        TED_PHOTON_BUSTER = registerFabricSound("ted_photon_buster");
        TED_PHOTON_BLASTER = registerFabricSound("ted_photon_blaster");
        TED_RAGNAROK = registerFabricSound("ted_ragnarok");
        TED_ROAR = registerFabricSound("ted_roar");
        TED_SHOT = registerFabricSound("ted_shot");
        TED_TACKLE = registerFabricSound("ted_tackle");

        TED_DEAD = registerFabricSound("ted_dead");
        TED_JET = registerFabricSound("ted_jet");

        SYUUMATU_NO_LAEVATEIN =
                registerFabricSound("syuumatu_no_laevatein");

        TheEndOfDragon.LOGGER.info(
                "Registered The End Of Dragon sounds for Fabric"
        );
    }

    private static SoundEvent registerFabricSound(String name) {
        Identifier id = TheEndOfDragon.id(name);

        return Registry.register(
                BuiltInRegistries.SOUND_EVENT,
                id,
                SoundEvent.createVariableRangeEvent(id)
        );
    }

    public static void bindNeoForge(
            SoundEvent judgmentRay,
            SoundEvent lighting,
            SoundEvent orbCharge,
            SoundEvent orbShooting,
            SoundEvent photonBuster,
            SoundEvent photonBlaster,
            SoundEvent ragnarok,
            SoundEvent roar,
            SoundEvent shot,
            SoundEvent tackle,
            SoundEvent dead,
            SoundEvent jet,
            SoundEvent bgm
    ) {
        TED_JUDGMENT_RAY = judgmentRay;
        TED_LIGHTING = lighting;
        TED_ORB_CHARGE = orbCharge;
        TED_ORB_SHOOTING = orbShooting;
        TED_PHOTON_BUSTER = photonBuster;
        TED_PHOTON_BLASTER = photonBlaster;
        TED_RAGNAROK = ragnarok;
        TED_ROAR = roar;
        TED_SHOT = shot;
        TED_TACKLE = tackle;

        TED_DEAD = dead;
        TED_JET = jet;

        SYUUMATU_NO_LAEVATEIN = bgm;

        TheEndOfDragon.LOGGER.info(
                "Bound NeoForge sounds to common registry references"
        );
    }

    private ModSounds() {
    }
}