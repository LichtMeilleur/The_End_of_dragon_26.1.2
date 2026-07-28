package com.licht_meilleur.the_end_of_dragon.item;

import com.licht_meilleur.the_end_of_dragon.world.phase
        .TedDifferentPhaseManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class RechorusMelonCutItem
        extends Item {

    public RechorusMelonCutItem(
            Properties properties
    ) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(
            ItemStack stack,
            Level level,
            LivingEntity livingEntity
    ) {
        ItemStack result =
                super.finishUsingItem(
                        stack,
                        level,
                        livingEntity
                );

        if (!level.isClientSide()
                && livingEntity
                instanceof ServerPlayer player) {

            TedDifferentPhaseManager
                    .enterTemporary(
                            player,
                            TedDifferentPhaseManager
                                    .MELON_CUT_TICKS
                    );
        }

        return result;
    }
}