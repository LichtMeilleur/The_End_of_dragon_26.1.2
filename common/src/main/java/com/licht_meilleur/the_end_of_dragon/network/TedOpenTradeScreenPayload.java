package com.licht_meilleur.the_end_of_dragon.network;

import com.licht_meilleur.the_end_of_dragon
        .TheEndOfDragon;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common
        .custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public record TedOpenTradeScreenPayload(
        int technicianEntityId,
        int trustPoints,
        int trustCap,
        int trustLevel,
        List<TedTradeEntryData> trades
) implements CustomPacketPayload {

    private static final int MAX_NETWORK_TRADES =
            256;

    public static final Type<
            TedOpenTradeScreenPayload
            > TYPE =
            new Type<>(
                    Identifier.fromNamespaceAndPath(
                            TheEndOfDragon.MOD_ID,
                            "open_trade_screen"
                    )
            );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            TedOpenTradeScreenPayload
            > STREAM_CODEC =
            StreamCodec.of(
                    TedOpenTradeScreenPayload::encode,
                    TedOpenTradeScreenPayload::decode
            );

    public TedOpenTradeScreenPayload {
        trustPoints =
                Math.max(
                        0,
                        trustPoints
                );

        trustCap =
                Math.max(
                        0,
                        trustCap
                );

        trustLevel =
                Math.max(
                        0,
                        trustLevel
                );

        trades =
                trades == null
                        ? List.of()
                        : List.copyOf(trades);
    }

    private static void encode(
            RegistryFriendlyByteBuf buffer,
            TedOpenTradeScreenPayload payload
    ) {
        buffer.writeVarInt(
                payload.technicianEntityId()
        );

        buffer.writeVarInt(
                payload.trustPoints()
        );

        buffer.writeVarInt(
                payload.trustCap()
        );

        buffer.writeVarInt(
                payload.trustLevel()
        );

        int tradeCount =
                Math.min(
                        payload.trades().size(),
                        MAX_NETWORK_TRADES
                );

        buffer.writeVarInt(
                tradeCount
        );

        for (int index = 0;
             index < tradeCount;
             index++) {

            TedTradeEntryData.STREAM_CODEC.encode(
                    buffer,
                    payload.trades().get(index)
            );
        }
    }

    private static TedOpenTradeScreenPayload decode(
            RegistryFriendlyByteBuf buffer
    ) {
        int technicianEntityId =
                buffer.readVarInt();

        int trustPoints =
                buffer.readVarInt();

        int trustCap =
                buffer.readVarInt();

        int trustLevel =
                buffer.readVarInt();

        int tradeCount =
                Math.clamp(
                        buffer.readVarInt(),
                        0,
                        MAX_NETWORK_TRADES
                );

        List<TedTradeEntryData> trades =
                new ArrayList<>(
                        tradeCount
                );

        for (int index = 0;
             index < tradeCount;
             index++) {

            trades.add(
                    TedTradeEntryData
                            .STREAM_CODEC
                            .decode(buffer)
            );
        }

        return new TedOpenTradeScreenPayload(
                technicianEntityId,
                trustPoints,
                trustCap,
                trustLevel,
                trades
        );
    }

    @Override
    public Type<
            ? extends CustomPacketPayload
            > type() {
        return TYPE;
    }
}