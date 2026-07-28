package com.licht_meilleur.the_end_of_dragon.item;

import com.licht_meilleur.the_end_of_dragon.world.phase
        .TedDifferentPhaseManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;

public final class RechorusJuiceBottleItem
        extends Item {

    public RechorusJuiceBottleItem(
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
        player.startUsingItem(hand);

        return InteractionResult.CONSUME;
    }

    @Override
    public int getUseDuration(
            ItemStack stack,
            LivingEntity entity
    ) {
        return 32;
    }

    @Override
    public ItemUseAnimation getUseAnimation(
            ItemStack stack
    ) {
        return ItemUseAnimation.DRINK;
    }

    @Override
    public ItemStack finishUsingItem(
            ItemStack stack,
            Level level,
            LivingEntity livingEntity
    ) {
        if (!level.isClientSide()
                && livingEntity
                instanceof ServerPlayer player) {

            TedDifferentPhaseManager
                    .enterTemporary(
                            player,
                            TedDifferentPhaseManager
                                    .JUICE_BOTTLE_TICKS
                    );
        }

        if (livingEntity
                instanceof Player player
                && player.getAbilities().instabuild) {
            return stack;
        }

        stack.shrink(1);

        if (stack.isEmpty()) {
            return new ItemStack(
                    Items.GLASS_BOTTLE
            );
        }

        if (livingEntity
                instanceof Player player) {
            player.getInventory()
                    .placeItemBackInInventory(
                            new ItemStack(
                                    Items.GLASS_BOTTLE
                            )
                    );
        }

        return stack;
    }
}