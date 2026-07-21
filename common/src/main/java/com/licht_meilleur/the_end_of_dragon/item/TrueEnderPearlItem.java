package com.licht_meilleur.the_end_of_dragon.item;

import com.licht_meilleur.the_end_of_dragon.entity.enderman.TedAllyEndermanEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Set;
import java.util.function.Consumer;

public final class TrueEnderPearlItem
        extends Item {

    /*
     * クロスヘア判定そのものの最大距離。
     */
    private static final double MAX_RAY_DISTANCE =
            64.0D;

    /*
     * これより遠い場所を指している場合は、
     * 目的地まで直接飛ばず短距離ワープにする。
     */
    private static final double DIRECT_TELEPORT_DISTANCE =
            24.0D;

    /*
     * 空、または遠方を向いている場合の
     * 短距離ワープ距離。
     */
    private static final double AIR_BLINK_DISTANCE =
            8.0D;

    /*
     * 壁際などで安全な立ち位置を探す範囲。
     */
    private static final int SAFE_SEARCH_RADIUS =
            3;

    private static final int SAFE_SEARCH_VERTICAL =
            4;

    /*
     * 10tick = 0.5秒。
     */
    private static final int COOLDOWN_TICKS =
            10;

    public TrueEnderPearlItem(
            Properties properties
    ) {
        super(properties);
    }

    @Override
    public InteractionResult use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        ItemStack stack =
                player.getItemInHand(
                        hand
                );

        int pearlLevel =
                TrueEnderPearlLevel.get(
                        stack
                );

        /*
         * ランクIではメインハンド専用。
         */
        if (hand == InteractionHand.OFF_HAND
                && !TrueEnderPearlLevel
                .canUseOffhand(stack)) {

            return InteractionResult.FAIL;
        }



        /*
         * 実際の転送はサーバー側のみ。
         */
        if (!(level instanceof ServerLevel serverLevel)
                || !(player instanceof ServerPlayer serverPlayer)) {

            return InteractionResult.SUCCESS;
        }

        if (serverPlayer.getCooldowns()
                .isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }

        java.util.List<LivingEntity>
                companions =
                java.util.List.of();

        if (pearlLevel >= 3
                && serverPlayer.isShiftKeyDown()) {

            AABB carryArea =
                    serverPlayer
                            .getBoundingBox()
                            .inflate(
                                    1.5D
                            );

            companions =
                    serverLevel.getEntitiesOfClass(
                            LivingEntity.class,
                            carryArea,
                            entity ->
                                    canTeleportWithPlayer(
                                            serverPlayer,
                                            entity
                                    )
                    );
        }


        Vec3 eyePosition =
                serverPlayer.getEyePosition();

        Vec3 lookDirection =
                serverPlayer.getLookAngle()
                        .normalize();

        Vec3 rayEnd =
                eyePosition.add(
                        lookDirection.scale(
                                MAX_RAY_DISTANCE
                        )
                );

        BlockHitResult hitResult =
                serverLevel.clip(
                        new ClipContext(
                                eyePosition,
                                rayEnd,
                                ClipContext.Block.COLLIDER,
                                ClipContext.Fluid.NONE,
                                serverPlayer
                        )
                );

        Vec3 destination;

        /*
         * 空を向いている場合は、
         * 視線方向へ短距離ワープ。
         */
        if (hitResult.getType()
                == HitResult.Type.MISS) {

            destination =
                    findAirBlinkDestination(
                            serverLevel,
                            serverPlayer,
                            lookDirection
                    );

        } else {
            double hitDistance =
                    eyePosition.distanceTo(
                            hitResult.getLocation()
                    );

            /*
             * 遠すぎる場所を指している場合も、
             * 一気に目的地へ飛ばず短距離ワープ。
             */
            if (hitDistance
                    > DIRECT_TELEPORT_DISTANCE) {

                destination =
                        findAirBlinkDestination(
                                serverLevel,
                                serverPlayer,
                                lookDirection
                        );

            } else {
                destination =
                        findSurfaceTeleportDestination(
                                serverLevel,
                                serverPlayer,
                                hitResult
                        );

                /*
                 * 指した場所の周囲に安全な足場がなければ、
                 * 短距離空中ワープへ切り替える。
                 */
                if (destination == null) {
                    destination =
                            findAirBlinkDestination(
                                    serverLevel,
                                    serverPlayer,
                                    lookDirection
                            );
                }


            }


        }

        if (destination == null) {
            return InteractionResult.FAIL;
        }

        Vec3 oldPosition =
                serverPlayer.position();

        boolean teleported =
                serverPlayer.teleportTo(
                        serverLevel,
                        destination.x,
                        destination.y,
                        destination.z,
                        Set.of(),
                        serverPlayer.getYRot(),
                        serverPlayer.getXRot(),
                        true
                );

        if (!teleported) {
            return InteractionResult.FAIL;
        }

        if (teleported
                && pearlLevel >= 3
                && serverPlayer.isShiftKeyDown()) {

            teleportNearbyCompanions(
                    serverLevel,
                    serverPlayer,
                    companions,
                    oldPosition,
                    destination
            );
        }

        /*
         * ワープ直後に以前の落下距離で
         * ダメージを受けないようにする。
         *
         * 空中へワープした後の新しい落下については、
         * その後通常どおり計算される。
         */
        serverPlayer.fallDistance =
                0.0F;

        serverPlayer.setDeltaMovement(
                Vec3.ZERO
        );

        if (!TrueEnderPearlLevel
                .hasNoCooldown(stack)) {

            serverPlayer.getCooldowns()
                    .addCooldown(
                            stack,
                            COOLDOWN_TICKS
                    );
        }

        spawnTeleportEffects(
                serverLevel,
                oldPosition,
                destination
        );

        return InteractionResult.SUCCESS;
    }

    private static boolean canTeleportWithPlayer(
            ServerPlayer owner,
            LivingEntity entity
    ) {
        if (entity == owner
                || !entity.isAlive()
                || entity.isRemoved()) {
            return false;
        }

        /*
         * 観戦中のプレイヤーは除外。
         *
         * 現在の仕様を維持し、
         * 周囲の通常プレイヤーは一緒に転送可能。
         */
        if (entity instanceof ServerPlayer player) {
            return !player.isSpectator();
        }

        /*
         * TED戦で共闘した味方エンダーマン。
         */
        if (entity instanceof TedAllyEndermanEntity) {
            return true;
        }

        /*
         * 使用者自身が飼い主のテイムMob。
         *
         * オオカミ、ネコ、オウムなど。
         */
        if (entity instanceof TamableAnimal tamable) {
            return tamable.isTame()
                    && tamable.isOwnedBy(
                    owner
            );
        }

        /*
         * スコアボードチームなどで
         * 使用者と味方判定になっているEntity。
         *
         * 一部のMODコンパニオンもこれで拾える。
         */
        return owner.isAlliedTo(entity)
                || entity.isAlliedTo(owner);
    }

    private static void teleportNearbyCompanions(
            ServerLevel level,
            ServerPlayer owner,
            java.util.List<LivingEntity> entities,
            Vec3 oldOwnerPosition,
            Vec3 newOwnerPosition
    ) {
        for (LivingEntity entity : entities) {

            /*
             * 検出時点から離れたEntityは転送しない。
             */
            Vec3 relativeOffset =
                    entity.position()
                            .subtract(
                                    oldOwnerPosition
                            );

            if (relativeOffset.lengthSqr()
                    > 1.75D * 1.75D) {
                continue;
            }

            /*
             * 使用者から見た相対位置を維持する。
             */
            Vec3 desiredPosition =
                    newOwnerPosition.add(
                            relativeOffset
                    );

            Vec3 safePosition =
                    findNearbyCompanionPosition(
                            level,
                            entity,
                            desiredPosition,
                            newOwnerPosition
                    );

            if (safePosition == null) {
                continue;
            }

            boolean moved =
                    entity.teleportTo(
                            level,
                            safePosition.x,
                            safePosition.y,
                            safePosition.z,
                            Set.of(),
                            entity.getYRot(),
                            entity.getXRot(),
                            true
                    );

            if (!moved) {
                continue;
            }

            entity.setDeltaMovement(
                    Vec3.ZERO
            );

            entity.fallDistance =
                    0.0F;

            /*
             * 同伴Entityの到着地点にも
             * 転送パーティクルを出す。
             */
            level.sendParticles(
                    ParticleTypes.PORTAL,
                    safePosition.x,
                    safePosition.y
                            + entity.getBbHeight()
                            * 0.5D,
                    safePosition.z,
                    20,
                    0.35D,
                    Math.max(
                            0.35D,
                            entity.getBbHeight()
                                    * 0.35D
                    ),
                    0.35D,
                    0.1D
            );
        }
    }

    private static Vec3 findNearbyCompanionPosition(
            ServerLevel level,
            LivingEntity entity,
            Vec3 desiredPosition,
            Vec3 ownerPosition
    ) {
        Vec3[] offsets = {
                new Vec3(0.0D, 0.0D, 0.0D),
                new Vec3(1.0D, 0.0D, 0.0D),
                new Vec3(-1.0D, 0.0D, 0.0D),
                new Vec3(0.0D, 0.0D, 1.0D),
                new Vec3(0.0D, 0.0D, -1.0D),
                new Vec3(1.0D, 0.0D, 1.0D),
                new Vec3(-1.0D, 0.0D, 1.0D),
                new Vec3(1.0D, 0.0D, -1.0D),
                new Vec3(-1.0D, 0.0D, -1.0D)
        };

        for (Vec3 offset : offsets) {
            Vec3 candidate =
                    desiredPosition.add(
                            offset
                    );

            /*
             * 使用者から大きく離れすぎない範囲。
             */
            if (candidate.distanceToSqr(
                    ownerPosition
            ) > 3.0D * 3.0D) {
                continue;
            }

            if (isSafeEntityPosition(
                    level,
                    entity,
                    candidate
            )) {
                return candidate;
            }
        }

        return null;
    }

    private static boolean isSafeEntityPosition(
            ServerLevel level,
            LivingEntity entity,
            Vec3 candidate
    ) {
        BlockPos feet =
                BlockPos.containing(
                        candidate
                );

        if (candidate.y
                <= level.getMinY() + 1.0D) {
            return false;
        }

        if (candidate.y
                + entity.getBbHeight()
                >= level.getMaxY()) {
            return false;
        }

        if (!level.getWorldBorder()
                .isWithinBounds(
                        feet
                )) {
            return false;
        }

        /*
         * 水・溶岩の中へ直接入れない。
         */
        if (!level.getFluidState(feet)
                .isEmpty()) {
            return false;
        }

        BlockPos upper =
                BlockPos.containing(
                        candidate.x,
                        candidate.y
                                + entity.getBbHeight()
                                - 0.01D,
                        candidate.z
                );

        if (!level.getFluidState(upper)
                .isEmpty()) {
            return false;
        }

        Vec3 movement =
                candidate.subtract(
                        entity.position()
                );

        AABB destinationBox =
                entity.getBoundingBox()
                        .move(
                                movement
                        );

        return level.noCollision(
                entity,
                destinationBox
        );
    }

    /**
     * 空または遠方を向いた際の短距離ワープ先を探す。
     *
     * 足場は不要だが、壁の中へは入れない。
     * 8ブロック先が塞がっていれば、
     * 少しずつ距離を短くして安全地点を探す。
     */
    private static Vec3 findAirBlinkDestination(
            ServerLevel level,
            ServerPlayer player,
            Vec3 lookDirection
    ) {
        Vec3 start =
                player.position();

        /*
         * 8ブロック先から0.5ブロックずつ戻しながら、
         * 最も遠い安全地点を探す。
         */
        for (double distance =
             AIR_BLINK_DISTANCE;
             distance >= 1.0D;
             distance -= 0.5D) {

            Vec3 candidate =
                    start.add(
                            lookDirection.scale(
                                    distance
                            )
                    );

            if (isSafeAirPosition(
                    level,
                    player,
                    candidate
            )) {
                return candidate;
            }
        }

        return null;
    }

    /**
     * クロスヘアが指したブロック付近から、
     * 足場のある安全な地点を探す。
     */
    private static Vec3 findSurfaceTeleportDestination(
            ServerLevel level,
            ServerPlayer player,
            BlockHitResult hitResult
    ) {
        Direction hitFace =
                hitResult.getDirection();

        Vec3 faceDirection =
                new Vec3(
                        hitFace.getStepX(),
                        hitFace.getStepY(),
                        hitFace.getStepZ()
                );

        /*
         * ブロック表面から外側へ少し出す。
         *
         * ヒット地点そのものへ飛ばすと、
         * プレイヤーが壁へ埋まる可能性がある。
         */
        Vec3 outsideSurface =
                hitResult.getLocation()
                        .add(
                                faceDirection.scale(
                                        0.75D
                                )
                        );

        BlockPos center =
                BlockPos.containing(
                        outsideSurface
                );

        /*
         * 直接位置を最初に調べる。
         */
        Vec3 direct =
                findSafeSurfaceAtColumn(
                        level,
                        player,
                        center
                );

        if (direct != null) {
            return direct;
        }

        /*
         * 周辺を広げながら探す。
         */
        for (int radius = 1;
             radius <= SAFE_SEARCH_RADIUS;
             radius++) {

            for (int offsetX = -radius;
                 offsetX <= radius;
                 offsetX++) {

                for (int offsetZ = -radius;
                     offsetZ <= radius;
                     offsetZ++) {

                    /*
                     * 同じ範囲を何度も調べないよう、
                     * 現在半径の外周だけを見る。
                     */
                    if (Math.abs(offsetX) != radius
                            && Math.abs(offsetZ) != radius) {
                        continue;
                    }

                    BlockPos column =
                            center.offset(
                                    offsetX,
                                    0,
                                    offsetZ
                            );

                    Vec3 safe =
                            findSafeSurfaceAtColumn(
                                    level,
                                    player,
                                    column
                            );

                    if (safe != null) {
                        return safe;
                    }
                }
            }
        }

        return null;
    }

    /**
     * 指定されたXZ列について、
     * 基準Yに近い場所から安全な足場を探す。
     */
    private static Vec3 findSafeSurfaceAtColumn(
            ServerLevel level,
            ServerPlayer player,
            BlockPos center
    ) {
        for (int distance = 0;
             distance <= SAFE_SEARCH_VERTICAL;
             distance++) {

            BlockPos upward =
                    center.above(
                            distance
                    );

            Vec3 upwardResult =
                    createSurfaceDestination(
                            level,
                            player,
                            upward
                    );

            if (upwardResult != null) {
                return upwardResult;
            }

            if (distance == 0) {
                continue;
            }

            BlockPos downward =
                    center.below(
                            distance
                    );

            Vec3 downwardResult =
                    createSurfaceDestination(
                            level,
                            player,
                            downward
                    );

            if (downwardResult != null) {
                return downwardResult;
            }
        }

        return null;
    }

    /**
     * 足元・頭上・床を確認し、
     * 安全なら実際の転送座標を返す。
     */
    private static Vec3 createSurfaceDestination(
            ServerLevel level,
            ServerPlayer player,
            BlockPos feet
    ) {
        if (feet.getY()
                <= level.getMinY()) {
            return null;
        }

        if (feet.getY() + 2
                >= level.getMaxY()) {
            return null;
        }

        BlockPos head =
                feet.above();

        BlockPos floor =
                feet.below();

        /*
         * 足と頭のブロックが空いていること。
         */
        if (!level.getBlockState(feet)
                .getCollisionShape(
                        level,
                        feet
                )
                .isEmpty()) {
            return null;
        }

        if (!level.getBlockState(head)
                .getCollisionShape(
                        level,
                        head
                )
                .isEmpty()) {
            return null;
        }

        /*
         * 足場が存在すること。
         */
        if (level.getBlockState(floor)
                .getCollisionShape(
                        level,
                        floor
                )
                .isEmpty()) {
            return null;
        }

        /*
         * 水や溶岩の中へ直接入れない。
         */
        if (!level.getFluidState(feet)
                .isEmpty()) {
            return null;
        }

        if (!level.getFluidState(head)
                .isEmpty()) {
            return null;
        }

        Vec3 candidate =
                new Vec3(
                        feet.getX() + 0.5D,
                        feet.getY(),
                        feet.getZ() + 0.5D
                );

        if (!isPlayerCollisionFree(
                level,
                player,
                candidate
        )) {
            return null;
        }

        return candidate;
    }

    /**
     * 空中短距離ワープ用。
     *
     * 足場は要求しない。
     */
    private static boolean isSafeAirPosition(
            ServerLevel level,
            ServerPlayer player,
            Vec3 candidate
    ) {
        BlockPos feet =
                BlockPos.containing(
                        candidate
                );

        if (candidate.y
                <= level.getMinY() + 1.0D) {
            return false;
        }

        if (candidate.y
                + player.getBbHeight()
                >= level.getMaxY()) {
            return false;
        }

        if (!level.getWorldBorder()
                .isWithinBounds(
                        feet
                )) {
            return false;
        }

        /*
         * 溶岩などの液体内部へ直接入れない。
         */
        if (!level.getFluidState(feet)
                .isEmpty()) {
            return false;
        }

        BlockPos head =
                feet.above();

        if (!level.getFluidState(head)
                .isEmpty()) {
            return false;
        }

        return isPlayerCollisionFree(
                level,
                player,
                candidate
        );
    }

    /**
     * プレイヤー全身の当たり判定が、
     * ブロックへ埋まらないか確認する。
     */
    private static boolean isPlayerCollisionFree(
            ServerLevel level,
            ServerPlayer player,
            Vec3 candidate
    ) {
        Vec3 movement =
                candidate.subtract(
                        player.position()
                );

        AABB destinationBox =
                player.getBoundingBox()
                        .move(
                                movement
                        );

        return level.noCollision(
                player,
                destinationBox
        );
    }

    private static void spawnTeleportEffects(
            ServerLevel level,
            Vec3 oldPosition,
            Vec3 newPosition
    ) {
        level.sendParticles(
                ParticleTypes.PORTAL,
                oldPosition.x,
                oldPosition.y + 1.0D,
                oldPosition.z,
                40,
                0.45D,
                0.9D,
                0.45D,
                0.15D
        );

        level.sendParticles(
                ParticleTypes.PORTAL,
                newPosition.x,
                newPosition.y + 1.0D,
                newPosition.z,
                40,
                0.45D,
                0.9D,
                0.45D,
                0.15D
        );

        level.playSound(
                null,
                oldPosition.x,
                oldPosition.y,
                oldPosition.z,
                SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS,
                1.0F,
                1.0F
        );

        level.playSound(
                null,
                newPosition.x,
                newPosition.y,
                newPosition.z,
                SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS,
                1.0F,
                1.0F
        );
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            Item.TooltipContext context,
            TooltipDisplay tooltipDisplay,
            Consumer<Component> tooltipAdder,
            TooltipFlag flag
    ) {
        super.appendHoverText(
                stack,
                context,
                tooltipDisplay,
                tooltipAdder,
                flag
        );

        int pearlLevel =
                TrueEnderPearlLevel.get(
                        stack
                );

        tooltipAdder.accept(
                Component.translatable(
                        "tooltip.the_end_of_dragon.true_ender_pearl.level",
                        toRomanNumeral(
                                pearlLevel
                        )
                ).withStyle(
                        ChatFormatting.LIGHT_PURPLE
                )
        );

        tooltipAdder.accept(
                Component.empty()
        );

        tooltipAdder.accept(
                Component.translatable(
                        "tooltip.the_end_of_dragon.true_ender_pearl.abilities"
                ).withStyle(
                        ChatFormatting.GOLD
                )
        );

        addAbilityTooltip(
                tooltipAdder,
                "teleport",
                true
        );

        addAbilityTooltip(
                tooltipAdder,
                "offhand",
                pearlLevel >= 2
        );

        addAbilityTooltip(
                tooltipAdder,
                "carry_players",
                pearlLevel >= 3
        );

        addAbilityTooltip(
                tooltipAdder,
                "no_fall_damage",
                pearlLevel >= 4
        );

        addAbilityTooltip(
                tooltipAdder,
                "no_cooldown",
                pearlLevel >= 5
        );
    }

    private static void addAbilityTooltip(
            Consumer<Component> tooltipAdder,
            String abilityName,
            boolean unlocked
    ) {
        Component abilityText =
                Component.translatable(
                        "tooltip.the_end_of_dragon.true_ender_pearl.ability."
                                + abilityName
                ).withStyle(
                        unlocked
                                ? ChatFormatting.GREEN
                                : ChatFormatting.DARK_GRAY
                );

        Component line =
                Component.literal(
                        unlocked
                                ? "✔ "
                                : "✖ "
                ).withStyle(
                        unlocked
                                ? ChatFormatting.GREEN
                                : ChatFormatting.DARK_GRAY
                ).append(
                        abilityText
                );

        tooltipAdder.accept(
                line
        );
    }

    private static String toRomanNumeral(
            int level
    ) {
        return switch (level) {
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> "I";
        };
    }
}