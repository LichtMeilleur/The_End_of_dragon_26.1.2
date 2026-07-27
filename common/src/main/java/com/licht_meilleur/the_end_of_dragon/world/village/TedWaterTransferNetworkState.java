package com.licht_meilleur.the_end_of_dragon.world.village;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.Map;

public final class TedWaterTransferNetworkState
        extends SavedData {

    public record FluidChannelData(
            String fluidId,
            long amount,
            String ownerDimension,
            long ownerPosition
    ) {
    }

    /*
     * チャンネル1つに保存できる最大水量。
     */
    public static final long CHANNEL_CAPACITY =
            64_000L;

    private final Map<
            String,
            FluidChannelData>
            channels =
            new HashMap<>();


    private static final Codec<FluidChannelData>
            CHANNEL_CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.STRING.fieldOf("fluid")
                                    .forGetter(FluidChannelData::fluidId),

                            Codec.LONG.fieldOf("amount")
                                    .forGetter(FluidChannelData::amount),

                            Codec.STRING.fieldOf("owner_dimension")
                                    .forGetter(FluidChannelData::ownerDimension),

                            Codec.LONG.fieldOf("owner_position")
                                    .forGetter(FluidChannelData::ownerPosition)
                    ).apply(instance, FluidChannelData::new)
            );

    private static final Codec<
            TedWaterTransferNetworkState>
            CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.unboundedMap(
                                            Codec.STRING,
                                            CHANNEL_CODEC
                                    )
                                    .optionalFieldOf(
                                            "channels",
                                            Map.of()
                                    )
                                    .forGetter(
                                            state ->
                                                    state.channels
                                    )
                    ).apply(
                            instance,
                            TedWaterTransferNetworkState::new
                    )
            );

    private static final SavedDataType<
            TedWaterTransferNetworkState> TYPE =
            new SavedDataType<>(
                    Identifier.fromNamespaceAndPath(
                            TheEndOfDragon.MOD_ID,
                            "ted_water_transfer_network"
                    ),
                    TedWaterTransferNetworkState::new,
                    CODEC,
                    null
            );

    public TedWaterTransferNetworkState() {
    }

    private TedWaterTransferNetworkState(
            Map<String, FluidChannelData> channels
    ) {
        this.channels.putAll(channels);
    }

    public static TedWaterTransferNetworkState get(
            ServerLevel level
    ) {
        if (level == null) {
            throw new IllegalArgumentException(
                    "ServerLevel must not be null"
            );
        }

        return get(
                level.getServer()
        );
    }

    public static TedWaterTransferNetworkState get(
            MinecraftServer server
    ) {
        if (server == null) {
            throw new IllegalArgumentException(
                    "MinecraftServer must not be null"
            );
        }

        return server.overworld()
                .getDataStorage()
                .computeIfAbsent(
                        TYPE
                );
    }

    public long getStoredAmount(
            String channelName
    ) {
        String channel =
                normalizeChannelName(
                        channelName
                );

        /*
         * 未設定チャンネルはネットワークを参照しない。
         */
        if (channel.isBlank()) {
            return 0L;
        }

        FluidChannelData data =
                this.channels.get(
                        channel
                );

        if (data == null) {
            return 0L;
        }

        return Math.max(
                0L,
                data.amount()
        );
    }

    public Fluid getFluid(
            String channelName
    ) {
        String channel =
                normalizeChannelName(
                        channelName
                );

        /*
         * 未設定チャンネルには液体が存在しない。
         */
        if (channel.isBlank()) {
            return Fluids.EMPTY;
        }

        FluidChannelData data =
                this.channels.get(
                        channel
                );

        if (data == null
                || data.fluidId() == null
                || data.fluidId().isBlank()) {
            return Fluids.EMPTY;
        }

        Identifier fluidIdentifier =
                parseIdentifier(
                        data.fluidId()
                );

        if (fluidIdentifier == null) {
            return Fluids.EMPTY;
        }

        Fluid fluid =
                BuiltInRegistries.FLUID.getValue(
                        fluidIdentifier
                );

        return fluid != null
                ? fluid
                : Fluids.EMPTY;
    }

    public boolean consumeFluid(
            String channelName,
            long amount
    ) {
        String channel =
                normalizeChannelName(
                        channelName
                );

        if (channel.isBlank()) {
            return false;
        }

        FluidChannelData current =
                this.channels.get(channel);

        if (current == null) {
            return false;
        }

        if (current.amount() < amount) {
            return false;
        }

        long remaining =
                current.amount()
                        - amount;

        if (remaining <= 0L) {

            this.channels.remove(
                    channel
            );

        } else {

            this.channels.put(
                    channel,
                    new FluidChannelData(
                            current.fluidId(),
                            remaining,
                            current.ownerDimension(),
                            current.ownerPosition()
                    )
            );
        }

        this.setDirty();

        return true;
    }

    private static Identifier parseIdentifier(
            String value
    ) {
        if (value == null
                || value.isBlank()) {
            return null;
        }

        String trimmed =
                value.trim();

        int separatorIndex =
                trimmed.indexOf(':');

        /*
         * namespace:path形式でない場合は、
         * Minecraft名前空間として扱う。
         */
        if (separatorIndex < 0) {
            return Identifier.fromNamespaceAndPath(
                    "minecraft",
                    trimmed
            );
        }

        if (separatorIndex == 0
                || separatorIndex
                >= trimmed.length() - 1) {
            return null;
        }

        String namespace =
                trimmed.substring(
                        0,
                        separatorIndex
                );

        String path =
                trimmed.substring(
                        separatorIndex + 1
                );

        try {
            return Identifier.fromNamespaceAndPath(
                    namespace,
                    path
            );
        } catch (IllegalArgumentException exception) {
            TheEndOfDragon.LOGGER.warn(
                    "Invalid fluid identifier stored in transfer channel: {}",
                    value
            );

            return null;
        }
    }


    public long addFluid(
            String channelName,
            Fluid fluid,
            long amount,
            ResourceKey<Level> ownerDimension,
            BlockPos ownerPosition
    ) {
        if (amount <= 0L) {
            return 0L;
        }

        String channel =
                normalizeChannelName(
                        channelName
                );

        if (channel.isBlank()) {
            return 0L;
        }

        String fluidId =
                BuiltInRegistries.FLUID
                        .getKey(fluid)
                        .toString();

        FluidChannelData current =
                this.channels.get(channel);

        if (current == null) {

            long accepted =
                    Math.min(
                            amount,
                            CHANNEL_CAPACITY
                    );

            this.channels.put(
                    channel,
                    new FluidChannelData(
                            fluidId,
                            accepted,
                            ownerDimension
                                    .identifier()
                                    .toString(),
                            ownerPosition.asLong()
                    )
            );

            this.setDirty();

            return accepted;
        }

        /*
         * 液体が違うなら拒否。
         */
        if (!current.fluidId().equals(
                fluidId
        )) {
            return 0L;
        }

        long accepted =
                Math.min(
                        amount,
                        CHANNEL_CAPACITY
                                - current.amount()
                );

        if (accepted <= 0L) {
            return 0L;
        }

        this.channels.put(
                channel,
                new FluidChannelData(
                        current.fluidId(),
                        current.amount()
                                + accepted,
                        current.ownerDimension(),
                        current.ownerPosition()
                )
        );

        this.setDirty();

        return accepted;
    }



    public static String normalizeChannelName(
            String channelName
    ) {
        if (channelName == null
                || channelName.isBlank()) {
            return "";
        }

        String normalized =
                channelName.trim();

        /*
         * 保存データやGUI表示が極端に長くならないよう制限。
         */
        if (normalized.length() > 32) {
            normalized =
                    normalized.substring(
                            0,
                            32
                    );
        }

        return normalized;
    }

    public boolean removeChannelIfOwnedBy(
            String channelName,
            ResourceKey<Level> dimension,
            BlockPos ownerPosition
    ) {
        String channel =
                normalizeChannelName(
                        channelName
                );

        FluidChannelData data =
                this.channels.get(channel);

        if (data == null) {
            return false;
        }

        if (!data.ownerDimension().equals(
                dimension.identifier()
                        .toString()
        )) {
            return false;
        }

        if (data.ownerPosition()
                != ownerPosition.asLong()) {
            return false;
        }

        this.channels.remove(channel);

        this.setDirty();

        return true;
    }
}