package com.licht_meilleur.the_end_of_dragon.world.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.BaseEntityBlock;

public class EndermanVillageReturnGatewayBlock
        extends EndermanVillageGatewayBlock {

    public static final MapCodec<
            EndermanVillageReturnGatewayBlock> CODEC =
            simpleCodec(
                    EndermanVillageReturnGatewayBlock::new
            );

    public EndermanVillageReturnGatewayBlock(
            Properties properties
    ) {
        super(properties);
    }

    @Override
    protected MapCodec<
            ? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean registersAsReturnDestination() {
        return false;
    }
}