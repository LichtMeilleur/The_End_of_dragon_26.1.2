package com.licht_meilleur.the_end_of_dragon.world.village.trade;

import com.licht_meilleur.the_end_of_dragon.network
        .TedTradeEntryData;
import com.licht_meilleur.the_end_of_dragon.network
        .TedTradeIngredientData;
import com.licht_meilleur.the_end_of_dragon.world.village.trust
        .TedVillageTrustStage;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record TedVillageTradeDefinition(
        String id,
        TedVillageTradeType type,
        TedVillageTrustStage requiredTrustStage,
        List<TedVillageTradeIngredient> ingredients,
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

        if (type == null) {
            throw new IllegalArgumentException(
                    "Trade type must not be null: "
                            + id
            );
        }

        if (requiredTrustStage == null) {
            throw new IllegalArgumentException(
                    "Trust stage must not be null: "
                            + id
            );
        }

        if (ingredients == null
                || ingredients.isEmpty()) {
            throw new IllegalArgumentException(
                    "Trade must have ingredients: "
                            + id
            );
        }

        ingredients =
                List.copyOf(
                        ingredients
                );

        if (ingredients.size()
                > MAX_INGREDIENTS) {
            throw new IllegalArgumentException(
                    "Trade has too many ingredients: "
                            + id
            );
        }

        if (ingredients.size()
                > type.getInputSlotCount()) {
            throw new IllegalArgumentException(
                    "Trade "
                            + id
                            + " has more ingredients than "
                            + type
                            + " supports"
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

    public int requiredTrustLevel() {
        return this.requiredTrustStage
                .getRequiredInternalLevel();
    }

    public boolean isUnlocked(
            int currentTrustLevel
    ) {
        return this.requiredTrustStage
                .isUnlocked(
                        currentTrustLevel
                );
    }

    @Override
    public List<TedVillageTradeIngredient>
    ingredients() {
        return List.copyOf(
                this.ingredients
        );
    }

    @Override
    public ItemStack result() {
        return this.result.copy();
    }

    public TedTradeEntryData toNetworkData() {
        return new TedTradeEntryData(
                this.id,
                this.type,
                this.requiredTrustLevel(),
                this.ingredients.stream()
                        .map(
                                ingredient ->
                                        new TedTradeIngredientData(
                                                ingredient
                                                        .displayStack()
                                        )
                        )
                        .toList(),
                this.result
        );
    }
}