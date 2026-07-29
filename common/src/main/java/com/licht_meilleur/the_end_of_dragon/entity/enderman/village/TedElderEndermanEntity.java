package com.licht_meilleur.the_end_of_dragon.entity.enderman.village;

import com.licht_meilleur.the_end_of_dragon.world.village.quest.TedQuestNpc;
import com.licht_meilleur.the_end_of_dragon.world.village.quest
        .TedVillageQuestManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class TedElderEndermanEntity
        extends TedVillageEndermanEntity {

    public TedElderEndermanEntity(
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

        this.beginMenuInteraction(
                serverPlayer
        );

        TedVillageQuestManager.openQuestList(
                serverPlayer,
                villageLevel,
                TedQuestNpc.ELDER
        );

        return InteractionResult.SUCCESS;
    }
}