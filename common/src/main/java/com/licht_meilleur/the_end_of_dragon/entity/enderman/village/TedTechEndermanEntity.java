package com.licht_meilleur.the_end_of_dragon.entity.enderman.village;

import net.minecraft.network.chat.Component;
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
        super(entityType, level);
    }

    @Override
    protected InteractionResult mobInteract(
            Player player,
            InteractionHand hand
    ) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }

        /*
         * 取引画面実装後に、
         * ここを独自Menuのopen処理へ変更する。
         */
        serverPlayer.sendSystemMessage(
                Component.translatable(
                        "message.the_end_of_dragon.technician.not_ready"
                )
        );

        return InteractionResult.SUCCESS;
    }
}