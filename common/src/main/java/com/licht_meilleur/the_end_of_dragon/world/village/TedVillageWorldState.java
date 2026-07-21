package com.licht_meilleur.the_end_of_dragon.world.village;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public final class TedVillageWorldState
        extends SavedData {

    /*
     * SavedDataの現在バージョン。
     *
     * 保存項目の意味や構造を変更したら増やす。
     */
    private static final int CURRENT_DATA_VERSION =
            2;

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

    private static final Codec<
            TedVillageWorldState> CODEC =
            RecordCodecBuilder.create(
                    instance ->
                            instance.group(
                                    Codec.INT
                                            .optionalFieldOf(
                                                    "data_version",
                                                    0
                                            )
                                            .forGetter(
                                                    state ->
                                                            state.dataVersion
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

                                    Codec.INT
                                            .optionalFieldOf(
                                                    "arrival_x",
                                                    0
                                            )
                                            .forGetter(
                                                    state ->
                                                            state.arrivalPosition
                                                                    .getX()
                                            ),

                                    Codec.INT
                                            .optionalFieldOf(
                                                    "arrival_y",
                                                    64
                                            )
                                            .forGetter(
                                                    state ->
                                                            state.arrivalPosition
                                                                    .getY()
                                            ),

                                    Codec.INT
                                            .optionalFieldOf(
                                                    "arrival_z",
                                                    0
                                            )
                                            .forGetter(
                                                    state ->
                                                            state.arrivalPosition
                                                                    .getZ()
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

                                    Codec.INT
                                            .optionalFieldOf(
                                                    "return_gateway_x",
                                                    0
                                            )
                                            .forGetter(
                                                    state ->
                                                            state.returnGatewayPosition
                                                                    .getX()
                                            ),

                                    Codec.INT
                                            .optionalFieldOf(
                                                    "return_gateway_y",
                                                    64
                                            )
                                            .forGetter(
                                                    state ->
                                                            state.returnGatewayPosition
                                                                    .getY()
                                            ),

                                    Codec.INT
                                            .optionalFieldOf(
                                                    "return_gateway_z",
                                                    0
                                            )
                                            .forGetter(
                                                    state ->
                                                            state.returnGatewayPosition
                                                                    .getZ()
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
                0,
                64,
                0,
                false,
                0,
                0,
                64,
                0
        );

    }

    /*
     * Codec読み込み用。
     */
    private TedVillageWorldState(
            int dataVersion,
            boolean villageGenerated,
            int arrivalX,
            int arrivalY,
            int arrivalZ,
            boolean firstArrivalCompleted,
            int villageQuestStage,
            int returnGatewayX,
            int returnGatewayY,
            int returnGatewayZ
    ) {
        this.dataVersion =
                Math.max(
                        0,
                        dataVersion
                );

        this.villageGenerated =
                villageGenerated;

        this.arrivalPosition =
                new BlockPos(
                        arrivalX,
                        arrivalY,
                        arrivalZ
                );

        this.firstArrivalCompleted =
                firstArrivalCompleted;

        this.villageQuestStage =
                Math.max(
                        0,
                        villageQuestStage
                );

        this.returnGatewayPosition =
                new BlockPos(
                        returnGatewayX,
                        returnGatewayY,
                        returnGatewayZ
                );



        migrateDataIfNeeded();
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
}