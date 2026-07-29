package com.licht_meilleur.the_end_of_dragon.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record TedTradeIngredientData(
        ItemStack stack
) {

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            TedTradeIngredientData
            > STREAM_CODEC =
            StreamCodec.of(
                    TedTradeIngredientData::encode,
                    TedTradeIngredientData::decode
            );

    public TedTradeIngredientData {
        stack =
                stack == null
                        ? ItemStack.EMPTY
                        : stack.copy();
    }

    private static void encode(
            RegistryFriendlyByteBuf buffer,
            TedTradeIngredientData data
    ) {
        ItemStack.OPTIONAL_STREAM_CODEC.encode(
                buffer,
                data.stack()
        );
    }

    private static TedTradeIngredientData decode(
            RegistryFriendlyByteBuf buffer
    ) {
        return new TedTradeIngredientData(
                ItemStack.OPTIONAL_STREAM_CODEC.decode(
                        buffer
                )
        );
    }

    @Override
    public ItemStack stack() {
        return this.stack.copy();
    }
}