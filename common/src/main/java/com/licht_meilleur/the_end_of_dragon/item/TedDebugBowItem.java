package com.licht_meilleur.the_end_of_dragon.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class TedDebugBowItem extends Item {
    public TedDebugBowItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = eye.add(look.scale(512.0D));

        EnderDragon best = null;
        double bestDistance = Double.MAX_VALUE;

        AABB searchBox = player.getBoundingBox().inflate(512.0D);

        for (EnderDragon dragon : serverLevel.getEntitiesOfClass(EnderDragon.class, searchBox)) {
            Vec3 dragonPos = dragon.position().add(0.0D, dragon.getBbHeight() * 0.5D, 0.0D);

            double distanceToRay = distancePointToLine(dragonPos, eye, end);
            double distanceToPlayer = dragon.distanceToSqr(player);

            if (distanceToRay < 16.0D && distanceToPlayer < bestDistance) {
                best = dragon;
                bestDistance = distanceToPlayer;
            }
        }

        if (best != null) {
            best.hurtServer(serverLevel, serverLevel.damageSources().playerAttack(player), 1000000.0F);
        }

        return InteractionResult.SUCCESS;
    }

    private static double distancePointToLine(Vec3 point, Vec3 lineStart, Vec3 lineEnd) {
        Vec3 line = lineEnd.subtract(lineStart);
        Vec3 pointToStart = point.subtract(lineStart);

        double lineLengthSqr = line.lengthSqr();
        if (lineLengthSqr <= 0.0001D) {
            return point.distanceTo(lineStart);
        }

        double t = pointToStart.dot(line) / lineLengthSqr;
        t = Math.max(0.0D, Math.min(1.0D, t));

        Vec3 projection = lineStart.add(line.scale(t));
        return point.distanceTo(projection);
    }
}