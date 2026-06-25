package com.licht_meilleur.the_end_of_dragon.client.hitbox;

import com.geckolib.cache.model.GeoBone;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.lang.reflect.Method;
import java.util.Map;

public final class GeckoLocatorUtil {
    public static Vec3 worldPosFromCurrentMatrix(PoseStack poseStack, float lx, float ly, float lz) {
        Matrix4f mat = new Matrix4f(poseStack.last().pose());
        Vector4f v = new Vector4f(lx, ly, lz, 1.0f);
        v.mul(mat);
        return new Vec3(v.x, v.y, v.z);
    }

    public static Vec3 tryGetLocatorLocalPos(GeoBone bone, String locatorName) {
        try {
            Method m = bone.getClass().getMethod("getLocators");
            Object map = m.invoke(bone);

            if (map instanceof Map<?, ?> mp) {
                Object value = mp.get(locatorName);
                if (value == null) {
                    return null;
                }

                return new Vec3(
                        readFieldAsDouble(value, "x"),
                        readFieldAsDouble(value, "y"),
                        readFieldAsDouble(value, "z")
                );
            }
        } catch (Throwable ignored) {
        }

        try {
            Method m = bone.getClass().getMethod("getLocatorPosition", String.class);
            Object value = m.invoke(bone, locatorName);

            if (value == null) {
                return null;
            }

            double x = readFieldAsDouble(value, "x");
            double y = readFieldAsDouble(value, "y");
            double z = readFieldAsDouble(value, "z");

            if (x == 0.0D && y == 0.0D && z == 0.0D) {
                return null;
            }

            return new Vec3(x, y, z);
        } catch (Throwable ignored) {
        }

        return null;
    }

    private static double readFieldAsDouble(Object obj, String field) {
        try {
            var f = obj.getClass().getField(field);
            Object value = f.get(obj);
            if (value instanceof Number number) {
                return number.doubleValue();
            }
        } catch (Throwable ignored) {
        }

        try {
            String getterName = "get" + Character.toUpperCase(field.charAt(0)) + field.substring(1);
            Method m = obj.getClass().getMethod(getterName);
            Object value = m.invoke(obj);
            if (value instanceof Number number) {
                return number.doubleValue();
            }
        } catch (Throwable ignored) {
        }

        return 0.0D;
    }

    private GeckoLocatorUtil() {
    }
}