package com.licht_meilleur.the_end_of_dragon.item;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.registry.ModDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public final class TrueEnderPearlLevel {

    public static final int MIN_LEVEL =
            1;

    public static final int MAX_LEVEL =
            5;

    public static int get(
            ItemStack stack
    ) {
        if (stack == null
                || stack.isEmpty()
                || ModDataComponents
                .TRUE_ENDER_PEARL_LEVEL == null) {

            return MIN_LEVEL;
        }

        int savedLevel =
                stack.getOrDefault(
                        ModDataComponents
                                .TRUE_ENDER_PEARL_LEVEL,
                        MIN_LEVEL
                );

        return Mth.clamp(
                savedLevel,
                MIN_LEVEL,
                MAX_LEVEL
        );
    }

    public static void set(
            ItemStack stack,
            int level
    ) {
        if (stack == null
                || stack.isEmpty()
                || ModDataComponents
                .TRUE_ENDER_PEARL_LEVEL == null) {
            return;
        }

        int safeLevel =
                Mth.clamp(
                        level,
                        MIN_LEVEL,
                        MAX_LEVEL
                );

        /*
         * 能力判定用のレベル。
         */
        stack.set(
                ModDataComponents
                        .TRUE_ENDER_PEARL_LEVEL,
                safeLevel
        );

        /*
         * 表示画像をランクに合わせて変更する。
         *
         * assets/the_end_of_dragon/items/
         * true_ender_pearl_1.json ～ 5.json
         * を参照する。
         */
        stack.set(
                DataComponents.ITEM_MODEL,
                TheEndOfDragon.id(
                        "true_ender_pearl_"
                                + safeLevel
                )
        );
    }

    public static boolean isAtLeast(
            ItemStack stack,
            int requiredLevel
    ) {
        return get(stack)
                >= requiredLevel;
    }

    public static boolean canUseOffhand(
            ItemStack stack
    ) {
        return isAtLeast(
                stack,
                2
        );
    }

    public static boolean canCarryPlayers(
            ItemStack stack
    ) {
        return isAtLeast(
                stack,
                3
        );
    }

    public static boolean preventsFallDamage(
            ItemStack stack
    ) {
        return isAtLeast(
                stack,
                4
        );
    }

    public static boolean hasNoCooldown(
            ItemStack stack
    ) {
        return isAtLeast(
                stack,
                5
        );
    }

    private TrueEnderPearlLevel() {
    }
}