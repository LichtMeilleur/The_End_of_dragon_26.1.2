package com.licht_meilleur.the_end_of_dragon.world.village.trade;

import com.licht_meilleur.the_end_of_dragon.entity.enderman.village
        .TedTechEndermanEntity;
import com.licht_meilleur.the_end_of_dragon.network
        .TedOpenTradeScreenPayload;
import com.licht_meilleur.the_end_of_dragon.network
        .TedTradeEntryData;
import com.licht_meilleur.the_end_of_dragon.network
        .TedVillageTradeNetwork;
import com.licht_meilleur.the_end_of_dragon.world.village
        .TedVillageWorldState;
import com.licht_meilleur.the_end_of_dragon.world.village.trust
        .TedVillageTrustManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class TedVillageTradeManager {

    private static final double
            MAX_INTERACTION_DISTANCE_SQUARED =
            8.0D * 8.0D;

    private static final long
            EXECUTE_COOLDOWN_TICKS =
            4L;

    private static final Map<UUID, Long>
            LAST_EXECUTE_TICK =
            new HashMap<>();

    private TedVillageTradeManager() {
    }

    public static void open(
            ServerPlayer player,
            TedTechEndermanEntity technician
    ) {
        if (!isValidInteraction(
                player,
                technician
        )) {
            return;
        }

        /*
         * ItemStackを安全に作成できる、
         * 実際の取引開始時に一度だけ登録する。
         */
        TedVillageTradeRegistry.bootstrap();

        /*
         * 先にContainerMenuを開く。
         */
        TedVillageTradeMenuOpener.open(
                player,
                technician
        );

        /*
         * 続いて取引一覧・信頼度を送る。
         */
        sendScreenData(
                player,
                technician
        );
    }

    public static void handleExecuteRequest(
            ServerPlayer player,
            int technicianEntityId,
            String tradeId
    ) {
        if (player == null
                || tradeId == null
                || tradeId.isBlank()) {
            return;
        }

        Entity entity =
                player.level()
                        .getEntity(
                                technicianEntityId
                        );

        if (!(entity
                instanceof TedTechEndermanEntity technician)) {
            return;
        }

        if (!isValidInteraction(
                player,
                technician
        )) {
            return;
        }

        if (!(player.containerMenu
                instanceof TedVillageTradeMenu menu)) {
            return;
        }

        if (menu.getTechnicianEntityId()
                != technicianEntityId) {
            return;
        }

        TedVillageTradeDefinition trade =
                TedVillageTradeRegistry.get(
                        tradeId
                );

        if (trade == null) {
            return;
        }

        TedVillageTradeDefinition
                selectedTrade =
                menu.getSelectedTrade();

        if (selectedTrade == null
                || !selectedTrade.id()
                .equals(trade.id())) {
            return;
        }

        ServerLevel villageLevel =
                (ServerLevel) technician.level();

        TedVillageWorldState villageState =
                TedVillageWorldState.get(
                        villageLevel
                );

        int trustLevel =
                villageState.getTrustLevel(
                        player.getUUID()
                );

        if (!trade.isUnlocked(
                trustLevel
        )) {
            player.sendSystemMessage(
                    Component.translatable(
                            "message.the_end_of_dragon.trade.locked",
                            trade.requiredTrustStage()
                                    .getDisplayLevel()
                    ),
                    false
            );

            return;
        }

        long currentTick =
                villageLevel.getGameTime();

        Long lastTick =
                LAST_EXECUTE_TICK.get(
                        player.getUUID()
                );

        if (lastTick != null
                && currentTick - lastTick
                < EXECUTE_COOLDOWN_TICKS) {
            return;
        }

        Container inputContainer =
                menu.getInputContainer();

        int inputStart =
                trade.type()
                        == TedVillageTradeType.NORMAL
                        ? TedVillageTradeMenu
                          .TRADE_SLOT_START
                        : TedVillageTradeMenu
                          .WORK_BENCH_SLOT_START;

        if (!hasRequiredIngredients(
                inputContainer,
                inputStart,
                trade
        )) {
            player.sendSystemMessage(
                    Component.translatable(
                            "message.the_end_of_dragon.trade.missing_ingredients"
                    ),
                    false
            );

            return;
        }

        ItemStack result =
                trade.result();

        if (!canFitInInventory(
                player.getInventory(),
                result
        )) {
            player.sendSystemMessage(
                    Component.translatable(
                            "message.the_end_of_dragon.trade.inventory_full"
                    ),
                    false
            );

            return;
        }

        /*
         * ここから交換成立。
         */
        LAST_EXECUTE_TICK.put(
                player.getUUID(),
                currentTick
        );

        consumeIngredients(
                inputContainer,
                inputStart,
                trade
        );

        ItemStack reward =
                result.copy();

        boolean inserted =
                player.getInventory()
                        .add(
                                reward
                        );

        /*
         * 事前確認済みだが、
         * MOD干渉などで入らなかった場合の保険。
         */
        if (!inserted
                && !reward.isEmpty()) {

            player.drop(
                    reward,
                    false
            );
        }

        TedVillageTrustManager.rewardTrade(
                villageState,
                player.getUUID()
        );

        inputContainer.setChanged();
        menu.broadcastChanges();

        player.sendSystemMessage(
                Component.translatable(
                        "message.the_end_of_dragon.trade.success",
                        result.getHoverName()
                ),
                true
        );

        /*
         * 信頼度表示を更新する。
         */
        sendScreenData(
                player,
                technician
        );
    }

    private static boolean hasRequiredIngredients(
            Container container,
            int inputStart,
            TedVillageTradeDefinition trade
    ) {
        List<TedVillageTradeIngredient>
                requirements =
                trade.ingredients();

        for (int index = 0;
             index < requirements.size();
             index++) {

            ItemStack stack =
                    container.getItem(
                            inputStart + index
                    );

            if (!requirements
                    .get(index)
                    .matches(stack)) {
                return false;
            }
        }

        return true;
    }

    private static void consumeIngredients(
            Container container,
            int inputStart,
            TedVillageTradeDefinition trade
    ) {
        List<TedVillageTradeIngredient>
                requirements =
                trade.ingredients();

        for (int index = 0;
             index < requirements.size();
             index++) {

            container.removeItem(
                    inputStart + index,
                    requirements
                            .get(index)
                            .count()
            );
        }
    }

    private static boolean canFitInInventory(
            Inventory inventory,
            ItemStack result
    ) {
        int remaining =
                result.getCount();

        /*
         * メインインベントリ＋ホットバー。
         */
        for (int index = 0;
             index < 36;
             index++) {

            ItemStack current =
                    inventory.getItem(
                            index
                    );

            if (current.isEmpty()) {
                remaining -=
                        result.getMaxStackSize();
            } else if (ItemStack
                    .isSameItemSameComponents(
                            current,
                            result
                    )) {

                remaining -=
                        Math.max(
                                0,
                                current.getMaxStackSize()
                                        - current.getCount()
                        );
            }

            if (remaining <= 0) {
                return true;
            }
        }

        return false;
    }

    private static void sendScreenData(
            ServerPlayer player,
            TedTechEndermanEntity technician
    ) {
        ServerLevel villageLevel =
                (ServerLevel) technician.level();

        TedVillageWorldState villageState =
                TedVillageWorldState.get(
                        villageLevel
                );

        int trustPoints =
                villageState.getTrustPoints(
                        player.getUUID()
                );

        int trustCap =
                villageState.getTrustCap();

        int trustLevel =
                villageState.getTrustLevel(
                        player.getUUID()
                );

        List<TedTradeEntryData> trades =
                TedVillageTradeRegistry
                        .getAll()
                        .stream()
                        .map(
                                TedVillageTradeDefinition
                                        ::toNetworkData
                        )
                        .toList();

        TedVillageTradeNetwork.sendOpenScreen(
                player,
                new TedOpenTradeScreenPayload(
                        technician.getId(),
                        trustPoints,
                        trustCap,
                        trustLevel,
                        trades
                )
        );
    }

    private static boolean isValidInteraction(
            ServerPlayer player,
            TedTechEndermanEntity technician
    ) {
        if (player == null
                || technician == null
                || !player.isAlive()
                || !technician.isAlive()
                || player.isSpectator()) {
            return false;
        }

        if (!(technician.level()
                instanceof ServerLevel)) {
            return false;
        }

        if (player.level()
                != technician.level()) {
            return false;
        }

        return player.distanceToSqr(
                technician
        ) <= MAX_INTERACTION_DISTANCE_SQUARED;
    }
}