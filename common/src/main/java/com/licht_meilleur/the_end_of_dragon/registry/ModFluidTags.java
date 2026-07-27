package com.licht_meilleur.the_end_of_dragon.registry;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;


public final class ModFluidTags {

    public static final TagKey<Fluid>
            RECHORUS_JUICE =
            TagKey.create(
                    Registries.FLUID,
                    TheEndOfDragon.id(
                            "rechorus_juice"
                    )
            );

    private ModFluidTags() {
    }
}