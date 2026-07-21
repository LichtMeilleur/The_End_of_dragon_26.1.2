package com.licht_meilleur.the_end_of_dragon.neoforge.registry;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.registry.ModSounds;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NeoForgeModSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(
                    Registries.SOUND_EVENT,
                    TheEndOfDragon.MOD_ID
            );

    public static final DeferredHolder<SoundEvent, SoundEvent> TED_JUDGMENT_RAY =
            register("ted_judgment_ray");

    public static final DeferredHolder<SoundEvent, SoundEvent> TED_LIGHTING =
            register("ted_lighting");

    public static final DeferredHolder<SoundEvent, SoundEvent> TED_ORB_CHARGE =
            register("ted_orb_charge");

    public static final DeferredHolder<SoundEvent, SoundEvent> TED_ORB_SHOOTING =
            register("ted_orb_shooting");

    public static final DeferredHolder<SoundEvent, SoundEvent> TED_PHOTON_BUSTER =
            register("ted_photon_buster");

    public static final DeferredHolder<SoundEvent, SoundEvent> TED_PHOTON_BLASTER =
            register("ted_photon_blaster");

    public static final DeferredHolder<SoundEvent, SoundEvent> TED_RAGNAROK =
            register("ted_ragnarok");

    public static final DeferredHolder<SoundEvent, SoundEvent> TED_ROAR =
            register("ted_roar");

    public static final DeferredHolder<SoundEvent, SoundEvent> TED_SHOT =
            register("ted_shot");

    public static final DeferredHolder<SoundEvent, SoundEvent> TED_TACKLE =
            register("ted_tackle");

    public static final DeferredHolder<SoundEvent, SoundEvent> TED_DEAD =
            register("ted_dead");

    public static final DeferredHolder<SoundEvent, SoundEvent> TED_JET =
            register("ted_jet");

    public static final DeferredHolder<SoundEvent, SoundEvent> SYUUMATU_NO_LAEVATEIN =
            register("syuumatu_no_laevatein");

    public static final DeferredHolder<SoundEvent, SoundEvent> ALLY_ENDERMAN_SONAR =
            register("sonar");

    private static DeferredHolder<SoundEvent, SoundEvent> register(
            String name
    ) {
        return SOUNDS.register(
                name,
                () -> SoundEvent.createVariableRangeEvent(
                        TheEndOfDragon.id(name)
                )
        );
    }

    public static void register(IEventBus modBus) {
        SOUNDS.register(modBus);
    }

    public static void bindCommonReferences() {
        ModSounds.bindNeoForge(
                TED_JUDGMENT_RAY.get(),
                TED_LIGHTING.get(),
                TED_ORB_CHARGE.get(),
                TED_ORB_SHOOTING.get(),
                TED_PHOTON_BUSTER.get(),
                TED_PHOTON_BLASTER.get(),
                TED_RAGNAROK.get(),
                TED_ROAR.get(),
                TED_SHOT.get(),
                TED_TACKLE.get(),
                TED_DEAD.get(),
                TED_JET.get(),
                SYUUMATU_NO_LAEVATEIN.get(),
                ALLY_ENDERMAN_SONAR.get()
        );
    }

    private NeoForgeModSounds() {
    }
}