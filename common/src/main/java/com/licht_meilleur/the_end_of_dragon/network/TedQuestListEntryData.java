package com.licht_meilleur.the_end_of_dragon.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record TedQuestListEntryData(
        String questId,
        boolean completable
) {

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            TedQuestListEntryData> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    TedQuestListEntryData::questId,

                    ByteBufCodecs.BOOL,
                    TedQuestListEntryData::completable,

                    TedQuestListEntryData::new
            );
}