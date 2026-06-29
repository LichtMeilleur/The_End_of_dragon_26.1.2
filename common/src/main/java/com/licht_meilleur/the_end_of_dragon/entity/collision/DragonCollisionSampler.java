package com.licht_meilleur.the_end_of_dragon.entity.collision;

import com.google.gson.*;
import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.entity.DragonState;
import com.licht_meilleur.the_end_of_dragon.entity.TheEndOfDragonCollisionEntity;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class DragonCollisionSampler {
    private static final double PIXEL = 1.0D / 16.0D;

    private static boolean loaded;
    private static final Map<String, CubeSpec> CUBES = new LinkedHashMap<>();
    private static final Map<String, AnimationSpec> ANIMATIONS = new HashMap<>();

    private static final String DEBUG_BONE = "front_left_hand_collision";

    private static final double MODEL_OFFSET_X = 0.0D;
    private static final double MODEL_OFFSET_Y = 0.0D;
    private static final double MODEL_OFFSET_Z = 0.0D;

    public static List<DragonCollisionBox> buildBoxes(TheEndOfDragonCollisionEntity entity) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return List.of();
        }

        load(serverLevel);

        String animationName = animationName(entity.getDragonState());
        AnimationSpec animation = ANIMATIONS.get(animationName);

        if (entity.tickCount % 40 == 0) {
            System.out.println(
                    "[TED COLLISION] state=" + entity.getDragonState()
                            + " animationName=" + animationName
                            + " animationFound=" + (animation != null)
                            + " cubes=" + CUBES.size()
                            + " animations=" + ANIMATIONS.size()
            );
        }

        if (animation == null || CUBES.isEmpty()) {
            return List.of();
        }

        double time = entity.getDragonStateAgeTicks() / 20.0D;
        if (animation.loop && animation.length > 0.0D) {
            time = time % animation.length;
        }

        List<DragonCollisionBox> result = new ArrayList<>();

        for (CubeSpec cube : CUBES.values()) {
            /*
            if (!cube.boneName.equals(DEBUG_BONE)) {
                continue;
            }

             */

            BoneAnim boneAnim = animation.bones.get(cube.boneName);
            if (boneAnim == null) {
                if (entity.tickCount % 40 == 0) {
                    //System.out.println("[TED ARM DEBUG] boneAnim not found: " + cube.boneName);
                }
                continue;
            }

            Vector3f pos = boneAnim.position.sample(time);
            Vector3f rot = boneAnim.rotation.sample(time);




            if (entity.tickCount % 2 == 0 && cube.boneName.equals(DEBUG_BONE)) {
                // アニメ移動・回転後の「箱そのもの」をパーティクルで表示
                debugDrawAnimatedCube(serverLevel, entity, cube, pos, rot);

                // アニメ移動後のpivotも表示
                Vec3 pivotWorld = transformCubePoint(
                        entity,
                        cube,
                        new Vec3(cube.pivot.x(), cube.pivot.y(), cube.pivot.z()),
                        pos,
                        rot
                );

                serverLevel.sendParticles(
                        net.minecraft.core.particles.ParticleTypes.FLAME,
                        pivotWorld.x,
                        pivotWorld.y,
                        pivotWorld.z,
                        3,
                        0.03D,
                        0.03D,
                        0.03D,
                        0.0D
                );
            }

            Vec3[] points = buildAnimatedCubePoints(entity, cube, pos, rot);
            AABB box = aabbFromPoints(points);
            DragonOBB obb = DragonOBB.fromPoints(points);

            if (entity.tickCount % 20 == 0 && cube.boneName.equals(DEBUG_BONE)) {
                logBoneDebug(entity, cube, pos, rot, box, time);
            }

            result.add(new DragonCollisionBox(
                    partFromBone(cube.boneName),
                    box,
                    points,
                    obb
            ));
        }

        return result;
    }

    private static void logBoneDebug(
            TheEndOfDragonCollisionEntity entity,
            CubeSpec cube,
            Vector3f pos,
            Vector3f rot,
            AABB box,
            double time
    ) {
        double centerX = (box.minX + box.maxX) * 0.5D;
        double centerY = (box.minY + box.maxY) * 0.5D;
        double centerZ = (box.minZ + box.maxZ) * 0.5D;

        double sizeX = box.maxX - box.minX;
        double sizeY = box.maxY - box.minY;
        double sizeZ = box.maxZ - box.minZ;

        /*
        System.out.println(
                "[TED ARM DEBUG]"
                        + " state=" + entity.getDragonState()
                        + " time=" + String.format("%.3f", time)
                        + " entityPos=("
                        + f(entity.getX()) + ", "
                        + f(entity.getY()) + ", "
                        + f(entity.getZ()) + ")"
                        + " entityYaw=" + f(entity.getYRot())
        );

        System.out.println(
                "[TED ARM DEBUG]"
                        + " bone=" + cube.boneName
                        + " pivot=" + v(cube.pivot)
                        + " origin=" + v(cube.origin)
                        + " size=" + v(cube.size)
                        + " animPos=" + v(pos)
                        + " animRot=" + v(rot)
        );

        System.out.println(
                "[TED ARM DEBUG]"
                        + " aabbCenter=("
                        + f(centerX) + ", "
                        + f(centerY) + ", "
                        + f(centerZ) + ")"
                        + " aabbSize=("
                        + f(sizeX) + ", "
                        + f(sizeY) + ", "
                        + f(sizeZ) + ")"
                        + " aabbMin=("
                        + f(box.minX) + ", "
                        + f(box.minY) + ", "
                        + f(box.minZ) + ")"
                        + " aabbMax=("
                        + f(box.maxX) + ", "
                        + f(box.maxY) + ", "
                        + f(box.maxZ) + ")"


        );

         */
    }

    /*
    private static String v(Vector3f v) {
        return "(" + f(v.x()) + ", " + f(v.y()) + ", " + f(v.z()) + ")";
    }

    private static String f(double value) {
        return String.format(java.util.Locale.ROOT, "%.3f", value);
    }

     */

    private static AABB makeWorldAabb(
            TheEndOfDragonCollisionEntity entity,
            CubeSpec cube,
            Vector3f pos,
            Vector3f rotDeg
    ) {
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double minZ = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        double maxZ = -Double.MAX_VALUE;

        double x0 = cube.origin.x;
        double y0 = cube.origin.y;
        double z0 = cube.origin.z;
        double x1 = cube.origin.x + cube.size.x;
        double y1 = cube.origin.y + cube.size.y;
        double z1 = cube.origin.z + cube.size.z;

        double[] xs = {x0, x1};
        double[] ys = {y0, y1};
        double[] zs = {z0, z1};

        for (double x : xs) {
            for (double y : ys) {
                for (double z : zs) {
                    Vec3 corner = new Vec3(x, y, z);

// pivot基準にする
                    Vec3 pivot = new Vec3(cube.pivot.x(), cube.pivot.y(), cube.pivot.z());

                    Vec3 local = corner.subtract(pivot);

                    local = rotateXYZ(
                            local,
                            Math.toRadians(rotDeg.x()),
                            Math.toRadians(rotDeg.y()),
                            Math.toRadians(rotDeg.z())
                    );

// pivotへ戻してからposition加算
                    local = local.add(pivot).add(pos.x(), pos.y(), pos.z());

                    local = new Vec3(
                            local.x,
                            local.y,
                            -local.z
                    ).scale(PIXEL);

                    local = local.add(
                            MODEL_OFFSET_X,
                            MODEL_OFFSET_Y,
                            MODEL_OFFSET_Z
                    );

                    Vec3 world = transformCubePoint(
                            entity,
                            cube,
                            new Vec3(x, y, z),
                            pos,
                            rotDeg
                    );




                    minX = Math.min(minX, world.x);
                    minY = Math.min(minY, world.y);
                    minZ = Math.min(minZ, world.z);
                    maxX = Math.max(maxX, world.x);
                    maxY = Math.max(maxY, world.y);
                    maxZ = Math.max(maxZ, world.z);
                }
            }
        }

        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    private static Vec3 applyEntityYaw(TheEndOfDragonCollisionEntity entity, Vec3 local) {
        // south固定なら、モデル全体を180度回す
        double yaw = Math.toRadians(-entity.getYRot() + 180);

        double cos = Math.cos(yaw);
        double sin = Math.sin(yaw);

        double x = local.x * cos + local.z * sin;
        double z = -local.x * sin + local.z * cos;

        return new Vec3(
                entity.getX() + x,
                entity.getY() + local.y,
                entity.getZ() + z
        );
    }



    private static void load(ServerLevel level) {
        if (loaded) {
            return;
        }

        try {
            CUBES.clear();
            ANIMATIONS.clear();

            loadGeo(level);
            loadAnimations(level);

            loaded = !CUBES.isEmpty() && !ANIMATIONS.isEmpty();
/*
            System.out.println(
                    "[TED COLLISION] load result loaded=" + loaded
                            + " cubes=" + CUBES.size()
                            + " animations=" + ANIMATIONS.size()
            );

            System.out.println("[TED COLLISION] cube keys=" + CUBES.keySet());
            System.out.println("[TED COLLISION] animation keys=" + ANIMATIONS.keySet());

 */
        } catch (Throwable e) {
            loaded = false;
            //System.err.println("[TED COLLISION] failed to load collision resources");
            e.printStackTrace();
        }
    }

    private static void loadGeo(ServerLevel level) throws Exception {
        try (Reader reader = openAsset("geckolib/models/the_end_of_dragon_collision.geo.json")) {
            if (reader == null) {
                return;
            }

            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray geometry = root.getAsJsonArray("minecraft:geometry");
            JsonArray bones = geometry.get(0).getAsJsonObject().getAsJsonArray("bones");

            for (JsonElement boneElement : bones) {
                JsonObject bone = boneElement.getAsJsonObject();
                String boneName = bone.get("name").getAsString();

                if (!bone.has("cubes")) {
                    continue;
                }

                JsonObject cube = bone.getAsJsonArray("cubes").get(0).getAsJsonObject();

                Vector3f pivot = bone.has("pivot")
                        ? readVector(bone.getAsJsonArray("pivot"))
                        : new Vector3f(0, 0, 0);

                Vector3f origin = readVector(cube.getAsJsonArray("origin"));
                Vector3f size = readVector(cube.getAsJsonArray("size"));

                CUBES.put(boneName, new CubeSpec(boneName, pivot, origin, size));
            }
        }
    }

    private static void loadAnimations(ServerLevel level) throws Exception {
        try (Reader reader = openAsset("geckolib/animations/the_end_of_dragon_collision.animation.json")) {
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

    private static Vector3f readVector(JsonArray array) {
        return new Vector3f(
                array.get(0).getAsFloat(),
                array.get(1).getAsFloat(),
                array.get(2).getAsFloat()
        );
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

    private static DragonCollisionPart partFromBone(String boneName) {
        return switch (boneName) {
            case "upper_body_collision" -> DragonCollisionPart.UPPER_BODY;
            case "head_collision" -> DragonCollisionPart.HEAD;
            case "chest_crystal_collision" -> DragonCollisionPart.CHEST_CRYSTAL;

            case "front_left_arm_collision" -> DragonCollisionPart.FRONT_LEFT_ARM;
            case "front_right_arm_collision" -> DragonCollisionPart.FRONT_RIGHT_ARM;
            case "back_left_arm_collision" -> DragonCollisionPart.BACK_LEFT_ARM;
            case "back_right_arm_collision" -> DragonCollisionPart.BACK_RIGHT_ARM;

            case "front_left_fore_arm_collision" -> DragonCollisionPart.FRONT_LEFT_FORE_ARM;
            case "front_right_fore_arm_collision" -> DragonCollisionPart.FRONT_RIGHT_FORE_ARM;
            case "back_left_fore_arm_collision" -> DragonCollisionPart.BACK_LEFT_FORE_ARM;
            case "back_right_fore_arm_collision" -> DragonCollisionPart.BACK_RIGHT_FORE_ARM;

            case "front_left_hand_collision" -> DragonCollisionPart.FRONT_LEFT_HAND;
            case "front_right_hand_collision" -> DragonCollisionPart.FRONT_RIGHT_HAND;
            case "back_left_hand_collision" -> DragonCollisionPart.BACK_LEFT_HAND;
            case "back_right_hand_collision" -> DragonCollisionPart.BACK_RIGHT_HAND;

            case "left_leg_collision" -> DragonCollisionPart.LEFT_LEG;
            case "right_leg_collision" -> DragonCollisionPart.RIGHT_LEG;

            case "tail_root_collision" -> DragonCollisionPart.TAIL_ROOT;
            case "tail_tip_collision" -> DragonCollisionPart.TAIL_TIP;

            default -> DragonCollisionPart.UNKNOWN;
        };
    }

    private record CubeSpec(String boneName, Vector3f pivot, Vector3f origin, Vector3f size) {
    }

    private static final class AnimationSpec {
        boolean loop;
        double length;
        final Map<String, BoneAnim> bones = new HashMap<>();
    }

    public static final class BoneAnim {
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

    private DragonCollisionSampler() {
    }

    private static Reader openAsset(String path) {
        InputStream stream = DragonCollisionSampler.class
                .getClassLoader()
                .getResourceAsStream("assets/the_end_of_dragon/" + path);

        if (stream == null) {
            System.err.println("[TED COLLISION] asset not found: assets/the_end_of_dragon/" + path);
            return null;
        }

        return new InputStreamReader(stream, StandardCharsets.UTF_8);
    }

    private static Vec3 makeWorldPoint(
            TheEndOfDragonCollisionEntity entity,
            Vector3f point,
            Vector3f pos
    ) {
        Vec3 local = new Vec3(
                point.x() + pos.x(),
                point.y() + pos.y(),
                point.z() + pos.z()
        );

        local = new Vec3(
                local.x,
                local.y,
                -local.z
        ).scale(PIXEL);

        local = local.add(
                MODEL_OFFSET_X,
                MODEL_OFFSET_Y,
                MODEL_OFFSET_Z
        );

        return applyEntityYaw(entity, local);
    }

    private static void debugDrawAnimatedCube(
            ServerLevel level,
            TheEndOfDragonCollisionEntity entity,
            CubeSpec cube,
            Vector3f pos,
            Vector3f rot
    ) {
        double x0 = cube.origin.x();
        double y0 = cube.origin.y();
        double z0 = cube.origin.z();
        double x1 = cube.origin.x() + cube.size.x();
        double y1 = cube.origin.y() + cube.size.y();
        double z1 = cube.origin.z() + cube.size.z();

        Vec3[] p = new Vec3[] {
                transformCubePoint(entity, cube, new Vec3(x0, y0, z0), pos, rot),
                transformCubePoint(entity, cube, new Vec3(x1, y0, z0), pos, rot),
                transformCubePoint(entity, cube, new Vec3(x1, y1, z0), pos, rot),
                transformCubePoint(entity, cube, new Vec3(x0, y1, z0), pos, rot),

                transformCubePoint(entity, cube, new Vec3(x0, y0, z1), pos, rot),
                transformCubePoint(entity, cube, new Vec3(x1, y0, z1), pos, rot),
                transformCubePoint(entity, cube, new Vec3(x1, y1, z1), pos, rot),
                transformCubePoint(entity, cube, new Vec3(x0, y1, z1), pos, rot)
        };

        drawLine(level, p[0], p[1]);
        drawLine(level, p[1], p[2]);
        drawLine(level, p[2], p[3]);
        drawLine(level, p[3], p[0]);

        drawLine(level, p[4], p[5]);
        drawLine(level, p[5], p[6]);
        drawLine(level, p[6], p[7]);
        drawLine(level, p[7], p[4]);

        drawLine(level, p[0], p[4]);
        drawLine(level, p[1], p[5]);
        drawLine(level, p[2], p[6]);
        drawLine(level, p[3], p[7]);
    }

    private static Vec3 transformCubePoint(
            TheEndOfDragonCollisionEntity entity,
            CubeSpec cube,
            Vec3 localPoint,
            Vector3f pos,
            Vector3f rotDeg
    ) {
        Vec3 pivot = new Vec3(cube.pivot.x(), cube.pivot.y(), cube.pivot.z());

        Vec3 local = localPoint.subtract(pivot);

        local = rotateXYZ(
                local,
                Math.toRadians(-rotDeg.x()),
                Math.toRadians(rotDeg.y()),
                Math.toRadians(-rotDeg.z())
        );
        // 180度反転テスト：Y軸回り
        //local = new Vec3(-local.x, local.y, -local.z);

        local = local.add(pivot).add(pos.x(), pos.y(), pos.z());

        local = new Vec3(
                -local.x,
                local.y,
                local.z
        ).scale(PIXEL);

        local = local.add(
                MODEL_OFFSET_X,
                MODEL_OFFSET_Y,
                MODEL_OFFSET_Z
        );

        return applyEntityYaw(entity, local);
    }

    private static void drawLine(ServerLevel level, Vec3 a, Vec3 b) {
        double dx = b.x - a.x;
        double dy = b.y - a.y;
        double dz = b.z - a.z;

        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        int count = Math.max(1, (int) (length / 0.25D));

        for (int i = 0; i <= count; i++) {
            double t = (double) i / count;

            level.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.END_ROD,
                    a.x + dx * t,
                    a.y + dy * t,
                    a.z + dz * t,
                    1,
                    0.0D,
                    0.0D,
                    0.0D,
                    0.0D
            );
        }
    }
    private static Vec3 rotateYXZ(Vec3 v, double rx, double ry, double rz) {
        double x = v.x;
        double y = v.y;
        double z = v.z;

        // Y
        double cos = Math.cos(ry);
        double sin = Math.sin(ry);
        double nx = x * cos + z * sin;
        double nz = -x * sin + z * cos;
        x = nx;
        z = nz;

        // X
        cos = Math.cos(rx);
        sin = Math.sin(rx);
        double ny = y * cos - z * sin;
        nz = y * sin + z * cos;
        y = ny;
        z = nz;

        // Z
        cos = Math.cos(rz);
        sin = Math.sin(rz);
        nx = x * cos - y * sin;
        ny = x * sin + y * cos;

        return new Vec3(nx, ny, z);
    }

    private static Vec3 rotateZYX(Vec3 v, double rx, double ry, double rz) {
        double x = v.x;
        double y = v.y;
        double z = v.z;

        // Z
        double cos = Math.cos(rz);
        double sin = Math.sin(rz);

        double nx = x * cos - y * sin;
        double ny = x * sin + y * cos;

        x = nx;
        y = ny;

        // Y
        cos = Math.cos(ry);
        sin = Math.sin(ry);

        nx = x * cos + z * sin;
        double nz = -x * sin + z * cos;

        x = nx;
        z = nz;

        // X
        cos = Math.cos(rx);
        sin = Math.sin(rx);

        ny = y * cos - z * sin;
        nz = y * sin + z * cos;

        return new Vec3(x, ny, nz);
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

    private static Vec3 rotateXZY(Vec3 v, double rx, double ry, double rz) {
        double x = v.x;
        double y = v.y;
        double z = v.z;

        // X
        double cos = Math.cos(rx);
        double sin = Math.sin(rx);
        double ny = y * cos - z * sin;
        double nz = y * sin + z * cos;
        y = ny;
        z = nz;

        // Z
        cos = Math.cos(rz);
        sin = Math.sin(rz);
        double nx = x * cos - y * sin;
        ny = x * sin + y * cos;
        x = nx;
        y = ny;

        // Y
        cos = Math.cos(ry);
        sin = Math.sin(ry);
        nx = x * cos + z * sin;
        nz = -x * sin + z * cos;

        return new Vec3(nx, y, nz);
    }

    private static Vec3[] buildAnimatedCubePoints(
            TheEndOfDragonCollisionEntity entity,
            CubeSpec cube,
            Vector3f pos,
            Vector3f rot
    ) {
        double x0 = cube.origin.x();
        double y0 = cube.origin.y();
        double z0 = cube.origin.z();
        double x1 = cube.origin.x() + cube.size.x();
        double y1 = cube.origin.y() + cube.size.y();
        double z1 = cube.origin.z() + cube.size.z();

        return new Vec3[]{
                transformCubePoint(entity, cube, new Vec3(x0, y0, z0), pos, rot),
                transformCubePoint(entity, cube, new Vec3(x1, y0, z0), pos, rot),
                transformCubePoint(entity, cube, new Vec3(x1, y1, z0), pos, rot),
                transformCubePoint(entity, cube, new Vec3(x0, y1, z0), pos, rot),
                transformCubePoint(entity, cube, new Vec3(x0, y0, z1), pos, rot),
                transformCubePoint(entity, cube, new Vec3(x1, y0, z1), pos, rot),
                transformCubePoint(entity, cube, new Vec3(x1, y1, z1), pos, rot),
                transformCubePoint(entity, cube, new Vec3(x0, y1, z1), pos, rot)
        };
    }

    private static AABB aabbFromPoints(Vec3[] points) {
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double minZ = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        double maxZ = -Double.MAX_VALUE;

        for (Vec3 p : points) {
            minX = Math.min(minX, p.x);
            minY = Math.min(minY, p.y);
            minZ = Math.min(minZ, p.z);
            maxX = Math.max(maxX, p.x);
            maxY = Math.max(maxY, p.y);
            maxZ = Math.max(maxZ, p.z);
        }

        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }


}