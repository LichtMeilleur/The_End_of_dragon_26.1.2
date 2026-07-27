package com.licht_meilleur.the_end_of_dragon.world.fluid;

import com.licht_meilleur.the_end_of_dragon.registry.ModBlocks;
import com.licht_meilleur.the_end_of_dragon.registry.ModFluids;
import com.licht_meilleur.the_end_of_dragon.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

import net.minecraft.world.level.material.WaterFluid;



public abstract class RechorusJuiceFluid
        extends FlowingFluid {

    /*
     * 水より少しだけ粘度を高くする設定。
     *
     * 水は通常5tick程度。
     * 値を大きくすると流れが遅くなる。
     */
    private static final int FLOW_TICK_DELAY =
            7;

    /*
     * 水平方向へ流れを探す距離。
     */
    private static final int SLOPE_FIND_DISTANCE =
            4;

    /*
     * 1ブロック流れるごとの水量減少。
     */
    private static final int DROP_OFF =
            1;

    /*
     * 無限水源のような自己増殖を許可するか。
     *
     * 果汁水を簡単に無限化させないためfalse。
     */
    private static final boolean CAN_CONVERT_TO_SOURCE =
            false;

    private final Supplier<? extends Fluid>
            sourceSupplier;

    private final Supplier<? extends Fluid>
            flowingSupplier;

    protected RechorusJuiceFluid(
            Supplier<? extends Fluid> sourceSupplier,
            Supplier<? extends Fluid> flowingSupplier
    ) {
        this.sourceSupplier =
                sourceSupplier;

        this.flowingSupplier =
                flowingSupplier;
    }

    @Override
    public Fluid getFlowing() {
        return this.flowingSupplier.get();
    }

    @Override
    public Fluid getSource() {
        return this.sourceSupplier.get();
    }

    @Override
    public boolean isSame(
            Fluid fluid
    ) {
        return fluid == this.getSource()
                || fluid == this.getFlowing();
    }

    @Override
    public Item getBucket() {
        return ModItems.RECHORUS_JUICE_BUCKET;
    }

    @Nullable
    @Override
    public ParticleOptions getDripParticle() {
        /*
         * 後で専用の黄色い雫パーティクルへ変更可能。
         */
        return ParticleTypes.DRIPPING_HONEY;
    }

    @Override
    protected boolean canConvertToSource(
            ServerLevel level
    ) {
        return CAN_CONVERT_TO_SOURCE;
    }

    @Override
    protected void beforeDestroyingBlock(
            LevelAccessor level,
            BlockPos position,
            BlockState state
    ) {
        BlockEntity blockEntity =
                state.hasBlockEntity()
                        ? level.getBlockEntity(position)
                        : null;

        Block.dropResources(
                state,
                level,
                position,
                blockEntity
        );
    }

    @Override
    protected int getSlopeFindDistance(
            LevelReader level
    ) {
        return SLOPE_FIND_DISTANCE;
    }

    @Override
    public int getDropOff(
            LevelReader level
    ) {
        return DROP_OFF;
    }

    @Override
    public int getTickDelay(
            LevelReader level
    ) {
        return FLOW_TICK_DELAY;
    }

    @Override
    public boolean canBeReplacedWith(
            FluidState state,
            BlockGetter level,
            BlockPos position,
            Fluid incomingFluid,
            Direction direction
    ) {
        /*
         * 上や横から別の流体に簡単に押し潰されない。
         * 下方向からの流入だけ許可する。
         */
        return direction == Direction.DOWN
                && !isSame(incomingFluid);
    }

    @Override
    protected float getExplosionResistance() {
        return 100.0F;
    }

    @Override
    public Optional<SoundEvent> getPickupSound() {
        return Optional.of(
                SoundEvents.BUCKET_FILL
        );
    }

    @Override
    protected BlockState createLegacyBlock(
            FluidState state
    ) {
        return ModBlocks.RECHORUS_JUICE
                .defaultBlockState()
                .setValue(
                        LiquidBlock.LEVEL,
                        getLegacyLevel(state)
                );
    }

    /*
     * エンダーマンに水ダメージを与えないため、
     * entityInside()では水由来の処理を呼ばない。
     *
     * 将来、プレイヤーへ空間系効果を与える場合は
     * ここへ追加できる。
     */



    public static class Flowing
            extends RechorusJuiceFluid {

        public Flowing(
                Supplier<? extends Fluid> sourceSupplier,
                Supplier<? extends Fluid> flowingSupplier
        ) {
            super(
                    sourceSupplier,
                    flowingSupplier
            );
        }

        @Override
        public Fluid getFlowing() {
            return this;
        }

        @Override
        protected void createFluidStateDefinition(
                StateDefinition.Builder<
                        Fluid,
                        FluidState> builder
        ) {
            super.createFluidStateDefinition(
                    builder
            );

            builder.add(LEVEL);
        }

        @Override
        public int getAmount(
                FluidState state
        ) {
            return state.getValue(
                    LEVEL
            );
        }

        @Override
        public boolean isSource(
                FluidState state
        ) {
            return false;
        }
    }

    public static class Source
            extends RechorusJuiceFluid {

        public Source(
                Supplier<? extends Fluid> sourceSupplier,
                Supplier<? extends Fluid> flowingSupplier
        ) {
            super(
                    sourceSupplier,
                    flowingSupplier
            );
        }

        @Override
        public Fluid getSource() {
            return this;
        }

        @Override
        public int getAmount(
                FluidState state
        ) {
            return 8;
        }

        @Override
        public boolean isSource(
                FluidState state
        ) {
            return true;
        }
    }

    public static boolean isRechorusJuice(
            FluidState fluidState
    ) {
        return fluidState != null
                && !fluidState.isEmpty()
                && isRechorusJuice(
                fluidState.getType()
        );
    }

    public static boolean isRechorusJuice(
            Fluid fluid
    ) {
        return fluid
                == ModFluids.RECHORUS_JUICE_SOURCE
                || fluid
                == ModFluids.RECHORUS_JUICE_FLOWING;
    }
}