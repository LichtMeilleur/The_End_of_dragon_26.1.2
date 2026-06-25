package com.licht_meilleur.the_end_of_dragon;

import com.licht_meilleur.the_end_of_dragon.registry.ModEntities;
import com.licht_meilleur.the_end_of_dragon.registry.ModItems;
import net.minecraft.resources.Identifier;

public final class TheEndOfDragon {
    public static final String MOD_ID = "the_end_of_dragon";

    public static void init() {
        ModEntities.init();
        ModItems.init();
    }

    private TheEndOfDragon() {
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}