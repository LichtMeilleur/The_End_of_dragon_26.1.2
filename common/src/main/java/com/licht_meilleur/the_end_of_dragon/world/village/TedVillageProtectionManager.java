package com.licht_meilleur.the_end_of_dragon.world.village;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.entity.enderman
        .TedAllyEndermanEntity;
import com.licht_meilleur.the_end_of_dragon.entity.enderman.village
        .TedElderEndermanEntity;
import com.licht_meilleur.the_end_of_dragon.entity.enderman.village
        .TedTechEndermanEntity;
import com.licht_meilleur.the_end_of_dragon.world.dimension
        .TedDimensions;
import com.licht_meilleur.the_end_of_dragon.world.village.trust
        .TedVillageTrustConstants;
import com.licht_meilleur.the_end_of_dragon.world.village.trust
        .TedVillageTrustManager;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TedVillageProtectionManager {

    /*
     * 違反地点から監視役を検索する範囲。
     *
     * 現在の村全体を十分に含む大きさ。
     */
    private static final double GUARD_SEARCH_RADIUS =
            192.0D;

    /*
     * 爆発などで消失した保護ブロックを
     * 復元する処理間隔。
     */
    private static final long RESTORE_INTERVAL =
            20L;

    private TedVillageProtectionManager() {
    }

    /**
     * マーカーから実ブロックを配置した直後に呼ぶ。
     *
     * 現在その座標にあるブロックを、
     * 復元対象として保存する。
     */
    public static void registerProtectedBlock(
            ServerLevel level,
            BlockPos position
    ) {
        if (!isVillageLevel(level)
                || position == null) {
            return;
        }

        BlockState blockState =
                level.getBlockState(
                        position
                );

        registerProtectedBlock(
                level,
                position,
                blockState
        );
    }

    /**
     * 指定BlockStateを配置して保護対象に登録する。
     *
     * TedVillageStructurePlacerから直接呼べる。
     */
    public static void placeAndRegisterProtectedBlock(
            ServerLevel level,
            BlockPos position,
            BlockState blockState
    ) {
        if (!isVillageLevel(level)
                || position == null
                || blockState == null
                || blockState.isAir()) {
            return;
        }

        level.setBlock(
                position,
                blockState,
                3
        );

        registerProtectedBlock(
                level,
                position,
                blockState
        );
    }

    /**
     * 指定座標とBlockStateを保存する。
     */
    public static void registerProtectedBlock(
            ServerLevel level,
            BlockPos position,
            BlockState blockState
    ) {
        if (!isVillageLevel(level)
                || position == null
                || blockState == null
                || blockState.isAir()) {
            return;
        }

        Block block =
                blockState.getBlock();

        Identifier blockId =
                BuiltInRegistries.BLOCK
                        .getKey(
                                block
                        );

        if (blockId == null
                || block == Blocks.AIR) {
            return;
        }

        ProtectionSavedData data =
                ProtectionSavedData.get(
                        level
                );

        data.put(
                new ProtectedBlockData(
                        position.immutable(),
                        blockId.toString()
                )
        );
    }

    /**
     * 村を作り直す直前などに、
     * 古い保護座標をすべて消す。
     */
    public static void clearProtectedBlocks(
            ServerLevel level
    ) {
        if (!isVillageLevel(level)) {
            return;
        }

        ProtectionSavedData.get(level)
                .clear();
    }

    public static boolean isProtectedBlock(
            ServerLevel level,
            BlockPos position
    ) {
        if (!isVillageLevel(level)
                || position == null) {
            return false;
        }

        return ProtectionSavedData
                .get(level)
                .contains(position);
    }

    /**
     * 保護ブロックへの破壊操作。
     *
     * trueを返した場合、
     * ローダー側で破壊イベントをキャンセルする。
     */
    public static boolean handleBlockBreakAttempt(
            ServerLevel level,
            ServerPlayer player,
            BlockPos position
    ) {
        if (!isVillageLevel(level)
                || player == null
                || position == null
                || player.isSpectator()) {
            return false;
        }

        ProtectionSavedData protectionData =
                ProtectionSavedData.get(
                        level
                );

        if (!protectionData.contains(
                position
        )) {
            return false;
        }

        applyPenalty(
                level,
                player,
                TedVillageTrustConstants
                        .BLOCK_BREAK_PENALTY
        );

        alertVillageGuard(
                level,
                player,
                position.getCenter()
        );

        TheEndOfDragon.LOGGER.warn(
                "Player {} attempted to break protected village block at {}",
                player.getGameProfile().name(),
                position
        );

        return true;
    }

    /**
     * 村のエンダーマンが受けるダメージ。
     *
     * trueを返した場合、
     * ローダー側でダメージイベントをキャンセルする。
     */
    public static boolean handleEndermanDamage(
            ServerLevel level,
            LivingEntity victim,
            DamageSource damageSource
    ) {
        if (!isVillageLevel(level)
                || victim == null
                || damageSource == null) {
            return false;
        }

        if (!isProtectedVillageEnderman(
                victim
        )) {
            return false;
        }

        /*
         * DamageSource#getEntity()は、
         * 矢の場合も矢を撃ったプレイヤーを返す。
         *
         * TNTや環境ダメージなど、
         * プレイヤーが直接原因ではないものは
         * 違反として扱わない。
         */
        Entity attacker =
                damageSource.getEntity();

        if (!(attacker
                instanceof ServerPlayer player)) {
            return false;
        }

        if (player.isSpectator()) {
            return false;
        }

        applyPenalty(
                level,
                player,
                TedVillageTrustConstants
                        .ATTACK_PENALTY
        );

        alertVillageGuard(
                level,
                player,
                victim.position()
        );

        TheEndOfDragon.LOGGER.warn(
                "Player {} attempted to attack protected village Enderman {}",
                player.getGameProfile().name(),
                victim.getUUID()
        );

        return true;
    }

    /**
     * TNT、爆発、コマンドなどで消失した
     * 保護ブロックを復元する。
     *
     * 同じブロックが存在する場合は、
     * BlockStateを変更しない。
     *
     * そのため、かまどのLITや
     * コンポスターのLEVELなどは維持される。
     */
    public static void restoreMissingProtectedBlocks(
            ServerLevel level
    ) {
        if (!isVillageLevel(level)) {
            return;
        }

        if (level.getGameTime()
                % RESTORE_INTERVAL != 0L) {
            return;
        }

        ProtectionSavedData data =
                ProtectionSavedData.get(
                        level
                );

        for (ProtectedBlockData entry :
                data.getEntries()) {

            BlockPos position =
                    entry.position();

            /*
             * 未ロードチャンクを
             * 復元処理だけで読み込まない。
             */
            if (!level.hasChunkAt(
                    position
            )) {
                continue;
            }

            Block expectedBlock =
                    resolveBlock(
                            entry.blockId()
                    );

            if (expectedBlock == null
                    || expectedBlock == Blocks.AIR) {
                continue;
            }

            BlockState currentState =
                    level.getBlockState(
                            position
                    );

            /*
             * 同じ種類なら現在のBlockStateを維持。
             */
            if (currentState.is(
                    expectedBlock
            )) {
                continue;
            }

            level.setBlock(
                    position,
                    expectedBlock
                            .defaultBlockState(),
                    3
            );

            TheEndOfDragon.LOGGER.warn(
                    "Restored protected village block {} at {}",
                    entry.blockId(),
                    position
            );
        }
    }

    private static void applyPenalty(
            ServerLevel level,
            ServerPlayer player,
            int penalty
    ) {
        TedVillageWorldState villageState =
                TedVillageWorldState.get(
                        level
                );

        TedVillageTrustManager.applyPenalty(
                villageState,
                player.getUUID(),
                penalty
        );
    }

    /**
     * 報復するのはTedAllyEndermanEntityだけ。
     *
     * 長老、技術者、一般エンダーマンは
     * 警備行動へ参加しない。
     */
    private static void alertVillageGuard(
            ServerLevel level,
            ServerPlayer offender,
            Vec3 offensePosition
    ) {
        if (offender == null
                || !offender.isAlive()
                || offender.isRemoved()) {
            return;
        }

        AABB searchArea =
                new AABB(
                        offensePosition,
                        offensePosition
                ).inflate(
                        GUARD_SEARCH_RADIUS,
                        96.0D,
                        GUARD_SEARCH_RADIUS
                );

        TedAllyEndermanEntity guard =
                level.getEntitiesOfClass(
                                TedAllyEndermanEntity.class,
                                searchArea,
                                entity ->
                                        entity.isAlive()
                                                && !entity.isRemoved()
                        )
                        .stream()
                        .min(
                                java.util.Comparator
                                        .comparingDouble(
                                                entity ->
                                                        entity.distanceToSqr(
                                                                offensePosition
                                                        )
                                        )
                        )
                        .orElse(null);

        if (guard == null) {
            TheEndOfDragon.LOGGER.warn(
                    "No village guard found for offense by {} at {}",
                    offender.getGameProfile().name(),
                    offensePosition
            );

            return;
        }

        guard.startVillageRetaliation(
                offender
        );
    }

    private static boolean isProtectedVillageEnderman(
            Entity entity
    ) {
        return entity
                instanceof TedAllyEndermanEntity
                || entity
                instanceof TedElderEndermanEntity
                || entity
                instanceof TedTechEndermanEntity
                || entity
                instanceof EnderMan;
    }

    private static boolean isVillageLevel(
            ServerLevel level
    ) {
        return level != null
                && level.dimension()
                .equals(
                        TedDimensions
                                .ENDERMAN_VILLAGE
                );
    }

    private static Block resolveBlock(
            String blockId
    ) {
        if (blockId == null
                || blockId.isBlank()) {
            return null;
        }

        try {
            Identifier identifier =
                    Identifier.parse(
                            blockId
                    );

            return BuiltInRegistries.BLOCK
                    .getValue(
                            identifier
                    );

        } catch (IllegalArgumentException exception) {
            TheEndOfDragon.LOGGER.error(
                    "Invalid protected village block id: {}",
                    blockId
            );

            return null;
        }
    }

    /**
     * 保存する保護ブロック1個分。
     *
     * BlockState全体ではなくBlock IDだけ保存する。
     * 通常の状態変化を復元処理で潰さないため。
     */
    private record ProtectedBlockData(
            BlockPos position,
            String blockId
    ) {
        private static final Codec<
                ProtectedBlockData> CODEC =
                RecordCodecBuilder.create(
                        instance ->
                                instance.group(
                                        BlockPos.CODEC
                                                .fieldOf(
                                                        "position"
                                                )
                                                .forGetter(
                                                        ProtectedBlockData
                                                                ::position
                                                ),

                                        Codec.STRING
                                                .fieldOf(
                                                        "block"
                                                )
                                                .forGetter(
                                                        ProtectedBlockData
                                                                ::blockId
                                                )
                                ).apply(
                                        instance,
                                        ProtectedBlockData::new
                                )
                );
    }

    /**
     * 保護ブロック専用SavedData。
     */
    private static final class ProtectionSavedData
            extends SavedData {

        private static final Codec<
                ProtectionSavedData> CODEC =
                RecordCodecBuilder.create(
                        instance ->
                                instance.group(
                                        ProtectedBlockData
                                                .CODEC
                                                .listOf()
                                                .optionalFieldOf(
                                                        "protected_blocks",
                                                        List.of()
                                                )
                                                .forGetter(
                                                        ProtectionSavedData
                                                                ::createSaveList
                                                )
                                ).apply(
                                        instance,
                                        ProtectionSavedData::new
                                )
                );

        private static final SavedDataType<
                ProtectionSavedData> TYPE =
                new SavedDataType<>(
                        Identifier.fromNamespaceAndPath(
                                TheEndOfDragon.MOD_ID,
                                "ted_village_protected_blocks"
                        ),
                        ProtectionSavedData::new,
                        CODEC,
                        null
                );

        private final Map<Long, ProtectedBlockData>
                entries =
                new HashMap<>();

        private ProtectionSavedData() {
        }

        private ProtectionSavedData(
                List<ProtectedBlockData> loadedEntries
        ) {
            if (loadedEntries == null) {
                return;
            }

            for (ProtectedBlockData entry :
                    loadedEntries) {

                if (entry == null
                        || entry.position() == null
                        || entry.blockId() == null
                        || entry.blockId().isBlank()) {
                    continue;
                }

                entries.put(
                        entry.position().asLong(),
                        new ProtectedBlockData(
                                entry.position().immutable(),
                                entry.blockId()
                        )
                );
            }
        }

        private static ProtectionSavedData get(
                ServerLevel level
        ) {
            return level.getDataStorage()
                    .computeIfAbsent(
                            TYPE
                    );
        }

        private boolean contains(
                BlockPos position
        ) {
            return position != null
                    && entries.containsKey(
                    position.asLong()
            );
        }

        private void put(
                ProtectedBlockData entry
        ) {
            if (entry == null
                    || entry.position() == null
                    || entry.blockId() == null
                    || entry.blockId().isBlank()) {
                return;
            }

            ProtectedBlockData safeEntry =
                    new ProtectedBlockData(
                            entry.position().immutable(),
                            entry.blockId()
                    );

            ProtectedBlockData previous =
                    entries.put(
                            safeEntry.position().asLong(),
                            safeEntry
                    );

            if (!safeEntry.equals(
                    previous
            )) {
                setDirty();
            }
        }

        private void clear() {
            if (entries.isEmpty()) {
                return;
            }

            entries.clear();
            setDirty();
        }

        private List<ProtectedBlockData>
        getEntries() {
            return List.copyOf(
                    entries.values()
            );
        }

        private List<ProtectedBlockData>
        createSaveList() {
            return new ArrayList<>(
                    entries.values()
            );
        }
    }
}