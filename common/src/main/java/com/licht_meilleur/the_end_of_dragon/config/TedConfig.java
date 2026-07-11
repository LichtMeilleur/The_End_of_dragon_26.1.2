package com.licht_meilleur.the_end_of_dragon.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class TedConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static Values values = new Values();

    private TedConfig() {}

    public static void load(Path configDir) {
        Path path = configDir.resolve(TheEndOfDragon.MOD_ID + ".json");

        try {
            if (!Files.exists(path)) {
                Files.createDirectories(configDir);
                Files.writeString(path, GSON.toJson(values));
                return;
            }

            Values loaded = GSON.fromJson(Files.readString(path), Values.class);
            values = loaded != null ? loaded : new Values();
        } catch (IOException e) {
            values = new Values();
            TheEndOfDragon.LOGGER.error("Failed to load The End of Dragon config", e);
        }
    }

    public static final class Values {
        public double healthMultiplier = 1.0D;
        public double damageMultiplier = 1.0D;

        public float roarDamage = 6.0F;
        public int roarEquipmentDamage = 40;

        public float blasterTackleDamage = 18.0F;
        public float superLandingDamage = 18.0F;

        public float laserDamage = 10.0F;

        public float lightProjectileDamage = 16.0F;

        public float judgmentRayDamage = 7.0F;

        public float photonBusterDamage = 22.0F;

        public float orbDamage = 9999.0F;




        public float tailWhipDamage = 14.0F;
        public double tailWhipRadius = 18.0D;
        public double tailWhipKnockback = 2.2D;
        public double tailWhipKnockbackY = 0.45D;


        public boolean enableBlockBreak = true;

        public boolean enableEquipmentBreak = true;

        public boolean blockBreakEnabled = true;
    }
}