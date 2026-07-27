package com.licht_meilleur.the_end_of_dragon.entity;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.util.GeckoLibUtil;
import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.registry.ModBlocks;
import com.licht_meilleur.the_end_of_dragon.registry.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * リコーラス果汁一杯分を表すEntity。
 *
 * Minecraft標準の流体計算は使用せず、
 *
 * 1. 重力で落下
 * 2. 下が塞がっていれば横へ1ブロック移動
 * 3. 再び落下
 * 4. 動けない状態が40tick続いたら水源へ変換
 *
 * という独自処理を行う。
 */
public class RechorusJuiceBlobEntity
        extends Entity
        implements GeoEntity {



    private static final EntityDataAccessor<Integer> DATA_BLOB_STATE =
            SynchedEntityData.defineId(
                    RechorusJuiceBlobEntity.class,
                    EntityDataSerializers.INT
            );

    private static final int STATE_FALLING = 0;
    private static final int STATE_LANDING = 1;
    private static final int STATE_IDLE = 2;

    /*
     * landingアニメーションを表示する時間。
     * 実際のアニメーション尺に合わせて後から調整可能。
     */
    private static final int LANDING_ANIMATION_TICKS = 12;

    private static final RawAnimation IDLE_ANIMATION =
            RawAnimation.begin()
                    .thenLoop(
                            "animation.model.idle"
                    );

    private static final RawAnimation LANDING_ANIMATION =
            RawAnimation.begin()
                    .thenPlayAndHold(
                            "animation.model.landing"
                    );


    private static final int MAX_CONVERSION_RETRIES =
            5;

    private int conversionRetries;

    private int landingAnimationTicks;
    /**
     * 停止してから流体へ変換されるまでの時間。
     *
     * 20tick = 1秒。
     */
    private static final int CONVERT_DELAY_TICKS = 40;

    /**
     * 落下時に加える重力。
     */
    private static final double GRAVITY = 0.04D;

    /**
     * 最大落下速度。
     */
    private static final double MAX_FALL_SPEED = 0.45D;

    /**
     * 横へ移動した直後、連続移動しないための待機時間。
     */
    private static final int SIDE_MOVE_COOLDOWN_TICKS = 4;


    private final AnimatableInstanceCache animationCache =
            GeckoLibUtil.createInstanceCache(this);

    /**
     * 完全停止している時間。
     */
    private int stoppedTicks;

    /**
     * 横へ移動した直後のクールタイム。
     */
    private int sideMoveCooldown;

    public RechorusJuiceBlobEntity(
            EntityType<? extends RechorusJuiceBlobEntity> entityType,
            Level level
    ) {
        super(entityType, level);

        /*
         * 独自に重力処理を行うため、
         * Entity標準のnoGravityは使わない。
         */
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData(
            SynchedEntityData.Builder builder
    ) {
        builder.define(
                DATA_BLOB_STATE,
                STATE_FALLING
        );
    }

    public int getBlobState() {
        return this.entityData.get(
                DATA_BLOB_STATE
        );
    }

    private void setBlobState(
            int state
    ) {
        if (this.getBlobState() == state) {
            return;
        }

        this.entityData.set(
                DATA_BLOB_STATE,
                state
        );
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            return;
        }

        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (this.sideMoveCooldown > 0) {
            this.sideMoveCooldown--;
        }

        tickBlobMovement(serverLevel);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return false;
    }

    private void tickBlobMovement(
            ServerLevel serverLevel
    ) {
        /*
         * 着地アニメーション中。
         */
        if (this.getBlobState()
                == STATE_LANDING) {

            this.stopMovement();

            this.snapToBlockCenter();

            this.landingAnimationTicks++;

            if (this.landingAnimationTicks
                    >= LANDING_ANIMATION_TICKS) {

                this.setBlobState(
                        STATE_IDLE
                );

                this.landingAnimationTicks = 0;
            }

            return;
        }

        BlockPos currentPos =
                BlockPos.containing(
                        this.getX(),
                        this.getY(),
                        this.getZ()
                );

        BlockPos belowPos =
                currentPos.below();

        /*
         * 下へ進める場合。
         */
        if (canEnterBlock(belowPos)) {
            this.setBlobState(
                    STATE_FALLING
            );

            fallDown();
            resetStoppedState();
            return;
        }

        /*
         * ブロック上面へ接触するまで落下させる。
         */
        if (!this.onGround()
                && !this.verticalCollision) {

            this.setBlobState(
                    STATE_FALLING
            );

            fallDown();

            if (!this.onGround()
                    && !this.verticalCollision) {

                resetStoppedState();
                return;
            }
        }

        /*
         * 下へ落ちられなくなった瞬間。
         *
         * まず横へ流れられるか確認する。
         */
        if (this.sideMoveCooldown <= 0
                && tryMoveSideways(currentPos)) {

            this.setBlobState(
                    STATE_FALLING
            );

            resetStoppedState();

            this.sideMoveCooldown =
                    SIDE_MOVE_COOLDOWN_TICKS;

            return;
        }

        /*
         * 初めて完全停止した瞬間だけ、
         * landingアニメーションへ移行。
         */
        if (this.getBlobState()
                == STATE_FALLING) {

            this.stopMovement();

            this.setBlobState(
                    STATE_LANDING
            );

            this.landingAnimationTicks = 0;
            return;
        }

        /*
         * 着地アニメーション終了後の停止状態。
         */
        this.setBlobState(
                STATE_IDLE
        );

        stopMovement();

        this.stoppedTicks++;

        if (this.stoppedTicks
                >= CONVERT_DELAY_TICKS) {

            convertToJuiceSource(
                    serverLevel
            );
        }
    }

    private void fallDown() {
        Vec3 movement =
                this.getDeltaMovement();

        double nextY =
                Math.max(
                        movement.y - GRAVITY,
                        -MAX_FALL_SPEED
                );

        this.setDeltaMovement(
                0.0D,
                nextY,
                0.0D
        );

        this.move(
                MoverType.SELF,
                this.getDeltaMovement()
        );
    }

    /**
     * 北・南・東・西から移動可能な方向を探す。
     *
     * 条件：
     *
     * ・横のブロックへ入れる
     * ・横へ移動した先の下へ入れる
     */
    private boolean tryMoveSideways(
            BlockPos currentPos
    ) {
        List<Direction> directions =
                new ArrayList<>(
                        List.of(
                                Direction.NORTH,
                                Direction.SOUTH,
                                Direction.WEST,
                                Direction.EAST
                        )
                );

        /*
         * 毎回同じ方向を優先すると一方向へ偏るため、
         * EntityのRandomSourceで順番を変える。
         */
        shuffleDirections(
                directions,
                this.getRandom()
        );

        for (Direction direction : directions) {
            BlockPos sidePos =
                    currentPos.relative(
                            direction
                    );

            BlockPos sideBelowPos =
                    sidePos.below();

            if (!canEnterBlock(sidePos)) {
                continue;
            }

            if (!canEnterBlock(sideBelowPos)) {
                continue;
            }

            double targetX =
                    sidePos.getX() + 0.5D;

            double targetY =
                    sidePos.getY() + 0.05D;

            double targetZ =
                    sidePos.getZ() + 0.5D;

            double moveX =
                    targetX - this.getX();

            double moveY =
                    targetY - this.getY();

            double moveZ =
                    targetZ - this.getZ();

            /*
             * BlockStateだけでなく、
             * Entityの当たり判定としても移動可能か確認する。
             */
            if (!this.level().noCollision(
                    this,
                    this.getBoundingBox().move(
                            moveX,
                            moveY,
                            moveZ
                    )
            )) {
                continue;
            }

            this.setPos(
                    targetX,
                    targetY,
                    targetZ
            );

            /*
             * 横へ移動した時点では落下速度をリセット。
             * 次tickから再び重力落下する。
             */
            this.setDeltaMovement(
                    Vec3.ZERO
            );

            return true;
        }

        return false;
    }

    private static void shuffleDirections(
            List<Direction> directions,
            RandomSource random
    ) {
        /*
         * Collections.shuffleはjava.util.Randomを要求するため、
         * MinecraftのRandomSourceを使って手動でシャッフルする。
         */
        for (int index =
             directions.size() - 1;
             index > 0;
             index--) {

            int swapIndex =
                    random.nextInt(
                            index + 1
                    );

            Collections.swap(
                    directions,
                    index,
                    swapIndex
            );
        }
    }

    private boolean canEnterBlock(
            BlockPos position
    ) {
        if (!this.level().isInWorldBounds(
                position
        )) {
            return false;
        }

        BlockState state =
                this.level().getBlockState(
                        position
                );

        /*
         * リコーラス施設を構成するブロックには、
         * Blobを侵入させない。
         */
        if (isProtectedPlantBlock(
                state
        )) {
            return false;
        }

        /*
         * 現段階では安全のため、
         * 完全な空気ブロックだけ通過可能とする。
         *
         * 草・花・苗木などを破壊または置換しない。
         */
        return state.isAir();
    }

    private boolean isProtectedPlantBlock(
            BlockState state
    ) {
        return state.is(
                ModBlocks.RECHORUS_FLOWER
        )
                || state.is(
                ModBlocks.RECHORUS_PLANT_CORE
        )
                || state.is(
                ModBlocks.RECHORUS_PLANT
        )
                || state.is(
                ModBlocks.RECHORUS_ROOT
        );
    }

    private void stopMovement() {
        this.setDeltaMovement(
                Vec3.ZERO
        );
    }

    private void resetStoppedState() {
        this.stoppedTicks = 0;
    }

    private void convertToJuiceSource(
            ServerLevel serverLevel
    ) {
        BlockPos sourcePos =
                findConversionPosition(
                        serverLevel
                );

        if (sourcePos == null) {


            this.conversionRetries++;

            if (this.conversionRetries
                    >= MAX_CONVERSION_RETRIES) {

                TheEndOfDragon.LOGGER.warn(
                        "Discarded Rechorus Juice Blob after failed conversion attempts at {}",
                        this.blockPosition()
                );

                this.discard();
                return;
            }
            /*
             * 変換位置が見つからない場合は、
             * すぐ消さずに再移動を試す。
             */
            this.setBlobState(
                    STATE_FALLING
            );

            this.stoppedTicks = 0;
            this.landingAnimationTicks = 0;
            this.sideMoveCooldown = 0;

            /*
             * わずかに持ち上げて、
             * 横ずれ処理を再試行できるようにする。
             */
            this.setPos(
                    this.getX(),
                    this.getY() + 0.25D,
                    this.getZ()
            );

            this.setDeltaMovement(
                    Vec3.ZERO
            );

            return;
        }

        if (ModFluids.RECHORUS_JUICE_SOURCE
                == null) {

            TheEndOfDragon.LOGGER.error(
                    "Could not convert Rechorus Juice Blob: source fluid is not bound"
            );

            this.discard();
            return;
        }

        boolean placed =
                serverLevel.setBlock(
                        sourcePos,
                        ModFluids.RECHORUS_JUICE_SOURCE
                                .defaultFluidState()
                                .createLegacyBlock(),
                        3
                );

        if (!placed) {
            /*
             * setBlock自体が失敗した場合も、
             * すぐ削除せず再試行する。
             */
            this.setBlobState(
                    STATE_FALLING
            );

            this.stoppedTicks = 0;
            this.sideMoveCooldown = 0;

            this.setPos(
                    this.getX(),
                    this.getY() + 0.25D,
                    this.getZ()
            );

            return;
        }

        TheEndOfDragon.LOGGER.info(
                "Converted Rechorus Juice Blob into source fluid at {}",
                sourcePos
        );

        this.discard();
    }

    @Override
    protected void addAdditionalSaveData(
            net.minecraft.world.level.storage.ValueOutput output
    ) {
        output.putInt(
                "StoppedTicks",
                Math.max(
                        0,
                        this.stoppedTicks
                )
        );

        output.putInt(
                "SideMoveCooldown",
                Math.max(
                        0,
                        this.sideMoveCooldown
                )
        );

        output.putInt(
                "BlobState",
                this.getBlobState()
        );

        output.putInt(
                "LandingAnimationTicks",
                Math.max(
                        0,
                        this.landingAnimationTicks
                )
        );

        output.putInt(
                "ConversionRetries",
                Math.max(
                        0,
                        this.conversionRetries
                )
        );
    }

    @Override
    protected void readAdditionalSaveData(
            net.minecraft.world.level.storage.ValueInput input
    ) {
        this.stoppedTicks =
                Math.max(
                        0,
                        input.getIntOr(
                                "StoppedTicks",
                                0
                        )
                );

        this.sideMoveCooldown =
                Math.max(
                        0,
                        input.getIntOr(
                                "SideMoveCooldown",
                                0
                        )
                );

        int loadedState =
                input.getIntOr(
                        "BlobState",
                        STATE_FALLING
                );

        if (loadedState < STATE_FALLING
                || loadedState > STATE_IDLE) {

            loadedState = STATE_FALLING;
        }

        if (loadedState == STATE_LANDING) {
            loadedState = STATE_IDLE;
        }

        this.landingAnimationTicks =
                Math.max(
                        0,
                        input.getIntOr(
                                "LandingAnimationTicks",
                                0
                        )
                );

        this.conversionRetries =
                Math.max(
                        0,
                        input.getIntOr(
                                "ConversionRetries",
                                0
                        )
                );
    }

    private void snapToBlockCenter() {
        BlockPos position =
                BlockPos.containing(
                        this.getX(),
                        this.getY() + 0.1D,
                        this.getZ()
                );

        this.setPos(
                position.getX() + 0.5D,
                this.getY(),
                position.getZ() + 0.5D
        );

        this.setDeltaMovement(
                Vec3.ZERO
        );
    }

    @Override
    public void registerControllers(
            AnimatableManager.ControllerRegistrar controllers
    ) {
        controllers.add(
                new AnimationController<>(
                        "main_controller",
                        2,
                        animationTest -> {
                            int state =
                                    this.getBlobState();

                            if (state == STATE_LANDING) {
                                animationTest.controller()
                                        .setAnimation(
                                                LANDING_ANIMATION
                                        );

                                return PlayState.CONTINUE;
                            }

                            animationTest.controller()
                                    .setAnimation(
                                            IDLE_ANIMATION
                                    );

                            return PlayState.CONTINUE;
                        }
                )
        );
    }

    private BlockPos findConversionPosition(
            ServerLevel level
    ) {
        BlockPos origin =
                BlockPos.containing(
                        this.getX(),
                        this.getY() + 0.1D,
                        this.getZ()
                );

        /*
         * 最初に現在位置を確認。
         */
        if (canPlaceJuiceAt(
                level,
                origin
        )) {
            return origin;
        }

        /*
         * Entityが床へ少し食い込んでいた場合。
         */
        BlockPos above =
                origin.above();

        if (canPlaceJuiceAt(
                level,
                above
        )) {
            return above;
        }

        /*
         * 周囲4方向を確認。
         */
        Direction[] directions = {
                Direction.NORTH,
                Direction.SOUTH,
                Direction.WEST,
                Direction.EAST
        };

        for (Direction direction
                : directions) {

            BlockPos side =
                    origin.relative(
                            direction
                    );

            if (canPlaceJuiceAt(
                    level,
                    side
            )) {
                return side;
            }
        }

        /*
         * 周囲4方向の1マス上も確認。
         */
        for (Direction direction
                : directions) {

            BlockPos sideAbove =
                    origin.relative(
                            direction
                    ).above();

            if (canPlaceJuiceAt(
                    level,
                    sideAbove
            )) {
                return sideAbove;
            }
        }

        return null;
    }

    private boolean canPlaceJuiceAt(
            ServerLevel level,
            BlockPos position
    ) {
        if (!level.isInWorldBounds(
                position
        )) {
            return false;
        }

        BlockState state =
                level.getBlockState(
                        position
                );

        /*
         * 花やプラント構成ブロックは、
         * 絶対に果汁水へ置換しない。
         */
        if (isProtectedPlantBlock(
                state
        )) {
            return false;
        }

        /*
         * 草や別の植物も破壊しないため、
         * 空気だけを許可する。
         */
        if (!state.isAir()) {
            return false;
        }

        BlockPos belowPosition =
                position.below();

        BlockState belowState =
                level.getBlockState(
                        belowPosition
                );

        /*
         * 花やプラントを足場にして水源化すること自体は
         * 破壊を伴わないものの、
         * 見た目や再生処理への影響を避けるため禁止する。
         */
        if (isProtectedPlantBlock(
                belowState
        )) {
            return false;
        }

        return !belowState
                .getCollisionShape(
                        level,
                        belowPosition
                )
                .isEmpty();
    }

    @Override
    public AnimatableInstanceCache
    getAnimatableInstanceCache() {
        return this.animationCache;
    }
}