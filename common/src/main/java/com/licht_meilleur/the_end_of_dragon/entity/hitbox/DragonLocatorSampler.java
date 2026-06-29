package com.licht_meilleur.the_end_of_dragon.entity.hitbox;

import com.google.gson.*;
import com.licht_meilleur.the_end_of_dragon.entity.DragonState;
import com.licht_meilleur.the_end_of_dragon.entity.TheEndOfDragonCoreEntity;
import com.licht_meilleur.the_end_of_dragon.entity.collision.DragonCollisionSampler;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class DragonLocatorSampler {
    private static final double PIXEL = 1.0D / 16.0D;

    private static boolean loaded;

    private static final Map<String, BoneSpec> BONES = new HashMap<>();
    private static final Map<String, LocatorSpec> LOCATORS = new HashMap<>();
    private static final Map<String, AnimationSpec> ANIMATIONS = new HashMap<>();

    public static Vec3 worldPos(TheEndOfDragonCoreEntity dragon, String locatorName) {
        load();

        LocatorSpec locator = LOCATORS.get(locatorName);
        if (locator == null) {
            return dragon.position();
        }

        Vec3 point = new Vec3(
                locator.pos.x(),
                locator.pos.y(),
                locator.pos.z()
        );

        point = applyBoneChain(dragon, locator.boneName, point);

        return transformModelPoint(dragon, point);
    }

    public static Vec3 forward(TheEndOfDragonCoreEntity dragon) {
        double yaw = Math.toRadians(dragon.getYRot());

        return new Vec3(
                -Math.sin(yaw),
                0.0D,
                Math.cos(yaw)
        ).normalize();
    }

    private static Vec3 applyBoneChain(TheEndOfDragonCoreEntity dragon, String boneName, Vec3 point) {
        AnimationSpec animation = ANIMATIONS.get(animationName(dragon.getDragonState()));
        if (animation == null) {
            return point;
        }

        double time = dragon.getDragonStateAgeTicks() / 20.0D;
        if (animation.loop && animation.length > 0.0D) {
            time = time % animation.length;
        }

        // 重要：
        // Blockbenchのlocator座標はほぼ「モデル全体座標」なので、
        // 親boneを全部かけると二重変換になって大きくズレやすい。
        // まずは locator が属している bone のアニメだけを適用する。
        BoneSpec bone = BONES.get(boneName);
        if (bone == null) {
            return point;
        }

        BoneAnim anim = animation.bones.get(bone.name);
        if (anim == null) {
            return point;
        }

        Vector3f pos = anim.position.sample(time);
        Vector3f rot = anim.rotation.sample(time);

        Vec3 pivot = new Vec3(
                bone.pivot.x(),
                bone.pivot.y(),
                bone.pivot.z()
        );

        Vec3 local = point.subtract(pivot);

        local = rotateXYZ(
                local,
                Math.toRadians(-rot.x()),
                Math.toRadians(rot.y()),
                Math.toRadians(-rot.z())
        );

        return local.add(pivot).add(pos.x(), pos.y(), pos.z());
    }

    private static Vec3 transformModelPoint(TheEndOfDragonCoreEntity dragon, Vec3 local) {
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

        try {
            BONES.clear();
            LOCATORS.clear();
            ANIMATIONS.clear();

            loadGeo();
            loadAnimations();

            loaded = true;

            System.out.println("[TED LOCATOR] bones=" + BONES.size()
                    + " locators=" + LOCATORS.size()
                    + " animations=" + ANIMATIONS.size());
        } catch (Throwable t) {
            loaded = true;
            System.err.println("[TED LOCATOR] failed to load");
            t.printStackTrace();
        }
    }

    private static void loadGeo() throws Exception {
        try (Reader reader = openAsset("geckolib/models/the_end_of_dragon.geo.json")) {
            if (reader == null) {
                return;
            }

            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray geometry = root.getAsJsonArray("minecraft:geometry");
            JsonArray bones = geometry.get(0).getAsJsonObject().getAsJsonArray("bones");

            for (JsonElement boneElement : bones) {
                JsonObject bone = boneElement.getAsJsonObject();

                String boneName = bone.get("name").getAsString();

                String parent = bone.has("parent")
                        ? bone.get("parent").getAsString()
                        : null;

                Vector3f pivot = bone.has("pivot")
                        ? readVector(bone.getAsJsonArray("pivot"))
                        : new Vector3f(0.0F, 0.0F, 0.0F);

                BONES.put(boneName, new BoneSpec(boneName, parent, pivot));

                if (!bone.has("locators")) {
                    continue;
                }

                JsonObject locators = bone.getAsJsonObject("locators");

                for (Map.Entry<String, JsonElement> entry : locators.entrySet()) {
                    Vector3f pos = readLocator(entry.getValue());
                    LOCATORS.put(entry.getKey(), new LocatorSpec(boneName, pos));
                }
            }
        }
    }

    private static void loadAnimations() throws Exception {
        try (Reader reader = openAsset("geckolib/animations/the_end_of_dragon.animation.json")) {
            if (reader == null) {
                return;
            }

            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonObject animations = root.getAsJsonObject("animations");

            for (Map.Entry<String, JsonElement> animationEntry : animations.entrySet()) {
                String animationName = animationEntry.getKey();
                JsonObject animationJson = animationEntry.getValue().getAsJsonObject();

                AnimationSpec spec = new AnimationSpec();
                spec.loop = animationJson.has("loop") && animationJson.get("loop").getAsBoolean();
                spec.length = animationJson.has("animation_length")
                        ? animationJson.get("animation_length").getAsDouble()
                        : 1.0D;

                if (animationJson.has("bones")) {
                    JsonObject bones = animationJson.getAsJsonObject("bones");

                    for (Map.Entry<String, JsonElement> boneEntry : bones.entrySet()) {
                        JsonObject boneJson = boneEntry.getValue().getAsJsonObject();

                        BoneAnim boneAnim = new BoneAnim();

                        if (boneJson.has("position")) {
                            boneAnim.position = Track.parse(boneJson.get("position"));
                        }

                        if (boneJson.has("rotation")) {
                            boneAnim.rotation = Track.parse(boneJson.get("rotation"));
                        }

                        spec.bones.put(boneEntry.getKey(), boneAnim);
                        spec.length = Math.max(spec.length, boneAnim.position.maxTime());
                        spec.length = Math.max(spec.length, boneAnim.rotation.maxTime());
                    }
                }

                ANIMATIONS.put(animationName, spec);
            }
        }
    }

    private static String animationName(DragonState state) {
        return switch (state) {
            case WALK -> "animation.model.walk";

            case FLY_START -> "animation.model.fly_start_12tick_start";
            case FLY -> "animation.model.fly";
            case FLY_LEFT -> "animation.model.fly_left";
            case FLY_RIGHT -> "animation.model.fly_right";
            case FLY_SHOT -> "animation.model.fly_shot_6tick_start";

            case FALL -> "animation.model.fall";
            case LANDING -> "animation.model.landing";
            case SUPER_LANDING -> "animation.model.super_landing";

            case ORB_OF_ANNIHILATION -> "animation.model.orb_of_annihilation_6tick_start_66tick_fire";
            case ROAR_OF_OBLITERATION -> "animation.model.roar_of_obliteration_3tick_start";
            case FLAMES_OF_RAGNAROK -> "animation.model.flames_of_ragnarok";
            case LIGHT_OF_DESTRUCTION -> "animation.model.light_of_destruction_24tick_start";
            case PHOTON_BLASTER -> "animation.model.photon_blaster_31tick_start";
            case BLASTER_TACKLE -> "animation.model.blaster_tackle_12tick_start";

            default -> "animation.model.idle";
        };
    }

    private static Vector3f readLocator(JsonElement element) {
        if (element.isJsonArray()) {
            return readVector(element.getAsJsonArray());
        }

        JsonObject obj = element.getAsJsonObject();
        JsonArray offset = obj.has("offset")
                ? obj.getAsJsonArray("offset")
                : obj.getAsJsonArray("position");

        return readVector(offset);
    }

    private static Vector3f readVector(JsonArray array) {
        return new Vector3f(
                array.get(0).getAsFloat(),
                array.get(1).getAsFloat(),
                array.get(2).getAsFloat()
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

    private static Vec3 rotateXYZ(Vec3 v, double rx, double ry, double rz) {
        double x = v.x;
        double y = v.y;
        double z = v.z;

        double cosX = Math.cos(rx);
        double sinX = Math.sin(rx);
        double y1 = y * cosX - z * sinX;
        double z1 = y * sinX + z * cosX;
        y = y1;
        z = z1;

        double cosY = Math.cos(ry);
        double sinY = Math.sin(ry);
        double x1 = x * cosY + z * sinY;
        z1 = -x * sinY + z * cosY;
        x = x1;
        z = z1;

        double cosZ = Math.cos(rz);
        double sinZ = Math.sin(rz);
        x1 = x * cosZ - y * sinZ;
        y1 = x * sinZ + y * cosZ;

        return new Vec3(x1, y1, z);
    }

    private record BoneSpec(String name, String parent, Vector3f pivot) {
    }

    private record LocatorSpec(String boneName, Vector3f pos) {
    }

    private static final class AnimationSpec {
        boolean loop;
        double length;
        final Map<String, BoneAnim> bones = new HashMap<>();
    }

    private static final class BoneAnim {
        Track position = Track.zero();
        Track rotation = Track.zero();
    }

    private static final class Track {
        private final TreeMap<Double, Vector3f> keys = new TreeMap<>();

        static Track zero() {
            Track track = new Track();
            track.keys.put(0.0D, new Vector3f(0, 0, 0));
            return track;
        }

        static Track parse(JsonElement element) {
            Track track = new Track();

            if (element.isJsonObject()) {
                JsonObject object = element.getAsJsonObject();

                if (object.has("vector")) {
                    track.keys.put(0.0D, readVector(object.getAsJsonArray("vector")));
                    return track;
                }

                for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                    double time = Double.parseDouble(entry.getKey());
                    JsonObject key = entry.getValue().getAsJsonObject();

                    if (key.has("vector")) {
                        track.keys.put(time, readVector(key.getAsJsonArray("vector")));
                    }
                }
            }

            if (track.keys.isEmpty()) {
                track.keys.put(0.0D, new Vector3f(0, 0, 0));
            }

            return track;
        }

        Vector3f sample(double time) {
            Map.Entry<Double, Vector3f> floor = keys.floorEntry(time);
            Map.Entry<Double, Vector3f> ceil = keys.ceilingEntry(time);

            if (floor == null) {
                return new Vector3f(keys.firstEntry().getValue());
            }

            if (ceil == null) {
                return new Vector3f(keys.lastEntry().getValue());
            }

            if (Objects.equals(floor.getKey(), ceil.getKey())) {
                return new Vector3f(floor.getValue());
            }

            double t = (time - floor.getKey()) / (ceil.getKey() - floor.getKey());

            Vector3f a = floor.getValue();
            Vector3f b = ceil.getValue();

            return new Vector3f(
                    (float) (a.x() + (b.x() - a.x()) * t),
                    (float) (a.y() + (b.y() - a.y()) * t),
                    (float) (a.z() + (b.z() - a.z()) * t)
            );
        }

        double maxTime() {
            return keys.isEmpty() ? 0.0D : keys.lastKey();
        }
    }

    private DragonLocatorSampler() {
    }

    public static void debugDrawLocator(
            net.minecraft.server.level.ServerLevel level,
            TheEndOfDragonCoreEntity dragon,
            String locatorName
    ) {
        Vec3 pos = worldPos(dragon, locatorName);

        level.sendParticles(
                net.minecraft.core.particles.ParticleTypes.FLAME,
                pos.x,
                pos.y,
                pos.z,
                12,
                0.08D,
                0.08D,
                0.08D,
                0.0D
        );

        if (dragon.tickCount % 20 == 0) {
            System.out.println("[TED LOCATOR DEBUG] " + locatorName + " world=" + pos);
        }
    }

    public record LocatorTransform(
            Vec3 position,
            Vec3 forward,
            Vec3 up,
            Vec3 right
    ) {
    }


    private record BoneTransform(
            Vec3 position,
            Vector3f rotation
    ){
    }



    private static Vec3 rotateDirection(
            Vec3 dir,
            Vector3f rot
    ){
        return rotateXYZ(
                dir,
                Math.toRadians(-rot.x()),
                Math.toRadians(rot.y()),
                Math.toRadians(-rot.z())
        );
    }



}