package com.licht_meilleur.the_end_of_dragon.network;

import com.licht_meilleur.the_end_of_dragon
        .TheEndOfDragon;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common
        .custom.CustomPacketPayload;

import java.util.UUID;

public record TedDifferentPhaseSyncPayload(
        UUID playerId,
        boolean persistent,
        int temporaryTicks
) implements CustomPacketPayload {

    public static final Type<
            TedDifferentPhaseSyncPayload> TYPE =
            new Type<>(
                    TheEndOfDragon.id(
                            "different_phase_sync"
                    )
            );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            TedDifferentPhaseSyncPayload> STREAM_CODEC =
            new StreamCodec<>() {

                @Override
                public TedDifferentPhaseSyncPayload decode(
                        RegistryFriendlyByteBuf buffer
                ) {
                    UUID playerId =
                            buffer.readUUID();

                    boolean persistent =
                            buffer.readBoolean();

                    int temporaryTicks =
                            buffer.readVarInt();

                    return new TedDifferentPhaseSyncPayload(
                            playerId,
                            persistent,
                            temporaryTicks
                    );
                }

                @Override
                public void encode(
                        RegistryFriendlyByteBuf buffer,
                        TedDifferentPhaseSyncPayload payload
                ) {
                    buffer.writeUUID(
                            payload.playerId()
                    );

                    buffer.writeBoolean(
                            payload.persistent()
                    );

                    buffer.writeVarInt(
                            payload.temporaryTicks()
                    );
                }
            };

    public boolean active() {
        return this.persistent
                || this.temporaryTicks > 0;
    }

    @Override
    public Type<
            ? extends CustomPacketPayload> type() {
        return TYPE;
    }
}