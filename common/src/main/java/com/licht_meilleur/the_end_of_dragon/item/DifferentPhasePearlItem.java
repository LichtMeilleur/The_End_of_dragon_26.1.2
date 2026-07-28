package com.licht_meilleur.the_end_of_dragon.item;

import com.licht_meilleur.the_end_of_dragon.world.phase
        .TedDifferentPhaseManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public final class DifferentPhasePearlItem
        extends Item {

    public DifferentPhasePearlItem(
            Properties properties
    ) {
        super(properties);
    }

    @Override
    public InteractionResult use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        if (!level.isClientSide()
                && player
                instanceof ServerPlayer serverPlayer) {

            TedDifferentPhaseManager
                    .togglePersistent(
                            serverPlayer
                    );

            player.getCooldowns()
                    .addCooldown(
                            player.getItemInHand(
                                    hand
                            ),
                            10
                    );
        }

        return InteractionResult.SUCCESS;
    }
}