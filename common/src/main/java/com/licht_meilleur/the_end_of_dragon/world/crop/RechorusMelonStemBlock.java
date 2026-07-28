package com.licht_meilleur.the_end_of_dragon.world.crop;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.util.function.Supplier;

public final class RechorusMelonStemBlock
        extends CropBlock {

    public static final IntegerProperty AGE =
            BlockStateProperties.AGE_7;

    public static final BooleanProperty CONNECTED =
            BooleanProperty.create(
                    "connected"
            );

    public static final EnumProperty<Direction> FACING =
            BlockStateProperties.HORIZONTAL_FACING;

    private final RechorusSeedType seedType;

    private final Supplier<? extends Item>
            seedItemSupplier;

    private final Supplier<? extends Block>
            rechorusMelonSupplier;

    public RechorusMelonStemBlock(
            RechorusSeedType seedType,
            Supplier<? extends Item> seedItemSupplier,
            Supplier<? extends Block> rechorusMelonSupplier,
            BlockBehaviour.Properties properties
    ) {
        super(properties);

        this.seedType =
                seedType;

        this.seedItemSupplier =
                seedItemSupplier;

        this.rechorusMelonSupplier =
                rechorusMelonSupplier;

        this.registerDefaultState(
                this.stateDefinition
                        .any()
                        .setValue(
                                AGE,
                                0
                        )
                        .setValue(
                                CONNECTED,
                                false
                        )
                        .setValue(
                                FACING,
                                Direction.NORTH
                        )
        );
    }

    @Override
    public IntegerProperty getAgeProperty() {
        return AGE;
    }

    @Override
    public int getMaxAge() {
        return 7;
    }

    @Override
    protected boolean mayPlaceOn(
            BlockState floorState,
            BlockGetter level,
            BlockPos floorPosition
    ) {
        return floorState.is(
                Blocks.FARMLAND
        ) || floorState.is(
                com.licht_meilleur.the_end_of_dragon
                        .registry.ModBlocks
                        .RECHORUS_FARMLAND
        );
    }

    @Override
    protected void randomTick(
            BlockState state,
            ServerLevel level,
            BlockPos position,
            RandomSource random
    ) {



        /*
         * 接続中の茎について、
         * 接続先の実が残っているか確認する。
         */
        if (state.getValue(
                CONNECTED
        )) {
            Direction facing =
                    state.getValue(
                            FACING
                    );

            BlockState fruitState =
                    level.getBlockState(
                            position.relative(
                                    facing
                            )
                    );

            if (!isSupportedFruit(
                    fruitState
            )) {
                level.setBlock(
                        position,
                        state.setValue(
                                CONNECTED,
                                false
                        ),
                        Block.UPDATE_CLIENTS
                );
            }

            return;
        }

        /*
         * バニラ作物と同じ明るさ条件。
         */
        if (level.getRawBrightness(
                position,
                0
        ) < 9) {
            return;
        }

        int age =
                this.getAge(
                        state
                );

        if (age < this.getMaxAge()) {
            float growthSpeed =
                    getRechorusGrowthSpeed(
                            level,
                            position
                    );

            int growthBound =
                    (int) (
                            25.0F
                                    / growthSpeed
                    ) + 1;

            if (random.nextInt(
                    growthBound
            ) == 0) {
                level.setBlock(
                        position,
                        this.getStateForAge(
                                age + 1
                        ),
                        Block.UPDATE_CLIENTS
                );
            }

            return;
        }

        tryCreateFruit(
                level,
                position,
                state,
                random
        );
    }

    private void tryCreateFruit(
            ServerLevel level,
            BlockPos stemPosition,
            BlockState stemState,
            RandomSource random
    ) {


        Direction direction =
                Direction.Plane.HORIZONTAL
                        .getRandomDirection(
                                random
                        );

        BlockPos fruitPosition =
                stemPosition.relative(
                        direction
                );

        /*
         * 実を生成する場所が空気でなければ、
         * このtickでは生成しない。
         */
        if (!level.getBlockState(
                fruitPosition
        ).isAir()) {
            return;
        }

        BlockPos floorPosition =
                fruitPosition.below();

        BlockState floorState =
                level.getBlockState(
                        floorPosition
                );

        /*
         * バニラ系の土か農地の上へ生成する。
         */
        boolean suitableFloor =
                floorState.is(
                        BlockTags.DIRT
                )
                        || floorState.is(
                        Blocks.FARMLAND
                )
                        || floorState.is(
                        com.licht_meilleur.the_end_of_dragon
                                .registry.ModBlocks
                                .RECHORUS_FARMLAND
                );

        if (!suitableFloor) {
            return;
        }

        RechorusIrrigationType irrigationType =
                RechorusIrrigationHelper
                        .findIrrigation(
                                level,
                                stemPosition.below()
                        );

        float mutationChance =
                RechorusMutationChances
                        .getChance(
                                this.seedType,
                                irrigationType
                        );

        boolean rechorusMutation =
                random.nextFloat()
                        < mutationChance;

        Block fruitBlock =
                rechorusMutation
                        ? this.rechorusMelonSupplier
                        .get()
                        : Blocks.MELON;

        level.setBlockAndUpdate(
                fruitPosition,
                fruitBlock.defaultBlockState()
        );

        level.setBlock(
                stemPosition,
                stemState
                        .setValue(
                                AGE,
                                this.getMaxAge()
                        )
                        .setValue(
                                CONNECTED,
                                true
                        )
                        .setValue(
                                FACING,
                                direction
                        ),
                Block.UPDATE_CLIENTS
        );
    }

    private boolean isSupportedFruit(
            BlockState fruitState
    ) {
        return fruitState.is(
                this.rechorusMelonSupplier
                        .get()
        ) || fruitState.is(
                Blocks.MELON
        );
    }

    @Override
    protected ItemStack getCloneItemStack(
            LevelReader level,
            BlockPos position,
            BlockState state,
            boolean includeData
    ) {
        return new ItemStack(
                this.seedItemSupplier
                        .get()
        );
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<
                    Block,
                    BlockState> builder
    ) {
        builder.add(
                AGE,
                CONNECTED,
                FACING
        );
    }

    @Override
    protected boolean isRandomlyTicking(
            BlockState state
    ) {
        /*
         * 最大成長後も実の生成処理を行う必要がある。
         * 接続中も、実が破壊されたか確認する必要があるため、
         * 常にランダムティック対象とする。
         */
        return true;
    }

    private static float getRechorusGrowthSpeed(
            BlockGetter level,
            BlockPos stemPosition
    ) {
        float growthSpeed =
                1.0F;

        BlockPos soilCenter =
                stemPosition.below();

        for (int offsetX = -1;
             offsetX <= 1;
             offsetX++) {

            for (int offsetZ = -1;
                 offsetZ <= 1;
                 offsetZ++) {

                BlockPos soilPosition =
                        soilCenter.offset(
                                offsetX,
                                0,
                                offsetZ
                        );

                BlockState soilState =
                        level.getBlockState(
                                soilPosition
                        );

                float soilBonus =
                        0.0F;

                if (soilState.is(
                        Blocks.FARMLAND
                )) {
                    soilBonus =
                            1.0F;

                    if (soilState.getValue(
                            FarmlandBlock.MOISTURE
                    ) > 0) {
                        soilBonus =
                                3.0F;
                    }
                } else if (soilState.is(
                        ModBlocks.RECHORUS_FARMLAND
                )) {
                    /*
                     * リコーラス農地は、果汁水によって
                     * 維持されている前提で湿った農地相当。
                     */
                    soilBonus =
                            3.0F;
                }

                /*
                 * 茎の真下以外の周囲8マスは、
                 * バニラと同様に効果を4分の1にする。
                 */
                if (offsetX != 0
                        || offsetZ != 0) {
                    soilBonus /=
                            4.0F;
                }

                growthSpeed +=
                        soilBonus;
            }
        }

        return growthSpeed;
    }
}