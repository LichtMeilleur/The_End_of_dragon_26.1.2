package com.licht_meilleur.the_end_of_dragon;

import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TheEndOfDragon {

    public static final String MOD_ID =
            "the_end_of_dragon";

    public static final Logger LOGGER =
            LoggerFactory.getLogger(MOD_ID);

    /**
     * Fabric / NeoForge共通の初期化場所。
     *
     * Registry登録は各ローダー側で行うため、
     * ここからModEntities、ModItems、ModSoundsは呼ばない。
     */
    public static void init() {
        LOGGER.info(
                "Initializing The End Of Dragon common systems"
        );
    }

    private TheEndOfDragon() {
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(
                MOD_ID,
                path
        );
    }
}