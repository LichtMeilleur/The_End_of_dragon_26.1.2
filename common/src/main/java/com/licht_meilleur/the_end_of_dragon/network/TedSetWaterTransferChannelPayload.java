package com.licht_meilleur.the_end_of_dragon.network;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record TedSetWaterTransferChannelPayload(
        BlockPos machinePosition,
        String channelName
) implements CustomPacketPayload {

    public static final Type<
            TedSetWaterTransferChannelPayload> TYPE =
            new Type<>(
                    TheEndOfDragon.id(
                            "set_water_transfer_channel"
                    )
            );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            TedSetWaterTransferChannelPayload>
            STREAM_CODEC =
            StreamCodec.of(
                    TedSetWaterTransferChannelPayload::write,
                    TedSetWaterTransferChannelPayload::read
            );

    private static void write(
            RegistryFriendlyByteBuf buffer,
            TedSetWaterTransferChannelPayload payload
    ) {
        buffer.writeBlockPos(
                payload.machinePosition()
        );

        buffer.writeUtf(
                payload.channelName(),
                32
        );
    }

    private static TedSetWaterTransferChannelPayload read(
            RegistryFriendlyByteBuf buffer
    ) {
        return new TedSetWaterTransferChannelPayload(
                buffer.readBlockPos(),
                buffer.readUtf(32)
        );
    }

    @Override
    public Type<
            ? extends CustomPacketPayload> type() {
        return TYPE;
    }
}