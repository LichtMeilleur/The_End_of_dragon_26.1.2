package com.licht_meilleur.the_end_of_dragon.entity.collision;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class DragonOBB {
    private static final double EPS = 1.0E-7D;

    private final Vec3 center;
    private final Vec3 axisX;
    private final Vec3 axisY;
    private final Vec3 axisZ;
    private final double halfX;
    private final double halfY;
    private final double halfZ;

    private DragonOBB(
            Vec3 center,
            Vec3 axisX,
            Vec3 axisY,
            Vec3 axisZ,
            double halfX,
            double halfY,
            double halfZ
    ) {
        this.center = center;
        this.axisX = normalizeSafe(axisX);
        this.axisY = normalizeSafe(axisY);
        this.axisZ = normalizeSafe(axisZ);
        this.halfX = halfX;
        this.halfY = halfY;
        this.halfZ = halfZ;
    }

    public static DragonOBB fromPoints(Vec3[] p) {
        // buildAnimatedCubePoints の順番:
        // 0=(x0,y0,z0), 1=(x1,y0,z0), 2=(x1,y1,z0), 3=(x0,y1,z0)
        // 4=(x0,y0,z1), 5=(x1,y0,z1), 6=(x1,y1,z1), 7=(x0,y1,z1)
        Vec3 center = new Vec3(0.0D, 0.0D, 0.0D);
        for (Vec3 point : p) {
            center = center.add(point);
        }
        center = center.scale(1.0D / 8.0D);

        Vec3 edgeX = p[1].subtract(p[0]);
        Vec3 edgeY = p[3].subtract(p[0]);
        Vec3 edgeZ = p[4].subtract(p[0]);

        double halfX = edgeX.length() * 0.5D;
        double halfY = edgeY.length() * 0.5D;
        double halfZ = edgeZ.length() * 0.5D;

        return new DragonOBB(
                center,
                edgeX,
                edgeY,
                edgeZ,
                halfX,
                halfY,
                halfZ
        );
    }

    public boolean intersects(AABB box) {
        Vec3 boxCenter = new Vec3(
                (box.minX + box.maxX) * 0.5D,
                (box.minY + box.maxY) * 0.5D,
                (box.minZ + box.maxZ) * 0.5D
        );

        Vec3 boxHalf = new Vec3(
                (box.maxX - box.minX) * 0.5D,
                (box.maxY - box.minY) * 0.5D,
                (box.maxZ - box.minZ) * 0.5D
        );

        Vec3[] a = {
                this.axisX,
                this.axisY,
                this.axisZ
        };

        Vec3[] b = {
                new Vec3(1.0D, 0.0D, 0.0D),
                new Vec3(0.0D, 1.0D, 0.0D),
                new Vec3(0.0D, 0.0D, 1.0D)
        };

        double[] ea = {this.halfX, this.halfY, this.halfZ};
        double[] eb = {boxHalf.x, boxHalf.y, boxHalf.z};

        double[][] r = new double[3][3];
        double[][] absR = new double[3][3];

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                r[i][j] = dot(a[i], b[j]);
                absR[i][j] = Math.abs(r[i][j]) + EPS;
            }
        }

        Vec3 tVec = boxCenter.subtract(this.center);
        double[] t = {
                dot(tVec, a[0]),
                dot(tVec, a[1]),
                dot(tVec, a[2])
        };

        double ra;
        double rb;

        // A0, A1, A2
        for (int i = 0; i < 3; i++) {
            ra = ea[i];
            rb = eb[0] * absR[i][0] + eb[1] * absR[i][1] + eb[2] * absR[i][2];

            if (Math.abs(t[i]) > ra + rb) {
                return false;
            }
        }

        // B0, B1, B2
        for (int j = 0; j < 3; j++) {
            ra = ea[0] * absR[0][j] + ea[1] * absR[1][j] + ea[2] * absR[2][j];
            rb = eb[j];

            double tj = Math.abs(
                    t[0] * r[0][j]
                            + t[1] * r[1][j]
                            + t[2] * r[2][j]
            );

            if (tj > ra + rb) {
                return false;
            }
        }

        // A0 x B0
        ra = ea[1] * absR[2][0] + ea[2] * absR[1][0];
        rb = eb[1] * absR[0][2] + eb[2] * absR[0][1];
        if (Math.abs(t[2] * r[1][0] - t[1] * r[2][0]) > ra + rb) return false;

        // A0 x B1
        ra = ea[1] * absR[2][1] + ea[2] * absR[1][1];
        rb = eb[0] * absR[0][2] + eb[2] * absR[0][0];
        if (Math.abs(t[2] * r[1][1] - t[1] * r[2][1]) > ra + rb) return false;

        // A0 x B2
        ra = ea[1] * absR[2][2] + ea[2] * absR[1][2];
        rb = eb[0] * absR[0][1] + eb[1] * absR[0][0];
        if (Math.abs(t[2] * r[1][2] - t[1] * r[2][2]) > ra + rb) return false;

        // A1 x B0
        ra = ea[0] * absR[2][0] + ea[2] * absR[0][0];
        rb = eb[1] * absR[1][2] + eb[2] * absR[1][1];
        if (Math.abs(t[0] * r[2][0] - t[2] * r[0][0]) > ra + rb) return false;

        // A1 x B1
        ra = ea[0] * absR[2][1] + ea[2] * absR[0][1];
        rb = eb[0] * absR[1][2] + eb[2] * absR[1][0];
        if (Math.abs(t[0] * r[2][1] - t[2] * r[0][1]) > ra + rb) return false;

        // A1 x B2
        ra = ea[0] * absR[2][2] + ea[2] * absR[0][2];
        rb = eb[0] * absR[1][1] + eb[1] * absR[1][0];
        if (Math.abs(t[0] * r[2][2] - t[2] * r[0][2]) > ra + rb) return false;

        // A2 x B0
        ra = ea[0] * absR[1][0] + ea[1] * absR[0][0];
        rb = eb[1] * absR[2][2] + eb[2] * absR[2][1];
        if (Math.abs(t[1] * r[0][0] - t[0] * r[1][0]) > ra + rb) return false;

        // A2 x B1
        ra = ea[0] * absR[1][1] + ea[1] * absR[0][1];
        rb = eb[0] * absR[2][2] + eb[2] * absR[2][0];
        if (Math.abs(t[1] * r[0][1] - t[0] * r[1][1]) > ra + rb) return false;

        // A2 x B2
        ra = ea[0] * absR[1][2] + ea[1] * absR[0][2];
        rb = eb[0] * absR[2][1] + eb[1] * absR[2][0];
        return Math.abs(t[1] * r[0][2] - t[0] * r[1][2]) <= ra + rb;
    }

    private static Vec3 normalizeSafe(Vec3 v) {
        double length = v.length();

        if (length < EPS) {
            return new Vec3(0.0D, 1.0D, 0.0D);
        }

        return v.scale(1.0D / length);
    }

    private static double dot(Vec3 a, Vec3 b) {
        return a.x * b.x + a.y * b.y + a.z * b.z;
    }
}