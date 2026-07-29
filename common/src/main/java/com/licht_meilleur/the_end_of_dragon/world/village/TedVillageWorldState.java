package com.licht_meilleur.the_end_of_dragon.world.village;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.world.village.trust.TedVillageTrustConstants;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class TedVillageWorldState
        extends SavedData {

    /*
     * SavedDataの現在バージョン。
     *
     * 保存項目の意味や構造を変更したら増やす。
     */
    private static final int CURRENT_DATA_VERSION =
            6;

    private int dataVersion;
    /*
     * 村中央に固定される帰還門Bの座標。
     */
    private BlockPos returnGatewayPosition =
            BlockPos.ZERO;
    /*
     * 村のNBT一式を生成済みか。
     */
    private boolean villageGenerated;

    /*
     * ゲートから来たプレイヤーの到着地点。
     */
    private BlockPos arrivalPosition;

    /*
     * 少なくとも一度プレイヤーが村へ到着したか。
     *
     * 将来の導入会話・実績・演出に使用できる。
     */
    private boolean firstArrivalCompleted;

    /*
     * 村全体のメインクエスト段階。
     *
     * 0 = 未開始
     * 1以降は将来追加。
     */
    private int villageQuestStage;

    /*
     * プレイヤーごとの現在信頼度。
     *
     * キーはプレイヤーUUIDの文字列表現。
     */
    private final Map<String, Integer>
            playerTrustPoints =
            new HashMap<>();


    private BlockPos elderSpawnPosition =
            BlockPos.ZERO;

    private BlockPos technicianSpawnPosition =
            BlockPos.ZERO;

    private BlockPos allyHomePosition =
            BlockPos.ZERO;

    /*
     * リコーラス果汁水施設の管理基準位置。
     */
    private BlockPos rechorusFacilityAnchorPosition =
            BlockPos.ZERO;

    /*
     * 水転送装置Bの指定設置位置。
     */
    private BlockPos waterTransferMachineBSlotPosition =
            BlockPos.ZERO;

    /*
     * リコーラスプラントコアの指定設置位置。
     *
     * rechorus_tree.nbt内の
     * ted:rechorus_coreをこの座標へ重ねる。
     */
    private BlockPos rechorusPlantCoreSlotPosition =
            BlockPos.ZERO;
    /*
     * 地下貯水槽に保存されている通常水量。
     * 単位はmB想定。
     */
    private int rechorusStoredWater;

    /*
     * 次の果汁水塊を作るまでの蓄積量。
     */
    private int rechorusPendingJuice;

    /*
     * 最後に果汁生産処理を行った時刻。
     */
    private long rechorusLastProductionTime;

    /*
     * 最後にプラント再生処理を行った時刻。
     */
    private long rechorusLastRegenerationTime;

    private boolean rechorusFacilityMarkersSaved;

    private boolean waterTransferMachineBInstalled;

    private boolean rechorusPlantCoreInstalled;

    private boolean rechorusPlantBuilt;

    public BlockPos getElderSpawnPosition() {
        return elderSpawnPosition;
    }

    public BlockPos getTechnicianSpawnPosition() {
        return technicianSpawnPosition;
    }

    public BlockPos getAllyHomePosition() {
        return allyHomePosition;
    }

    public int getRechorusStoredWater() {
        return this.rechorusStoredWater;
    }

    public int getRechorusPendingJuice() {
        return this.rechorusPendingJuice;
    }

    public void setRechorusStoredWater(
            int amount
    ) {
        int safeAmount =
                Math.max(
                        0,
                        amount
                );

        if (this.rechorusStoredWater
                == safeAmount) {
            return;
        }

        this.rechorusStoredWater =
                safeAmount;

        this.setDirty();
    }

    public void setRechorusPendingJuice(
            int amount
    ) {
        int safeAmount =
                Math.max(
                        0,
                        amount
                );

        if (this.rechorusPendingJuice
                == safeAmount) {
            return;
        }

        this.rechorusPendingJuice =
                safeAmount;

        this.setDirty();
    }

    public boolean consumeRechorusWater(
            int amount
    ) {
        if (amount <= 0
                || this.rechorusStoredWater
                < amount) {
            return false;
        }

        this.rechorusStoredWater -=
                amount;

        this.setDirty();

        return true;
    }

    public void addRechorusWater(
            int amount,
            int capacity
    ) {
        if (amount <= 0) {
            return;
        }

        this.setRechorusStoredWater(
                Math.min(
                        Math.max(0, capacity),
                        this.rechorusStoredWater
                                + amount
                )
        );
    }

    public void addRechorusPendingJuice(
            int amount
    ) {
        if (amount <= 0) {
            return;
        }

        this.setRechorusPendingJuice(
                this.rechorusPendingJuice
                        + amount
        );
    }

    private static final Codec<TedVillageWorldState> CODEC =
            RecordCodecBuilder.create(
                    instance ->
                            instance.group(
                                    Codec.INT
                                            .optionalFieldOf(
                                                    "data_version",
                                                    0
                                            )
                                            .forGetter(
                                                    TedVillageWorldState
                                                            ::getDataVersion
                                            ),

                                    Codec.BOOL
                                            .optionalFieldOf(
                                                    "village_generated",
                                                    false
                                            )
                                            .forGetter(
                                                    TedVillageWorldState
                                                            ::isVillageGenerated
                                            ),

                                    BlockPos.CODEC
                                            .optionalFieldOf(
                                                    "arrival_position",
                                                    new BlockPos(
                                                            0,
                                                            64,
                                                            0
                                                    )
                                            )
                                            .forGetter(
                                                    TedVillageWorldState
                                                            ::getArrivalPosition
                                            ),

                                    Codec.BOOL
                                            .optionalFieldOf(
                                                    "first_arrival_completed",
                                                    false
                                            )
                                            .forGetter(
                                                    TedVillageWorldState
                                                            ::isFirstArrivalCompleted
                                            ),

                                    Codec.INT
                                            .optionalFieldOf(
                                                    "village_quest_stage",
                                                    0
                                            )
                                            .forGetter(
                                                    TedVillageWorldState
                                                            ::getVillageQuestStage
                                            ),

                                    BlockPos.CODEC
                                            .optionalFieldOf(
                                                    "return_gateway_position",
                                                    new BlockPos(
                                                            0,
                                                            64,
                                                            0
                                                    )
                                            )
                                            .forGetter(
                                                    TedVillageWorldState
                                                            ::getReturnGatewayPosition
                                            ),

                                    BlockPos.CODEC
                                            .optionalFieldOf(
                                                    "elder_spawn_position",
                                                    BlockPos.ZERO
                                            )
                                            .forGetter(
                                                    TedVillageWorldState
                                                            ::getElderSpawnPosition
                                            ),

                                    BlockPos.CODEC
                                            .optionalFieldOf(
                                                    "technician_spawn_position",
                                                    BlockPos.ZERO
                                            )
                                            .forGetter(
                                                    TedVillageWorldState
                                                            ::getTechnicianSpawnPosition
                                            ),

                                    BlockPos.CODEC
                                            .optionalFieldOf(
                                                    "ally_home_position",
                                                    BlockPos.ZERO
                                            )
                                            .forGetter(
                                                    TedVillageWorldState
                                                            ::getAllyHomePosition
                                            ),

                                    RechorusFacilityData.CODEC
                                            .optionalFieldOf(
                                                    "rechorus_facility",
                                                    RechorusFacilityData.empty()
                                            )
                                            .forGetter(
                                                    TedVillageWorldState
                                                            ::createFacilityData
                                            ),

                                    Codec.unboundedMap(
                                                    Codec.STRING,
                                                    Codec.INT
                                            )
                                            .optionalFieldOf(
                                                    "player_trust_points",
                                                    Map.of()
                                            )
                                            .forGetter(
                                                    TedVillageWorldState
                                                            ::createPlayerTrustData
                                            )

                            ).apply(
                                    instance,
                                    TedVillageWorldState::new
                            )
            );

    private static final SavedDataType<
            TedVillageWorldState> TYPE =
            new SavedDataType<>(
                    Identifier.fromNamespaceAndPath(
                            TheEndOfDragon.MOD_ID,
                            "ted_enderman_village_state"
                    ),
                    TedVillageWorldState::new,
                    CODEC,
                    null
            );

    /*
     * 新規ワールド用。
     */
    public TedVillageWorldState() {
        this(
                CURRENT_DATA_VERSION,
                false,
                new BlockPos(
                        0,
                        64,
                        0
                ),
                false,
                0,
                new BlockPos(
                        0,
                        64,
                        0
                ),
                BlockPos.ZERO,
                BlockPos.ZERO,
                BlockPos.ZERO,
                RechorusFacilityData.empty(),
                Map.of()
        );
    }

    /*
     * Codec読み込み用。
     */
    private TedVillageWorldState(
            int dataVersion,
            boolean villageGenerated,
            BlockPos arrivalPosition,
            boolean firstArrivalCompleted,
            int villageQuestStage,
            BlockPos returnGatewayPosition,
            BlockPos elderSpawnPosition,
            BlockPos technicianSpawnPosition,
            BlockPos allyHomePosition,
            RechorusFacilityData facilityData,
            Map<String, Integer> playerTrustPoints
    ) {
        this.dataVersion =
                Math.max(
                        0,
                        dataVersion
                );

        this.villageGenerated =
                villageGenerated;

        this.arrivalPosition =
                safePosition(
                        arrivalPosition,
                        new BlockPos(
                                0,
                                64,
                                0
                        )
                );

        this.firstArrivalCompleted =
                firstArrivalCompleted;

        this.villageQuestStage =
                Math.max(
                        0,
                        villageQuestStage
                );

        this.returnGatewayPosition =
                safePosition(
                        returnGatewayPosition,
                        new BlockPos(
                                0,
                                64,
                                0
                        )
                );

        this.elderSpawnPosition =
                safePosition(
                        elderSpawnPosition,
                        BlockPos.ZERO
                );

        this.technicianSpawnPosition =
                safePosition(
                        technicianSpawnPosition,
                        BlockPos.ZERO
                );

        this.allyHomePosition =
                safePosition(
                        allyHomePosition,
                        BlockPos.ZERO
                );

        RechorusFacilityData safeFacilityData =
                facilityData == null
                        ? RechorusFacilityData.empty()
                        : facilityData;

        this.rechorusFacilityAnchorPosition =
                safePosition(
                        safeFacilityData
                                .anchorPosition(),
                        BlockPos.ZERO
                );

        this.waterTransferMachineBSlotPosition =
                safePosition(
                        safeFacilityData
                                .machineBSlotPosition(),
                        BlockPos.ZERO
                );

        this.rechorusPlantCoreSlotPosition =
                safePosition(
                        safeFacilityData
                                .plantCoreSlotPosition(),
                        BlockPos.ZERO
                );

        this.rechorusFacilityMarkersSaved =
                safeFacilityData.markersSaved();

        this.waterTransferMachineBInstalled =
                safeFacilityData.machineBInstalled();

        this.rechorusPlantCoreInstalled =
                safeFacilityData.plantCoreInstalled();

        this.rechorusPlantBuilt =
                safeFacilityData.plantBuilt();

        this.rechorusStoredWater =
                Math.max(
                        0,
                        safeFacilityData.storedWater()
                );

        this.rechorusPendingJuice =
                Math.max(
                        0,
                        safeFacilityData.pendingJuice()
                );

        if (playerTrustPoints != null) {
            for (Map.Entry<String, Integer> entry
                    : playerTrustPoints.entrySet()) {

                String playerId =
                        entry.getKey();

                Integer points =
                        entry.getValue();

                if (playerId == null
                        || points == null) {
                    continue;
                }

                /*
                 * 不正なUUID文字列は保存対象にしない。
                 */
                try {
                    UUID.fromString(playerId);
                } catch (IllegalArgumentException exception) {
                    continue;
                }

                int safePoints =
                        Math.clamp(
                                points,
                                0,
                                TedVillageTrustConstants
                                        .ABSOLUTE_MAX_TRUST
                        );

                if (safePoints > 0) {
                    this.playerTrustPoints.put(
                            playerId,
                            safePoints
                    );
                }
            }
        }



        migrateDataIfNeeded();
    }


    private static BlockPos safePosition(
            BlockPos position,
            BlockPos fallback
    ) {
        if (position != null) {
            return position.immutable();
        }

        if (fallback != null) {
            return fallback.immutable();
        }

        return BlockPos.ZERO;
    }

    public static TedVillageWorldState get(
            ServerLevel level
    ) {
        return level.getDataStorage()
                .computeIfAbsent(
                        TYPE
                );
    }

    private void migrateDataIfNeeded() {
        int version =
                this.dataVersion;

        /*
         * version 0 → 1
         *
         * versionフィールドが存在しない
         * 初期データからの移行。
         *
         * 村生成状態と到着座標はそのまま維持し、
         * 新規のクエスト項目を初期化する。
         */
        if (version < 1) {
            this.firstArrivalCompleted =
                    false;

            this.villageQuestStage =
                    0;

            version = 1;
        }

        /*
         * 将来はこの下へ順番に追加する。
         *
         * if (version < 2) {
         *     // version 1 → 2
         *     version = 2;
         * }
         */

        if (version < 2) {
            this.returnGatewayPosition =
                    new BlockPos(
                            0,
                            64,
                            0
                    );

            version = 2;
        }

        if (version < 3) {
            this.elderSpawnPosition =
                    BlockPos.ZERO;

            this.technicianSpawnPosition =
                    BlockPos.ZERO;

            this.allyHomePosition =
                    BlockPos.ZERO;

            version = 3;
        }

        if (version < 4) {
            this.rechorusFacilityAnchorPosition =
                    BlockPos.ZERO;

            this.waterTransferMachineBSlotPosition =
                    BlockPos.ZERO;

            this.rechorusPlantCoreSlotPosition =
                    BlockPos.ZERO;

            this.rechorusFacilityMarkersSaved =
                    false;

            this.waterTransferMachineBInstalled =
                    false;

            this.rechorusPlantCoreInstalled =
                    false;

            this.rechorusPlantBuilt =
                    false;

            version = 4;
        }

        if (version < 5) {
            this.rechorusStoredWater =
                    0;

            this.rechorusPendingJuice =
                    0;

            version = 5;
        }

        if (version < 6) {
            /*
             * 信頼度マップはCodecのデフォルト値で
             * 空の状態から開始する。
             */
            version = 6;
        }

        this.dataVersion =
                Math.min(
                        version,
                        CURRENT_DATA_VERSION
                );
    }

    public int getDataVersion() {
        return this.dataVersion;
    }

    public boolean isVillageGenerated() {
        return this.villageGenerated;
    }

    public BlockPos getArrivalPosition() {
        return this.arrivalPosition;
    }

    public boolean isFirstArrivalCompleted() {
        return this.firstArrivalCompleted;
    }

    public int getVillageQuestStage() {
        return this.villageQuestStage;
    }


    private RechorusFacilityData createFacilityData() {
        return new RechorusFacilityData(
                this.rechorusFacilityAnchorPosition,
                this.waterTransferMachineBSlotPosition,
                this.rechorusPlantCoreSlotPosition,
                this.rechorusFacilityMarkersSaved,
                this.waterTransferMachineBInstalled,
                this.rechorusPlantCoreInstalled,
                this.rechorusPlantBuilt,
                this.rechorusStoredWater,
                this.rechorusPendingJuice
        );
    }

    private Map<String, Integer>
    createPlayerTrustData() {
        return Map.copyOf(
                this.playerTrustPoints
        );
    }

    public int getTrustPoints(
            UUID playerId
    ) {
        if (playerId == null) {
            return 0;
        }

        return this.playerTrustPoints
                .getOrDefault(
                        playerId.toString(),
                        0
                );
    }

    public int getTrustCap() {
        return TedVillageTrustConstants
                .getTrustCapForStage(
                        this.getVillageQuest()
                );
    }

    public int getTrustLevel(
            UUID playerId
    ) {
        return TedVillageTrustConstants
                .levelFromPoints(
                        this.getTrustPoints(
                                playerId
                        )
                );
    }

    public void setTrustPoints(
            UUID playerId,
            int points
    ) {
        if (playerId == null) {
            return;
        }

        int trustCap =
                this.getTrustCap();

        int safePoints =
                Math.clamp(
                        points,
                        0,
                        trustCap
                );

        String key =
                playerId.toString();

        int oldPoints =
                this.playerTrustPoints
                        .getOrDefault(
                                key,
                                0
                        );

        if (oldPoints == safePoints) {
            return;
        }

        if (safePoints <= 0) {
            this.playerTrustPoints.remove(
                    key
            );
        } else {
            this.playerTrustPoints.put(
                    key,
                    safePoints
            );
        }

        this.setDirty();
    }

    public int addTrustPoints(
            UUID playerId,
            int amount
    ) {
        if (playerId == null
                || amount == 0) {
            return this.getTrustPoints(
                    playerId
            );
        }

        int currentPoints =
                this.getTrustPoints(
                        playerId
                );

        this.setTrustPoints(
                playerId,
                currentPoints + amount
        );

        return this.getTrustPoints(
                playerId
        );
    }

    public boolean hasTrustLevel(
            UUID playerId,
            int requiredLevel
    ) {
        if (requiredLevel <= 0) {
            return true;
        }

        return this.getTrustLevel(
                playerId
        ) >= requiredLevel;
    }

    public void completeGeneration(
            BlockPos arrivalPosition
    ) {
        if (arrivalPosition == null) {
            return;
        }

        this.villageGenerated = true;

        this.arrivalPosition =
                arrivalPosition.immutable();

        this.setDirty();
    }

    public void completeFirstArrival() {
        if (this.firstArrivalCompleted) {
            return;
        }

        this.firstArrivalCompleted =
                true;

        this.setDirty();
    }

    public void setVillageQuestStage(
            int stage
    ) {
        int safeStage =
                Math.max(
                        0,
                        stage
                );

        if (this.villageQuestStage
                == safeStage) {
            return;
        }

        this.villageQuestStage =
                safeStage;

        this.setDirty();
    }

    public boolean hasRechorusFacilityMarkers() {
        return this.rechorusFacilityMarkersSaved;
    }

    public BlockPos getRechorusFacilityAnchorPosition() {
        return this.rechorusFacilityAnchorPosition;
    }

    public BlockPos getWaterTransferMachineBSlotPosition() {
        return this.waterTransferMachineBSlotPosition;
    }

    public BlockPos getRechorusPlantCoreSlotPosition() {
        return this.rechorusPlantCoreSlotPosition;
    }

    public boolean isWaterTransferMachineBInstalled() {
        return this.waterTransferMachineBInstalled;
    }

    public boolean isRechorusPlantCoreInstalled() {
        return this.rechorusPlantCoreInstalled;
    }

    public boolean isRechorusPlantBuilt() {
        return this.rechorusPlantBuilt;
    }

    public void setRechorusFacilityPositions(
            BlockPos facilityAnchor,
            BlockPos machineBSlot,
            BlockPos plantCoreSlot
    ) {
        if (facilityAnchor == null
                || machineBSlot == null
                || plantCoreSlot == null) {
            return;
        }

        this.rechorusFacilityAnchorPosition =
                facilityAnchor.immutable();

        this.waterTransferMachineBSlotPosition =
                machineBSlot.immutable();

        this.rechorusPlantCoreSlotPosition =
                plantCoreSlot.immutable();

        this.rechorusFacilityMarkersSaved =
                true;

        this.setDirty();
    }

    public void setWaterTransferMachineBInstalled(
            boolean installed
    ) {
        if (this.waterTransferMachineBInstalled
                == installed) {
            return;
        }

        this.waterTransferMachineBInstalled =
                installed;

        this.setDirty();
    }

    public void setRechorusPlantCoreInstalled(
            boolean installed
    ) {
        if (this.rechorusPlantCoreInstalled
                == installed) {
            return;
        }

        this.rechorusPlantCoreInstalled =
                installed;

        this.setDirty();
    }

    public void setRechorusPlantBuilt(
            boolean built
    ) {
        if (this.rechorusPlantBuilt == built) {
            return;
        }

        this.rechorusPlantBuilt = built;

        this.setDirty();
    }

    /*
     * デバッグで村を再生成したい場合に使用する。
     */
    public void resetGeneration() {
        this.villageGenerated =
                false;

        this.arrivalPosition =
                new BlockPos(
                        0,
                        64,
                        0
                );

        this.firstArrivalCompleted =
                false;

        this.villageQuestStage =
                0;

        this.elderSpawnPosition =
                BlockPos.ZERO;

        this.technicianSpawnPosition =
                BlockPos.ZERO;

        this.allyHomePosition =
                BlockPos.ZERO;

        this.rechorusFacilityAnchorPosition =
                BlockPos.ZERO;

        this.waterTransferMachineBSlotPosition =
                BlockPos.ZERO;

        this.rechorusPlantCoreSlotPosition =
                BlockPos.ZERO;

        this.rechorusFacilityMarkersSaved =
                false;

        this.waterTransferMachineBInstalled =
                false;

        this.rechorusPlantCoreInstalled =
                false;

        this.rechorusPlantBuilt =
                false;

        this.playerTrustPoints.clear();

        this.setDirty();
    }

    public BlockPos getReturnGatewayPosition() {
        return this.returnGatewayPosition;
    }

    public void setReturnGatewayPosition(
            BlockPos position
    ) {
        if (position == null) {
            return;
        }

        BlockPos safePosition =
                position.immutable();

        if (this.returnGatewayPosition
                .equals(safePosition)) {
            return;
        }

        this.returnGatewayPosition =
                safePosition;

        this.setDirty();
    }

    public void setResidentPositions(
            BlockPos elderSpawnPosition,
            BlockPos technicianSpawnPosition,
            BlockPos allyHomePosition
    ) {
        if (elderSpawnPosition == null
                || technicianSpawnPosition == null
                || allyHomePosition == null) {
            return;
        }

        this.elderSpawnPosition =
                elderSpawnPosition.immutable();

        this.technicianSpawnPosition =
                technicianSpawnPosition.immutable();

        this.allyHomePosition =
                allyHomePosition.immutable();

        this.setDirty();
    }

    public TedVillageQuestStage getVillageQuest() {
        return TedVillageQuestStage.fromId(
                this.villageQuestStage
        );
    }

    public void setVillageQuestStage(
            TedVillageQuestStage stage
    ) {
        if (stage == null) {
            return;
        }

        setVillageQuestStage(
                stage.getId()
        );
    }

    public boolean advanceVillageQuest(
            TedVillageQuestStage minimumCurrent,
            TedVillageQuestStage next
    ) {
        if (minimumCurrent == null
                || next == null) {
            return false;
        }

        if (!getVillageQuest().isAtLeast(
                minimumCurrent
        )) {
            return false;
        }

        if (getVillageQuest().isAtLeast(next)) {
            return false;
        }

        setVillageQuestStage(next);
        return true;
    }

    private record RechorusFacilityData(
            BlockPos anchorPosition,
            BlockPos machineBSlotPosition,
            BlockPos plantCoreSlotPosition,
            boolean markersSaved,
            boolean machineBInstalled,
            boolean plantCoreInstalled,
            boolean plantBuilt,
            int storedWater,
            int pendingJuice
    ) {
        private static final Codec<RechorusFacilityData> CODEC =
                RecordCodecBuilder.create(
                        instance ->
                                instance.group(
                                        BlockPos.CODEC
                                                .optionalFieldOf(
                                                        "anchor_position",
                                                        BlockPos.ZERO
                                                )
                                                .forGetter(
                                                        RechorusFacilityData
                                                                ::anchorPosition
                                                ),

                                        BlockPos.CODEC
                                                .optionalFieldOf(
                                                        "machine_b_slot_position",
                                                        BlockPos.ZERO
                                                )
                                                .forGetter(
                                                        RechorusFacilityData
                                                                ::machineBSlotPosition
                                                ),

                                        BlockPos.CODEC
                                                .optionalFieldOf(
                                                        "plant_core_slot_position",
                                                        BlockPos.ZERO
                                                )
                                                .forGetter(
                                                        RechorusFacilityData
                                                                ::plantCoreSlotPosition
                                                ),

                                        Codec.BOOL
                                                .optionalFieldOf(
                                                        "markers_saved",
                                                        false
                                                )
                                                .forGetter(
                                                        RechorusFacilityData
                                                                ::markersSaved
                                                ),

                                        Codec.BOOL
                                                .optionalFieldOf(
                                                        "machine_b_installed",
                                                        false
                                                )
                                                .forGetter(
                                                        RechorusFacilityData
                                                                ::machineBInstalled
                                                ),

                                        Codec.BOOL
                                                .optionalFieldOf(
                                                        "plant_core_installed",
                                                        false
                                                )
                                                .forGetter(
                                                        RechorusFacilityData
                                                                ::plantCoreInstalled
                                                ),

                                        Codec.BOOL
                                                .optionalFieldOf(
                                                        "plant_built",
                                                        false
                                                )
                                                .forGetter(
                                                        RechorusFacilityData
                                                                ::plantBuilt
                                                ),
                                        Codec.INT
                                                .optionalFieldOf(
                                                        "stored_water",
                                                        0
                                                )
                                                .forGetter(
                                                        RechorusFacilityData
                                                                ::storedWater
                                                ),

                                        Codec.INT
                                                .optionalFieldOf(
                                                        "pending_juice",
                                                        0
                                                )
                                                .forGetter(
                                                        RechorusFacilityData
                                                                ::pendingJuice
                                                )
                                ).apply(
                                        instance,
                                        RechorusFacilityData::new
                                )
                );

        private static RechorusFacilityData empty() {
            return new RechorusFacilityData(
                    BlockPos.ZERO,
                    BlockPos.ZERO,
                    BlockPos.ZERO,
                    false,
                    false,
                    false,
                    false,
                    0,
                    0
            );
        }
    }
}
