package com.licht_meilleur.the_end_of_dragon.entity.enderman.goal;

import com.licht_meilleur.the_end_of_dragon.entity.TheEndOfDragonCoreEntity;
import com.licht_meilleur.the_end_of_dragon.entity.enderman.TedAllyEndermanEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;

public final class AllyEndermanAiUtil {

    private AllyEndermanAiUtil() {
    }

    public static TheEndOfDragonCoreEntity findDragon(
            TedAllyEndermanEntity ally,
            ServerLevel level
    ) {
        AABB searchArea =
                ally.getBoundingBox().inflate(
                        256.0D,
                        192.0D,
                        256.0D
                );

        return level.getEntitiesOfClass(
                        TheEndOfDragonCoreEntity.class,
                        searchArea,
                        dragon ->
                                dragon.isAlive()
                                        && !dragon.isRemoved()
                )
                .stream()
                .min(
                        Comparator.comparingDouble(
                                ally::distanceToSqr
                        )
                )
                .orElse(null);
    }

    public static Player findPlayer(
            TedAllyEndermanEntity ally,
            ServerLevel level
    ) {
        return level.getNearestPlayer(
                ally,
                192.0D
        );
    }

    public static void faceTarget(
            TedAllyEndermanEntity ally,
            LivingEntity target
    ) {
        Vec3 direction =
                target.position()
                        .subtract(ally.position());

        if (direction.horizontalDistanceSqr()
                < 1.0E-6D) {
            return;
        }

        float yaw =
                (float) Math.toDegrees(
                        Math.atan2(
                                -direction.x,
                                direction.z
                        )
                );

        ally.setYRot(yaw);
        ally.setYBodyRot(yaw);
        ally.setYHeadRot(yaw);

        ally.yRotO = yaw;
        ally.yBodyRotO = yaw;
        ally.yHeadRotO = yaw;
    }

    public static boolean teleportAlly(
            TedAllyEndermanEntity ally,
            ServerLevel level,
            Vec3 destination
    ) {
        if (destination == null) {
            return false;
        }

        Vec3 origin = ally.position();

        playTeleportEffect(
                level,
                origin
        );

        ally.getNavigation().stop();

        ally.snapTo(
                destination.x,
                destination.y,
                destination.z,
                ally.getYRot(),
                ally.getXRot()
        );

        ally.setDeltaMovement(Vec3.ZERO);
        ally.hurtMarked = true;

        playTeleportEffect(
                level,
                destination
        );

        return true;
    }

    public static void playTeleportEffect(
            ServerLevel level,
            Vec3 position
    ) {
        level.playSound(
                null,
                position.x,
                position.y,
                position.z,
                SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.NEUTRAL,
                1.0F,
                1.0F
        );

        level.sendParticles(
                ParticleTypes.PORTAL,
                position.x,
                position.y + 1.0D,
                position.z,
                32,
                0.45D,
                0.9D,
                0.45D,
                0.2D
        );
    }

    public static Vec3 findSafePositionAround(
            TedAllyEndermanEntity ally,
            ServerLevel level,
            Vec3 center,
            double minimumRadius,
            double maximumRadius
    ) {
        for (int attempt = 0;
             attempt < 32;
             attempt++) {

            double angle =
                    ally.getRandom().nextDouble()
                            * Math.PI
                            * 2.0D;

            double radius =
                    minimumRadius
                            + ally.getRandom().nextDouble()
                            * Math.max(
                            0.0D,
                            maximumRadius
                                    - minimumRadius
                    );

            int x =
                    Mth.floor(
                            center.x
                                    + Math.cos(angle)
                                    * radius
                    );

            int z =
                    Mth.floor(
                            center.z
                                    + Math.sin(angle)
                                    * radius
                    );

            int y =
                    level.getHeight(
                            Heightmap.Types
                                    .MOTION_BLOCKING_NO_LEAVES,
                            x,
                            z
                    );

            BlockPos feet =
                    new BlockPos(
                            x,
                            y,
                            z
                    );

            if (!isSafeTeleportPosition(
                    ally,
                    level,
                    feet
            )) {
                continue;
            }

            return Vec3.atBottomCenterOf(feet);
        }

        return null;
    }

    public static Vec3 findPositionAwayFromDragon(
            TedAllyEndermanEntity ally,
            ServerLevel level,
            TheEndOfDragonCoreEntity dragon,
            Vec3 origin,
            double minimumDistance,
            double maximumDistance
    ) {
        Vec3 away =
                origin.subtract(
                        dragon.position()
                );

        away =
                new Vec3(
                        away.x,
                        0.0D,
                        away.z
                );

        if (away.lengthSqr() < 1.0E-6D) {
            double randomAngle =
                    ally.getRandom().nextDouble()
                            * Math.PI
                            * 2.0D;

            away =
                    new Vec3(
                            Math.cos(randomAngle),
                            0.0D,
                            Math.sin(randomAngle)
                    );
        } else {
            away = away.normalize();
        }

        Vec3 side =
                new Vec3(
                        away.z,
                        0.0D,
                        -away.x
                );

        for (int attempt = 0;
             attempt < 32;
             attempt++) {

            double distance =
                    minimumDistance
                            + ally.getRandom().nextDouble()
                            * (
                            maximumDistance
                                    - minimumDistance
                    );

            double sideOffset =
                    (
                            ally.getRandom().nextDouble()
                                    - 0.5D
                    ) * 16.0D;

            Vec3 center =
                    origin
                            .add(
                                    away.scale(distance)
                            )
                            .add(
                                    side.scale(sideOffset)
                            );

            Vec3 destination =
                    findSafePositionAround(
                            ally,
                            level,
                            center,
                            0.0D,
                            4.0D
                    );

            if (destination == null) {
                continue;
            }

            if (destination.distanceToSqr(
                    dragon.position()
            ) < minimumDistance
                    * minimumDistance) {
                continue;
            }

            return destination;
        }

        return null;
    }

    public static Vec3 findAttackPosition(
            TedAllyEndermanEntity ally,
            ServerLevel level,
            TheEndOfDragonCoreEntity dragon
    ) {
        Vec3 toAlly =
                ally.position()
                        .subtract(
                                dragon.position()
                        );

        toAlly =
                new Vec3(
                        toAlly.x,
                        0.0D,
                        toAlly.z
                );

        if (toAlly.lengthSqr() < 1.0E-6D) {
            double angle =
                    ally.getRandom().nextDouble()
                            * Math.PI
                            * 2.0D;

            toAlly =
                    new Vec3(
                            Math.cos(angle),
                            0.0D,
                            Math.sin(angle)
                    );
        } else {
            toAlly = toAlly.normalize();
        }

        /*
         * 龍の中心ではなく、
         * 当たり判定のすぐ隣へ置く。
         */
        double attackDistance =
                Math.max(
                        4.0D,
                        dragon.getBbWidth()
                                * 0.5D
                                + 2.0D
                );

        Vec3 preferred =
                dragon.position()
                        .add(
                                toAlly.scale(
                                        attackDistance
                                )
                        );

        return findSafePositionAround(
                ally,
                level,
                preferred,
                0.0D,
                2.5D
        );
    }

    private static boolean isSafeTeleportPosition(
            TedAllyEndermanEntity ally,
            ServerLevel level,
            BlockPos feet
    ) {
        BlockPos head =
                feet.above();

        BlockPos upperHead =
                head.above();

        BlockPos floor =
                feet.below();

        if (!level.getWorldBorder()
                .isWithinBounds(feet)) {
            return false;
        }

        if (level.getBlockState(floor)
                .isAir()) {
            return false;
        }

        if (!level.getFluidState(feet)
                .isEmpty()) {
            return false;
        }

        if (!level.getBlockState(feet)
                .getCollisionShape(
                        level,
                        feet
                )
                .isEmpty()) {
            return false;
        }

        if (!level.getBlockState(head)
                .getCollisionShape(
                        level,
                        head
                )
                .isEmpty()) {
            return false;
        }

        if (!level.getBlockState(upperHead)
                .getCollisionShape(
                        level,
                        upperHead
                )
                .isEmpty()) {
            return false;
        }

        AABB box =
                ally.getBoundingBox()
                        .move(
                                feet.getX() + 0.5D
                                        - ally.getX(),
                                feet.getY()
                                        - ally.getY(),
                                feet.getZ() + 0.5D
                                        - ally.getZ()
                        );

        return level.noCollision(
                ally,
                box
        );
    }
}