package com.licht_meilleur.the_end_of_dragon.network;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record TedSelectQuestPayload(
        String questId
) implements CustomPacketPayload {

    public static final Type<TedSelectQuestPayload> TYPE =
            new Type<>(
                    TheEndOfDragon.id(
                            "select_village_quest"
                    )
            );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            TedSelectQuestPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    TedSelectQuestPayload::questId,

                    TedSelectQuestPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}