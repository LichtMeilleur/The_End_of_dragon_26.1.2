package com.licht_meilleur.the_end_of_dragon.world.village.quest;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.registry.ModItems;
import com.licht_meilleur.the_end_of_dragon.world.village
        .TedVillageQuestStage;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;

import java.util.List;

public enum TedVillageQuestDefinition {

    /*
     * 1. 水転送装置の開発
     *
     * 画像：
     * textures/gui/quest/main/water_transfer.png
     */
    WATER_TRANSFER(
            TedVillageQuestId
                    .WATER_TRANSFER_RESEARCH,

            TedVillageQuestType.MAIN,

            TedQuestNpc.ELDER,

            /*
             * langキーに使用。
             */
            "water_transfer",

            /*
             * 画像ファイル名に使用。
             */
            "water_transfer",

            TedVillageQuestStage.NOT_STARTED,

            TedVillageQuestStage
                    .WATER_TRANSFER_RESEARCH
    ),

    /*
     * 2. リコーラスメロン試作
     *
     * 画像：
     * textures/gui/quest/main/rechorus_melon.png
     */
    RECHORUS_MELON(
            TedVillageQuestId
                    .RECHORUS_MELON_PROTOTYPE,

            TedVillageQuestType.MAIN,

            TedQuestNpc.ELDER,

            "rechorus_melon",

            "rechorus_melon",

            TedVillageQuestStage
                    .WATER_TRANSFER_RESEARCH,

            TedVillageQuestStage
                    .RECHORUS_MELON_PROTOTYPE
    ),

    /*
     * 3. 成功したリコーラスメロンの種を届ける
     *
     * 第2クエストと同じイラストを使用する。
     *
     * lang：
     * quest.the_end_of_dragon.rechorus_seed.*
     *
     * 画像：
     * textures/gui/quest/main/rechorus_melon.png
     */
    RECHORUS_MELON_SEED(
            TedVillageQuestId
                    .RECHORUS_MELON_SEED_DELIVERY,

            TedVillageQuestType.MAIN,

            TedQuestNpc.ELDER,

            "rechorus_seed",

            "rechorus_melon",

            TedVillageQuestStage
                    .RECHORUS_MELON_PROTOTYPE,

            TedVillageQuestStage
                    .RECHORUS_MELON_SEED_DELIVERY
    ),

    /*
     * 4. リコーラスプラント試作
     *
     * 画像：
     * textures/gui/quest/main/rechorus_plant_seed.png
     */
    RECHORUS_PLANT(
            TedVillageQuestId
                    .RECHORUS_PLANT_PROTOTYPE,

            TedVillageQuestType.MAIN,

            TedQuestNpc.ELDER,

            "rechorus_plant",

            "rechorus_plant_seed",

            TedVillageQuestStage
                    .RECHORUS_MELON_SEED_DELIVERY,

            TedVillageQuestStage
                    .RECHORUS_PLANT_PROTOTYPE
    ),

    /*
     * 5. リコーラスプラントコアと
     * 水転送装置Bを施設へ設置
     *
     * 画像：
     * textures/gui/quest/main/rechorus_water.png
     */
    RECHORUS_FACILITY(
            TedVillageQuestId
                    .RECHORUS_FACILITY_CONSTRUCTION,

            TedVillageQuestType.MAIN,

            TedQuestNpc.ELDER,

            "facility",

            "rechorus_water",

            TedVillageQuestStage
                    .RECHORUS_PLANT_PROTOTYPE,

            TedVillageQuestStage
                    .RECHORUS_PLANT_BUILT
    );

    /*
     * セーブデータやネットワークで使用する
     * クエスト固有ID。
     */
    private final TedVillageQuestId id;

    /*
     * MAIN / SIDE / DAILYなど。
     *
     * 画像フォルダの決定にも使用する。
     */
    private final TedVillageQuestType type;

    /*
     * このクエストを担当するNPC。
     */
    private final TedQuestNpc npc;

    /*
     * langファイル用のキー。
     *
     * 例：
     * water_transfer
     *
     * ↓
     *
     * quest.the_end_of_dragon
     *     .water_transfer.title
     *
     * quest.the_end_of_dragon
     *     .water_transfer.description
     */
    private final String translationKey;

    /*
     * 拡張子とフォルダを除いた画像名。
     *
     * 例：
     * water_transfer
     *
     * ↓
     *
     * textures/gui/quest/main/
     *     water_transfer.png
     */
    private final String illustrationKey;

    /*
     * このクエストが表示可能になる段階。
     */
    private final TedVillageQuestStage requiredStage;

    /*
     * クエスト完了時に設定する村進行段階。
     */
    private final TedVillageQuestStage completedStage;

    TedVillageQuestDefinition(
            TedVillageQuestId id,
            TedVillageQuestType type,
            TedQuestNpc npc,
            String translationKey,
            String illustrationKey,
            TedVillageQuestStage requiredStage,
            TedVillageQuestStage completedStage
    ) {
        this.id = id;
        this.type = type;
        this.npc = npc;
        this.translationKey = translationKey;
        this.illustrationKey = illustrationKey;
        this.requiredStage = requiredStage;
        this.completedStage = completedStage;
    }

    public TedVillageQuestId getId() {
        return this.id;
    }

    public TedVillageQuestType getType() {
        return this.type;
    }

    public TedQuestNpc getNpc() {
        return this.npc;
    }

    public String getTranslationKey() {
        return this.translationKey;
    }

    public String getIllustrationKey() {
        return this.illustrationKey;
    }

    public TedVillageQuestStage getRequiredStage() {
        return this.requiredStage;
    }

    public TedVillageQuestStage getCompletedStage() {
        return this.completedStage;
    }

    public Identifier getPicture() {
        return TheEndOfDragon.id(
                "textures/gui/quest/"
                        + this.type.getFolderName()
                        + "/"
                        + this.illustrationKey
                        + ".png"
        );
    }

    public Component getTitle() {
        return Component.translatable(
                "quest.the_end_of_dragon."
                        + this.translationKey
                        + ".title"
        );
    }

    public Component getDescription() {
        return Component.translatable(
                "quest.the_end_of_dragon."
                        + this.translationKey
                        + ".description"
        );
    }

    public static TedVillageQuestDefinition fromId(
            TedVillageQuestId id
    ) {
        if (id == null) {
            return null;
        }

        for (TedVillageQuestDefinition definition
                : values()) {

            if (definition.id == id) {
                return definition;
            }
        }

        return null;
    }

    public static TedVillageQuestDefinition fromSerializedName(
            String serializedName
    ) {
        TedVillageQuestId id =
                TedVillageQuestId.fromSerializedName(
                        serializedName
                );

        return fromId(id);
    }

    public TedVillageQuest createQuest() {
        return new TedVillageQuest(
                this.id,
                this.requiredStage,
                this.completedStage,
                this.getTitle(),
                this.getDescription(),
                this.getPicture(),
                this.createRequirements(),
                this.createObjectives(),
                this.createRewards(),
                false,
                0
        );
    }

    /*
     * UIへ表示する必要素材。
     */
    private List<TedQuestItemRequirement>
    createRequirements() {
        return switch (this) {
            /*
             * 1. 水転送装置開発
             */
            case WATER_TRANSFER ->
                    List.of(
                            new TedQuestItemRequirement(
                                    Items.REDSTONE,
                                    32
                            ),
                            new TedQuestItemRequirement(
                                    Items.AMETHYST_SHARD,
                                    10
                            ),
                            new TedQuestItemRequirement(
                                    Items.LAPIS_LAZULI,
                                    10
                            )
                    );

            /*
             * 2. リコーラスメロン試作
             */
            case RECHORUS_MELON ->
                    List.of(
                            new TedQuestItemRequirement(
                                    Items.MELON_SEEDS,
                                    16
                            ),
                            new TedQuestItemRequirement(
                                    Items.CHORUS_FRUIT,
                                    16
                            )
                    );

            /*
             * 3. 成功したリコーラスメロンの種
             */
            case RECHORUS_MELON_SEED ->
                    List.of(
                            new TedQuestItemRequirement(
                                    ModItems.RECHORUS_MELON_SEED,
                                    4
                            )
                    );

            /*
             * 4. リコーラスプラント試作
             */
            case RECHORUS_PLANT ->
                    List.of(
                            new TedQuestItemRequirement(
                                    Items.MANGROVE_PROPAGULE,
                                    16,
                                    true
                            ),
                            new TedQuestItemRequirement(
                                    Items.CHORUS_FRUIT,
                                    16
                            )
                    );

            /*
             * 5. 施設設置はアイテム納品ではなく
             * ワールド内の設備状態で判定する。
             */
            case RECHORUS_FACILITY ->
                    List.of();
        };
    }

    /*
     * サーバー側で確認する達成条件。
     */
    private List<TedVillageQuestObjective>
    createObjectives() {
        return switch (this) {
            case WATER_TRANSFER ->
                    List.of(
                            new ItemDeliveryObjective(
                                    Items.REDSTONE,
                                    32
                            ),
                            new ItemDeliveryObjective(
                                    Items.AMETHYST_SHARD,
                                    10
                            ),
                            new ItemDeliveryObjective(
                                    Items.LAPIS_LAZULI,
                                    10
                            )
                    );

            case RECHORUS_MELON ->
                    List.of(
                            new ItemDeliveryObjective(
                                    Items.MELON_SEEDS,
                                    16
                            ),
                            new ItemDeliveryObjective(
                                    Items.CHORUS_FRUIT,
                                    16
                            )
                    );

            case RECHORUS_MELON_SEED ->
                    List.of(
                            new ItemDeliveryObjective(
                                    ModItems.RECHORUS_MELON_SEED,
                                    4
                            )
                    );

            case RECHORUS_PLANT ->
                    List.of(
                            new ItemDeliveryObjective(
                                    Items.MANGROVE_PROPAGULE,
                                    16
                            ),
                            new ItemDeliveryObjective(
                                    Items.CHORUS_FRUIT,
                                    16
                            )
                    );

            case RECHORUS_FACILITY ->
                    List.of(
                            new RechorusFacilityObjective()
                    );
        };
    }

    /*
     * クエスト完了時にプレイヤーへ渡すもの。
     */
    private List<TedQuestItemReward>
    createRewards() {
        return switch (this) {
            /*
             * 装置A・Bと指南書。
             */
            case WATER_TRANSFER ->
                    List.of(
                            new TedQuestItemReward(
                                    ModItems
                                            .WATER_TRANSFER_MACHINE_A,
                                    1
                            ),
                            new TedQuestItemReward(
                                    ModItems
                                            .WATER_TRANSFER_MACHINE_B,
                                    1
                            ),
                            new TedQuestItemReward(
                                    ModItems
                                            .ENDER_PEARL_GUIDE_BOOK,
                                    1
                            )
                    );

            /*
             * 試作リコーラスメロン種と応用書。
             */
            case RECHORUS_MELON ->
                    List.of(
                            new TedQuestItemReward(
                                    ModItems
                                            .RECHORUS_MELON_SEED_PROTOTYPE,
                                    16
                            ),
                            new TedQuestItemReward(
                                    ModItems
                                            .ENDER_PEARL_APPLICATION_BOOK,
                                    1
                            )
                    );

            /*
             * 第3段階では上級書。
             */
            case RECHORUS_MELON_SEED ->
                    List.of(
                            new TedQuestItemReward(
                                    ModItems
                                            .ENDER_PEARL_ADVANCED_BOOK,
                                    1
                            )
                    );

            /*
             * 試作苗木と熟達書。
             */
            case RECHORUS_PLANT ->
                    List.of(
                            new TedQuestItemReward(
                                    ModItems.RECHORUS_PLANT_SEED,
                                    16
                            ),
                            new TedQuestItemReward(
                                    ModItems
                                            .ENDER_PEARL_MASTERY_BOOK,
                                    1
                            )
                    );

            /*
             * 施設完成で極意書。
             */
            case RECHORUS_FACILITY ->
                    List.of(
                            new TedQuestItemReward(
                                    ModItems
                                            .ENDER_PEARL_SECRET_BOOK,
                                    1
                            )
                    );
        };
    }
}