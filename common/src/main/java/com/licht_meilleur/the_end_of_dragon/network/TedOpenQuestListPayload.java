package com.licht_meilleur.the_end_of_dragon.network;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

public record TedOpenQuestListPayload(
        List<TedQuestListEntryData> quests
) implements CustomPacketPayload {

    public static final Type<TedOpenQuestListPayload> TYPE =
            new Type<>(
                    TheEndOfDragon.id(
                            "open_quest_list"
                    )
            );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            TedOpenQuestListPayload> STREAM_CODEC =
            StreamCodec.composite(
                    TedQuestListEntryData.STREAM_CODEC
                            .apply(
                                    ByteBufCodecs.list()
                            ),
                    TedOpenQuestListPayload::quests,

                    TedOpenQuestListPayload::new
            );

    public TedOpenQuestListPayload {
        quests =
                quests == null
                        ? List.of()
                        : List.copyOf(quests);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}