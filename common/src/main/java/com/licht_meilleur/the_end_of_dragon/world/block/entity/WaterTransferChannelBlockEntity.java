package com.licht_meilleur.the_end_of_dragon.world.block.entity;

import com.licht_meilleur.the_end_of_dragon.world.village
        .TedWaterTransferNetworkState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public abstract class WaterTransferChannelBlockEntity
        extends BlockEntity {

    private String channelName =
            "";

    protected WaterTransferChannelBlockEntity(
            BlockEntityType<?> type,
            BlockPos position,
            BlockState state
    ) {
        super(
                type,
                position,
                state
        );
    }

    public String getChannelName() {
        return this.channelName;
    }

    public void setChannelName(
            String channelName
    ) {
        String normalized =
                TedWaterTransferNetworkState
                        .normalizeChannelName(
                                channelName
                        );

        if (this.channelName.equals(
                normalized
        )) {
            return;
        }

        this.channelName =
                normalized;

        this.setChanged();

        if (this.level != null) {
            this.level.sendBlockUpdated(
                    this.worldPosition,
                    this.getBlockState(),
                    this.getBlockState(),
                    3
            );
        }
    }

    @Override
    protected void saveAdditional(
            ValueOutput output
    ) {
        super.saveAdditional(
                output
        );

        output.putString(
                "ChannelName",
                this.channelName
        );
    }

    @Override
    protected void loadAdditional(
            ValueInput input
    ) {
        super.loadAdditional(
                input
        );

        this.channelName =
                TedWaterTransferNetworkState
                        .normalizeChannelName(
                                input.getStringOr(
                                        "ChannelName",
                                        ""
                                )
                        );
    }
}