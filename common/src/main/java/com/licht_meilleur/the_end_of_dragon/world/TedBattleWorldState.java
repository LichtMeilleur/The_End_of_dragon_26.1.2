package com.licht_meilleur.the_end_of_dragon.world;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
    private static final int CURRENT_DATA_VERSION = 1;

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
     * 味方エンダーマン救助イベントの進行状況。
     */
    private TedAllyProgress allyProgress;

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
                TedAllyProgress.NOT_STARTED,
                0L
        );
    }

    /*
     * Codecからの読み込み用コンストラクタ。
     */
    private TedBattleWorldState(
            int dataVersion,
            boolean battleActive,
            TedAllyProgress allyProgress,
            long endermanSpawnSuppressedUntil
    ) {
        this.dataVersion =
                Math.max(
                        0,
                        dataVersion
                );

        this.battleActive =
                battleActive;

        this.allyProgress =
                allyProgress != null
                        ? allyProgress
                        : TedAllyProgress.NOT_STARTED;

        this.endermanSpawnSuppressedUntil =
                Math.max(
                        0L,
                        endermanSpawnSuppressedUntil
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
         *
         * battle_activeしか保存していなかった時代。
         * ally_progressはNOT_STARTEDとして扱う。
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
         * 将来CURRENT_DATA_VERSIONが2になった場合は、
         * この下へ追加する。
         *
         * 例:
         *
         * if (this.dataVersion < 2) {
         *     新しい項目の初期化処理;
         *     this.dataVersion = 2;
         *     changed = true;
         * }
         */

        if (this.dataVersion < CURRENT_DATA_VERSION) {
            this.dataVersion =
                    CURRENT_DATA_VERSION;

            changed = true;
        }

        /*
         * 変換されたデータを、
         * 次回ワールド保存時に書き直す。
         */
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
     * TED戦終了時に、
     * 一般エンダーマンのスポーン停止を解除する。
     *
     * Allyの進行状態は別処理で判断するため、
     * ここでは変更しない。
     */
    public void endBattle() {
        setBattleActive(false);
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
        ITEM_GIVEN
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
}