package com.licht_meilleur.the_end_of_dragon.network;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record TedOpenQuestLetterPayload(
        String questId,
        boolean completable
) implements CustomPacketPayload {

    public static final Type<TedOpenQuestLetterPayload> TYPE =
            new Type<>(
                    TheEndOfDragon.id(
                            "open_quest_letter"
                    )
            );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            TedOpenQuestLetterPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    TedOpenQuestLetterPayload::questId,

                    ByteBufCodecs.BOOL,
                    TedOpenQuestLetterPayload::completable,

                    TedOpenQuestLetterPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}