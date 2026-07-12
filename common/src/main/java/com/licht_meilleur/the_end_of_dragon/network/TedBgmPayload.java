package com.licht_meilleur.the_end_of_dragon.network;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record TedBgmPayload(
        TedBgmCommand command
) implements CustomPacketPayload {

    public static final Type<TedBgmPayload> TYPE =
            new Type<>(
                    TheEndOfDragon.id("bgm_command")
            );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            TedBgmPayload
            > STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            payload -> payload.command().ordinal(),
            id -> new TedBgmPayload(commandById(id))
    );

    private static TedBgmCommand commandById(int id) {
        TedBgmCommand[] commands =
                TedBgmCommand.values();

        if (id < 0 || id >= commands.length) {
            return TedBgmCommand.STOP;
        }

        return commands[id];
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}