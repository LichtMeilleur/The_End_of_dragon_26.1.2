package com.licht_meilleur.the_end_of_dragon.entity.enderman.village;

import com.licht_meilleur.the_end_of_dragon.world.village
        .TedVillageQuestStage;
import com.licht_meilleur.the_end_of_dragon.world.village
        .TedVillageWorldState;
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

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }

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
         * 水転送研究が始まる前。
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
         * 現段階では取引画面未実装。
         *
         * 後でここを
         * TedVillageTradeManager.open(...)
         * などに置き換える。
         */
        serverPlayer.sendSystemMessage(
                Component.translatable(
                        "message.the_end_of_dragon.technician.preparing"
                ),
                false
        );

        return InteractionResult.SUCCESS;
    }
}