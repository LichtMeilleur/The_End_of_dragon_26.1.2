package com.licht_meilleur.the_end_of_dragon.world.village.quest;

import com.licht_meilleur.the_end_of_dragon.world.village
        .TedVillageQuestStage;

import java.util.EnumMap;
import java.util.Map;

public final class TedVillageQuestRegistry {

    private static final Map<
            TedVillageQuestId,
            TedVillageQuest> QUESTS =
            new EnumMap<>(
                    TedVillageQuestId.class
            );

    private static boolean bootstrapped;

    public static void bootstrap() {
        if (bootstrapped) {
            return;
        }

        /*
         * ModItemsの登録・バインド完了後に
         * 呼ぶ必要がある。
         */
        for (TedVillageQuestDefinition definition
                : TedVillageQuestDefinition.values()) {

            TedVillageQuest quest =
                    definition.createQuest();

            QUESTS.put(
                    quest.id(),
                    quest
            );
        }

        bootstrapped = true;
    }

    public static TedVillageQuest getById(
            TedVillageQuestId id
    ) {
        if (id == null) {
            return null;
        }

        return QUESTS.get(id);
    }

    public static TedVillageQuest getBySerializedName(
            String serializedName
    ) {
        TedVillageQuestId id =
                TedVillageQuestId
                        .fromSerializedName(
                                serializedName
                        );

        return getById(id);
    }

    public static TedVillageQuest getMainQuestForStage(
            TedVillageQuestStage stage
    ) {
        if (stage == null) {
            return getQuest(
                    TedVillageQuestDefinition
                            .WATER_TRANSFER
            );
        }

        return switch (stage) {
            /*
             * 現在の段階がNOT_STARTEDなら、
             * 第1クエストを表示。
             */
            case NOT_STARTED ->
                    getQuest(
                            TedVillageQuestDefinition
                                    .WATER_TRANSFER
                    );

            /*
             * 第1クエスト完了後。
             */
            case WATER_TRANSFER_RESEARCH ->
                    getQuest(
                            TedVillageQuestDefinition
                                    .RECHORUS_MELON
                    );

            /*
             * 第2クエスト完了後。
             */
            case RECHORUS_MELON_PROTOTYPE ->
                    getQuest(
                            TedVillageQuestDefinition
                                    .RECHORUS_MELON_SEED
                    );

            /*
             * 第3クエスト完了後。
             */
            case RECHORUS_MELON_SEED_DELIVERY ->
                    getQuest(
                            TedVillageQuestDefinition
                                    .RECHORUS_PLANT
                    );

            /*
             * 第4クエスト完了後から、
             * 設備完成までは第5クエスト。
             */
            case RECHORUS_PLANT_PROTOTYPE,
                 RECHORUS_PLANT_CORE_DELIVERY,
                 FACILITY_CONSTRUCTION_AVAILABLE,
                 MACHINE_B_INSTALLED,
                 PLANT_CORE_INSTALLED ->
                    getQuest(
                            TedVillageQuestDefinition
                                    .RECHORUS_FACILITY
                    );

            /*
             * 施設完成後は現在のメインクエストなし。
             */
            case RECHORUS_PLANT_BUILT,
                 MACHINE_A_CONNECTED,
                 JUICE_WATER_PRODUCED,
                 COMPLETED ->
                    null;
        };
    }

    private static TedVillageQuest getQuest(
            TedVillageQuestDefinition definition
    ) {
        if (definition == null) {
            return null;
        }

        return getById(
                definition.getId()
        );
    }

    private TedVillageQuestRegistry() {
    }
}