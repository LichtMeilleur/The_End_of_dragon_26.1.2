package com.licht_meilleur.the_end_of_dragon.network;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record TedOpenWaterTransferScreenPayload(
        BlockPos machinePosition,
        boolean machineA,
        String channelName,
        long storedWater
) implements CustomPacketPayload {

    public static final Type<
            TedOpenWaterTransferScreenPayload> TYPE =
            new Type<>(
                    TheEndOfDragon.id(
                            "open_water_transfer_screen"
                    )
            );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            TedOpenWaterTransferScreenPayload>
            STREAM_CODEC =
            StreamCodec.of(
                    TedOpenWaterTransferScreenPayload::write,
                    TedOpenWaterTransferScreenPayload::read
            );

    private static void write(
            RegistryFriendlyByteBuf buffer,
            TedOpenWaterTransferScreenPayload payload
    ) {
        buffer.writeBlockPos(
                payload.machinePosition()
        );

        buffer.writeBoolean(
                payload.machineA()
        );

        buffer.writeUtf(
                payload.channelName(),
                32
        );

        buffer.writeVarLong(
                Math.max(
                        0L,
                        payload.storedWater()
                )
        );
    }

    private static TedOpenWaterTransferScreenPayload read(
            RegistryFriendlyByteBuf buffer
    ) {
        return new TedOpenWaterTransferScreenPayload(
                buffer.readBlockPos(),
                buffer.readBoolean(),
                buffer.readUtf(32),
                buffer.readVarLong()
        );
    }

    @Override
    public Type<
            ? extends CustomPacketPayload> type() {
        return TYPE;
    }
}