package com.licht_meilleur.the_end_of_dragon.network;

import com.licht_meilleur.the_end_of_dragon
        .TheEndOfDragon;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common
        .custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record TedExecuteTradePayload(
        int technicianEntityId,
        String tradeId
) implements CustomPacketPayload {

    public static final Type<
            TedExecuteTradePayload
            > TYPE =
            new Type<>(
                    Identifier.fromNamespaceAndPath(
                            TheEndOfDragon.MOD_ID,
                            "execute_village_trade"
                    )
            );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            TedExecuteTradePayload
            > STREAM_CODEC =
            StreamCodec.of(
                    TedExecuteTradePayload::encode,
                    TedExecuteTradePayload::decode
            );

    public TedExecuteTradePayload {
        tradeId =
                tradeId == null
                        ? ""
                        : tradeId;
    }

    private static void encode(
            RegistryFriendlyByteBuf buffer,
            TedExecuteTradePayload payload
    ) {
        buffer.writeVarInt(
                payload.technicianEntityId()
        );

        buffer.writeUtf(
                payload.tradeId()
        );
    }

    private static TedExecuteTradePayload decode(
            RegistryFriendlyByteBuf buffer
    ) {
        return new TedExecuteTradePayload(
                buffer.readVarInt(),
                buffer.readUtf()
        );
    }

    @Override
    public Type<
            ? extends CustomPacketPayload
            > type() {
        return TYPE;
    }
}