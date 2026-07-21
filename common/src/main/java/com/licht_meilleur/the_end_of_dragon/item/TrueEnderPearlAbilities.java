package com.licht_meilleur.the_end_of_dragon.item;

import com.licht_meilleur.the_end_of_dragon.registry
        .ModItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class TrueEnderPearlAbilities {

    public static boolean preventsFallDamage(
            Player player
    ) {
        if (player == null) {
            return false;
        }

        return preventsFallDamage(
                player.getItemInHand(
                        InteractionHand.MAIN_HAND
                )
        ) || preventsFallDamage(
                player.getItemInHand(
                        InteractionHand.OFF_HAND
                )
        );
    }

    private static boolean preventsFallDamage(
            ItemStack stack
    ) {
        return stack.is(
                ModItems.TRUE_ENDER_PEARL
        ) && TrueEnderPearlLevel
                .preventsFallDamage(
                        stack
                );
    }

    private TrueEnderPearlAbilities() {
    }
}