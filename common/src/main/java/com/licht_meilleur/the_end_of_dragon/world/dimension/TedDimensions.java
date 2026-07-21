package com.licht_meilleur.the_end_of_dragon.world.dimension;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public final class TedDimensions {

    public static final ResourceKey<Level>
            ENDERMAN_VILLAGE =
            ResourceKey.create(
                    Registries.DIMENSION,
                    TheEndOfDragon.id(
                            "enderman_village"
                    )
            );

    private TedDimensions() {
    }
}