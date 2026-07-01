package com.licht_meilleur.the_end_of_dragon.entity.hitbox;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Predicate;

public record TedBeamHitbox(
        Vec3 start,
        Vec3 direction,
        double length,
        double radius
) {
    public Vec3 end() {
        return start.add(direction.normalize().scale(length));
    }

    public AABB bounds() {
        Vec3 end = end();

        return new AABB(start, end).inflate(radius);
    }

    public boolean contains(Vec3 point) {
        Vec3 dir = direction.normalize();
        Vec3 rel = point.subtract(start);

        double t = rel.dot(dir);

        if (t < 0.0D || t > length) {
            return false;
        }

        Vec3 closest = start.add(dir.scale(t));
        return closest.distanceToSqr(point) <= radius * radius;
    }

    public void damageEntities(
            ServerLevel level,
            Entity owner,
            float damage,
            Predicate<Entity> filter
    ) {
        List<Entity> entities = level.getEntities(owner, bounds(), entity ->
                entity instanceof LivingEntity
                        && entity.isAlive()
                        && entity != owner
                        && filter.test(entity)
        );

        for (Entity entity : entities) {
            if (!contains(entity.position().add(0.0D, entity.getBbHeight() * 0.5D, 0.0D))) {
                continue;
            }

            if (owner instanceof LivingEntity livingOwner) {
                entity.hurtServer(
                        level,
                        level.damageSources().mobAttack(livingOwner),
                        damage
                );
            } else {
                entity.hurtServer(
                        level,
                        level.damageSources().generic(),
                        damage
                );
            }
        }
    }

    public Vec3[] vertices() {
        Vec3 dir = direction.normalize();

        Vec3 baseUp = Math.abs(dir.dot(new Vec3(0, 1, 0))) > 0.95D
                ? new Vec3(1, 0, 0)
                : new Vec3(0, 1, 0);

        Vec3 right = dir.cross(baseUp).normalize();
        Vec3 up = right.cross(dir).normalize();

        Vec3 end = end();

        Vec3 r = right.scale(radius);
        Vec3 u = up.scale(radius);

        return new Vec3[]{
                start.add(r).add(u),
                start.add(r).subtract(u),
                start.subtract(r).add(u),
                start.subtract(r).subtract(u),

                end.add(r).add(u),
                end.add(r).subtract(u),
                end.subtract(r).add(u),
                end.subtract(r).subtract(u)
        };
    }

    public void debugDraw(ServerLevel level) {
        Vec3[] v = vertices();

        // 8頂点
        for (Vec3 p : v) {
            level.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.FLAME,
                    p.x, p.y, p.z,
                    2,
                    0.0D, 0.0D, 0.0D,
                    0.0D
            );
        }

        // 中心線
        debugLine(level, start, end(), net.minecraft.core.particles.ParticleTypes.END_ROD);

        // start面 4辺
        debugLine(level, v[0], v[1], net.minecraft.core.particles.ParticleTypes.SMOKE);
        debugLine(level, v[1], v[3], net.minecraft.core.particles.ParticleTypes.SMOKE);
        debugLine(level, v[3], v[2], net.minecraft.core.particles.ParticleTypes.SMOKE);
        debugLine(level, v[2], v[0], net.minecraft.core.particles.ParticleTypes.SMOKE);

        // end面 4辺
        debugLine(level, v[4], v[5], net.minecraft.core.particles.ParticleTypes.SMOKE);
        debugLine(level, v[5], v[7], net.minecraft.core.particles.ParticleTypes.SMOKE);
        debugLine(level, v[7], v[6], net.minecraft.core.particles.ParticleTypes.SMOKE);
        debugLine(level, v[6], v[4], net.minecraft.core.particles.ParticleTypes.SMOKE);

        // 長辺 4本
        debugLine(level, v[0], v[4], net.minecraft.core.particles.ParticleTypes.FLAME);
        debugLine(level, v[1], v[5], net.minecraft.core.particles.ParticleTypes.FLAME);
        debugLine(level, v[2], v[6], net.minecraft.core.particles.ParticleTypes.FLAME);
        debugLine(level, v[3], v[7], net.minecraft.core.particles.ParticleTypes.FLAME);
    }

    private static void debugLine(
            ServerLevel level,
            Vec3 from,
            Vec3 to,
            net.minecraft.core.particles.SimpleParticleType particle
    ) {
        int count = 24;

        for (int i = 0; i <= count; i++) {
            double t = i / (double) count;
            Vec3 p = from.lerp(to, t);

            level.sendParticles(
                    particle,
                    p.x, p.y, p.z,
                    1,
                    0.0D, 0.0D, 0.0D,
                    0.0D
            );
        }
    }

    public void spawnLaserParticles(ServerLevel level) {
        RandomSource random = level.getRandom();

        Vec3 dir = direction.normalize();

        Vec3 baseUp = Math.abs(dir.dot(new Vec3(0, 1, 0))) > 0.95D
                ? new Vec3(1, 0, 0)
                : new Vec3(0, 1, 0);

        Vec3 right = dir.cross(baseUp).normalize();
        Vec3 up = right.cross(dir).normalize();

        int steps = Math.max(12, (int) (length * 7.0D));

        for (int i = 0; i <= steps; i++) {
            double t = i / (double) steps;
            Vec3 center = start.add(dir.scale(length * t));

            // 白い芯
            level.sendParticles(
                    ParticleTypes.END_ROD,
                    center.x, center.y, center.z,
                    4,
                    0.003D, 0.003D, 0.003D,
                    0.0D
            );

// 黄色っぽい光
            level.sendParticles(
                    ParticleTypes.ELECTRIC_SPARK,
                    center.x, center.y, center.z,
                    4,
                    0.025D, 0.025D, 0.025D,
                    0.01D
            );

            // 外周：黄色〜白っぽい炎
            for (int r = 0; r < 7; r++) {
                double angle = random.nextDouble() * Math.PI * 2.0D + t * Math.PI * 10.0D;
                double dist = radius * (0.25D + random.nextDouble() * 0.75D);

                Vec3 p = center
                        .add(right.scale(Math.cos(angle) * dist))
                        .add(up.scale(Math.sin(angle) * dist));

                level.sendParticles(
                        ParticleTypes.SMALL_FLAME,
                        p.x, p.y, p.z,
                        1,
                        0.01D, 0.01D, 0.01D,
                        0.0D
                );
            }
        }

        // 着弾点の強調
        Vec3 end = end();

        level.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,
                end.x, end.y, end.z,
                12,
                0.2D, 0.2D, 0.2D,
                0.08D
        );

        level.sendParticles(
                ParticleTypes.END_ROD,
                end.x, end.y, end.z,
                8,
                0.15D, 0.15D, 0.15D,
                0.02D
        );
    }

    public void spawnJetParticles(ServerLevel level) {
        RandomSource random = level.getRandom();

        Vec3 dir = direction.normalize();

        Vec3 baseUp = Math.abs(dir.dot(new Vec3(0, 1, 0))) > 0.95D
                ? new Vec3(1, 0, 0)
                : new Vec3(0, 1, 0);

        Vec3 right = dir.cross(baseUp).normalize();
        Vec3 up = right.cross(dir).normalize();

        // 全体を少し長く使う
        double totalLength = length * 1.65D;

        // ① 外側に短く広がる炎
        spawnJetOuter(level, random, dir, right, up, totalLength);

        // ② 中央の長く細い芯
        spawnJetCore(level, random, dir, right, up, totalLength);
    }

    private void spawnJetOuter(
            ServerLevel level,
            RandomSource random,
            Vec3 dir,
            Vec3 right,
            Vec3 up,
            double totalLength
    ) {
        double outerLength = totalLength * 0.38D;
        int steps = Math.max(18, (int) (outerLength * 7.0D));

        for (int i = 0; i <= steps; i++) {
            double t = i / (double) steps;

            Vec3 center = start.add(dir.scale(outerLength * t));

            // 短く外へ広がって、最後は少し細る
            double r = radius * 0.45D + radius * 0.95D * smoothstep(t);
            if (t > 0.65D) {
                double k = (t - 0.65D) / 0.35D;
                r = lerp(r, radius * 0.45D, smoothstep(k));
            }

            int density = Math.max(2, (int) (22.0D * (1.0D - t * 0.45D)));

            for (int j = 0; j < density; j++) {
                double angle = random.nextDouble() * Math.PI * 2.0D;
                double dist = Math.sqrt(random.nextDouble()) * r;

                Vec3 p = center
                        .add(right.scale(Math.cos(angle) * dist))
                        .add(up.scale(Math.sin(angle) * dist));

                level.sendParticles(
                        jetOuterDust(t),
                        p.x, p.y, p.z,
                        1,
                        0.0D, 0.0D, 0.0D,
                        0.0D
                );

                if (t < 0.28D && random.nextFloat() < 0.25F) {
                    level.sendParticles(
                            ParticleTypes.END_ROD,
                            p.x, p.y, p.z,
                            1,
                            0.003D, 0.003D, 0.003D,
                            0.0D
                    );
                }
            }
        }
    }

    private void spawnJetCore(
            ServerLevel level,
            RandomSource random,
            Vec3 dir,
            Vec3 right,
            Vec3 up,
            double totalLength
    ) {
        double coreStart = totalLength * 0.12D;
        double coreLength = totalLength * 0.88D;
        int steps = Math.max(36, (int) (coreLength * 9.0D));

        for (int i = 0; i <= steps; i++) {
            double t = i / (double) steps;

            Vec3 center = start.add(dir.scale(coreStart + coreLength * t));

            // 芯は細い。先端ほどさらに細くする
            double r = radius * lerp(0.22D, 0.035D, Math.pow(t, 1.35D));

            int density = Math.max(1, (int) (14.0D * Math.pow(1.0D - t, 0.75D)));

            for (int j = 0; j < density; j++) {
                double angle = random.nextDouble() * Math.PI * 2.0D;
                double dist = Math.sqrt(random.nextDouble()) * r;

                Vec3 p = center
                        .add(right.scale(Math.cos(angle) * dist))
                        .add(up.scale(Math.sin(angle) * dist));

                level.sendParticles(
                        jetCoreDust(t),
                        p.x, p.y, p.z,
                        1,
                        0.0D, 0.0D, 0.0D,
                        0.0D
                );

                if (random.nextFloat() < 0.08F) {
                    level.sendParticles(
                            ParticleTypes.SMALL_FLAME,
                            p.x, p.y, p.z,
                            1,
                            0.004D, 0.004D, 0.004D,
                            0.0D
                    );
                }
            }
        }
    }

    private static DustParticleOptions jetOuterDust(double t) {
        if (t < 0.18D) {
            return new DustParticleOptions(0xFFFFF0, 1.55F);
        }

        if (t < 0.45D) {
            double k = (t - 0.18D) / 0.27D;
            return new DustParticleOptions(lerpColor(0xFFFFF0, 0xFFE600, k), 1.45F);
        }

        double k = (t - 0.45D) / 0.55D;
        return new DustParticleOptions(lerpColor(0xFFE600, 0xFF8A00, k), 1.15F);
    }

    private static DustParticleOptions jetCoreDust(double t) {
        if (t < 0.30D) {
            double k = t / 0.30D;
            return new DustParticleOptions(lerpColor(0xFFD000, 0xFF6A00, k), 1.05F);
        }

        if (t < 0.75D) {
            double k = (t - 0.30D) / 0.45D;
            return new DustParticleOptions(lerpColor(0xFF6A00, 0xE02000, k), 0.85F);
        }

        double k = (t - 0.75D) / 0.25D;
        return new DustParticleOptions(lerpColor(0xE02000, 0x702020, k), 0.55F);
    }

    private static double smoothstep(double x) {
        x = Math.max(0.0D, Math.min(1.0D, x));
        return x * x * (3.0D - 2.0D * x);
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    private static int lerpColor(int a, int b, double t) {
        t = Math.max(0.0D, Math.min(1.0D, t));

        int ar = (a >> 16) & 0xFF;
        int ag = (a >> 8) & 0xFF;
        int ab = a & 0xFF;

        int br = (b >> 16) & 0xFF;
        int bg = (b >> 8) & 0xFF;
        int bb = b & 0xFF;

        int r = (int) Math.round(lerp(ar, br, t));
        int g = (int) Math.round(lerp(ag, bg, t));
        int bl = (int) Math.round(lerp(ab, bb, t));

        return (r << 16) | (g << 8) | bl;
    }


}