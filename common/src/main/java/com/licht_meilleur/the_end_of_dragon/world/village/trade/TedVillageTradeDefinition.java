package com.licht_meilleur.the_end_of_dragon.world.village.trade;

import com.licht_meilleur.the_end_of_dragon.network
        .TedTradeEntryData;
import com.licht_meilleur.the_end_of_dragon.network
        .TedTradeIngredientData;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record TedVillageTradeDefinition(
        String id,
        int requiredTrustLevel,
        List<ItemStack> ingredients,
        ItemStack result
) {

    public static final int MAX_INGREDIENTS =
            4;

    public TedVillageTradeDefinition {
        if (id == null
                || id.isBlank()) {
            throw new IllegalArgumentException(
                    "Trade id must not be blank"
            );
        }

        requiredTrustLevel =
                Math.max(
                        0,
                        requiredTrustLevel
                );

        if (ingredients == null
                || ingredients.isEmpty()) {
            throw new IllegalArgumentException(
                    "Trade must have at least one ingredient: "
                            + id
            );
        }

        if (ingredients.size()
                > MAX_INGREDIENTS) {
            throw new IllegalArgumentException(
                    "Trade has more than "
                            + MAX_INGREDIENTS
                            + " ingredients: "
                            + id
            );
        }

        ingredients =
                ingredients.stream()
                        .filter(
                                stack ->
                                        stack != null
                                                && !stack.isEmpty()
                                                && stack.getCount() > 0
                        )
                        .map(ItemStack::copy)
                        .toList();

        if (ingredients.isEmpty()) {
            throw new IllegalArgumentException(
                    "Trade has no valid ingredients: "
                            + id
            );
        }

        if (result == null
                || result.isEmpty()) {
            throw new IllegalArgumentException(
                    "Trade result must not be empty: "
                            + id
            );
        }

        result =
                result.copy();
    }

    @Override
    public List<ItemStack> ingredients() {
        return this.ingredients.stream()
                .map(ItemStack::copy)
                .toList();
    }

    @Override
    public ItemStack result() {
        return this.result.copy();
    }

    public TedTradeEntryData toNetworkData() {
        return new TedTradeEntryData(
                this.id,
                this.requiredTrustLevel,
                this.ingredients.stream()
                        .map(
                                TedTradeIngredientData::new
                        )
                        .toList(),
                this.result
        );
    }
}