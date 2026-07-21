package com.licht_meilleur.the_end_of_dragon.world.block.entity;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.registry.ModBlockEntities;
import com.licht_meilleur.the_end_of_dragon.registry.ModBlocks;
import com.licht_meilleur.the_end_of_dragon.world.TedBattleWorldState;
import com.licht_meilleur.the_end_of_dragon.world.dimension.TedDimensions;
import com.licht_meilleur.the_end_of_dragon.world.village
        .TedVillageStructurePlacer;
import com.licht_meilleur.the_end_of_dragon.world.village
        .TedVillageWorldState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class EndermanVillageGatewayBlockEntity
        extends BlockEntity {

    /*
     * 60tick = 3秒。
     */
    private static final int TELEPORT_WAIT_TICKS =
            60;

    /*
     * 転送失敗時、毎tickエラーを出さないため、
     * 1秒分だけ進行を戻す。
     */
    private static final int RETRY_BACKOFF_TICKS =
            20;

    /*
     * プレイヤーごとのゲート滞在時間。
     *
     * ゲートから降りると削除されるため、
     * 保存する必要はない。
     */
    private final Map<UUID, Integer>
            standingTicks =
            new HashMap<>();

    public EndermanVillageGatewayBlockEntity(
            BlockPos pos,
            BlockState state
    ) {
        super(
                ModBlockEntities
                        .ENDERMAN_VILLAGE_GATEWAY,
                pos,
                state
        );
    }

    public static void serverTick(
            Level level,
            BlockPos pos,
            BlockState state,
            EndermanVillageGatewayBlockEntity gateway
    ) {
        if (!(level
                instanceof ServerLevel serverLevel)) {
            return;
        }

        /*
         * 高さ0.5ブロックのゲート上を検出する。
         */
        AABB detectionArea =
                new AABB(
                        pos.getX(),
                        pos.getY() + 0.45D,
                        pos.getZ(),
                        pos.getX() + 1.0D,
                        pos.getY() + 2.5D,
                        pos.getZ() + 1.0D
                );

        var players =
                serverLevel.getEntitiesOfClass(
                        ServerPlayer.class,
                        detectionArea,
                        player ->
                                player.isAlive()
                                        && !player.isSpectator()
                );

        Set<UUID> currentlyStanding =
                new HashSet<>();

        for (ServerPlayer player : players) {
            UUID uuid =
                    player.getUUID();

            currentlyStanding.add(
                    uuid
            );

            int ticks =
                    gateway.standingTicks
                            .getOrDefault(
                                    uuid,
                                    0
                            )
                            + 1;

            gateway.standingTicks.put(
                    uuid,
                    ticks
            );

            if (ticks
                    < TELEPORT_WAIT_TICKS) {
                continue;
            }

            boolean teleported;

            if (state.is(
                    ModBlocks.ENDERMAN_VILLAGE_RETURN_GATEWAY
            )) {
                teleported =
                        gateway.teleportToRegisteredGateway(
                                serverLevel,
                                player
                        );
            } else {
                teleported =
                        gateway.teleportToVillage(
                                serverLevel,
                                player
                        );
            }

            if (teleported) {
                gateway.standingTicks.remove(
                        uuid
                );

                continue;
            }

            /*
             * ディメンション取得や村生成に失敗した場合、
             * 約1秒後に再試行する。
             */
            gateway.standingTicks.put(
                    uuid,
                    Math.max(
                            0,
                            TELEPORT_WAIT_TICKS
                                    - RETRY_BACKOFF_TICKS
                    )
            );
        }

        /*
         * ゲートから降りたプレイヤーの進行を消す。
         */
        gateway.standingTicks
                .keySet()
                .removeIf(
                        uuid ->
                                !currentlyStanding
                                        .contains(uuid)
                );
    }

    private boolean teleportToVillage(
            ServerLevel sourceLevel,
            ServerPlayer player
    ) {
        ServerLevel destinationLevel =
                sourceLevel.getServer()
                        .getLevel(
                                TedDimensions
                                        .ENDERMAN_VILLAGE
                        );

        if (destinationLevel == null) {
            TheEndOfDragon.LOGGER.error(
                    "Enderman village dimension is not loaded: {}",
                    TedDimensions.ENDERMAN_VILLAGE
                            .identifier()
            );

            return false;
        }

        TedVillageWorldState villageState =
                TedVillageWorldState.get(
                        destinationLevel
                );

        BlockPos destinationPos;

        /*
         * 初回だけ村NBTを生成する。
         */
        if (!villageState
                .isVillageGenerated()) {

            destinationPos =
                    TedVillageStructurePlacer
                            .generateVillage(
                                    destinationLevel
                            );

            if (destinationPos == null) {
                TheEndOfDragon.LOGGER.error(
                        "Failed to generate Enderman village"
                );

                return false;
            }

            villageState.completeGeneration(
                    destinationPos
            );
        } else {
            destinationPos =
                    villageState
                            .getArrivalPosition();
        }

        destinationLevel.getChunk(
                destinationPos
        );

        boolean teleported =
                player.teleportTo(
                        destinationLevel,
                        destinationPos.getX()
                                + 0.5D,
                        destinationPos.getY(),
                        destinationPos.getZ()
                                + 0.5D,
                        Set.of(),
                        player.getYRot(),
                        player.getXRot(),
                        true
                );

        if (!teleported) {
            TheEndOfDragon.LOGGER.error(
                    "Failed to teleport player {} to Enderman village",
                    player.getGameProfile()
                            .name()
            );

            return false;
        }



        /*
         * 初回到着状態を保存する。
         *
         * 将来ここで導入クエストやメッセージを開始できる。
         */
        villageState.completeFirstArrival();

        this.standingTicks.remove(
                player.getUUID()
        );

        TheEndOfDragon.LOGGER.info(
                "Teleported player {} to Enderman village at {}",
                player.getGameProfile()
                        .name(),
                destinationPos
        );

        return true;
    }

    private boolean teleportToRegisteredGateway(
            ServerLevel villageLevel,
            ServerPlayer player
    ) {
        ServerLevel endLevel =
                villageLevel.getServer()
                        .getLevel(
                                Level.END
                        );

        if (endLevel == null) {
            TheEndOfDragon.LOGGER.error(
                    "Could not access End dimension for gateway return data"
            );

            return false;
        }

        TedBattleWorldState worldState =
                TedBattleWorldState.get(
                        endLevel
                );

        if (!worldState
                .hasRegisteredReturnGateway()) {

            TheEndOfDragon.LOGGER.warn(
                    "Return gateway B used, but gateway A is not registered"
            );

            return false;
        }

        Identifier dimensionId;

        try {
            dimensionId =
                    Identifier.parse(
                            worldState
                                    .getReturnGatewayDimensionId()
                    );
        } catch (
                IllegalArgumentException exception
        ) {
            TheEndOfDragon.LOGGER.error(
                    "Invalid registered gateway dimension: {}",
                    worldState
                            .getReturnGatewayDimensionId()
            );

            return false;
        }

        ResourceKey<Level> destinationKey =
                ResourceKey.create(
                        Registries.DIMENSION,
                        dimensionId
                );

        ServerLevel destinationLevel =
                villageLevel.getServer()
                        .getLevel(
                                destinationKey
                        );

        if (destinationLevel == null) {
            TheEndOfDragon.LOGGER.error(
                    "Registered gateway dimension is unavailable: {}",
                    dimensionId
            );

            return false;
        }

        BlockPos gatewayPos =
                worldState
                        .getReturnGatewayPosition();

        /*
         * 門Aが本当にその場所に存在するか確認。
         */
        destinationLevel.getChunk(
                gatewayPos
        );

        if (!destinationLevel
                .getBlockState(
                        gatewayPos
                )
                .is(
                        ModBlocks
                                .ENDERMAN_VILLAGE_GATEWAY
                )) {

            TheEndOfDragon.LOGGER.warn(
                    "Registered gateway A no longer exists at {} in {}",
                    gatewayPos,
                    dimensionId
            );

            worldState.clearReturnGateway();

            return false;
        }

        /*
         * 門Aの真上へ戻す。
         *
         * 門は高さ0.5ブロックなので、
         * Y+1で安全に立たせる。
         */
        BlockPos destinationPos =
                gatewayPos.above();

        boolean teleported =
                player.teleportTo(
                        destinationLevel,
                        destinationPos.getX()
                                + 0.5D,
                        destinationPos.getY(),
                        destinationPos.getZ()
                                + 0.5D,
                        Set.<Relative>of(),
                        player.getYRot(),
                        player.getXRot(),
                        true
                );

        if (!teleported) {
            TheEndOfDragon.LOGGER.error(
                    "Failed to return player {} to gateway A",
                    player.getGameProfile()
                            .name()
            );

            return false;
        }

        this.standingTicks.remove(
                player.getUUID()
        );

        TheEndOfDragon.LOGGER.info(
                "Returned player {} to gateway A at {} in {}",
                player.getGameProfile()
                        .name(),
                gatewayPos,
                dimensionId
        );

        return true;
    }
}