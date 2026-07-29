package com.licht_meilleur.the_end_of_dragon.network;

import com.licht_meilleur.the_end_of_dragon.world.village.trade
        .TedVillageTradeType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record TedTradeEntryData(
        String tradeId,
        TedVillageTradeType type,
        int requiredTrustLevel,
        List<TedTradeIngredientData> ingredients,
        ItemStack result
) {

    private static final int MAX_NETWORK_INGREDIENTS =
            4;

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            TedTradeEntryData
            > STREAM_CODEC =
            StreamCodec.of(
                    TedTradeEntryData::encode,
                    TedTradeEntryData::decode
            );

    public TedTradeEntryData {
        tradeId =
                tradeId == null
                        ? ""
                        : tradeId;

        type =
                type == null
                        ? TedVillageTradeType.NORMAL
                        : type;

        requiredTrustLevel =
                Math.max(
                        0,
                        requiredTrustLevel
                );

        ingredients =
                ingredients == null
                        ? List.of()
                        : List.copyOf(ingredients);

        result =
                result == null
                        ? ItemStack.EMPTY
                        : result.copy();
    }

    private static void encode(
            RegistryFriendlyByteBuf buffer,
            TedTradeEntryData data
    ) {
        buffer.writeUtf(
                data.tradeId()
        );

        buffer.writeVarInt(
                data.type().ordinal()
        );

        buffer.writeVarInt(
                data.requiredTrustLevel()
        );

        int ingredientCount =
                Math.min(
                        data.ingredients().size(),
                        MAX_NETWORK_INGREDIENTS
                );

        buffer.writeVarInt(
                ingredientCount
        );

        for (int index = 0;
             index < ingredientCount;
             index++) {

            TedTradeIngredientData
                    .STREAM_CODEC
                    .encode(
                            buffer,
                            data.ingredients()
                                    .get(index)
                    );
        }

        ItemStack.OPTIONAL_STREAM_CODEC.encode(
                buffer,
                data.result()
        );
    }

    private static TedTradeEntryData decode(
            RegistryFriendlyByteBuf buffer
    ) {
        String tradeId =
                buffer.readUtf();

        TedVillageTradeType type =
                TedVillageTradeType
                        .fromNetworkId(
                                buffer.readVarInt()
                        );

        int requiredTrustLevel =
                buffer.readVarInt();

        int ingredientCount =
                Math.clamp(
                        buffer.readVarInt(),
                        0,
                        MAX_NETWORK_INGREDIENTS
                );

        List<TedTradeIngredientData> ingredients =
                new ArrayList<>(
                        ingredientCount
                );

        for (int index = 0;
             index < ingredientCount;
             index++) {

            ingredients.add(
                    TedTradeIngredientData
                            .STREAM_CODEC
                            .decode(buffer)
            );
        }

        ItemStack result =
                ItemStack.OPTIONAL_STREAM_CODEC.decode(
                        buffer
                );

        return new TedTradeEntryData(
                tradeId,
                type,
                requiredTrustLevel,
                ingredients,
                result
        );
    }

    @Override
    public List<TedTradeIngredientData>
    ingredients() {
        return List.copyOf(
                this.ingredients
        );
    }

    @Override
    public ItemStack result() {
        return this.result.copy();
    }
}