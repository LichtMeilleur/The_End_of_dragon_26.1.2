package com.licht_meilleur.the_end_of_dragon.world.village.trade;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public record TedVillageTradeIngredient(
        Ingredient ingredient,
        int count,
        ItemStack displayStack
) {

    public TedVillageTradeIngredient {
        if (ingredient == null) {
            throw new IllegalArgumentException(
                    "Trade ingredient must not be null"
            );
        }

        if (count <= 0) {
            throw new IllegalArgumentException(
                    "Trade ingredient count must be positive"
            );
        }

        if (displayStack == null
                || displayStack.isEmpty()) {
            throw new IllegalArgumentException(
                    "Trade display stack must not be empty"
            );
        }

        displayStack =
                displayStack.copyWithCount(
                        count
                );
    }

    /*
     * 特定のアイテムを素材にする。
     */
    public static TedVillageTradeIngredient of(
            ItemStack stack
    ) {
        if (stack == null
                || stack.isEmpty()) {
            throw new IllegalArgumentException(
                    "Trade stack must not be empty"
            );
        }

        return new TedVillageTradeIngredient(
                Ingredient.of(
                        stack.getItem()
                ),
                stack.getCount(),
                stack
        );
    }

    /*
     * 指定タグに含まれるアイテムすべてを
     * 素材として受け付ける。
     *
     * displayStackは取引画面に表示する代表アイテム。
     */
    public static TedVillageTradeIngredient ofTag(
            TagKey<Item> tag,
            int count,
            ItemStack displayStack
    ) {
        if (tag == null) {
            throw new IllegalArgumentException(
                    "Trade ingredient tag must not be null"
            );
        }

        if (count <= 0) {
            throw new IllegalArgumentException(
                    "Trade ingredient count must be positive"
            );
        }

        if (displayStack == null
                || displayStack.isEmpty()) {
            throw new IllegalArgumentException(
                    "Trade tag display stack must not be empty"
            );
        }

        return new TedVillageTradeIngredient(
                Ingredient.of(
                        BuiltInRegistries.ITEM
                                .getOrThrow(
                                        tag
                                )
                ),
                count,
                displayStack
        );
    }

    public boolean matches(
            ItemStack stack
    ) {
        return stack != null
                && !stack.isEmpty()
                && stack.getCount() >= this.count
                && this.ingredient.test(stack);
    }

    @Override
    public ItemStack displayStack() {
        return this.displayStack.copy();
    }
}