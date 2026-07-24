package com.licht_meilleur.the_end_of_dragon.world.village.quest;

import com.licht_meilleur.the_end_of_dragon.network.TedNetwork;
import com.licht_meilleur.the_end_of_dragon.network.TedQuestListEntryData;
import com.licht_meilleur.the_end_of_dragon.world.dimension
        .TedDimensions;
import com.licht_meilleur.the_end_of_dragon.world.village
        .TedVillageQuestStage;
import com.licht_meilleur.the_end_of_dragon.world.village
        .TedVillageWorldState;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class TedVillageQuestManager {

    public static TedVillageQuest getCurrentMainQuest(
            ServerLevel villageLevel
    ) {
        if (villageLevel == null
                || !villageLevel.dimension()
                .equals(
                        TedDimensions.ENDERMAN_VILLAGE
                )) {
            return null;
        }

        TedVillageWorldState state =
                TedVillageWorldState.get(
                        villageLevel
                );

        return TedVillageQuestRegistry
                .getMainQuestForStage(
                        state.getVillageQuest()
                );
    }

    public static boolean canCompleteCurrentQuest(
            ServerPlayer player,
            ServerLevel villageLevel
    ) {
        return canCompleteQuest(
                player,
                villageLevel,
                getCurrentMainQuest(
                        villageLevel
                )
        );
    }

    public static boolean completeCurrentQuest(
            ServerPlayer player,
            ServerLevel villageLevel
    ) {
        if (player == null
                || villageLevel == null) {
            return false;
        }

        TedVillageQuest quest =
                getCurrentMainQuest(
                        villageLevel
                );

        if (quest == null) {
            player.sendSystemMessage(
                    Component.translatable(
                            "message.the_end_of_dragon.quest.none"
                    ),
                    false
            );

            return false;
        }

        TedVillageWorldState state =
                TedVillageWorldState.get(
                        villageLevel
                );

        TedVillageQuestStage currentStage =
                state.getVillageQuest();

        if (!currentStage.isAtLeast(
                quest.requiredStage()
        )) {
            return false;
        }

        TedVillageQuestContext context =
                new TedVillageQuestContext(
                        villageLevel,
                        state
                );

        for (TedVillageQuestObjective objective
                : quest.objectives()) {

            if (!objective.isComplete(
                    player,
                    context
            )) {
                player.sendSystemMessage(
                        Component.translatable(
                                "message.the_end_of_dragon.quest.incomplete"
                        ),
                        false
                );

                return false;
            }
        }

        /*
         * 条件確認後に納品物を消費する。
         */
        for (TedVillageQuestObjective objective
                : quest.objectives()) {

            if (objective
                    instanceof ItemDeliveryObjective delivery) {

                boolean removed =
                        removeItem(
                                player,
                                delivery.item(),
                                delivery.count()
                        );

                if (!removed) {
                    return false;
                }
            }
        }

        for (TedQuestItemReward reward
                : quest.rewards()) {

            ItemStack rewardStack =
                    new ItemStack(
                            reward.item(),
                            reward.count()
                    );

            boolean inserted =
                    player.getInventory()
                            .add(
                                    rewardStack
                            );

            if (!inserted
                    || !rewardStack.isEmpty()) {

                player.drop(
                        rewardStack,
                        false
                );
            }
        }

        if (quest.completionStage() != null) {
            state.setVillageQuestStage(
                    quest.completionStage()
            );
        }

        player.sendSystemMessage(
                Component.translatable(
                        "message.the_end_of_dragon.quest.completed",
                        quest.title()
                ),
                false
        );

        return true;
    }

    public static void showCurrentQuest(
            ServerPlayer player,
            ServerLevel villageLevel
    ) {
        if (player == null
                || villageLevel == null) {
            return;
        }

        TedVillageQuest quest =
                getCurrentMainQuest(
                        villageLevel
                );

        if (quest == null) {
            player.sendSystemMessage(
                    Component.translatable(
                            "message.the_end_of_dragon.quest.none"
                    ),
                    false
            );

            return;
        }

        player.sendSystemMessage(
                Component.literal("§6")
                        .append(
                                quest.title()
                        ),
                false
        );

        player.sendSystemMessage(
                quest.description(),
                false
        );

        if (canCompleteCurrentQuest(
                player,
                villageLevel
        )) {
            player.sendSystemMessage(
                    Component.translatable(
                            "message.the_end_of_dragon.quest.ready"
                    ),
                    false
            );
        } else {
            player.sendSystemMessage(
                    Component.translatable(
                            "message.the_end_of_dragon.quest.not_ready"
                    ),
                    false
            );
        }
    }

    private static boolean removeItem(
            ServerPlayer player,
            Item item,
            int requestedCount
    ) {
        if (item == null
                || requestedCount <= 0) {
            return false;
        }

        int remaining =
                requestedCount;

        for (int slot = 0;
             slot < player.getInventory()
                     .getContainerSize();
             slot++) {

            ItemStack stack =
                    player.getInventory()
                            .getItem(slot);

            if (!stack.is(item)) {
                continue;
            }

            int removeCount =
                    Math.min(
                            remaining,
                            stack.getCount()
                    );

            stack.shrink(
                    removeCount
            );

            remaining -=
                    removeCount;

            if (remaining <= 0) {
                player.getInventory()
                        .setChanged();

                return true;
            }
        }

        return false;
    }

    public static void openCurrentQuestLetter(
            ServerPlayer player,
            ServerLevel villageLevel
    ) {
        if (player == null
                || villageLevel == null) {
            return;
        }

        TedVillageQuest quest =
                getCurrentMainQuest(
                        villageLevel
                );

        if (quest == null) {
            player.sendSystemMessage(
                    Component.translatable(
                            "message.the_end_of_dragon.quest.none"
                    ),
                    false
            );

            return;
        }

        boolean completable =
                canCompleteCurrentQuest(
                        player,
                        villageLevel
                );

        TedNetwork.sendOpenQuestLetter(
                player,
                quest.id().getSerializedName(),
                completable
        );
    }

    public static boolean submitQuest(
            ServerPlayer player,
            String submittedQuestId
    ) {
        if (player == null
                || submittedQuestId == null
                || submittedQuestId.isBlank()) {
            return false;
        }

        ServerLevel playerLevel =
                player.level();

        if (playerLevel.getServer() == null) {
            return false;
        }

        ServerLevel villageLevel =
                playerLevel.getServer()
                        .getLevel(
                                TedDimensions.ENDERMAN_VILLAGE
                        );

        if (villageLevel == null) {
            return false;
        }

        TedVillageQuest currentQuest =
                getCurrentMainQuest(
                        villageLevel
                );

        if (currentQuest == null) {
            return false;
        }

        if (!currentQuest.id()
                .getSerializedName()
                .equals(
                        submittedQuestId
                )) {
            return false;
        }

        boolean completed =
                completeCurrentQuest(
                        player,
                        villageLevel
                );

        if (completed) {
            openCurrentQuestLetter(
                    player,
                    villageLevel
            );
        }

        return completed;
    }

    public static List<TedVillageQuest>
    getAvailableQuests(
            ServerPlayer player,
            ServerLevel villageLevel,
            TedQuestNpc npc
    ) {
        if (player == null
                || villageLevel == null
                || npc == null) {
            return List.of();
        }

        TedVillageWorldState state =
                TedVillageWorldState.get(
                        villageLevel
                );

        List<TedVillageQuest> result =
                new ArrayList<>();

        /*
         * 現在のメインクエスト。
         */
        TedVillageQuest mainQuest =
                TedVillageQuestRegistry
                        .getMainQuestForStage(
                                state.getVillageQuest()
                        );

        if (mainQuest != null) {
            TedVillageQuestDefinition definition =
                    TedVillageQuestDefinition.fromId(
                            mainQuest.id()
                    );

            if (definition != null
                    && definition.getNpc() == npc) {
                result.add(mainQuest);
            }
        }

        /*
         * 後で反復・サブクエストを追加する場所。
         *
         * Definition.values()から、
         * type != MAINかつ条件を満たすものを追加する。
         */

        return List.copyOf(result);
    }

    public static void openQuestList(
            ServerPlayer player,
            ServerLevel villageLevel,
            TedQuestNpc npc
    ) {
        if (player == null
                || villageLevel == null
                || npc == null) {
            return;
        }

        List<TedVillageQuest> quests =
                getAvailableQuests(
                        player,
                        villageLevel,
                        npc
                );

        List<TedQuestListEntryData> entries =
                new ArrayList<>();

        for (TedVillageQuest quest : quests) {
            boolean completable =
                    canCompleteQuest(
                            player,
                            villageLevel,
                            quest
                    );

            entries.add(
                    new TedQuestListEntryData(
                            quest.id()
                                    .getSerializedName(),
                            completable
                    )
            );
        }

        TedNetwork.sendOpenQuestList(
                player,
                entries
        );
    }

    public static boolean canCompleteQuest(
            ServerPlayer player,
            ServerLevel villageLevel,
            TedVillageQuest quest
    ) {
        if (player == null
                || villageLevel == null
                || quest == null) {
            return false;
        }

        TedVillageWorldState state =
                TedVillageWorldState.get(
                        villageLevel
                );

        TedVillageQuestContext context =
                new TedVillageQuestContext(
                        villageLevel,
                        state
                );

        for (TedVillageQuestObjective objective
                : quest.objectives()) {

            if (!objective.isComplete(
                    player,
                    context
            )) {
                return false;
            }
        }

        return true;
    }

    public static boolean selectQuest(
            ServerPlayer player,
            String selectedQuestId
    ) {
        if (player == null
                || selectedQuestId == null
                || selectedQuestId.isBlank()) {
            return false;
        }

        ServerLevel playerLevel =
                player.level();

        ServerLevel villageLevel =
                playerLevel.getServer()
                        .getLevel(
                                TedDimensions.ENDERMAN_VILLAGE
                        );

        if (villageLevel == null) {
            return false;
        }

        TedVillageQuest selectedQuest =
                TedVillageQuestRegistry
                        .getBySerializedName(
                                selectedQuestId
                        );

        if (selectedQuest == null) {
            return false;
        }

        /*
         * サーバー側でも、現在表示可能なクエストか確認。
         */
        List<TedVillageQuest> available =
                getAvailableQuests(
                        player,
                        villageLevel,
                        TedQuestNpc.ELDER
                );

        boolean allowed =
                available.stream()
                        .anyMatch(
                                quest ->
                                        quest.id()
                                                == selectedQuest.id()
                        );

        if (!allowed) {
            return false;
        }

        boolean completable =
                canCompleteQuest(
                        player,
                        villageLevel,
                        selectedQuest
                );

        TedNetwork.sendOpenQuestLetter(
                player,
                selectedQuest.id()
                        .getSerializedName(),
                completable
        );

        return true;
    }

    private TedVillageQuestManager() {
    }
}