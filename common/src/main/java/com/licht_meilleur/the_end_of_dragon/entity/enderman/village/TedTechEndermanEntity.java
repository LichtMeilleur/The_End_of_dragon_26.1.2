package com.licht_meilleur.the_end_of_dragon.entity.enderman.village;

import com.licht_meilleur.the_end_of_dragon.world.village
        .TedVillageQuestStage;
import com.licht_meilleur.the_end_of_dragon.world.village
        .TedVillageWorldState;
import com.licht_meilleur.the_end_of_dragon.world.village.trade.TedVillageTradeManager;
import com.licht_meilleur.the_end_of_dragon.world.village.trade.TedVillageTradeMenuOpener;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class TedTechEndermanEntity
        extends TedVillageEndermanEntity {

    public TedTechEndermanEntity(
            EntityType<? extends PathfinderMob> entityType,
            Level level
    ) {
        super(
                entityType,
                level
        );
    }

    @Override
    protected InteractionResult mobInteract(
            Player player,
            InteractionHand hand
    ) {
        /*
         * オフハンドによる二重実行を防ぐ。
         */
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        /*
         * クライアント側では処理せず、
         * サーバーからMenuを開く。
         */
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }

        /*
         * 村のクエスト進行度を取得する。
         */
        if (!(this.level()
                instanceof ServerLevel villageLevel)) {

            return InteractionResult.PASS;
        }

        TedVillageWorldState villageState =
                TedVillageWorldState.get(
                        villageLevel
                );

        TedVillageQuestStage stage =
                villageState.getVillageQuest();

        /*
         * 水転送研究が始まるまでは
         * 技術者との取引を利用できない。
         */
        if (!stage.isAtLeast(
                TedVillageQuestStage
                        .WATER_TRANSFER_RESEARCH
        )) {
            serverPlayer.sendSystemMessage(
                    Component.translatable(
                            "message.the_end_of_dragon.technician.locked"
                    ),
                    false
            );

            return InteractionResult.SUCCESS;
        }

        /*
         * 解禁済みなら、
         * サーバー管理の取引Menuを開く。
         */
        TedVillageTradeManager.open(
                serverPlayer,
                this
        );

        return InteractionResult.SUCCESS;
    }
}