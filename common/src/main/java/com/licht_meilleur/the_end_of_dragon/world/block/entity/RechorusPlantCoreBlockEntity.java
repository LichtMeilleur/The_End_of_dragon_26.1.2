package com.licht_meilleur.the_end_of_dragon.world.block.entity;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.entity.RechorusJuiceBlobEntity;
import com.licht_meilleur.the_end_of_dragon.registry.ModBlockEntities;
import com.licht_meilleur.the_end_of_dragon.registry.ModBlocks;
import com.licht_meilleur.the_end_of_dragon.registry.ModEntities;
import com.licht_meilleur.the_end_of_dragon.world.block.RechorusFlowerBlock;
import com.licht_meilleur.the_end_of_dragon.world.village.TedRechorusPlantManager;
import com.licht_meilleur.the_end_of_dragon.world.village.TedRechorusTreePlacer;
import com.licht_meilleur.the_end_of_dragon.world.village.TedVillageWorldState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class RechorusPlantCoreBlockEntity
        extends net.minecraft.world.level.block.entity.BlockEntity {

    /*
     * ========================================
     * 水・成長設定
     * ========================================
     */

    /*
     * BlockEntityの基本処理間隔。
     *
     * 20tick = 1秒。
     */
    private static final int UPDATE_INTERVAL_TICKS =
            20;

    /*
     * 生産処理間隔。
     *
     * 200tick = 10秒。
     */
    private static final int PRODUCTION_INTERVAL_TICKS =
            200;

    /*
     * コア内部の最大水量。
     */
    public static final int WATER_CAPACITY =
            16_000;

    /*
     * 水に触れている場合、1秒ごとに吸収する量。
     *
     * 水源ブロック自体は削除しない。
     */
    private static final int WATER_PER_ABSORPTION =
            250;

    /*
     * 初回成長に必要な水量。
     */
    private static final int WATER_REQUIRED_TO_GROW =
            4_000;

    /*
     * 1回の果汁生産に必要な水量。
     */
    private static final int WATER_PER_PRODUCTION_CYCLE =
            100;

    /*
     * 花1個が1回に作る果汁量。
     */
    private static final int JUICE_PER_FLOWER =
            25;

    /*
     * Blob1個へ変換する果汁量。
     */
    private static final int JUICE_PER_BLOB =
            1_000;

    /*
     * 1回の生産処理で生成するBlob数。
     */
    private static final int MAX_BLOBS_PER_CYCLE =
            1;

    /*
     * DOWNは台座があるため除外。
     */
    private static final Direction[] WATER_DIRECTIONS = {
            Direction.UP,
            Direction.NORTH,
            Direction.SOUTH,
            Direction.WEST,
            Direction.EAST
    };

    /*
     * ========================================
     * コアごとの保存値
     * ========================================
     */

    private int storedWater;

    private int pendingJuice;

    private boolean plantBuilt;

    public RechorusPlantCoreBlockEntity(
            BlockPos position,
            BlockState state
    ) {
        super(
                ModBlockEntities.RECHORUS_PLANT_CORE,
                position,
                state
        );
    }

    /*
     * ========================================
     * Tick
     * ========================================
     */

    public static void serverTick(
            Level level,
            BlockPos position,
            BlockState state,
            RechorusPlantCoreBlockEntity blockEntity
    ) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        long gameTime =
                serverLevel.getGameTime();

        if (gameTime
                % UPDATE_INTERVAL_TICKS == 0L) {

            blockEntity.tickCore(
                    serverLevel,
                    position
            );
        }

        if (gameTime
                % PRODUCTION_INTERVAL_TICKS == 0L) {

            blockEntity.tickProduction(
                    serverLevel,
                    position
            );
        }
    }

    private void tickCore(
            ServerLevel level,
            BlockPos corePosition
    ) {
        /*
         * 設置場所・ディメンションを問わず、
         * コアに接した通常水源を吸収する。
         */
        absorbTouchingWater(
                level,
                corePosition
        );

        /*
         * 未成長なら、必要水量へ到達後に成長。
         */
        if (!this.plantBuilt) {
            tryGrowPlant(
                    level,
                    corePosition
            );

            return;
        }

        /*
         * 成長済みなら、このコアが管理する
         * root・plant・花を再生する。
         */
        TedRechorusPlantManager
                .regenerateManagedPlant(
                        level,
                        corePosition,
                        this
                );
    }

    /*
     * ========================================
     * 吸水
     * ========================================
     */

    private void absorbTouchingWater(
            ServerLevel level,
            BlockPos corePosition
    ) {
        if (this.storedWater
                >= WATER_CAPACITY) {
            return;
        }

        for (Direction direction
                : WATER_DIRECTIONS) {

            BlockPos waterPosition =
                    corePosition.relative(
                            direction
                    );

            FluidState fluidState =
                    level.getFluidState(
                            waterPosition
                    );

            /*
             * 現在は通常水の水源のみ。
             */
            if (!fluidState.is(
                    Fluids.WATER
            )) {
                continue;
            }

            if (!fluidState.isSource()) {
                continue;
            }

            int accepted =
                    Math.min(
                            WATER_PER_ABSORPTION,
                            WATER_CAPACITY
                                    - this.storedWater
                    );

            if (accepted <= 0) {
                return;
            }

            this.storedWater +=
                    accepted;

            this.setChanged();

            TheEndOfDragon.LOGGER.debug(
                    "Rechorus core absorbed water: core={}, source={}, accepted={}, stored={}/{}",
                    corePosition,
                    waterPosition,
                    accepted,
                    this.storedWater,
                    WATER_CAPACITY
            );

            /*
             * 1回につき1方向からだけ吸う。
             */
            return;
        }
    }

    /*
     * ========================================
     * 初回成長
     * ========================================
     */

    private void tryGrowPlant(
            ServerLevel level,
            BlockPos corePosition
    ) {
        if (this.storedWater
                < WATER_REQUIRED_TO_GROW) {
            return;
        }

        boolean placed =
                TedRechorusTreePlacer.placeAtCore(
                        level,
                        corePosition
                );

        if (!placed) {
            TheEndOfDragon.LOGGER.warn(
                    "Failed to grow Rechorus plant at core {}",
                    corePosition
            );

            return;
        }

        this.storedWater -=
                WATER_REQUIRED_TO_GROW;

        this.plantBuilt =
                true;

        this.setChanged();

        /*
         * クエスト施設の指定コアだった場合のみ、
         * VillageWorldStateへ完成状態を反映する。
         *
         * 2個目以降の自由設置コアには影響しない。
         */
        updateQuestCoreState(
                level,
                corePosition,
                true
        );

        TheEndOfDragon.LOGGER.info(
                "Rechorus plant grew: core={}, consumedWater={}, remainingWater={}",
                corePosition,
                WATER_REQUIRED_TO_GROW,
                this.storedWater
        );
    }

    /*
     * ========================================
     * 果汁・Blob生産
     * ========================================
     */

    private void tickProduction(
            ServerLevel level,
            BlockPos corePosition
    ) {
        if (!this.plantBuilt) {
            return;
        }

        if (this.storedWater
                < WATER_PER_PRODUCTION_CYCLE) {
            return;
        }

        List<BlockPos> flowers =
                TedRechorusPlantManager
                        .getManagedFlowers(
                                level,
                                corePosition
                        );

        if (flowers.isEmpty()) {
            return;
        }

        /*
         * 生産可能な場合だけ水を消費。
         */
        this.storedWater -=
                WATER_PER_PRODUCTION_CYCLE;

        this.pendingJuice +=
                flowers.size()
                        * JUICE_PER_FLOWER;

        int spawnedBlobs =
                0;

        while (this.pendingJuice
                >= JUICE_PER_BLOB
                && spawnedBlobs
                < MAX_BLOBS_PER_CYCLE) {

            if (!spawnBlobFromRandomFlower(
                    level,
                    flowers
            )) {
                break;
            }

            this.pendingJuice -=
                    JUICE_PER_BLOB;

            spawnedBlobs++;
        }

        this.setChanged();

        if (spawnedBlobs > 0) {
            TheEndOfDragon.LOGGER.info(
                    "Rechorus core produced Blob: core={}, blobs={}, flowers={}, water={}, pendingJuice={}",
                    corePosition,
                    spawnedBlobs,
                    flowers.size(),
                    this.storedWater,
                    this.pendingJuice
            );
        }
    }

    private static boolean spawnBlobFromRandomFlower(
            ServerLevel level,
            List<BlockPos> flowers
    ) {
        if (flowers == null
                || flowers.isEmpty()) {
            return false;
        }

        List<BlockPos> shuffled =
                new ArrayList<>(
                        flowers
                );

        Collections.shuffle(
                shuffled,
                new Random(
                        level.getGameTime()
                                ^ level.getRandom()
                                .nextLong()
                )
        );

        for (BlockPos flowerPosition
                : shuffled) {

            if (spawnBlobFromFlower(
                    level,
                    flowerPosition
            )) {
                return true;
            }
        }

        return false;
    }

    private static boolean spawnBlobFromFlower(
            ServerLevel level,
            BlockPos flowerPosition
    ) {
        BlockState flowerState =
                level.getBlockState(
                        flowerPosition
                );

        if (!flowerState.is(
                ModBlocks.RECHORUS_FLOWER
        )) {
            return false;
        }

        if (!flowerState.hasProperty(
                RechorusFlowerBlock.FACING
        )) {
            return false;
        }

        if (ModEntities.RECHORUS_JUICE_BLOB
                == null) {
            return false;
        }

        Direction facing =
                flowerState.getValue(
                        RechorusFlowerBlock.FACING
                );

        /*
         * 花自身のマスではなく、
         * 花が向いている外側へBlobを生成する。
         */
        BlockPos spawnBlockPosition =
                flowerPosition.relative(
                        facing
                );

        if (!level.getBlockState(
                spawnBlockPosition
        ).isAir()) {
            return false;
        }

        double spawnX =
                spawnBlockPosition.getX()
                        + 0.5D;

        double spawnY =
                spawnBlockPosition.getY()
                        + 0.5D;

        double spawnZ =
                spawnBlockPosition.getZ()
                        + 0.5D;

        if (facing == Direction.DOWN) {
            spawnY =
                    spawnBlockPosition.getY()
                            + 0.85D;
        } else if (facing == Direction.UP) {
            spawnY =
                    spawnBlockPosition.getY()
                            + 0.15D;
        }

        RechorusJuiceBlobEntity blob =
                new RechorusJuiceBlobEntity(
                        ModEntities.RECHORUS_JUICE_BLOB,
                        level
                );

        blob.setPos(
                spawnX,
                spawnY,
                spawnZ
        );

        /*
         * 花や幹へ食い込む場合は生成しない。
         */
        if (!level.noCollision(
                blob,
                blob.getBoundingBox()
        )) {
            return false;
        }

        return level.addFreshEntity(
                blob
        );
    }

    /*
     * ========================================
     * クエスト施設との同期
     * ========================================
     */

    private static void updateQuestCoreState(
            ServerLevel level,
            BlockPos corePosition,
            boolean built
    ) {
        TedVillageWorldState state =
                TedVillageWorldState.get(
                        level
                );

        BlockPos questCorePosition =
                state.getRechorusPlantCoreSlotPosition();

        if (questCorePosition == null
                || !questCorePosition.equals(
                corePosition
        )) {
            return;
        }

        state.setRechorusPlantCoreInstalled(
                true
        );

        state.setRechorusPlantBuilt(
                built
        );
    }

    /*
     * ========================================
     * 外部操作
     * ========================================
     */

    public int getStoredWater() {
        return this.storedWater;
    }

    public int getPendingJuice() {
        return this.pendingJuice;
    }

    public boolean isPlantBuilt() {
        return this.plantBuilt;
    }

    public boolean consumeWater(
            int amount
    ) {
        if (amount <= 0
                || this.storedWater < amount) {
            return false;
        }

        this.storedWater -=
                amount;

        this.setChanged();

        return true;
    }

    public int addWater(
            int amount
    ) {
        if (amount <= 0) {
            return 0;
        }

        int accepted =
                Math.min(
                        amount,
                        WATER_CAPACITY
                                - this.storedWater
                );

        if (accepted <= 0) {
            return 0;
        }

        this.storedWater +=
                accepted;

        this.setChanged();

        return accepted;
    }

    /*
     * ========================================
     * 保存
     * ========================================
     */

    @Override
    protected void saveAdditional(
            ValueOutput output
    ) {
        super.saveAdditional(
                output
        );

        output.putInt(
                "StoredWater",
                Math.clamp(
                        this.storedWater,
                        0,
                        WATER_CAPACITY
                )
        );

        output.putInt(
                "PendingJuice",
                Math.max(
                        0,
                        this.pendingJuice
                )
        );

        output.putBoolean(
                "PlantBuilt",
                this.plantBuilt
        );
    }

    @Override
    protected void loadAdditional(
            ValueInput input
    ) {
        super.loadAdditional(
                input
        );

        this.storedWater =
                Math.clamp(
                        input.getIntOr(
                                "StoredWater",
                                0
                        ),
                        0,
                        WATER_CAPACITY
                );

        this.pendingJuice =
                Math.max(
                        0,
                        input.getIntOr(
                                "PendingJuice",
                                0
                        )
                );

        this.plantBuilt =
                input.getBooleanOr(
                        "PlantBuilt",
                        false
                );
    }
}