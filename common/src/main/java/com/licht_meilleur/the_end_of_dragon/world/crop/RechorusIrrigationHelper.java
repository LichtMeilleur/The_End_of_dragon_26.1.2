package com.licht_meilleur.the_end_of_dragon.world.crop;

import com.licht_meilleur.the_end_of_dragon.world.fluid
        .RechorusJuiceFluid;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.material.FluidState;

public final class RechorusIrrigationHelper {

    private static final int
            HORIZONTAL_RANGE =
            4;

    private static final int
            VERTICAL_RANGE =
            1;

    private RechorusIrrigationHelper() {
    }

    public static RechorusIrrigationType findIrrigation(
            LevelReader level,
            BlockPos farmlandPosition
    ) {
        boolean foundWater =
                false;

        BlockPos.MutableBlockPos searchPosition =
                new BlockPos.MutableBlockPos();

        for (int x = -HORIZONTAL_RANGE;
             x <= HORIZONTAL_RANGE;
             x++) {

            for (int z = -HORIZONTAL_RANGE;
                 z <= HORIZONTAL_RANGE;
                 z++) {

                for (int y = -VERTICAL_RANGE;
                     y <= VERTICAL_RANGE;
                     y++) {

                    searchPosition.setWithOffset(
                            farmlandPosition,
                            x,
                            y,
                            z
                    );

                    FluidState fluidState =
                            level.getFluidState(
                                    searchPosition
                            );

                    if (RechorusJuiceFluid
                            .isRechorusJuice(
                                    fluidState
                            )) {

                        return RechorusIrrigationType
                                .RECHORUS_JUICE;
                    }

                    if (fluidState.is(
                            FluidTags.WATER
                    )) {
                        foundWater = true;
                    }
                }
            }
        }

        return foundWater
                ? RechorusIrrigationType.WATER
                : RechorusIrrigationType.NONE;
    }
}