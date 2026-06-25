package com.licht_meilleur.the_end_of_dragon.client.hitbox;

import net.minecraft.world.phys.Vec3;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class DragonLocatorSnapshot {
    private static final Map<UUID, Map<String, Vec3>> LOCATORS = new HashMap<>();

    public static void begin(UUID uuid) {
        LOCATORS.put(uuid, new HashMap<>());
    }

    public static void put(UUID uuid, String locatorName, Vec3 worldPos) {
        LOCATORS.computeIfAbsent(uuid, ignored -> new HashMap<>()).put(locatorName, worldPos);
    }

    public static Map<String, Vec3> get(UUID uuid) {
        return LOCATORS.getOrDefault(uuid, Collections.emptyMap());
    }

    public static void clear(UUID uuid) {
        LOCATORS.remove(uuid);
    }

    private DragonLocatorSnapshot() {
    }
}