package com.licht_meilleur.the_end_of_dragon.entity.hitbox;

import com.google.gson.*;
import com.licht_meilleur.the_end_of_dragon.entity.TheEndOfDragonCoreEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class DragonLocatorSampler {
    private static final double PIXEL = 1.0D / 16.0D;

    private static boolean loaded;
    private static final Map<String, LocatorSpec> LOCATORS = new HashMap<>();

    public static Vec3 worldPos(TheEndOfDragonCoreEntity dragon, String locatorName) {
        load();

        LocatorSpec locator = LOCATORS.get(locatorName);
        if (locator == null) {
            return dragon.position();
        }

        // 最初は「geo上のlocator位置だけ」を使う。
        // アニメ追従まで必要なら、後で BoneAnim を足す。
        return transformModelPoint(dragon, new Vec3(
                locator.pos.x(),
                locator.pos.y(),
                locator.pos.z()
        ));
    }

    public static Vec3 forward(TheEndOfDragonCoreEntity dragon) {
        double yaw = Math.toRadians(dragon.getYRot());

        // Minecraft yaw: 0=south(+Z), 90=west(-X)
        return new Vec3(
                -Math.sin(yaw),
                0.0D,
                Math.cos(yaw)
        ).normalize();
    }

    private static Vec3 transformModelPoint(TheEndOfDragonCoreEntity dragon, Vec3 local) {
        // collisionで確定した座標変換と同じ系
        local = new Vec3(
                -local.x,
                local.y,
                local.z
        ).scale(PIXEL);

        double yaw = Math.toRadians(-dragon.getYRot() + 180.0D);
        double cos = Math.cos(yaw);
        double sin = Math.sin(yaw);

        double x = local.x * cos + local.z * sin;
        double z = -local.x * sin + local.z * cos;

        return new Vec3(
                dragon.getX() + x,
                dragon.getY() + local.y,
                dragon.getZ() + z
        );
    }

    private static void load() {
        if (loaded) {
            return;
        }

        try (Reader reader = openAsset("geckolib/models/the_end_of_dragon.geo.json")) {
            if (reader == null) {
                loaded = true;
                return;
            }

            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray geometry = root.getAsJsonArray("minecraft:geometry");
            JsonArray bones = geometry.get(0).getAsJsonObject().getAsJsonArray("bones");

            for (JsonElement boneElement : bones) {
                JsonObject bone = boneElement.getAsJsonObject();
                String boneName = bone.get("name").getAsString();

                if (!bone.has("locators")) {
                    continue;
                }

                JsonObject locators = bone.getAsJsonObject("locators");

                for (Map.Entry<String, JsonElement> entry : locators.entrySet()) {
                    Vector3f pos = readLocator(entry.getValue());
                    LOCATORS.put(entry.getKey(), new LocatorSpec(boneName, pos));
                }
            }

            loaded = true;
            //System.out.println("[TED LOCATOR] loaded=" + LOCATORS.keySet());
        } catch (Throwable t) {
            loaded = true;
            //System.err.println("[TED LOCATOR] failed to load locators");
            t.printStackTrace();
        }
    }

    private static Vector3f readLocator(JsonElement element) {
        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            return new Vector3f(
                    array.get(0).getAsFloat(),
                    array.get(1).getAsFloat(),
                    array.get(2).getAsFloat()
            );
        }

        JsonObject obj = element.getAsJsonObject();
        JsonArray offset = obj.has("offset")
                ? obj.getAsJsonArray("offset")
                : obj.getAsJsonArray("position");

        return new Vector3f(
                offset.get(0).getAsFloat(),
                offset.get(1).getAsFloat(),
                offset.get(2).getAsFloat()
        );
    }

    private static Reader openAsset(String path) {
        InputStream stream = DragonLocatorSampler.class
                .getClassLoader()
                .getResourceAsStream("assets/the_end_of_dragon/" + path);

        if (stream == null) {
            System.err.println("[TED LOCATOR] asset not found: assets/the_end_of_dragon/" + path);
            return null;
        }

        return new InputStreamReader(stream, StandardCharsets.UTF_8);
    }

    private record LocatorSpec(String boneName, Vector3f pos) {
    }

    private DragonLocatorSampler() {
    }
}