package com.licht_meilleur.the_end_of_dragon.world;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public final class TedBattleWorldState extends SavedData {

    /*
     * SavedDataの現在のデータバージョン。
     *
     * 保存項目の構成を変更した場合は、
     * 2、3……と増やしていく。
     */
    private static final int CURRENT_DATA_VERSION = 4;

    /*
     * 保存されていたデータのバージョン。
     *
     * 古いワールドでは項目自体が存在しないため、
     * Codec側で0として読み込む。
     */
    private int dataVersion;

    /*
     * true:
     * TED戦闘中。
     * 一般エンダーマンのスポーンを停止する。
     *
     * false:
     * TED戦闘外。
     * 通常のスポーンを許可する。
     */
    private boolean battleActive;
    /*
     * TEDの討伐が完了しているか。
     *
     * false:
     * 戦闘開始前、または戦闘中。
     *
     * true:
     * TED討伐後。
     *
     * 門アイテムの手渡しや、
     * 討伐後エンダーマンの再生成判定に使用する。
     */
    private boolean battleCompleted;
    /*
     * 味方エンダーマン救助イベントの進行状況。
     */
    private TedAllyProgress allyProgress;
    /*
     * プレイヤーへ渡していない
     * エンダーマン村の門アイテム数。
     *
     * 初期値は1。
     * 配布成功後は0になる。
     */
    private int invitationGatewayCount;
    /*
     * ワールド内に設置されている門Aの情報。
     *
     * 門Bからの帰還先として使用する。
     */

    private boolean returnGatewayRegistered = false;

    private String returnGatewayDimensionId =
            "";

    private BlockPos returnGatewayPosition =
            BlockPos.ZERO;
    /*
     * enumを文字列として保存するCodec。
     *
     * 保存例:
     *
     * ally_progress: "ALLY_ACTIVE"
     *
     * 不明な名前が保存されていた場合は、
     * 読み込みエラーとして扱う。
     */
    private static final Codec<TedAllyProgress>
            ALLY_PROGRESS_CODEC =
            Codec.STRING.comapFlatMap(
                    savedName -> {
                        try {
                            return DataResult.success(
                                    TedAllyProgress.valueOf(
                                            savedName
                                    )
                            );
                        } catch (
                                IllegalArgumentException exception
                        ) {
                            return DataResult.error(
                                    () ->
                                            "Unknown TED ally progress: "
                                                    + savedName
                            );
                        }
                    },
                    TedAllyProgress::name
            );

    /*
     * SavedDataと保存データの相互変換。
     *
     * 保存内容は概ね以下の形になる。
     *
     * data_version: 1
     * battle_active: true / false
     * ally_progress: "NOT_STARTED"
     *
     * optionalFieldOfを使用しているため、
     * 既存ワールドに新しい項目が存在しなくても
     * デフォルト値で読み込める。
     */
    private static final Codec<TedBattleWorldState> CODEC =
            RecordCodecBuilder.create(
                    instance -> instance.group(
                            Codec.INT
                                    .optionalFieldOf(
                                            "data_version",
                                            0
                                    )
                                    .forGetter(
                                            TedBattleWorldState
                                                    ::getDataVersion
                                    ),

                            Codec.BOOL
                                    .optionalFieldOf(
                                            "battle_active",
                                            false
                                    )
                                    .forGetter(
                                            TedBattleWorldState
                                                    ::isBattleActive
                                    ),
                            /*
                             * TED討伐済みフラグ。
                             */
                            Codec.BOOL
                                    .optionalFieldOf(
                                            "battle_completed",
                                            false
                                    )
                                    .forGetter(
                                            TedBattleWorldState
                                                    ::isBattleCompleted
                                    ),

                            ALLY_PROGRESS_CODEC
                                    .optionalFieldOf(
                                            "ally_progress",
                                            TedAllyProgress.NOT_STARTED
                                    )
                                    .forGetter(
                                            TedBattleWorldState
                                                    ::getAllyProgress
                                    ),

                            Codec.LONG
                                    .optionalFieldOf(
                                            "enderman_spawn_suppressed_until",
                                            0L
                                    )
                                    .forGetter(
                                            TedBattleWorldState
                                                    ::getEndermanSpawnSuppressedUntil
                                    ),

                            Codec.INT
                                    .optionalFieldOf(
                                            "invitation_gateway_count",
                                            1
                                    )
                                    .forGetter(
                                            TedBattleWorldState
                                                    ::getInvitationGatewayCount
                                    ),
                            Codec.BOOL
                                    .optionalFieldOf(
                                            "return_gateway_registered",
                                            false
                                    )
                                    .forGetter(
                                            TedBattleWorldState
                                                    ::hasRegisteredReturnGateway
                                    ),

                            Codec.STRING
                                    .optionalFieldOf(
                                            "return_gateway_dimension",
                                            ""
                                    )
                                    .forGetter(
                                            TedBattleWorldState
                                                    ::getReturnGatewayDimensionId
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
                            TedBattleWorldState::new
                    )
            );

    /*
     * SavedDataの識別子。
     *
     * ワールドのdataフォルダーへ、
     * このIDに対応するデータが保存される。
     */
    private static final SavedDataType<TedBattleWorldState> TYPE =
            new SavedDataType<>(
                    Identifier.fromNamespaceAndPath(
                            TheEndOfDragon.MOD_ID,
                            "ted_battle_world_state"
                    ),
                    TedBattleWorldState::new,
                    CODEC,
                    null
            );

    /*
     * 保存データがまだ存在しない
     * 新規ワールド用コンストラクタ。
     */
    public TedBattleWorldState() {
        this(
                CURRENT_DATA_VERSION,
                false,
                false,
                TedAllyProgress.NOT_STARTED,
                0L,
                1,
                false,
                "",
                0,
                64,
                0
        );
    }

    /*
     * Codecからの読み込み用コンストラクタ。
     */
    private TedBattleWorldState(
            int dataVersion,
            boolean battleActive,
            boolean battleCompleted,
            TedAllyProgress allyProgress,
            long endermanSpawnSuppressedUntil,
            int invitationGatewayCount,
            boolean returnGatewayRegistered,
            String returnGatewayDimensionId,
            int returnGatewayX,
            int returnGatewayY,
            int returnGatewayZ
    ) {
        this.dataVersion =
                Math.max(
                        0,
                        dataVersion
                );

        this.battleActive =
                battleActive;

        this.battleCompleted =
                battleCompleted;

        this.allyProgress =
                allyProgress != null
                        ? allyProgress
                        : TedAllyProgress.NOT_STARTED;

        this.endermanSpawnSuppressedUntil =
                Math.max(
                        0L,
                        endermanSpawnSuppressedUntil
                );

        this.invitationGatewayCount =
                Math.max(
                        0,
                        invitationGatewayCount
                );

        this.returnGatewayRegistered =
                returnGatewayRegistered;

        this.returnGatewayDimensionId =
                returnGatewayDimensionId != null
                        ? returnGatewayDimensionId
                        : "";

        this.returnGatewayPosition =
                new BlockPos(
                        returnGatewayX,
                        returnGatewayY,
                        returnGatewayZ
                );

        migrateDataIfNeeded();
    }

    /*
     * 古いデータを現在の形式へ更新する。
     *
     * CURRENT_DATA_VERSIONを増やした場合は、
     * ここへ変換処理を追加する。
     */
    private void migrateDataIfNeeded() {
        boolean changed = false;

        /*
         * バージョン0:
         * allyProgress未実装時代。
         */
        if (this.dataVersion < 1) {
            if (this.allyProgress == null) {
                this.allyProgress =
                        TedAllyProgress.NOT_STARTED;
            }

            this.dataVersion = 1;
            changed = true;
        }

        /*
         * バージョン2:
         * 未配布の村門アイテム数を追加。
         *
         * すでにITEM_GIVENなら0、
         * それ以外なら未配布として1。
         */
        if (this.dataVersion < 2) {
            this.invitationGatewayCount =
                    this.allyProgress
                            == TedAllyProgress.ITEM_GIVEN
                            ? 0
                            : 1;

            this.dataVersion = 2;
            changed = true;
        }

        /*
         * バージョン3:
         * TED討伐済みフラグを追加。
         *
         * 旧SavedDataでは進行状態から推測する。
         */
        if (this.dataVersion < 3) {
            this.battleCompleted =
                    switch (this.allyProgress) {
                        case WOUNDED_AFTER_BATTLE,
                             RECOVERED_AFTER_BATTLE,
                             ITEM_GIVEN -> true;

                        default -> false;
                    };

            this.dataVersion = 3;
            changed = true;
        }

        /*
         * バージョン4:
         * 村から戻るための門Aの位置を追加。
         */
        if (this.dataVersion < 4) {
            this.returnGatewayRegistered =
                    false;

            this.returnGatewayDimensionId =
                    "";

            this.returnGatewayPosition =
                    new BlockPos(
                            0,
                            64,
                            0
                    );

            this.dataVersion = 4;
            changed = true;
        }

        /*
         * 将来バージョンを増やした際の最終補正。
         */
        if (this.dataVersion
                < CURRENT_DATA_VERSION) {

            this.dataVersion =
                    CURRENT_DATA_VERSION;

            changed = true;
        }

        if (changed) {
            setDirty();
        }
    }

    /*
     * ServerLevelに保存されている状態を取得する。
     *
     * 保存済み:
     * Codecを使って読み込む。
     *
     * 未作成:
     * TedBattleWorldState()で新規作成する。
     */
    public static TedBattleWorldState get(
            ServerLevel level
    ) {
        return level.getDataStorage()
                .computeIfAbsent(TYPE);
    }

    public int getDataVersion() {
        return dataVersion;
    }

    public boolean isBattleActive() {
        return battleActive;
    }

    public void setBattleActive(
            boolean battleActive
    ) {
        if (this.battleActive == battleActive) {
            return;
        }

        this.battleActive = battleActive;

        /*
         * 変更をワールドデータへ保存対象として登録する。
         */
        setDirty();
    }

    public boolean isBattleCompleted() {
        return this.battleCompleted;
    }

    public void setBattleCompleted(
            boolean battleCompleted
    ) {
        if (this.battleCompleted
                == battleCompleted) {
            return;
        }

        this.battleCompleted =
                battleCompleted;

        setDirty();
    }

    public TedAllyProgress getAllyProgress() {
        return allyProgress;
    }

    public void setAllyProgress(
            TedAllyProgress allyProgress
    ) {
        TedAllyProgress safeProgress =
                allyProgress != null
                        ? allyProgress
                        : TedAllyProgress.NOT_STARTED;

        if (this.allyProgress == safeProgress) {
            return;
        }

        this.allyProgress = safeProgress;

        /*
         * 変更をワールドデータへ保存対象として登録する。
         */
        setDirty();
    }

    /*
     * TED戦開始時の状態へ設定する。
     */
    public void beginBattle() {
        boolean changed = false;

        if (!this.battleActive) {
            this.battleActive = true;
            changed = true;
        }

        /*
         * battleCompletedは一度討伐した事実なので、
         * 再戦開始時にもfalseへ戻さない。
         */

        if (this.allyProgress
                == TedAllyProgress.NOT_STARTED) {

            this.allyProgress =
                    TedAllyProgress.WOUNDED_DURING_BATTLE;

            changed = true;
        }

        if (changed) {
            setDirty();
        }
    }

    /*
     * TED討伐成功時に呼ぶ。
     */
    public void completeBattle() {
        boolean changed = false;

        if (this.battleActive) {
            this.battleActive = false;
            changed = true;
        }

        if (!this.battleCompleted) {
            this.battleCompleted = true;
            changed = true;
        }

        if (changed) {
            setDirty();
        }
    }

    /*
     * TED戦終了時に、
     * 一般エンダーマンのスポーン停止を解除する。
     *
     * Allyの進行状態は別処理で判断するため、
     * ここでは変更しない。
     */
    public void endBattle() {
        setBattleActive(false);
    }
    public int getInvitationGatewayCount() {
        return this.invitationGatewayCount;
    }

    public boolean hasInvitationGatewayToGive() {
        return this.invitationGatewayCount > 0;
    }

    /*
     * 未配布数を直接設定する。
     */
    public void setInvitationGatewayCount(
            int count
    ) {
        int safeCount =
                Math.max(
                        0,
                        count
                );

        if (this.invitationGatewayCount
                == safeCount) {
            return;
        }

        this.invitationGatewayCount =
                safeCount;

        setDirty();
    }

    /*
     * 門アイテムを1個配布したことを確定する。
     *
     * 残数がない場合はfalse。
     */
    public boolean consumeInvitationGateway() {
        if (this.invitationGatewayCount <= 0) {
            return false;
        }

        this.invitationGatewayCount--;

        setDirty();

        return true;
    }


    public enum TedAllyProgress {

        /*
         * 救助イベントがまだ始まっていない。
         */
        NOT_STARTED,

        /*
         * TED戦中に瀕死状態で出現している。
         */
        WOUNDED_DURING_BATTLE,

        /*
         * 救助され、TED戦へ参加できる状態。
         */
        ALLY_ACTIVE,

        /*
         * TED戦中に倒された。
         */
        DIED_DURING_BATTLE,

        /*
         * TED討伐後に瀕死状態で再出現している。
         */
        WOUNDED_AFTER_BATTLE,

        /*
         * TED討伐後に救助された。
         * 村への招待アイテムを渡す前の状態。
         */
        RECOVERED_AFTER_BATTLE,



        /*
         * 村への招待アイテムを渡し終えた。
         */
        ITEM_GIVEN,

        /*
         * TED討伐後、味方エンダーマンが存在せず、
         * 瀕死状態で再生成する必要がある。
         */
        RESPAWN_AFTER_BATTLE_PENDING,
    }

    /*
     * 一般エンダーマンのスポーン抑制を解除する
     * ワールドゲーム時刻。
     *
     * 0以下なら時間指定の抑制なし。
     */
    private long endermanSpawnSuppressedUntil;





    public long getEndermanSpawnSuppressedUntil() {
        return endermanSpawnSuppressedUntil;
    }

    /*
     * 指定tick数だけ、一般エンダーマンのスポーンを止める。
     */
    public void suppressEndermanSpawnsFor(
            ServerLevel level,
            long durationTicks
    ) {
        long safeDuration =
                Math.max(
                        0L,
                        durationTicks
                );

        long newEndTime =
                level.getGameTime()
                        + safeDuration;

        /*
         * すでにより長い抑制が設定されている場合は、
         * 短く上書きしない。
         */
        if (newEndTime
                <= this.endermanSpawnSuppressedUntil) {
            return;
        }

        this.endermanSpawnSuppressedUntil =
                newEndTime;

        setDirty();
    }

    /*
     * 戦闘中、または討伐後クールダウン中ならtrue。
     */
    public boolean shouldSuppressEndermanSpawns(
            ServerLevel level
    ) {
        if (this.battleActive) {
            return true;
        }

        return level.getGameTime()
                < this.endermanSpawnSuppressedUntil;
    }

    public long getRemainingSuppressionTicks(
            ServerLevel level
    ) {
        if (this.battleActive) {
            return Long.MAX_VALUE;
        }

        return Math.max(
                0L,
                this.endermanSpawnSuppressedUntil
                        - level.getGameTime()
        );
    }
    public boolean hasRegisteredReturnGateway() {
        return this.returnGatewayRegistered
                && this.returnGatewayDimensionId != null
                && !this.returnGatewayDimensionId.isBlank();
    }

    public String getReturnGatewayDimensionId() {
        return this.returnGatewayDimensionId;
    }

    public BlockPos getReturnGatewayPosition() {
        return this.returnGatewayPosition;
    }

    /*
     * 門Aの設置位置を登録する。
     *
     * 門アイテムは一品物なので、
     * 新しい位置が登録された場合は上書きする。
     */
    public void registerReturnGateway(
            ServerLevel level,
            BlockPos position
    ) {
        if (level == null
                || position == null) {
            return;
        }

        this.returnGatewayRegistered =
                true;

        this.returnGatewayDimensionId =
                level.dimension()
                        .identifier()
                        .toString();

        this.returnGatewayPosition =
                position.immutable();

        setDirty();
    }

    /*
     * 指定された門が現在登録中の門Aなら、
     * 帰還先登録を解除する。
     */
    public void unregisterReturnGateway(
            ServerLevel level,
            BlockPos position
    ) {
        if (!hasRegisteredReturnGateway()
                || level == null
                || position == null) {
            return;
        }

        String dimensionId =
                level.dimension()
                        .identifier()
                        .toString();

        if (!this.returnGatewayDimensionId
                .equals(dimensionId)) {
            return;
        }

        if (!this.returnGatewayPosition
                .equals(position)) {
            return;
        }

        clearReturnGateway();
    }

    public void clearReturnGateway() {
        this.returnGatewayRegistered =
                false;

        this.returnGatewayDimensionId =
                "";

        this.returnGatewayPosition =
                new BlockPos(
                        0,
                        64,
                        0
                );

        setDirty();
    }

}