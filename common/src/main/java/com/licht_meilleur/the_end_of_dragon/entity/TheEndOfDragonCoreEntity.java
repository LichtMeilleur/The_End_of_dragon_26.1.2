package com.licht_meilleur.the_end_of_dragon.entity;

import com.licht_meilleur.the_end_of_dragon.config.TedConfig;
import com.licht_meilleur.the_end_of_dragon.entity.beam.TedBeamSpec;
import com.licht_meilleur.the_end_of_dragon.entity.beam.TedBeamSpecs;
import com.licht_meilleur.the_end_of_dragon.entity.collision.DragonCollisionBox;
import com.licht_meilleur.the_end_of_dragon.entity.collision.DragonCollisionPart;
import com.licht_meilleur.the_end_of_dragon.entity.hitbox.DragonLocatorSampler;
import com.licht_meilleur.the_end_of_dragon.entity.hitbox.TedBeamHitbox;
import com.licht_meilleur.the_end_of_dragon.entity.projectile.TedProjectileSpec;
import com.licht_meilleur.the_end_of_dragon.entity.projectile.TedProjectileSpecs;
import com.licht_meilleur.the_end_of_dragon.entity.state.DragonAttackStateMachine;
import com.licht_meilleur.the_end_of_dragon.entity.vfx.*;
import com.licht_meilleur.the_end_of_dragon.network.TedNetwork;
import com.licht_meilleur.the_end_of_dragon.registry.ModEntities;

import com.licht_meilleur.the_end_of_dragon.entity.ai.*;
import com.licht_meilleur.the_end_of_dragon.registry.ModItems;
import com.licht_meilleur.the_end_of_dragon.registry.ModSounds;
import com.licht_meilleur.the_end_of_dragon.sound.TedSoundHelper;
import com.licht_meilleur.the_end_of_dragon.world.EndPortalSealHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.UUID;

public class TheEndOfDragonCoreEntity extends Monster {
    private static final EntityDataAccessor<Integer> DATA_STATE =
            SynchedEntityData.defineId(TheEndOfDragonCoreEntity.class, EntityDataSerializers.INT);

    private int displayEntityId = -1;
    private int collisionEntityId = -1;

    private int frontLeftLaserVfxId = -1;
    private int frontRightLaserVfxId = -1;
    private int backLeftLaserVfxId = -1;
    private int backRightLaserVfxId = -1;



    // Photon Blaster
    private static final int PHOTON_CHARGE_START = 1;
    private static final int PHOTON_FIRE_START = 27;
    private static final int PHOTON_FIRE_END = 70;

    // Flames of Ragnarok
    private static final int FLAMES_FIRE_START = 1;
    private static final int FLAMES_FIRE_END = 120;

    //Orb of Annihilation
    private static final int ORB_CHARGE_START = 6;
    private static final int ORB_FIRE_TICK = 55;

    //Fly Shot
    private static final int FLY_SHOT_FIRE_TICK = 5;

    private int photonBusterBeamVfxId = -1;

    /*
     * 攻撃開始時に固定する照準位置。
     * 照射中のプレイヤーを完全追尾させず、横へ避けられるようにする。
     */
    private Vec3 photonBusterLockedTarget = null;




    private Vec3 introFlyTarget = null;
    private Vec3 introPortalAboveTarget = null;


    private boolean attackMovementLocked = false;

    // 強制チャンクロード
    private final java.util.Set<Long> forcedChunks = new java.util.HashSet<>();


    private int flyShotAnimTicks = 0;
    private int flyShotFireDelay = 0;
    //テイルウィップ
    private boolean tailWhipHitDone = false;

    //フォトンバスター
    private int photonBusterVfxId = -1;

    //ジャッジメントレイ
    private int judgmentShotCooldown = 0;
    private int judgmentShotCount = 0;
    // Judgment Ray
    private static final int JUDGMENT_CHARGE_START = 1;
    private static final int JUDGMENT_FIRE_START = 25;
    private static final int JUDGMENT_FIRE_END = 40;
    private static final int JUDGMENT_SHOT_INTERVAL = 2;
    private static final int JUDGMENT_MAX_SHOTS = 32;

    private int unsafeFallTicks = 0;

    //ドロップアイテム
    private boolean deathDropSpawned = false;
    //ジェット音時間
    private int jetSoundCooldown = 0;

    private Vec3 recoveryDiveTarget = null;



    private static boolean between(int age, int start, int end) {
        return age >= start && age <= end;
    }

    public TheEndOfDragonCoreEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 500;
        this.setPersistenceRequired();
        this.setInvisible(true);
        this.noPhysics = true;
        this.setNoGravity(true);

        float yaw = 0.0F;

        this.setYRot(yaw);
        this.setYBodyRot(yaw);
        this.setYHeadRot(yaw);
        this.yRotO = yaw;
        this.yBodyRotO = yaw;
        this.yHeadRotO = yaw;
    }



    private final ServerBossEvent bossBar = new ServerBossEvent(
            UUID.randomUUID(),
            net.minecraft.network.chat.Component.translatable("boss.the_end_of_dragon.the_end_of_dragon"),
            BossEvent.BossBarColor.PURPLE,
            BossEvent.BossBarOverlay.PROGRESS
    );



    @Override
    protected void registerGoals() {
        /*
         * 奈落・大幅な位置逸脱用。
         */
        this.goalSelector.addGoal(
                0,
                new DragonRecoveryGoal(this)
        );

        /*
         * 長時間攻撃できていない場合の踏みつけ復帰。
         */
        this.goalSelector.addGoal(
                1,
                new DragonStallRecoveryGoal(this)
        );

        this.goalSelector.addGoal(
                2,
                new DragonAirAttackGoal(this)
        );

        this.goalSelector.addGoal(
                3,
                new DragonTailWhipGoal(this)
        );

        this.goalSelector.addGoal(
                4,
                new DragonGroundAttackGoal(this)
        );

        this.goalSelector.addGoal(
                5,
                new DragonMoveGoal(this)
        );
    }



    private float visualPitch = 0.0F;

    public boolean isAirborneBoss(ServerLevel level) {
        int x = Mth.floor(this.getX());
        int z = Mth.floor(this.getZ());

        int groundY = level.getHeight(
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                x,
                z
        );

        return this.getY() > groundY + 10.0D;
    }

    public void setVisualRotation(float yaw, float pitch) {
        this.setYRot(yaw);
        this.setYBodyRot(yaw);
        this.setYHeadRot(yaw);

        this.yRotO = yaw;
        this.yBodyRotO = yaw;
        this.yHeadRotO = yaw;

        this.visualPitch = pitch;
    }

    public float getVisualPitch() {
        return this.visualPitch;
    }

    public boolean isNearGroundForSuperLandingPublic(ServerLevel level) {
        return isNearGroundForSuperLanding(level);
    }


    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_STATE, DragonState.IDLE.ordinal());
    }

    private int stateStartTick = 0;

    public void setDragonState(DragonState state) {
        DragonState previousState = this.getDragonState();

        if (previousState == state) {
            return;
        }

    /*
    System.out.println("[TED STATE] " + previousState + " -> " + state
            + " age=" + this.getDragonStateAgeTicks()
            + " tick=" + this.tickCount);
    */

        if (state == DragonState.FALL) {
            this.noPhysics = false;
            this.setNoGravity(false);

            this.setDeltaMovement(
                    this.getDeltaMovement().x,
                    Math.min(this.getDeltaMovement().y, -1.2D),
                    this.getDeltaMovement().z
            );

            this.fallDistance = 0.0F;
        }

        // Photon Busterを抜ける時は、残っているビームを消す
        if (previousState == DragonState.PHOTON_BUSTER
                && state != DragonState.PHOTON_BUSTER) {
            hidePhotonBusterBeam();
            this.photonBusterLockedTarget = null;
        }

        if (isMeaningfulAttackStart(state)
                && !isMeaningfulAttackStart(previousState)) {
            this.markAttackStarted();
        }

        this.entityData.set(DATA_STATE, state.ordinal());
        this.stateStartTick = this.tickCount;

        if (state == DragonState.JUDGMENT_RAY) {
            this.judgmentShotCooldown = 0;
            this.judgmentShotCount = 0;
        }

        if (state == DragonState.PHOTON_BUSTER) {
            this.photonBusterLockedTarget = null;
            this.photonBusterBeamVfxId = -1;
            this.setDeltaMovement(Vec3.ZERO);
            this.setAttackMovementLocked(true);
        }

        if (state == DragonState.TAIL_WHIP) {
            this.tailWhipHitDone = false;
        }

        if (state == DragonState.SUPER_LANDING
                || state == DragonState.INTRO_SUPER_LANDING) {
            this.superLandingImpacted = false;
        }

        if (state == DragonState.DEAD
                && previousState != DragonState.DEAD) {
            this.deathDropSpawned = false;
        }
    }

    private Vec3 findRecoveryDiveGroundTarget(
            ServerLevel level
    ) {
        LivingEntity target = findBossTarget(level);

        double targetX;
        double targetZ;

        if (target != null && target.isAlive()) {
            targetX = target.getX();
            targetZ = target.getZ();
        } else {
            Vec3 arena = arenaCenter(level);
            targetX = arena.x;
            targetZ = arena.z;
        }

        int blockX = Mth.floor(targetX);
        int blockZ = Mth.floor(targetZ);

        int groundY = level.getHeight(
                net.minecraft.world.level.levelgen.Heightmap.Types
                        .MOTION_BLOCKING_NO_LEAVES,
                blockX,
                blockZ
        );

        return new Vec3(
                blockX + 0.5D,
                groundY,
                blockZ + 0.5D
        );
    }

    private boolean isMeaningfulAttackStart(DragonState state) {
        return switch (state) {
            case ORB_OF_ANNIHILATION,
                 ROAR_OF_OBLITERATION,
                 LIGHT_OF_DESTRUCTION,
                 PHOTON_BLASTER,
                 PHOTON_BUSTER,
                 BLASTER_TACKLE,
                 TAIL_WHIP,
                 FLAMES_OF_RAGNAROK,
                 FIGURE_EIGHT,
                 JUDGMENT_RAY -> true;

            default -> false;
        };
    }

    public int getDragonStateAgeTicks() {
        return Math.max(0, this.tickCount - this.stateStartTick);
    }

    public DragonState getDragonState() {
        int id = this.entityData.get(DATA_STATE);
        DragonState[] values = DragonState.values();
        if (id < 0 || id >= values.length) {
            return DragonState.IDLE;
        }
        return values[id];
    }

    private boolean debugFrozen = false;

    public void setDebugFrozen(boolean frozen) {
        this.debugFrozen = frozen;

        if (frozen) {
            this.setAttackMovementLocked(true);
            this.getNavigation().stop();
            this.setDeltaMovement(Vec3.ZERO);
            this.setDragonState(DragonState.IDLE);
        } else {
            this.setAttackMovementLocked(false);
        }
    }
    //停止状態防止用
    private int lastAttackStartedTick = 0;

    public void markAttackStarted() {
        this.lastAttackStartedTick = this.tickCount;
    }

    public int getTicksSinceLastAttack() {
        return Math.max(
                0,
                this.tickCount - this.lastAttackStartedTick
        );
    }

    public boolean isDebugFrozen() {
        return this.debugFrozen;
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            this.initializeNormalSpawnIfNeeded();
        }

        this.setInvisible(true);

        updatePhysicsMode();

        if (this.level().isClientSide()) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) this.level();



        if (deathSequenceStarted || this.getDragonState() == DragonState.DEAD) {
            tickDeathSequence(serverLevel);
            return;
        }

        if (this.debugFrozen) {
            this.setDeltaMovement(Vec3.ZERO);
            this.getNavigation().stop();

            updateCrystalFadeStage();
            tickChildren();

            bossBar.setProgress(this.getHealth() / this.getMaxHealth());
            return;
        }

        tickVoidRecovery(serverLevel);

        if (this.getDragonState() == DragonState.RECOVERY_ASCEND
                || this.getDragonState() == DragonState.RECOVERY_RETURN) {
            tickChildren();
            bossBar.setProgress(this.getHealth() / this.getMaxHealth());
            return;
        }

        if (!this.isIntroStateNow() && this.getDragonState() != DragonState.DEAD) {
            this.combatStarted = true;
        }

        updateCrystalFadeStage();

        tickVoidRecovery(serverLevel);
        tickUnknownPositionRecovery(serverLevel);

        attackStateMachine.tick(serverLevel);

        this.tickChildren();
        this.tickAttackVfx();
        this.tickFlyShotRequest(serverLevel);
        this.tickBodyBlockBreak(serverLevel);
        this.tickAirAttackCooldown();

        this.tickFlightJetSound(serverLevel);


        if (!this.level().isClientSide() && this.level() instanceof ServerLevel level) {

            maintainForcedChunks(level);
        }

        bossBar.setProgress(this.getHealth() / this.getMaxHealth());
        bossBar.setName(
                net.minecraft.network.chat.Component.translatable("boss.the_end_of_dragon.the_end_of_dragon")
        );
    }

    private boolean normalSpawnInitialized = false;

    private void initializeNormalSpawnIfNeeded() {
        if (this.level().isClientSide()) {
            return;
        }

        if (this.normalSpawnInitialized) {
            return;
        }

        /*
         * EndDragonSpawnHandlerがイベント設定を行うための
         * 最初の1tickを待つ。
         */
        if (this.tickCount < 2) {
            return;
        }

        this.normalSpawnInitialized = true;

        if (this.spawnKind != DragonSpawnKind.NORMAL) {
            return;
        }

        this.setCombatStarted(true);
        this.setDragonState(DragonState.IDLE);
        this.setPersistenceRequired();
    }

    private void updateCrystalFadeStage() {
        if (this.getDragonState() != DragonState.DEAD) {
            this.crystalFadeStage = 0;
            return;
        }

        int age = this.getDragonStateAgeTicks();

        if (age >= 110) {
            this.crystalFadeStage = 4;
        } else if (age >= 90) {
            this.crystalFadeStage = 3;
        } else if (age >= 60) {
            this.crystalFadeStage = 2;
        } else if (age >= 30) {
            this.crystalFadeStage = 1;
        } else {
            this.crystalFadeStage = 0;
        }
    }

    private int crystalFadeStage = 0;

    public int getCrystalFadeStage() {
        return crystalFadeStage;
    }

    private boolean enderDragonEventFight = false;
    private boolean eventBgmStarted = false;

    public void setEnderDragonEventFight(boolean eventFight) {
        this.enderDragonEventFight = eventFight;
    }


    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);

        bossBar.addPlayer(player);

        if (this.isEnderDragonEventFight()
                && this.combatStarted
                && !this.deathSequenceStarted
                && this.getDragonState() != DragonState.DEAD) {
            TedNetwork.sendBgmStart(player);
        }
    }
    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossBar.removePlayer(player);
    }

    private boolean isAirAttackStateForRecovery() {
        return switch (this.getDragonState()) {
            case FLY_START,
                 FLY,
                 FLY_ASCEND,
                 FIGURE_EIGHT,
                 FLY_DESCEND,
                 FLY_SHOT,
                 FLAMES_OF_RAGNAROK,
                 JUDGMENT_RAY,
                 FALL,
                 LANDING,
                 SUPER_LANDING -> true;

            default -> false;
        };
    }

    public boolean shouldEmergencyRecover(ServerLevel level) {
        if (!this.isAlive()) return false;
        if (this.getDragonState() == DragonState.DEAD) return false;
        if (this.isIntroStateNow()) return false;

        // 空中攻撃中は復帰判定しない
        if (isAirAttackStateForRecovery()) {
            unsafeFallTicks = 0;
            return false;
        }

        Vec3 center = this.arenaCenter(level);

        double dx = this.getX() - center.x;
        double dz = this.getZ() - center.z;

        boolean outOfArena = (dx * dx + dz * dz) > 220.0D * 220.0D;
        boolean tooLow = this.getY() < center.y - 35.0D;

        // 地上攻撃・通常中なのに落ちている状態
        boolean falling = this.getY() < center.y - 20.0D;

        if ((outOfArena || tooLow) && falling) {
            unsafeFallTicks++;
        } else {
            unsafeFallTicks = 0;
        }

        return unsafeFallTicks >= 20;
    }

    public void startEmergencyRecovery() {
        this.setAttackMovementLocked(true);
        this.setDeltaMovement(Vec3.ZERO);
        this.fallDistance = 0.0F;
        this.setDragonState(DragonState.RECOVERY_ASCEND);
        this.attackStateMachine.cancelAirSequence();
    }

    public void tickEmergencyRecoveryMove(
            ServerLevel level
    ) {
        if (this.recoveryDiveTarget == null) {
            this.recoveryDiveTarget =
                    findRecoveryDiveGroundTarget(level);
        }

        Vec3 groundTarget = this.recoveryDiveTarget;

        /*
         * 地表から45ブロック上を水平移動の目標にする。
         */
        Vec3 aboveTarget = groundTarget.add(
                0.0D,
                45.0D,
                0.0D
        );

        switch (this.getDragonState()) {
            case RECOVERY_ASCEND -> {
                /*
                 * 現在位置からまず十分な高度まで上昇。
                 */
                double targetY = Math.max(
                        aboveTarget.y,
                        this.arenaCenter(level).y + 45.0D
                );

                double difference =
                        targetY - this.getY();

                if (difference > 2.0D) {
                    double speed =
                            Math.min(10.0D, difference);

                    moveBossByNoFace(
                            level,
                            new Vec3(0.0D, speed, 0.0D)
                    );

                    return;
                }

                this.setDragonState(
                        DragonState.RECOVERY_RETURN
                );
            }

            case RECOVERY_RETURN -> {
                /*
                 * 高度を維持して、プレイヤー地表の真上へ移動。
                 */
                Vec3 horizontalTarget = new Vec3(
                        aboveTarget.x,
                        this.getY(),
                        aboveTarget.z
                );

                Vec3 offset =
                        horizontalTarget.subtract(this.position());

                Vec3 horizontal = new Vec3(
                        offset.x,
                        0.0D,
                        offset.z
                );

                double distance = horizontal.length();

                if (distance <= 4.0D) {
                    this.snapTo(
                            horizontalTarget.x,
                            this.getY(),
                            horizontalTarget.z,
                            this.getYRot(),
                            this.getXRot()
                    );

                    this.setDeltaMovement(Vec3.ZERO);

                    /*
                     * ダイブ直前に地表高度だけ再取得。
                     * プレイヤーが少し動いた場合にも対応しやすい。
                     */
                    this.recoveryDiveTarget =
                            findRecoveryDiveGroundTarget(level);

                    this.setDragonState(
                            DragonState.RECOVERY_DIVE
                    );

                    return;
                }

                if (horizontal.lengthSqr() > 1.0E-6D) {
                    moveBossBy(
                            level,
                            horizontal.normalize().scale(
                                    Math.min(12.0D, distance)
                            )
                    );
                }
            }

            case RECOVERY_DIVE -> {
                Vec3 currentGroundTarget =
                        this.recoveryDiveTarget;

                if (currentGroundTarget == null) {
                    currentGroundTarget =
                            findRecoveryDiveGroundTarget(level);

                    this.recoveryDiveTarget =
                            currentGroundTarget;
                }

                double distanceToGround =
                        this.getY() - currentGroundTarget.y;

                /*
                 * 地面へ近づいたらワープ移動せず、
                 * SUPER_LANDINGへ切り替える。
                 */
                if (distanceToGround <= 8.0D) {
                    this.setDeltaMovement(Vec3.ZERO);
                    this.fallDistance = 0.0F;

                    this.setDragonState(
                            DragonState.SUPER_LANDING
                    );

                    return;
                }

                double diveSpeed =
                        Math.min(
                                8.0D,
                                Math.max(3.0D, distanceToGround - 6.0D)
                        );

                moveBossByNoFace(
                        level,
                        new Vec3(0.0D, -diveSpeed, 0.0D)
                );
            }

            default -> {
            }
        }
    }




    public void moveBossBy(ServerLevel level, Vec3 move) {
        moveByVector(level, move);
    }

    public void descendForRagnarok(ServerLevel level) {
        /*
         * FALL中は通常重力に任せる。
         * setPosによる瞬間移動は行わない。
         */

        if (shouldLandFromFall(level)) {
            this.setDeltaMovement(Vec3.ZERO);
            this.fallDistance = 0.0F;
            this.setDragonState(DragonState.LANDING);
            return;
        }

        /*
         * 奈落付近まで落ちた場合は緊急復帰。
         */
        if (this.getY() <= level.getMinY() + 12.0D) {
            this.setDeltaMovement(Vec3.ZERO);
            this.fallDistance = 0.0F;
            this.startEmergencyRecovery();
        }
    }

    private boolean shouldLandFromFall(ServerLevel level) {
        Vec3 start = this.position();

        /*
         * モデルの足元位置に合わせて調整可能。
         * まずはCore位置から下8ブロックを確認する。
         */
        Vec3 end = start.add(0.0D, -8.0D, 0.0D);

        var hit = level.clip(
                new net.minecraft.world.level.ClipContext(
                        start,
                        end,
                        net.minecraft.world.level.ClipContext.Block.COLLIDER,
                        net.minecraft.world.level.ClipContext.Fluid.NONE,
                        this
                )
        );

        if (hit.getType()
                != net.minecraft.world.phys.HitResult.Type.MISS) {
            double distance =
                    hit.getLocation().distanceTo(start);

            return distance <= 7.0D;
        }

        return this.onGround();
    }

    public void moveBossByNoFace(ServerLevel level, Vec3 move) {
        moveByVector(level, move, false);
    }

    public void moveByVector(ServerLevel level, Vec3 move) {
        moveByVector(level, move, true);
    }

    public void moveByVector(ServerLevel level, Vec3 move, boolean faceMoveDirection) {
        if (move.lengthSqr() < 1.0E-6D) {
            return;
        }

        this.setPos(
                this.getX() + move.x,
                this.getY() + move.y,
                this.getZ() + move.z
        );

        if (faceMoveDirection) {
            faceMovementDirection(move);
        }

        this.setDeltaMovement(Vec3.ZERO);
        this.hurtMarked = true;
        syncChildrenNow();
    }


    private void maintainForcedChunks(ServerLevel level) {

        int cx = Mth.floor(this.getX()) >> 4;
        int cz = Mth.floor(this.getZ()) >> 4;

        int radius = 4;

        java.util.Set<Long> next = new java.util.HashSet<>();

        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int z = cz - radius; z <= cz + radius; z++) {

                long key = ChunkPos.pack(x, z);

                next.add(key);

                if (!forcedChunks.contains(key)) {
                    level.setChunkForced(x, z, true);
                }
            }
        }

        for (long key : forcedChunks) {

            if (!next.contains(key)) {

                int x = ChunkPos.getX(key);
                int z = ChunkPos.getZ(key);

                level.setChunkForced(x, z, false);
            }
        }

        forcedChunks.clear();
        forcedChunks.addAll(next);
    }

    public void cancelAttackAndRecover() {
        this.setAttackMovementLocked(false);
        this.setDeltaMovement(Vec3.ZERO);
        this.fallDistance = 0.0F;
    }

    private <T extends Entity> T getChild(int entityId, Class<T> type) {
        if (entityId == -1) {
            return null;
        }

        Entity entity = this.level().getEntity(entityId);
        if (type.isInstance(entity) && !entity.isRemoved()) {
            return type.cast(entity);
        }

        return null;
    }

    private void syncChildrenNow() {
        if (this.level().isClientSide()) return;
        if (this.isRemoved()) return;
        if (this.deathSequenceFinished) return;

        TheEndOfDragonDisplayEntity display =
                this.getChild(this.displayEntityId, TheEndOfDragonDisplayEntity.class);

        TheEndOfDragonCollisionEntity collision =
                this.getChild(this.collisionEntityId, TheEndOfDragonCollisionEntity.class);

        if (display != null) display.syncFromCore(this);
        if (collision != null) collision.syncFromCore(this);
    }



    private void tickChildren() {

        if (this.level().isClientSide()) return;
        if (this.isRemoved()) return;
        if (this.deathSequenceFinished) return;

        TheEndOfDragonDisplayEntity display =
                this.getChild(this.displayEntityId, TheEndOfDragonDisplayEntity.class);

        TheEndOfDragonCollisionEntity collision =
                this.getChild(this.collisionEntityId, TheEndOfDragonCollisionEntity.class);


        if (display == null) {
            display = new TheEndOfDragonDisplayEntity(
                    ModEntities.THE_END_OF_DRAGON_DISPLAY,
                    this.level()
            );
            display.setOwnerCoreUuid(this.getUUID());
            display.syncFromCore(this);
            this.level().addFreshEntity(display);
            this.displayEntityId = display.getId();
            //logChildren("CREATE_DISPLAY");
        }

        if (collision == null) {
            collision = new TheEndOfDragonCollisionEntity(
                    ModEntities.THE_END_OF_DRAGON_COLLISION,
                    this.level()
            );
            collision.setOwnerCoreUuid(this.getUUID());
            collision.syncFromCore(this);
            this.level().addFreshEntity(collision);
            this.collisionEntityId = collision.getId();
            //logChildren("CREATE_COLLISION");
        }

        display.syncFromCore(this);
        collision.syncFromCore(this);
    }

    private void discardChildren() {
        UUID owner = this.getUUID();

        AABB area = new AABB(
                this.getX() - 1024.0D,
                this.level().getMinY(),
                this.getZ() - 1024.0D,
                this.getX() + 1024.0D,
                this.level().getMaxY(),
                this.getZ() + 1024.0D
        );

        for (TheEndOfDragonDisplayEntity e : this.level().getEntitiesOfClass(
                TheEndOfDragonDisplayEntity.class,
                area,
                e -> e.hasOwnerCoreUuid(owner)
        )) {
            e.discard();
        }

        for (TheEndOfDragonCollisionEntity e : this.level().getEntitiesOfClass(
                TheEndOfDragonCollisionEntity.class,
                area,
                e -> e.hasOwnerCoreUuid(owner)
        )) {
            e.discard();
        }

        this.displayEntityId = -1;
        this.collisionEntityId = -1;
    }

    private void logChildren(String label) {
        if (this.level().isClientSide()) return;

        System.out.println("[TED CHILD][" + label + "]"
                + " coreId=" + this.getId()
                + " coreUuid=" + this.getUUID()
                + " state=" + this.getDragonState()
                + " removed=" + this.isRemoved()
                + " displayId=" + this.displayEntityId
                + " collisionId=" + this.collisionEntityId);

        Entity display = this.level().getEntity(this.displayEntityId);
        Entity collision = this.level().getEntity(this.collisionEntityId);



        System.out.println("[TED CHILD][" + label + "] displayById="
                + entityDebug(display));

        System.out.println("[TED CHILD][" + label + "] collisionById="
                + entityDebug(collision));

        AABB area = this.getBoundingBox().inflate(1024.0D);

        for (TheEndOfDragonDisplayEntity e : this.level().getEntitiesOfClass(
                TheEndOfDragonDisplayEntity.class,
                area
        )) {
            System.out.println("[TED CHILD][" + label + "] nearby display "
                    + entityDebug(e)
                    + " owner=" + e.getOwnerCoreUuid());
        }

        for (TheEndOfDragonCollisionEntity e : this.level().getEntitiesOfClass(
                TheEndOfDragonCollisionEntity.class,
                area
        )) {
            System.out.println("[TED CHILD][" + label + "] nearby collision "
                    + entityDebug(e)
                    + " owner=" + e.getOwnerCoreUuid());
        }
    }

    private String entityDebug(Entity e) {
        if (e == null) return "null";

        return e.getClass().getSimpleName()
                + "{id=" + e.getId()
                + ", uuid=" + e.getUUID()
                + ", removed=" + e.isRemoved()
                + ", alive=" + e.isAlive()
                + ", pos=" + e.position()
                + "}";
    }

    private void updatePhysicsMode() {
        DragonState state = this.getDragonState();

        boolean flying = switch (state) {
            case FLY_START,
                 FLY,
                 FLY_ASCEND,
                 FLY_DESCEND,
                 FIGURE_EIGHT,
                 FLY_SHOT,
                 FLAMES_OF_RAGNAROK,
                 JUDGMENT_RAY,
                 INTRO_RISE,
                 INTRO_WAIT_PORTAL,
                 INTRO_FLY_TO_PORTAL,
                 INTRO_DIVE_TO_PORTAL,
                 RECOVERY_ASCEND,
                 RECOVERY_RETURN,
                 RECOVERY_DIVE-> true;

            default -> false;
        };

        this.noPhysics = flying;
        this.setNoGravity(flying);
        this.fallDistance = 0.0F;
    }

    @Override
    public boolean causeFallDamage(
            double fallDistance,
            float damageMultiplier,
            net.minecraft.world.damagesource.DamageSource source
    ) {
        this.fallDistance = 0.0F;
        return false;
    }
    //個体識別
    private DragonSpawnKind spawnKind =
        DragonSpawnKind.NORMAL;

    public void setSpawnKind(
            DragonSpawnKind spawnKind
    ) {
        this.spawnKind = spawnKind != null
                ? spawnKind
                : DragonSpawnKind.NORMAL;
    }

    public DragonSpawnKind getSpawnKind() {
        return this.spawnKind;
    }

    public boolean isEnderDragonEventFight() {
        return this.spawnKind
                == DragonSpawnKind.ENDER_DRAGON_EVENT;
    }
    //BGM
    private void startEventBgmForPlayers(
            ServerLevel level
    ) {
        if (!this.isEnderDragonEventFight()
                || this.eventBgmStarted) {
            return;
        }

        this.eventBgmStarted = true;

        for (ServerPlayer player : level.players()) {
            TedNetwork.sendBgmStart(player);
        }
    }

    private void stopEventBgmForPlayers(
            ServerLevel level
    ) {
        if (!this.isEnderDragonEventFight()) {
            return;
        }

        for (ServerPlayer player : level.players()) {
            TedNetwork.sendBgmStop(player);
        }

        this.eventBgmStarted = false;
    }
    //NBT保存
    @Override
    protected void addAdditionalSaveData(
            net.minecraft.world.level.storage.ValueOutput output
    ) {
        super.addAdditionalSaveData(output);

        output.putString(
                "TedSpawnKind",
                this.spawnKind.name()
        );

    }

    @Override
    protected void readAdditionalSaveData(
            net.minecraft.world.level.storage.ValueInput input
    ) {
        super.readAdditionalSaveData(input);

        String savedKind =
                input.getStringOr(
                        "TedSpawnKind",
                        DragonSpawnKind.NORMAL.name()
                );

        try {
            this.spawnKind =
                    DragonSpawnKind.valueOf(savedKind);
        } catch (IllegalArgumentException exception) {
            this.spawnKind =
                    DragonSpawnKind.NORMAL;
        }
    }







    public boolean isIntroStateNow() {
        return isIntroState(this.getDragonState());
    }

    public boolean isAttackStateNow() {
        return isAttackState(this.getDragonState());
    }

    public LivingEntity findBossTarget(ServerLevel level) {
        LivingEntity player = level.getNearestPlayer(this, 256.0D);

        if (player != null && player.isAlive()) {
            return player;
        }

        LivingEntity revenge = this.getLastHurtByMob();

        if (revenge != null && revenge.isAlive()) {
            return revenge;
        }

        return null;
    }





    public Vec3 arenaCenter(ServerLevel level) {
        return getArenaCenter(level);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!this.level().isClientSide()) {
            hidePhotonBusterBeam();

            if (!deathSequenceStarted || deathSequenceFinished) {
                discardChildren();
            }

            if (this.level() instanceof ServerLevel serverLevel) {
                for (long key : forcedChunks) {
                    int x = ChunkPos.getX(key);
                    int z = ChunkPos.getZ(key);
                    serverLevel.setChunkForced(x, z, false);
                }

                forcedChunks.clear();
            }
        }

        bossBar.removeAllPlayers();
        super.remove(reason);
    }



    public boolean isAttackMovementLocked() {
        return attackMovementLocked;
    }

    public void setAttackMovementLocked(boolean locked) {
        this.attackMovementLocked = locked;
    }

    private boolean deathSequenceStarted;
    private boolean deathSequenceFinished;
    private int deathTicks;

    @Override
    public void die(DamageSource source) {
        if (deathSequenceStarted) {
            return;
        }

        deathSequenceStarted = true;

        this.setHealth(0.0F);
        this.setDragonState(DragonState.DEAD);

        this.setAttackMovementLocked(true);
        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);

        if (!level().isClientSide() && level() instanceof ServerLevel serverLevel) {
            EndPortalSealHandler.restorePortal(serverLevel);
        }
    }

    private TheEndOfDragonDisplayEntity displayEntity;
    private TheEndOfDragonCollisionEntity collisionEntity;

    private void tickDeathSequence(ServerLevel level) {
        deathTicks++;

        updateCrystalFadeStage();
        tickChildren();

        if (!deathDropSpawned && deathTicks >= 110) {
            deathDropSpawned = true;

            ItemEntity item = new ItemEntity(
                    level,
                    this.getX(),
                    this.getY() + 1.0D,
                    this.getZ(),
                    new ItemStack(ModItems.THE_END_PIECE)
            );

            item.setDeltaMovement(
                    this.getRandom().nextGaussian() * 0.08D,
                    0.25D,
                    this.getRandom().nextGaussian() * 0.08D
            );

            level.addFreshEntity(item);
        }

        if (deathTicks < 160) {
            return;
        }

        spawnDeathShatterEffect(level);

        TedSoundHelper.playDeathShatter(
                level,
                this
        );

        stopEventBgmForPlayers(level);

        deathSequenceFinished = true;
        discardChildren();
        this.discard();
    }

    private void spawnDeathShatterEffect(ServerLevel level) {
        Vec3 center = this.position().add(0.0D, 4.0D, 0.0D);


        level.sendParticles(
                ParticleTypes.END_ROD,
                center.x, center.y, center.z,
                420,
                7.0D, 5.0D, 7.0D,
                0.28D
        );

        level.sendParticles(
                ParticleTypes.GLOW,
                center.x, center.y, center.z,
                260,
                6.0D, 4.0D, 6.0D,
                0.22D
        );

        level.sendParticles(
                ParticleTypes.FIREWORK,
                center.x, center.y, center.z,
                120,
                5.0D, 3.5D, 5.0D,
                0.18D
        );

        level.sendParticles(
                ParticleTypes.DUST_PLUME,
                center.x, center.y - 1.0D, center.z,
                90,
                5.0D, 0.8D, 5.0D,
                0.08D
        );
    }

    @Override
    protected void tickDeath() {
        if (this.deathSequenceStarted) {
            this.deathTime = 0;
            return;
        }

        super.tickDeath();
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(
                        Attributes.MAX_HEALTH,
                        600.0D * TedConfig.values.healthMultiplier
                )
                .add(Attributes.ATTACK_DAMAGE, 20.0D)
                .add(Attributes.ARMOR, 40.0D)
                .add(Attributes.FOLLOW_RANGE, 128.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    public void startRagnarokSequence() {
        this.markAttackStarted();
        attackStateMachine.startRagnarok();
    }

    public void startFigureEightSequence() {
        this.markAttackStarted();
        attackStateMachine.startFigureEight();
    }

    public void startJudgmentRaySequence() {
        this.markAttackStarted();
        attackStateMachine.startJudgmentRay();
    }

    public void startDiveStompSequence() {
        this.markAttackStarted();
        startRecoveryDiveSequence();
    }

    public void startRecoveryDiveSequence() {
        if (!this.isAlive()) {
            return;
        }

        if (this.getDragonState() == DragonState.DEAD
                || this.isIntroStateNow()) {
            return;
        }

        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }

        if (isRecoveryStateNow()) {
            return;
        }

        this.attackStateMachine.cancelAirSequence();
        this.hidePhotonBusterBeam();

        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);
        this.fallDistance = 0.0F;
        this.setAttackMovementLocked(true);

        /*
         * 開始時点のプレイヤー地表座標を固定。
         * ダイブ直前まで追尾させたい場合は後で更新可能。
         */
        this.recoveryDiveTarget =
                findRecoveryDiveGroundTarget(level);

        this.setDragonState(
                DragonState.RECOVERY_ASCEND
        );
    }

    public boolean isRecoveryStateNow() {
        return switch (this.getDragonState()) {
            case RECOVERY_ASCEND,
                 RECOVERY_RETURN,
                 RECOVERY_DIVE,
                 SUPER_LANDING -> true;

            default -> false;
        };
    }
    public void finishRecoveryDive() {
        this.recoveryDiveTarget = null;
        this.markAttackStarted();
    }




    private void drawDebugLine(ServerLevel serverLevel, Vec3 start, Vec3 end) {
        int count = 80;

        for (int i = 0; i <= count; i++) {
            double t = (double) i / (double) count;
            Vec3 p = start.lerp(end, t);

            serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.FLAME,
                    p.x,
                    p.y,
                    p.z,
                    1,
                    0.0D,
                    0.0D,
                    0.0D,
                    0.0D
            );
        }
    }

    private void tickBodyBlockBreak(ServerLevel level) {
        if (!shouldBodyBreakBlocks()) {
            return;
        }

        TheEndOfDragonCollisionEntity collision =
                this.getChild(this.collisionEntityId, TheEndOfDragonCollisionEntity.class);

        if (collision == null) {
            return;
        }

        for (DragonCollisionBox box : collision.getCollisionBoxes()) {
            if (!canBodyPartBreakBlocks(box.part())) {
                continue;
            }

            if (box.points() == null || box.points().length == 0) {
                continue;
            }

            breakBlocksInBodyBox(level, box);
        }
    }
    private boolean shouldBodyBreakBlocks() {
        if (!this.isAlive() || !TedConfig.values.enableBlockBreak) {
            return false;
        }

        return switch (this.getDragonState()) {
            case SUPER_LANDING,
                 INTRO_SUPER_LANDING,
                 FALL,
                 LANDING,
                 FLAMES_OF_RAGNAROK -> false;

            case INTRO_RISE,
                 INTRO_WAIT_PORTAL,
                 INTRO_FLY_TO_PORTAL,
                 INTRO_DIVE_TO_PORTAL -> false;

            case RECOVERY_ASCEND,
                 RECOVERY_RETURN,
                 RECOVERY_DIVE -> false;

            default -> true;
        };
    }

    private boolean canBodyPartBreakBlocks(DragonCollisionPart part) {
        return switch (part) {
            case LEFT_LEG,
                 RIGHT_LEG,
                 TAIL_ROOT,
                 TAIL_TIP -> false;

            case UNKNOWN -> false;

            default -> true;
        };
    }

    private void breakBlocksInBodyBox(ServerLevel level, DragonCollisionBox box) {
        AABB area = aabbFromPoints(box.points()).inflate(0.35D);

        int minX = (int) Math.floor(area.minX);
        int minY = (int) Math.floor(area.minY);
        int minZ = (int) Math.floor(area.minZ);
        int maxX = (int) Math.ceil(area.maxX);
        int maxY = (int) Math.ceil(area.maxY);
        int maxZ = (int) Math.ceil(area.maxZ);

        int groundLimitY = this.blockPosition().getY();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (y <= groundLimitY) {
                        continue;
                    }

                    BlockPos pos = new BlockPos(x, y, z);
                    var state = level.getBlockState(pos);

                    if (state.isAir()) continue;
                    if (!canBodyDestroyBlock(level, pos, state)) continue;

                    level.destroyBlock(pos, false, this);
                }
            }
        }
    }

    private static AABB aabbFromPoints(Vec3[] points) {
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double minZ = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        double maxZ = -Double.MAX_VALUE;

        for (Vec3 p : points) {
            minX = Math.min(minX, p.x);
            minY = Math.min(minY, p.y);
            minZ = Math.min(minZ, p.z);
            maxX = Math.max(maxX, p.x);
            maxY = Math.max(maxY, p.y);
            maxZ = Math.max(maxZ, p.z);
        }

        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    private boolean canBodyDestroyBlock(ServerLevel level, BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        if (state.getDestroySpeed(level, pos) < 0.0F) {
            return false;
        }

        var block = state.getBlock();

        if (block == Blocks.BEDROCK) return false;
        if (block == Blocks.END_PORTAL) return false;
        if (block == Blocks.END_PORTAL_FRAME) return false;
        if (block == Blocks.BARRIER) return false;
        if (block == Blocks.COMMAND_BLOCK) return false;
        if (block == Blocks.CHAIN_COMMAND_BLOCK) return false;
        if (block == Blocks.REPEATING_COMMAND_BLOCK) return false;
        if (block == Blocks.STRUCTURE_BLOCK) return false;
        if (block == Blocks.JIGSAW) return false;

        return true;
    }

    private int airAttackCooldown = 160;

    public boolean tryConsumeAirAttackCooldown() {
        if (airAttackCooldown > 0) {
            return false;
        }

        airAttackCooldown = 260 + this.getRandom().nextInt(160);
        return true;
    }

    private void tickAirAttackCooldown() {
        if (this.level().isClientSide()) return;
        if (this.isCombatLocked()) return;
        if (this.isIntroStateNow()) return;
        if (this.getDragonState() == DragonState.DEAD) return;

        if (airAttackCooldown > 0) {
            airAttackCooldown--;
        }
    }




    public boolean shouldPunishOverpoweredEquipment(LivingEntity target) {
        if (target == null || !target.isAlive()) {
            return false;
        }

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = target.getItemBySlot(slot);

            if (isOverpoweredEquipment(stack)) {
                return true;
            }
        }

        for (ItemStack stack : com.licht_meilleur.the_end_of_dragon.compat.TedAccessories.getAccessories(target)) {
            if (isOverpoweredEquipment(stack)) {
                return true;
            }
        }

        return false;
    }

    private boolean isOverpoweredEquipment(ItemStack stack) {
        if (!isRoarTargetItem(stack)) {
            return false;
        }

        if (stack.has(net.minecraft.core.component.DataComponents.UNBREAKABLE)) {
            return true;
        }

        return stack.isDamageableItem() && stack.getMaxDamage() >= 1000;
    }





    private boolean isIntroState(DragonState state) {
        return switch (state) {
            case INTRO_RISE,
                 INTRO_WAIT_PORTAL,
                 INTRO_FLY_TO_PORTAL,
                 INTRO_DIVE_TO_PORTAL,
                 INTRO_SUPER_LANDING -> true;
            default -> false;
        };
    }

    private boolean isAttackState(DragonState state) {
        return switch (state) {
            case ORB_OF_ANNIHILATION,
                 ROAR_OF_OBLITERATION,
                 FLAMES_OF_RAGNAROK,
                 LIGHT_OF_DESTRUCTION,
                 PHOTON_BLASTER,
                 PHOTON_BUSTER,
                 JUDGMENT_RAY,
                 BLASTER_TACKLE,
                 SUPER_LANDING,
                 FIGURE_EIGHT,
                 FLY_ASCEND,
                 FLY_DESCEND,
                 TAIL_WHIP -> true;

            default -> false;
        };
    }

    @Override
    public boolean hurtServer(
            ServerLevel level,
            DamageSource source,
            float damage
    ) {
        if (this.getDragonState() == DragonState.DEAD
                || this.deathSequenceStarted) {
            return false;
        }

        /*
         * 奈落ダメージは受けず、緊急復帰へ移行する。
         */
        if (source.is(DamageTypes.FELL_OUT_OF_WORLD)) {
            triggerVoidRecovery(level);
            return false;
        }

        damage = reduceIncomingBossDamage(damage);

        return super.hurtServer(level, source, damage);
    }

    private void tickVoidRecovery(ServerLevel level) {
        if (!this.isAlive()) {
            return;
        }

        if (this.getDragonState() == DragonState.DEAD
                || this.deathSequenceStarted) {
            return;
        }

        if (this.isIntroStateNow()) {
            return;
        }

        if (this.getDragonState() == DragonState.RECOVERY_ASCEND
                || this.getDragonState() == DragonState.RECOVERY_RETURN) {
            return;
        }

        /*
         * 奈落ダメージが始まる前に復帰させる。
         * +8～+16程度で調整可能。
         */
        double recoveryY = level.getMinY() + 12.0D;

        if (this.getY() <= recoveryY) {
            triggerVoidRecovery(level);
        }
    }

    private void triggerVoidRecovery(ServerLevel level) {
        if (!this.isAlive()) {
            return;
        }

        if (this.getDragonState() == DragonState.DEAD
                || this.deathSequenceStarted) {
            return;
        }

        if (this.getDragonState() == DragonState.RECOVERY_ASCEND
                || this.getDragonState() == DragonState.RECOVERY_RETURN) {
            return;
        }

        this.setHealth(Math.max(this.getHealth(), 1.0F));
        this.setDeltaMovement(Vec3.ZERO);
        this.fallDistance = 0.0F;

        this.attackStateMachine.cancelAirSequence();
        this.startEmergencyRecovery();
    }

//ダメージ軽減
    private float reduceIncomingBossDamage(float damage) {
        if (damage >= 500.0F) {
            return damage * 0.01F; // 99%カット
        }

        if (damage >= 100.0F) {
            return damage * 0.30F; // 70%カット
        }

        if (damage >= 50.0F) {
            return damage * 0.50F; // 50%カット
        }

        return damage;
    }

    private static final float BOSS_FIGHT_PLAYER_HEALTH_CAP = 40.0F; // ハート20個分

    private void applyBossPresenceLimits(ServerLevel level) {
        AABB area = this.getBoundingBox().inflate(128.0D);

        var players = level.getEntitiesOfClass(
                net.minecraft.server.level.ServerPlayer.class,
                area,
                player -> player.isAlive()
                        && !player.isCreative()
                        && !player.isSpectator()
        );

        for (var player : players) {
            if (player.getHealth() > BOSS_FIGHT_PLAYER_HEALTH_CAP) {
                player.setHealth(BOSS_FIGHT_PLAYER_HEALTH_CAP);
            }
        }
    }

    public boolean isCombatLocked() {
        DragonState state = this.getDragonState();

        return isIntroState(state)
                || isAttackState(state)
                || state == DragonState.FLY_START
                || state == DragonState.FLY_ASCEND
                || state == DragonState.FLY_DESCEND
                || state == DragonState.FIGURE_EIGHT
                || state == DragonState.FLY_SHOT
                || state == DragonState.FALL
                || state == DragonState.LANDING;
    }

    public boolean isRecoveringNeeded(ServerLevel level) {
        Vec3 center = this.arenaCenter(level);

        double dx = this.getX() - center.x;
        double dz = this.getZ() - center.z;
        double horizontalSq = dx * dx + dz * dz;

        return this.getY() < center.y - 18.0D
                || horizontalSq > 210.0D * 210.0D;
    }

    public void setBossYawOnly(Vec3 dir) {
        if (dir.lengthSqr() < 1.0E-6D) {
            return;
        }

        Vec3 n = new Vec3(dir.x, 0.0D, dir.z);

        if (n.lengthSqr() < 1.0E-6D) {
            return;
        }

        n = n.normalize();

        float yaw = (float) Math.toDegrees(Math.atan2(-n.x, n.z));

        this.setVisualRotation(yaw, 0.0F);
    }




    private void tickAttackVfx() {
        if (!(this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
            return;
        }

    /*

    com.licht_meilleur.the_end_of_dragon.entity.hitbox.DragonLocatorSampler.debugDrawLocator(
            serverLevel,
            this,
            com.licht_meilleur.the_end_of_dragon.entity.hitbox.DragonLocators.FRONT_LEFT_JET
    );

     */
    /*
    //レイキャスト確認
    DragonCollisionBox box = getCollisionPartBox(DragonCollisionPart.FRONT_LEFT_HAND);
    if (box != null && box.obb() != null) {
        debugDrawRayFromHandTip(
                serverLevel,
                DragonCollisionPart.FRONT_LEFT_HAND,
                box.obb().axisY(),
                false,
                64.0D
        );
    }

     */

        //頭の判定確認
        //debugDrawHeadCollision(serverLevel);

        // DEBUG: idleでも常時レーザー表示
    /*
    if (this.tickCount % 2 == 0) {
        updateAttachedVfx(
                serverLevel,
                TedVfxSpecs.FRONT_LEFT_LASER,
                true
        );
        updateAttachedVfx(serverLevel, TedVfxSpecs.FRONT_RIGHT_LASER, true);
        updateAttachedVfx(serverLevel, TedVfxSpecs.BACK_LEFT_LASER, true);
        updateAttachedVfx(serverLevel, TedVfxSpecs.BACK_RIGHT_LASER, true);
    }

     */

        int age = this.getDragonStateAgeTicks();

        switch (this.getDragonState()) {


            case ORB_OF_ANNIHILATION -> {
                if (age == ORB_CHARGE_START) {
                    TedSoundHelper.playBossSound(
                            serverLevel,
                            this,
                            ModSounds.TED_ORB_CHARGE,
                            7.0F,
                            1.0F
                    );
                }

                if (between(age, ORB_CHARGE_START, ORB_FIRE_TICK - 1)) {
                    updateOrbCharge(serverLevel, age);
                }

                if (age == ORB_FIRE_TICK) {
                    TedSoundHelper.playBossSound(
                            serverLevel,
                            this,
                            ModSounds.TED_ORB_SHOOTING,
                            9.0F,
                            1.0F
                    );

                    fireOrbOfAnnihilation(serverLevel);
                }
            }

            case PHOTON_BLASTER -> {
                if (age == PHOTON_FIRE_START) {
                    TedSoundHelper.playBossSound(
                            serverLevel,
                            this,
                            ModSounds.TED_PHOTON_BLASTER,
                            8.0F,
                            1.0F
                    );
                }

                boolean firing =
                        between(age, PHOTON_FIRE_START, PHOTON_FIRE_END);

                if (firing) {
                    updatePhotonLasers(serverLevel);
                }
            }

            case FLAMES_OF_RAGNAROK -> {
                if (age == FLAMES_FIRE_START) {
                    TedSoundHelper.playBossSound(
                            serverLevel,
                            this,
                            ModSounds.TED_RAGNAROK,
                            11.0F,
                            1.0F
                    );
                }

                boolean firing =
                        between(age, FLAMES_FIRE_START, FLAMES_FIRE_END);

                if (firing) {
                    updateRagnarokLaserBeam(
                            serverLevel,
                            DragonCollisionPart.FRONT_LEFT_HAND
                    );
                    updateRagnarokLaserBeam(
                            serverLevel,
                            DragonCollisionPart.FRONT_RIGHT_HAND
                    );
                    updateRagnarokLaserBeam(
                            serverLevel,
                            DragonCollisionPart.BACK_LEFT_HAND
                    );
                    updateRagnarokLaserBeam(
                            serverLevel,
                            DragonCollisionPart.BACK_RIGHT_HAND
                    );
                }

                syncChildrenNow();
            }

            case LIGHT_OF_DESTRUCTION -> {
                if (age == 20) {
                    TedSoundHelper.playBossSound(
                            serverLevel,
                            this,
                            ModSounds.TED_LIGHTING,
                            7.0F,
                            1.0F
                    );
                }

                if (age >= 20 && age <= 30) {
                    updateLightOfDestruction(serverLevel, age);
                }
            }

            case FLY_SHOT -> {
                if (age == FLY_SHOT_FIRE_TICK) {
                    fireLightProjectile(serverLevel);
                }
            }

            case ROAR_OF_OBLITERATION -> {
                if (age == 1) {
                    TedSoundHelper.playBossSound(
                            serverLevel,
                            this,
                            ModSounds.TED_ROAR,
                            10.0F,
                            1.0F
                    );

                    spawnRoarOfObliterationVfx(serverLevel);
                }

                if (age == 10) {
                    applyRoarOfObliteration(serverLevel);
                }
            }

            case BLASTER_TACKLE -> {
                if (age == 7) {
                    TedSoundHelper.playTackleJet(
                            serverLevel,
                            this
                    );
                }

                if (between(age, 7, 8)) {
                    updateFlightJets(serverLevel);
                }

                if (age >= 9) {
                    updateBlasterTackleMove(serverLevel, age);
                }
            }

            case TAIL_WHIP -> {
                if (!tailWhipHitDone && age >= 6 && age <= 12) {
                    tailWhipHitDone = true;
                    applyTailWhipDamage(serverLevel);
                }
            }

            case JUDGMENT_RAY -> {
                if (age == JUDGMENT_FIRE_START) {
                    TedSoundHelper.playBossSound(
                            serverLevel,
                            this,
                            ModSounds.TED_JUDGMENT_RAY,
                            9.0F,
                            1.0F
                    );
                }

                if (age >= JUDGMENT_CHARGE_START
                        && age < JUDGMENT_FIRE_START) {
                    updateJudgmentCharge(serverLevel);
                }

                boolean firing =
                        age >= JUDGMENT_FIRE_START
                                && age <= JUDGMENT_FIRE_END
                                && judgmentShotCount < JUDGMENT_MAX_SHOTS;

                if (firing) {
                    judgmentShotCooldown--;

                    if (judgmentShotCooldown <= 0) {
                        boolean fired =
                                fireJudgmentProjectile(serverLevel);

                        if (fired) {
                            judgmentShotCount++;
                            judgmentShotCooldown =
                                    JUDGMENT_SHOT_INTERVAL;
                        } else {
                            judgmentShotCooldown = 1;
                        }
                    }
                }
            }

            case PHOTON_BUSTER -> {
                TedBeamSpec spec = TedBeamSpecs.PHOTON_BUSTER;

                if (age == spec.fireStartTick()) {
                    TedSoundHelper.playBossSound(
                            serverLevel,
                            this,
                            ModSounds.TED_PHOTON_BUSTER,
                            12.0F,
                            1.0F
                    );
                }

                updatePhotonBuster(
                        serverLevel,
                        age,
                        spec
                );
            }



            case SUPER_LANDING -> {
                updateSuperLanding(serverLevel);
            }

            case FALL -> {
                updateRagnarokFall(serverLevel);
            }

            case LANDING -> {
                updateRagnarokLanding(serverLevel);
            }

            case INTRO_RISE -> {
                updateIntroRise(serverLevel, age);
            }

            case INTRO_WAIT_PORTAL -> {
                updateIntroWaitPortal(serverLevel, age);
            }

            case INTRO_FLY_TO_PORTAL -> {
                updateIntroFlyToPortal(serverLevel);
            }

            case INTRO_DIVE_TO_PORTAL -> {
                updateIntroDiveToPortal(serverLevel);
            }

            case INTRO_SUPER_LANDING -> {
                updateIntroSuperLanding(serverLevel);

            }

            default -> {
                //hideAllLaserVfx(serverLevel);
            }
        }
    }

    private final DragonAttackStateMachine attackStateMachine =
            new DragonAttackStateMachine(this);



    public void debugStartRagnarok() {
        this.startRagnarokSequence();
    }

    public void debugStartFigureEight() {
        this.startFigureEightSequence();
    }



    private void tickFlightJetSound(ServerLevel level) {
        if (!isJetFlightState()) {
            jetSoundCooldown = 0;
            return;
        }

        if (jetSoundCooldown > 0) {
            jetSoundCooldown--;
            return;
        }

        TedSoundHelper.playJet(
                level,
                this
        );

        /*
         * ted_jet.oggが約1秒なら20tick。
         * 2秒なら40tick程度へ変更する。
         */
        jetSoundCooldown = 38;
    }

    private boolean isJetFlightState() {
        return switch (this.getDragonState()) {
            case FLY_START,
                 FLY,
                 FLY_ASCEND,
                 FLY_DESCEND,
                 FIGURE_EIGHT,
                 FLY_SHOT,
                 FLAMES_OF_RAGNAROK,
                 JUDGMENT_RAY,
                 RECOVERY_ASCEND,
                 RECOVERY_RETURN,
                 INTRO_RISE,
                 INTRO_WAIT_PORTAL,
                 INTRO_FLY_TO_PORTAL,
                 INTRO_DIVE_TO_PORTAL -> true;

            /*
             * FALLは自由落下なのでジェット音を鳴らさない。
             * LANDINGも着地モーションなので鳴らさない。
             */
            default -> false;
        };
    }


    private void updatePhotonBuster(
            ServerLevel level,
            int age,
            TedBeamSpec spec
    ) {
        DragonCollisionBox head =
                getCollisionPartBox(DragonCollisionPart.HEAD);

        if (head == null || head.obb() == null) {
            return;
        }

        Vec3 headForward = head.obb().axisY().normalize();

        Vec3 start = head.obb().center()
                .add(headForward.scale(2.5D));

        // 攻撃開始時に照準を固定
        if (this.photonBusterLockedTarget == null) {
            lockPhotonBusterTarget(level, start, headForward);
        }

        // チャージ中
        if (age < spec.fireStartTick()) {
            updatePhotonBusterCharge(level, start, age, spec);
            return;
        }

        // 発射終了
        if (age > spec.fireEndTick()) {
            hidePhotonBusterBeam();
            return;
        }

        Vec3 direction =
                getPhotonBusterSweepDirection(start, age, spec, headForward);

        /*
         * Photon Busterはブロックを貫通して破壊するため、
         * 最初のブロックで止まる通常レイキャストは使用しない。
         */
        double length = spec.maxLength();

        TedVfxEntity beamVfx =
                getOrCreatePhotonBusterBeam(level, spec);

        if (beamVfx == null) {
            return;
        }

        Vec3 up = head.obb().axisZ().normalize();

        // forwardとupがほぼ平行なら代替軸を使用
        if (Math.abs(direction.dot(up)) > 0.98D) {
            up = new Vec3(0.0D, 1.0D, 0.0D);

            if (Math.abs(direction.dot(up)) > 0.98D) {
                up = new Vec3(1.0D, 0.0D, 0.0D);
            }
        }

        beamVfx.setBasis(direction, up);

        beamVfx.snapTo(
                start.x,
                start.y,
                start.z,
                0.0F,
                0.0F
        );

        beamVfx.updateVfx(
                spec.modelScale(),
                (float) length
        );

        TedBeamHitbox beamHitbox = new TedBeamHitbox(
                start,
                direction,
                length,
                spec.hitRadius()
        );

        beamHitbox.damageEntities(
                level,
                this,
                TedConfig.values.photonBusterDamage
                        * (float) TedConfig.values.damageMultiplier,
                entity -> entity.isAlive()
                        && !(entity instanceof TheEndOfDragonCoreEntity)
                        && !(entity instanceof TheEndOfDragonEntity)
                        && !(entity instanceof TedVfxEntity)
        );

        spawnPhotonBusterOuterParticles(
                level,
                start,
                direction,
                length
        );

        spawnPhotonBusterImpact(
                level,
                start.add(direction.scale(length))
        );

        if (spec.destroyBlocks()
                && TedConfig.values.enableBlockBreak
                && age % spec.blockBreakInterval() == 0) {
            breakBlocksAlongPhotonBuster(
                    level,
                    start,
                    direction,
                    length,
                    spec
            );
        }
    }

    private void lockPhotonBusterTarget(
            ServerLevel level,
            Vec3 start,
            Vec3 fallbackForward
    ) {
        LivingEntity target = findBossTarget(level);

        if (target != null && target.isAlive()) {
            this.photonBusterLockedTarget =
                    target.getEyePosition();

            Vec3 horizontalDirection =
                    target.position().subtract(this.position());

            this.setBossYawOnly(horizontalDirection);
            return;
        }

        this.photonBusterLockedTarget =
                start.add(fallbackForward.scale(80.0D));
    }

    private Vec3 getPhotonBusterSweepDirection(
            Vec3 start,
            int age,
            TedBeamSpec spec,
            Vec3 fallbackForward
    ) {
        if (this.photonBusterLockedTarget == null) {
            return fallbackForward;
        }

        double duration =
                Math.max(
                        1.0D,
                        spec.fireEndTick() - spec.fireStartTick()
                );

        double progress = Mth.clamp(
                (age - spec.fireStartTick()) / duration,
                0.0D,
                1.0D
        );

        /*
         * 固定したプレイヤー位置を基準に、
         * 下6ブロックから上18ブロックまで薙ぎ上げる。
         */
        Vec3 lowTarget =
                this.photonBusterLockedTarget.add(0.0D, -6.0D, 0.0D);

        Vec3 highTarget =
                this.photonBusterLockedTarget.add(0.0D, 18.0D, 0.0D);

        Vec3 aimPoint = lowTarget.lerp(highTarget, progress);
        Vec3 direction = aimPoint.subtract(start);

        if (direction.lengthSqr() < 1.0E-6D) {
            return fallbackForward;
        }

        return direction.normalize();
    }

    private void updatePhotonBusterCharge(
            ServerLevel level,
            Vec3 mouth,
            int age,
            TedBeamSpec spec
    ) {
        double progress = Mth.clamp(
                age / (double) Math.max(1, spec.fireStartTick()),
                0.0D,
                1.0D
        );

        double spread = 0.6D + progress * 1.4D;

        level.sendParticles(
                ParticleTypes.END_ROD,
                mouth.x,
                mouth.y,
                mouth.z,
                8,
                spread,
                spread,
                spread,
                0.02D
        );

        level.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,
                mouth.x,
                mouth.y,
                mouth.z,
                4,
                spread * 0.7D,
                spread * 0.7D,
                spread * 0.7D,
                0.03D
        );
    }

    private TedVfxEntity getOrCreatePhotonBusterBeam(
            ServerLevel level,
            TedBeamSpec spec
    ) {
        if (this.photonBusterBeamVfxId != -1) {
            Entity entity =
                    level.getEntity(this.photonBusterBeamVfxId);

            if (entity instanceof TedVfxEntity beam
                    && !beam.isRemoved()) {
                return beam;
            }
        }

        TedVfxEntity beam =
                ModEntities.TED_VFX.create(
                        level,
                        EntitySpawnReason.EVENT
                );

        if (beam == null) {
            return null;
        }

        beam.setup(
                spec.type(),
                spec.modelScale(),
                1.0F,
                999999
        );

        beam.setProjectileOwner(this);

        beam.snapTo(
                this.getX(),
                this.getY(),
                this.getZ(),
                0.0F,
                0.0F
        );

        level.addFreshEntity(beam);

        this.photonBusterBeamVfxId = beam.getId();

        return beam;
    }


    private void spawnPhotonBusterOuterParticles(
            ServerLevel level,
            Vec3 start,
            Vec3 direction,
            double length
    ) {
        Vec3 helperUp = Math.abs(direction.y) < 0.95D
                ? new Vec3(0.0D, 1.0D, 0.0D)
                : new Vec3(1.0D, 0.0D, 0.0D);

        Vec3 right = direction.cross(helperUp).normalize();
        Vec3 up = right.cross(direction).normalize();

        int steps = Math.max(
                8,
                Math.min(48, (int) (length / 3.0D))
        );

        for (int i = 0; i <= steps; i++) {
            double t = i / (double) steps;

            Vec3 center =
                    start.add(direction.scale(length * t));

            double angle =
                    this.tickCount * 0.28D
                            + t * Math.PI * 8.0D;

            double radius = 2.8D;

            Vec3 particlePos = center
                    .add(right.scale(Math.cos(angle) * radius))
                    .add(up.scale(Math.sin(angle) * radius));

            level.sendParticles(
                    ParticleTypes.END_ROD,
                    particlePos.x,
                    particlePos.y,
                    particlePos.z,
                    1,
                    0.08D,
                    0.08D,
                    0.08D,
                    0.0D
            );
        }
    }

    private void spawnPhotonBusterImpact(
            ServerLevel level,
            Vec3 pos
    ) {
        level.sendParticles(
                ParticleTypes.END_ROD,
                pos.x,
                pos.y,
                pos.z,
                32,
                1.8D,
                1.8D,
                1.8D,
                0.08D
        );

        level.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,
                pos.x,
                pos.y,
                pos.z,
                20,
                1.3D,
                1.3D,
                1.3D,
                0.12D
        );

        level.sendParticles(
                ParticleTypes.EXPLOSION,
                pos.x,
                pos.y,
                pos.z,
                1,
                0.2D,
                0.2D,
                0.2D,
                0.0D
        );
    }

    private void breakBlocksAlongPhotonBuster(
            ServerLevel level,
            Vec3 start,
            Vec3 direction,
            double length,
            TedBeamSpec spec
    ) {
        Vec3 dir = direction.normalize();
        Vec3 end = start.add(dir.scale(length));

        double radius = spec.destroyRadius();
        double radiusSqr = radius * radius;

        /*
         * ビーム全体を覆うAABBを作る。
         */
        double minX = Math.min(start.x, end.x) - radius;
        double minY = Math.min(start.y, end.y) - radius;
        double minZ = Math.min(start.z, end.z) - radius;

        double maxX = Math.max(start.x, end.x) + radius;
        double maxY = Math.max(start.y, end.y) + radius;
        double maxZ = Math.max(start.z, end.z) + radius;

        BlockPos minPos = BlockPos.containing(
                Math.floor(minX),
                Math.floor(minY),
                Math.floor(minZ)
        );

        BlockPos maxPos = BlockPos.containing(
                Math.ceil(maxX),
                Math.ceil(maxY),
                Math.ceil(maxZ)
        );

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int x = minPos.getX(); x <= maxPos.getX(); x++) {
            for (int y = minPos.getY(); y <= maxPos.getY(); y++) {
                for (int z = minPos.getZ(); z <= maxPos.getZ(); z++) {
                    mutable.set(x, y, z);

                    /*
                     * 未ロードチャンクを、この破壊処理だけで強制ロードしない。
                     */
                    if (!level.hasChunkAt(mutable)) {
                        continue;
                    }

                    Vec3 blockCenter = Vec3.atCenterOf(mutable);

                    /*
                     * ブロック中心とビーム線分との距離を調べる。
                     * 半径内なら円柱状の破壊範囲に入っている。
                     */
                    double distanceSqr = distanceToSegmentSqr(
                            blockCenter,
                            start,
                            end
                    );

                    if (distanceSqr > radiusSqr) {
                        continue;
                    }

                    var state = level.getBlockState(mutable);

                    if (state.isAir()) {
                        continue;
                    }

                    if (!canPhotonBusterDestroyBlock(
                            level,
                            mutable,
                            state
                    )) {
                        continue;
                    }

                    level.destroyBlock(
                            mutable,
                            false,
                            this
                    );
                }
            }
        }
    }

    private static double distanceToSegmentSqr(
            Vec3 point,
            Vec3 start,
            Vec3 end
    ) {
        Vec3 segment = end.subtract(start);
        double segmentLengthSqr = segment.lengthSqr();

        if (segmentLengthSqr < 1.0E-8D) {
            return point.distanceToSqr(start);
        }

        double t = point.subtract(start).dot(segment)
                / segmentLengthSqr;

        t = Mth.clamp(t, 0.0D, 1.0D);

        Vec3 closest = start.add(segment.scale(t));

        return point.distanceToSqr(closest);
    }



    private boolean canPhotonBusterDestroyBlock(
            ServerLevel level,
            BlockPos pos,
            net.minecraft.world.level.block.state.BlockState state
    ) {
        if (state.getDestroySpeed(level, pos) < 0.0F) {
            return false;
        }

        var block = state.getBlock();

        if (block == Blocks.BEDROCK) return false;
        if (block == Blocks.END_PORTAL) return false;
        if (block == Blocks.END_PORTAL_FRAME) return false;
        if (block == Blocks.BARRIER) return false;

        if (block == Blocks.COMMAND_BLOCK) return false;
        if (block == Blocks.CHAIN_COMMAND_BLOCK) return false;
        if (block == Blocks.REPEATING_COMMAND_BLOCK) return false;

        if (block == Blocks.STRUCTURE_BLOCK) return false;
        if (block == Blocks.JIGSAW) return false;

        return true;
    }

    private void hidePhotonBusterBeam() {
        if (this.photonBusterBeamVfxId == -1) {
            return;
        }

        Entity entity =
                this.level().getEntity(
                        this.photonBusterBeamVfxId
                );

        if (entity instanceof TedVfxEntity beam) {
            beam.discard();
        }

        this.photonBusterBeamVfxId = -1;
    }

    public LivingEntity findJudgmentTarget(ServerLevel level) {
        AABB area = this.getBoundingBox().inflate(256.0D);

        ServerPlayer highestPlayer = level.getEntitiesOfClass(
                        ServerPlayer.class,
                        area,
                        p -> p.isAlive()
                                && !p.isCreative()
                                && !p.isSpectator()
                ).stream()
                .max(Comparator.comparingDouble(Entity::getY))
                .orElse(null);

        return highestPlayer != null
                ? highestPlayer
                : findBossTarget(level);

    }

    private Vec3 getJudgmentRayMuzzlePosition() {
        DragonCollisionBox head =
                getCollisionPartBox(DragonCollisionPart.HEAD);

        if (head != null && head.obb() != null) {
            Vec3 forward = head.obb().axisY().normalize();

            return head.obb().center()
                    .add(forward.scale(2.5D));
        }

        /*
         * CollisionアニメからHEADが取れない場合の予備位置。
         * Coreの向きと概算の頭位置を使用する。
         */
        Vec3 forward = DragonLocatorSampler.forward(this);

        if (forward.lengthSqr() < 1.0E-7D) {
            forward = new Vec3(0.0D, 0.0D, 1.0D);
        } else {
            forward = forward.normalize();
        }

        return this.position()
                .add(0.0D, 5.0D, 0.0D)
                .add(forward.scale(6.0D));
    }

    private Vec3 getJudgmentRayForward() {
        DragonCollisionBox head =
                getCollisionPartBox(DragonCollisionPart.HEAD);

        if (head != null && head.obb() != null) {
            return head.obb().axisY().normalize();
        }

        Vec3 forward = DragonLocatorSampler.forward(this);

        if (forward.lengthSqr() < 1.0E-7D) {
            return new Vec3(0.0D, 0.0D, 1.0D);
        }

        return forward.normalize();
    }

    private Vec3 getJudgmentRayUp(Vec3 forward) {
        DragonCollisionBox head =
                getCollisionPartBox(DragonCollisionPart.HEAD);

        if (head != null && head.obb() != null) {
            Vec3 up = head.obb().axisZ();

            if (up.lengthSqr() > 1.0E-7D
                    && Math.abs(up.normalize().dot(forward)) < 0.98D) {
                return up.normalize();
            }
        }

        if (Math.abs(forward.y) < 0.98D) {
            return new Vec3(0.0D, 1.0D, 0.0D);
        }

        return new Vec3(1.0D, 0.0D, 0.0D);
    }


    private boolean fireJudgmentProjectile(ServerLevel level) {
        LivingEntity target = findJudgmentTarget(level);

        if (target == null || !target.isAlive()) {
            return false;
        }

        if (target == null || !target.isAlive()) {
            //System.out.println("[TED JUDGMENT] target missing");
            return false;
        }

        Vec3 forward = getJudgmentRayForward();
        Vec3 up = getJudgmentRayUp(forward);
        Vec3 start = getJudgmentRayMuzzlePosition();

        Vec3 targetDirection =
                target.getEyePosition()
                        .subtract(start);

        if (targetDirection.lengthSqr() < 1.0E-7D) {
            targetDirection = forward;
        } else {
            targetDirection = targetDirection.normalize();
        }

        Vec3 spread = new Vec3(
                this.random.nextGaussian() * 0.12D,
                this.random.nextGaussian() * 0.09D,
                this.random.nextGaussian() * 0.12D
        );

        Vec3 direction =
                targetDirection.add(spread);

        if (direction.lengthSqr() < 1.0E-7D) {
            direction = targetDirection;
        } else {
            direction = direction.normalize();
        }

        TedVfxEntity projectile =
                ModEntities.TED_VFX.create(
                        level,
                        EntitySpawnReason.EVENT
                );

        if (projectile == null) {
            //System.out.println("[TED JUDGMENT] TED_VFX create failed");
            return false;
        }

        projectile.setup(TedProjectileSpecs.JUDGMENT_RAY);
        projectile.setProjectileOwner(this);
        projectile.setHomingTarget(target);

        projectile.snapTo(
                start.x,
                start.y,
                start.z,
                0.0F,
                0.0F
        );

        projectile.setDeltaMovement(
                direction.scale(
                        TedProjectileSpecs.JUDGMENT_RAY.speed()
                )
        );

        projectile.setBasis(direction, up);

        boolean added = level.addFreshEntity(projectile);

        if (added) {
            TedSoundHelper.playShot(
                    level,
                    start
            );
        }

        return added;
    }


    private void updateJudgmentCharge(ServerLevel level) {

        DragonCollisionBox head =
                getCollisionPartBox(DragonCollisionPart.HEAD);

        if (head == null) {
            return;
        }

        Vec3 center = head.obb().center();

        level.sendParticles(
                ParticleTypes.END_ROD,
                center.x,
                center.y,
                center.z,
                6,
                0.35,
                0.35,
                0.35,
                0.01
        );

        level.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,
                center.x,
                center.y,
                center.z,
                3,
                0.25,
                0.25,
                0.25,
                0.02
        );
    }



    private void applyTailWhipDamage(ServerLevel level) {
        double radius = TedConfig.values.tailWhipRadius;

        AABB area = this.getBoundingBox().inflate(radius, 5.0D, radius);

        var entities = level.getEntitiesOfClass(
                LivingEntity.class,
                area,
                entity -> entity.isAlive()
                        && entity != this
                        && !(entity instanceof TheEndOfDragonCoreEntity)
                        && !(entity instanceof TheEndOfDragonEntity)
                        && !(entity instanceof TheEndOfDragonDisplayEntity)
                        && !(entity instanceof TheEndOfDragonCollisionEntity)
        );

        for (LivingEntity entity : entities) {
            Vec3 to = entity.position().subtract(this.position());

            double horizontalSq = to.x * to.x + to.z * to.z;
            if (horizontalSq > radius * radius) {
                continue;
            }

            entity.hurtServer(
                    level,
                    level.damageSources().mobAttack(this),
                    TedConfig.values.tailWhipDamage
                            * (float) TedConfig.values.damageMultiplier
            );

            Vec3 knock = new Vec3(to.x, 0.0D, to.z);

            if (knock.lengthSqr() > 1.0E-6D) {
                knock = knock.normalize();

                entity.push(
                        knock.x * TedConfig.values.tailWhipKnockback,
                        TedConfig.values.tailWhipKnockbackY,
                        knock.z * TedConfig.values.tailWhipKnockback
                );

                entity.hurtMarked = true;
            }
        }
    }


    public void requestFlyShot(ServerLevel level) {
        this.flyShotAnimTicks = 10;
        this.flyShotFireDelay = 5;
    }

    private void tickFlyShotRequest(ServerLevel level) {
        if (flyShotAnimTicks <= 0) {
            return;
        }

        flyShotAnimTicks--;

        if (flyShotFireDelay > 0) {
            flyShotFireDelay--;

            if (flyShotFireDelay == 0) {
                fireLightProjectile(level);
            }
        }
    }

    public boolean isFlyShotAnimPlaying() {
        return flyShotAnimTicks > 0;
    }

    private Vec3 getArenaCenter(ServerLevel level) {
        if (this.introPortalCenter != null) {
            return Vec3.atCenterOf(this.introPortalCenter);
        }

        return new Vec3(0.5D, 70.0D, 0.5D);
    }







    private void updateIntroSuperLanding(ServerLevel level) {
        if (!superLandingImpacted) {
            applyIntroSuperLandingImpact(level);
            superLandingImpacted = true;
        }

        if (this.getDragonStateAgeTicks() > 45) {
            finishIntroAndStartCombat();
        }
    }

    private void applyIntroSuperLandingImpact(ServerLevel level) {
        BlockPos base = findPortalDestroyBase(level);

        int radius = 12;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -8; y <= 12; y++) {
                for (int z = -radius; z <= radius; z++) {
                    double dist = Math.sqrt(x * x + z * z);
                    if (dist > radius) continue;

                    BlockPos pos = base.offset(x, y, z);
                    var state = level.getBlockState(pos);

                    if (state.is(Blocks.BEDROCK)
                            || state.is(Blocks.END_PORTAL)
                            || state.is(Blocks.END_PORTAL_FRAME)) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }

        spawnSuperLandingSmoke(level, Vec3.atCenterOf(base));
        EndPortalSealHandler.sealPortal(level);
    }

    private BlockPos findPortalDestroyBase(ServerLevel level) {
        if (this.introPortalCenter == null) {
            return this.blockPosition();
        }

        BlockPos center = this.introPortalCenter;

        for (int y = -16; y <= 16; y++) {
            for (int x = -8; x <= 8; x++) {
                for (int z = -8; z <= 8; z++) {
                    BlockPos pos = center.offset(x, y, z);

                    if (level.getBlockState(pos).is(Blocks.END_PORTAL)) {
                        return pos;
                    }
                }
            }
        }

        return center;
    }


    private BlockPos introPortalCenter = null;




    private void faceMovementDirection(Vec3 dir) {
        if (dir.lengthSqr() < 1.0E-6D) return;

        Vec3 n = dir.normalize();

        float yaw = (float) Math.toDegrees(Math.atan2(-n.x, n.z));

        double horizontal = Math.sqrt(n.x * n.x + n.z * n.z);
        float pitch = (float) -Math.toDegrees(Math.atan2(n.y, horizontal));

        this.setVisualRotation(yaw, pitch);
    }




    private void updateIntroWaitPortal(ServerLevel level, int age) {
        EndPortalSealHandler.coverPortalArea(level);
        updateFlightJets(level);

        if (age >= 160) {
            this.setDragonState(DragonState.INTRO_DIVE_TO_PORTAL);
        }
    }

    private void updateIntroRise(ServerLevel level, int age) {
        updateFlightJets(level);

        double targetY = this.introPortalCenter != null
                ? this.introPortalCenter.getY() + 1000.0D
                : this.getY() + 1000.0D;

        if (this.getY() < targetY) {
            moveByVector(level, new Vec3(0.0D, 20.0D, 0.0D), false);
            return;
        }

        this.setDragonState(DragonState.INTRO_WAIT_PORTAL);
    }

    public void startIntroAscendFlight() {
        this.attackStateMachine.startIntroAscend();
    }



    private void updateIntroFlyToPortal(ServerLevel level) {
        if (this.introPortalCenter == null) {
            this.setDragonState(DragonState.IDLE);
            return;
        }

        if (this.introPortalAboveTarget == null) {
            this.introPortalAboveTarget = Vec3.atCenterOf(this.introPortalCenter)
                    .add(0.0D, 1000.0D, 0.0D);
        }

        Vec3 current = this.position();

        Vec3 target = new Vec3(
                this.introPortalAboveTarget.x,
                current.y,
                this.introPortalAboveTarget.z
        );

        Vec3 toTarget = target.subtract(current);
        double dist = toTarget.length();

        if (dist < 4.0D) {
            this.snapTo(
                    target.x,
                    target.y,
                    target.z,
                    this.getYRot(),
                    this.getXRot()
            );

            syncChildrenNow();
            this.setDragonState(DragonState.INTRO_DIVE_TO_PORTAL);
            return;
        }

        Vec3 dir = toTarget.normalize();
        Vec3 move = dir.scale(Math.min(20.0D, dist));



        moveByVector(level, move);
        updateFlightJets(level);



        syncChildrenNow();
        updateFlightJets(level);


    }


    private void updateIntroDiveToPortal(ServerLevel level) {
        updateFlightJets(level);

        if (this.introPortalCenter != null) {
            Vec3 center = Vec3.atCenterOf(this.introPortalCenter);

            this.setPos(center.x, this.getY(), center.z);
        }

        moveByVector(level, new Vec3(0.0D, -24.0D, 0.0D), false);

        if (isNearPortalImpactHeight()) {
            this.setDragonState(DragonState.INTRO_SUPER_LANDING);
        }
    }

    private boolean isNearPortalImpactHeight() {
        if (this.introPortalCenter == null) {
            return isNearGroundForSuperLanding((ServerLevel) this.level());
        }

        return this.getY() <= this.introPortalCenter.getY() + 8.0D;
    }

    private boolean combatStarted = true;

    public boolean isCombatStarted() {
        return combatStarted;
    }

    public void setCombatStarted(boolean combatStarted) {
        this.combatStarted = combatStarted;
    }

    public void startIntroSequence(BlockPos portalCenter) {
        this.combatStarted = false;
        this.attackMovementLocked = true;

        this.introPortalCenter = portalCenter.below(1);

        this.introPortalAboveTarget = Vec3.atCenterOf(this.introPortalCenter)
                .add(0.0D, 1000.0D, 0.0D);

        this.setDragonState(DragonState.INTRO_RISE);
    }

    private void finishIntroAndStartCombat() {
        this.combatStarted = true;
        this.attackMovementLocked = false;
        this.setDeltaMovement(Vec3.ZERO);
        this.fallDistance = 0.0F;
        this.setDragonState(DragonState.IDLE);

        this.markAttackStarted();
        syncChildrenNow();

        if (this.level() instanceof ServerLevel level) {
            startEventBgmForPlayers(level);
        }
    }






    private boolean superLandingImpacted = false;



    private void updateSuperLanding(ServerLevel level) {
        if (!superLandingImpacted) {
            moveByVector(level, new Vec3(0.0D, -2.0D, 0.0D));

            if (isNearGroundForSuperLanding(level)) {
                applySuperLandingImpact(level);
                superLandingImpacted = true;
            }
        }
    }

    private int unknownPositionTicks = 0;

    private boolean isUnknownGroundAirState(ServerLevel level) {
        DragonState state = this.getDragonState();

        if (state == DragonState.DEAD) return false;
        if (this.isIntroStateNow()) return false;

        if (state == DragonState.RECOVERY_ASCEND
                || state == DragonState.RECOVERY_RETURN
                || state == DragonState.RECOVERY_DIVE) {
            return false;
        }

        if (this.isAirAttackStateForRecovery()) {
            return false;
        }

        boolean airborne = this.isAirborneBoss(level);
        boolean nearGround = this.isNearGroundForSuperLandingPublic(level);

        return !airborne && !nearGround;
    }

    private void tickUnknownPositionRecovery(ServerLevel level) {
        if (isUnknownGroundAirState(level)) {
            unknownPositionTicks++;
        } else {
            unknownPositionTicks = 0;
        }

        /*
         * 一瞬の段差では発動せず、
         * 約1秒間おかしい状態が続いたら復帰。
         */
        if (unknownPositionTicks >= 20) {
            unknownPositionTicks = 0;
            startRecoveryDiveSequence();
        }
    }


    private boolean isNearGroundForSuperLanding(ServerLevel level) {
        BlockPos base = this.blockPosition();

        for (int y = 0; y <= 5; y++) {
            BlockPos pos = base.below(y);

            if (!level.getBlockState(pos).isAir()) {
                return true;
            }
        }

        return false;
    }



    private void applySuperLandingImpact(ServerLevel level) {
        Vec3 center = this.introPortalCenter != null
                ? Vec3.atCenterOf(this.introPortalCenter)
                : this.position();

        BlockPos base = BlockPos.containing(center);

        int radius = 7;



        if (TedConfig.values.enableBlockBreak) {
            for (int x = -radius; x <= radius; x++) {
                for (int y = -1; y <= 0; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        double dist = Math.sqrt(x * x + z * z);
                        if (dist > radius) continue;

                        BlockPos pos = base.offset(x, y, z);
                        var state = level.getBlockState(pos);

                        if (state.isAir()) continue;
                        if (!canSuperLandingDestroyBlock(level, pos, state)) continue;

                        if (state.is(Blocks.BEDROCK)
                                || state.is(Blocks.END_PORTAL)
                                || state.is(Blocks.END_PORTAL_FRAME)) {
                            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                        } else {
                            level.destroyBlock(pos, false, this);
                        }
                    }
                }
            }
        }

// ダメージはブロック破壊ループの外で1回だけ
        float damage = TedConfig.values.superLandingDamage
                * (float) TedConfig.values.damageMultiplier;

        AABB area = new AABB(center, center).inflate(14.0D);

        var entities = level.getEntitiesOfClass(
                LivingEntity.class,
                area,
                entity -> entity.isAlive()
                        && !(entity instanceof TheEndOfDragonCoreEntity)
                        && !(entity instanceof TheEndOfDragonEntity)
        );

        for (LivingEntity entity : entities) {
            entity.hurtServer(
                    level,
                    level.damageSources().mobAttack(this),
                    damage
            );

            Vec3 knock = entity.position().subtract(center);
            if (knock.lengthSqr() > 1.0E-6D) {
                knock = knock.normalize();
                entity.push(knock.x * 2.8D, 0.8D, knock.z * 2.8D);
                entity.hurtMarked = true;
            }
        }


        spawnSuperLandingSmoke(level, center);


        EndPortalSealHandler.sealPortal(level);

    }



    private boolean canSuperLandingDestroyBlock(
            ServerLevel level,
            BlockPos pos,
            net.minecraft.world.level.block.state.BlockState state
    ) {
        var block = state.getBlock();

        if (block == Blocks.BARRIER) return false;
        if (block == Blocks.COMMAND_BLOCK) return false;
        if (block == Blocks.CHAIN_COMMAND_BLOCK) return false;
        if (block == Blocks.REPEATING_COMMAND_BLOCK) return false;
        if (block == Blocks.STRUCTURE_BLOCK) return false;
        if (block == Blocks.JIGSAW) return false;

        // SUPER_LANDINGでは BEDROCK / END_PORTAL / END_PORTAL_FRAME も壊す
        return true;
    }

    private void spawnSuperLandingSmoke(ServerLevel level, Vec3 center) {
        level.sendParticles(
                ParticleTypes.EXPLOSION,
                center.x,
                center.y + 0.5D,
                center.z,
                8,
                3.0D,
                1.0D,
                3.0D,
                0.0D
        );

        level.sendParticles(
                ParticleTypes.LARGE_SMOKE,
                center.x,
                center.y + 0.8D,
                center.z,
                120,
                7.0D,
                1.5D,
                7.0D,
                0.10D
        );

        level.sendParticles(
                ParticleTypes.CAMPFIRE_COSY_SMOKE,
                center.x,
                center.y + 0.5D,
                center.z,
                60,
                6.0D,
                0.8D,
                6.0D,
                0.04D
        );
    }

    private void updateBlasterTackleMove(ServerLevel level, int age) {

        Vec3 forward = DragonLocatorSampler.forward(this).normalize();

        // 9tick目だけ少し加速
        double speed = age == 9 ? 3.2D : 2.4D;

        Vec3 motion = forward.scale(speed);

        moveByVector(level, motion);

        applyBlasterTackleDamage(level);
    }

    private void applyBlasterTackleDamage(ServerLevel level) {
        TheEndOfDragonCollisionEntity collision =
                this.getChild(this.collisionEntityId, TheEndOfDragonCollisionEntity.class);

        if (collision == null) {
            return;
        }

        for (DragonCollisionBox box : collision.getCollisionBoxes()) {
            if (box.points() == null || box.points().length == 0) {
                continue;
            }

            AABB area = aabbFromPoints(box.points()).inflate(0.8D);

            var entities = level.getEntitiesOfClass(
                    LivingEntity.class,
                    area,
                    entity -> entity.isAlive()
                            && !(entity instanceof TheEndOfDragonCoreEntity)
                            && !(entity instanceof TheEndOfDragonEntity)
                            && !(entity instanceof TheEndOfDragonDisplayEntity)
                            && !(entity instanceof TheEndOfDragonCollisionEntity)
            );

            for (LivingEntity entity : entities) {
                entity.hurtServer(
                        level,
                        level.damageSources().mobAttack(this),
                        TedConfig.values.blasterTackleDamage
                                * (float) TedConfig.values.damageMultiplier
                );

                Vec3 knock = entity.position().subtract(this.position());

                if (knock.lengthSqr() > 1.0E-6D) {
                    knock = knock.normalize();
                    entity.push(knock.x * 0.45D, 0.18D, knock.z * 0.45D);
                    entity.hurtMarked = true;
                }
            }
        }
    }

    private boolean isRoarTargetItem(ItemStack stack) {
        if (stack.isEmpty()) return false;

        var item = stack.getItem();

        // スタック可能アイテムは除外
        if (stack.getMaxStackSize() > 1) return false;
        // 食べ物・設置ブロックは除外
        if (stack.has(net.minecraft.core.component.DataComponents.FOOD)) return false;
        if (item instanceof net.minecraft.world.item.BlockItem) return false;
        if (stack.has(DataComponents.CONSUMABLE)) return false;

        // 装備スロットを持つもの。MOD防具/アクセサリー系を拾いやすい
        if (stack.has(net.minecraft.core.component.DataComponents.EQUIPPABLE)) return true;

        // 属性補正を持つもの。MOD武器/防具を拾いやすい
        if (stack.has(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS)) return true;

        // 射撃武器系タグやコンポーネントはMOD側次第なので、必要なら後でタグ追加
        return false;
    }

    private void spawnRoarOfObliterationVfx(ServerLevel serverLevel) {
        DragonCollisionBox head = getCollisionPartBox(DragonCollisionPart.HEAD);

        Vec3 pos;

        if (head != null && head.obb() != null) {
            pos = head.obb().center();
        } else {
            pos = this.position().add(0.0D, 4.0D, 0.0D);
        }

        TedVfxSpawner.spawnAt(
                serverLevel,
                pos,
                0.0F,
                0.0F,
                TedVfxType.ROAR_OF_OBLITERATION,
                1.0F,
                1.0F,
                44
        );
    }



    private boolean isRoarBreakableEquipmentSlot(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD, CHEST, LEGS, FEET, MAINHAND, OFFHAND -> true;
            default -> false;
        };
    }


    private void applyRoarOfObliteration(ServerLevel level) {
        double radius = 48.0D;

        AABB area = this.getBoundingBox().inflate(radius);

        var entities = level.getEntitiesOfClass(
                LivingEntity.class,
                area,
                entity -> entity.isAlive()
                        && entity != this
                        && !(entity instanceof TheEndOfDragonCoreEntity)
                        && !(entity instanceof TheEndOfDragonEntity)
        );

        for (LivingEntity entity : entities) {
            if (TedConfig.values.enableEquipmentBreak) {
                int equipmentDamage = (int) (
                        TedConfig.values.roarEquipmentDamage
                                * TedConfig.values.damageMultiplier
                );

                damageEquipmentByRoar(level, entity, equipmentDamage);
            }

            Vec3 away = entity.position().subtract(this.position());
            if (away.lengthSqr() > 1.0E-6D) {
                away = away.normalize();
                entity.push(away.x * 1.6D, 0.35D, away.z * 1.6D);
                entity.hurtMarked = true;
            }

            float damage = TedConfig.values.roarDamage
                    * (float) TedConfig.values.damageMultiplier;

            entity.hurtServer(
                    level,
                    level.damageSources().mobAttack(this),
                    damage
            );
        }
    }



    private void damageEquipmentByRoar(ServerLevel level, LivingEntity entity, int amount) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = entity.getItemBySlot(slot);

            if (!isRoarBreakableEquipmentSlot(slot)) {
                continue;
            }

            if (!isRoarTargetItem(stack)) {
                continue;
            }

// 対象装備なら、不可壊・耐久なしも即破壊
            if (!stack.isDamageableItem()
                    || stack.has(net.minecraft.core.component.DataComponents.UNBREAKABLE)) {
                entity.setItemSlot(slot, ItemStack.EMPTY);
                continue;
            }

            // 高耐久装備は即破壊
            if (stack.getMaxDamage() >= 1000) {
                entity.setItemSlot(slot, ItemStack.EMPTY);
                continue;
            }

            int nextDamage = stack.getDamageValue() + amount;

            if (nextDamage >= stack.getMaxDamage()) {
                entity.setItemSlot(slot, ItemStack.EMPTY);
            } else {
                stack.setDamageValue(nextDamage);
            }
        }

        for (ItemStack stack : com.licht_meilleur.the_end_of_dragon.compat.TedAccessories.getAccessories(entity)) {
            damageAccessoryStackByRoar(stack, amount);
        }
    }

    private void damageAccessoryStackByRoar(ItemStack stack, int amount) {
        if (!isRoarTargetItem(stack)) {
            return;
        }

        // 耐久なし・不可壊アクセサリーは即破壊
        if (!stack.isDamageableItem()
                || stack.has(DataComponents.UNBREAKABLE)) {
            stack.shrink(1);
            return;
        }

        // 超高耐久も即破壊
        if (stack.getMaxDamage() >= 1000) {
            stack.shrink(1);
            return;
        }

        int nextDamage = stack.getDamageValue() + amount;

        if (nextDamage >= stack.getMaxDamage()) {
            stack.shrink(1);
        } else {
            stack.setDamageValue(nextDamage);
        }
    }



    private void updateLightOfDestruction(ServerLevel serverLevel, int age) {
        Vec3 center = getChestCrystalCenter();
        if (center == null) {
            return;
        }

        // 20～30tickを0～1へ
        float t = (age - 20) / 10.0F;
        t = Math.min(1.0F, Math.max(0.0F, t));

        float scale = 2.0F + t * 4000.0F;

        TedVfxSpawner.spawnAt(
                serverLevel,
                center,
                this.getYRot(),
                this.getXRot(),
                TedVfxType.LIGHT_OF_DESTRUCTION,
                scale,
                1.0F,
                2
        );

        serverLevel.sendParticles(
                ParticleTypes.END_ROD,
                center.x,
                center.y,
                center.z,
                20,
                1.0D + scale * 0.25D,
                1.0D + scale * 0.25D,
                1.0D + scale * 0.25D,
                0.02D
        );

        // 最大まで広がった瞬間
        if (age == 30) {
            clearBuffsByLight(serverLevel, center, scale * 2.5D);


        }
    }

    private Vec3 getChestCrystalCenter() {
        DragonCollisionBox box = getCollisionPartBox(DragonCollisionPart.CHEST_CRYSTAL);
        if (box == null || box.obb() == null) {
            return null;
        }

        return box.obb().center();
    }

    private void clearBuffsByLight(ServerLevel level, Vec3 center, double radius) {
        AABB area = new AABB(center, center).inflate(radius);

        var entities = level.getEntitiesOfClass(
                LivingEntity.class,
                area,
                entity -> entity.isAlive()
                        && entity != this
                        && !(entity instanceof TheEndOfDragonCoreEntity)
                        && !(entity instanceof TheEndOfDragonEntity)
        );

        for (LivingEntity entity : entities) {
            entity.removeAllEffects();

            level.sendParticles(
                    ParticleTypes.END_ROD,
                    entity.getX(),
                    entity.getY() + entity.getBbHeight() * 0.5D,
                    entity.getZ(),
                    20,
                    0.5D, 0.8D, 0.5D,
                    0.08D
            );
        }
    }

    private void fireLightProjectile(ServerLevel serverLevel) {
        DragonCollisionBox head =
                getCollisionPartBox(DragonCollisionPart.HEAD);

        if (head == null || head.obb() == null) {
            return;
        }

        Vec3 axisY =
                head.obb().axisY().normalize();

        Vec3 start =
                head.obb().center()
                        .add(axisY.scale(2.5D));

        Vec3 shotDir = axisY;

        Player target =
                serverLevel.getNearestPlayer(this, 512.0D);

        if (target != null) {
            Vec3 toTarget =
                    target.getEyePosition()
                            .subtract(start)
                            .normalize();

            double dot = axisY.dot(toTarget);
            double maxAngleCos =
                    Math.cos(Math.toRadians(120.0D));

            if (dot > maxAngleCos) {
                shotDir = toTarget;
            }
        }

        TedSoundHelper.playShot(
                serverLevel,
                start
        );

        spawnProjectile(
                serverLevel,
                TedProjectileSpecs.LIGHT_PROJECTILE,
                start,
                shotDir
        );
    }

    private void updateJetBeam(
            ServerLevel serverLevel,
            DragonCollisionPart part,
            double offsetX,
            double offsetY,
            double offsetZ
    ) {
        DragonCollisionBox box = getCollisionPartBox(part);
        if (box == null || box.obb() == null) {
            return;
        }

        Vec3 axisX = box.obb().axisX().normalize();
        Vec3 axisY = box.obb().axisY().normalize();
        Vec3 axisZ = box.obb().axisZ().normalize();

        Vec3 start = box.obb().center()
                .add(axisX.scale(offsetX))
                .add(axisY.scale(offsetY))
                .add(axisZ.scale(offsetZ));

        Vec3 direction = axisY.normalize();

        TedBeamHitbox beam = new TedBeamHitbox(
                start,
                direction,
                5.0D,
                1.2D
        );

        beam.spawnJetParticles(serverLevel);
    }

    private void updateOrbCharge(ServerLevel serverLevel, int age) {
        float t = Math.min(1.0F, age / 55.0F);
        float scale = 0.3F + t * 8F;

        Vec3 pos = orbChargePos();

        TedVfxSpawner.spawnAt(
                serverLevel,
                pos,
                this.getYRot(),
                this.getXRot(),
                TedVfxType.ORB_OF_ANIHILATION,
                scale,
                1.0F,
                2
        );
    }
    public boolean shouldUseOrbAgainstDefense(
            LivingEntity target
    ) {
        if (target == null || !target.isAlive()) {
            return false;
        }

        double armor =
                target.getAttributeValue(
                        Attributes.ARMOR
                );

        double toughness =
                target.getAttributeValue(
                        Attributes.ARMOR_TOUGHNESS
                );

        /*
         * バニラのネザライト一式を基準に、
         * 少し上までは許容する。
         */
        boolean excessiveArmor =
                armor > 24.0D;

        boolean excessiveToughness =
                toughness > 16.0D;

        /*
         * 防具強度は防御力より希少なので、
         * 少し重めに評価する。
         *
         * ネザライト一式:
         * 20 + 12 * 0.75 = 29
         *
         * 発動基準:
         * 36超過
         */
        double defenseScore =
                armor + toughness * 0.75D;

        boolean excessiveCombinedDefense =
                defenseScore > 36.0D;

        return excessiveArmor
                || excessiveToughness
                || excessiveCombinedDefense;
    }

    public LivingEntity findHighDefenseTarget(
            ServerLevel level
    ) {
        AABB area =
                this.getBoundingBox().inflate(256.0D);

        LivingEntity bestTarget = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (ServerPlayer player :
                level.getEntitiesOfClass(
                        ServerPlayer.class,
                        area,
                        p -> p.isAlive()
                                && !p.isCreative()
                                && !p.isSpectator()
                )) {

            if (!shouldUseOrbAgainstDefense(player)) {
                continue;
            }

            double armor =
                    player.getAttributeValue(
                            Attributes.ARMOR
                    );

            double toughness =
                    player.getAttributeValue(
                            Attributes.ARMOR_TOUGHNESS
                    );

            double score =
                    armor + toughness * 0.75D;

            if (score > bestScore) {
                bestScore = score;
                bestTarget = player;
            }
        }

        return bestTarget;
    }

    private LivingEntity orbTarget;

    public void setOrbTarget(LivingEntity target) {
        this.orbTarget = target;
    }

    public LivingEntity getOrbTarget() {
        return this.orbTarget;
    }

    public void clearOrbTarget() {
        this.orbTarget = null;
    }

    private Vec3 orbChargePos() {
        Vec3 forward = DragonLocatorSampler.forward(this).normalize();

        return this.position()
                .add(0.0D, 5.0D, 0.0D)
                .add(forward.scale(6.0D));
    }

    private void fireOrbOfAnnihilation(ServerLevel serverLevel) {
        Vec3 start = orbChargePos();
        Vec3 direction = DragonLocatorSampler.forward(this).normalize();

        spawnProjectile(
                serverLevel,
                TedProjectileSpecs.ORB_OF_ANNIHILATION,
                start,
                direction
        );
    }

    private void spawnProjectile(
            ServerLevel serverLevel,
            TedProjectileSpec spec,
            Vec3 start,
            Vec3 direction
    ) {
        TedVfxEntity projectile = ModEntities.TED_VFX.create(serverLevel, EntitySpawnReason.EVENT);
        if (projectile == null) {
            return;
        }

        projectile.setup(spec);
        projectile.setProjectileOwner(this);

        Vec3 dir = direction.normalize();

        float yaw = (float) Math.toDegrees(Math.atan2(-dir.x, dir.z));
        float pitch = (float) -Math.toDegrees(Math.asin(dir.y));

        projectile.snapTo(
                start.x,
                start.y,
                start.z,
                yaw,
                pitch
        );

        projectile.setDeltaMovement(dir.scale(spec.speed()));


        serverLevel.addFreshEntity(projectile);
    }



    public void updateRagnarokFall(ServerLevel level) {
        moveByVector(level, new Vec3(0.0D, -14.0D, 0.0D));

        if (isNearGroundForSuperLanding(level)) {
            this.setDragonState(DragonState.LANDING);
        }
    }

    private boolean ragnarokFalling = false;

    private void updateRagnarokLanding(ServerLevel level) {
        if (this.getDragonStateAgeTicks() > 20) {
            this.ragnarokFalling = false;
            this.setDragonState(DragonState.IDLE);
        }
    }



    private void updatePhotonLasers(ServerLevel serverLevel) {
        updateHandLaserBeam(serverLevel, DragonCollisionPart.FRONT_LEFT_HAND, 0.0D, 0.0D, 0.0D);
        updateHandLaserBeam(serverLevel, DragonCollisionPart.FRONT_RIGHT_HAND, 0.0D, 0.0D, 0.0D);
        updateHandLaserBeam(serverLevel, DragonCollisionPart.BACK_LEFT_HAND, 0.0D, 0.0D, 0.0D);
        updateHandLaserBeam(serverLevel, DragonCollisionPart.BACK_RIGHT_HAND, 0.0D, 0.0D, 0.0D);

        updateHeadLaserBeam(serverLevel, true);
    }

    private void updateHandLaserBeam(
            ServerLevel serverLevel,
            DragonCollisionPart part,
            double offsetX,
            double offsetY,
            double offsetZ
    ) {
        DragonCollisionBox box = getCollisionPartBox(part);
        if (box == null || box.obb() == null) {
            return;
        }

        Vec3 axisX = box.obb().axisX().normalize();
        Vec3 axisY = box.obb().axisY().normalize();
        Vec3 axisZ = box.obb().axisZ().normalize();

        Vec3 start = box.obb().center()
                .add(axisX.scale(offsetX))
                .add(axisY.scale(offsetY))
                .add(axisZ.scale(offsetZ));

        Vec3 direction = axisY.normalize();

        double length = raycastLaserLength(
                serverLevel,
                start,
                direction,
                64.0D
        );

        TedBeamHitbox beam = new TedBeamHitbox(
                start,
                direction,
                length,
                1.5D
        );

        beam.spawnLaserParticles(serverLevel);

        beam.damageEntities(
                serverLevel,
                this,
                8.0F,
                entity -> entity != this
                        && !(entity instanceof TheEndOfDragonCoreEntity)
                        && !(entity instanceof TheEndOfDragonEntity)
                        && !(entity instanceof TheEndOfDragonDisplayEntity)
                        && !(entity instanceof TheEndOfDragonCollisionEntity)
        );
    }

    private void updateFlightJets(ServerLevel serverLevel) {
        updateJetBeam(serverLevel, DragonCollisionPart.FRONT_LEFT_HAND, 0.0D, 2.0D, 0.0D);
        updateJetBeam(serverLevel, DragonCollisionPart.FRONT_RIGHT_HAND, 0.0D, 2.0D, 0.0D);
        updateJetBeam(serverLevel, DragonCollisionPart.BACK_LEFT_HAND, 0.0D, 2.0D, 0.0D);
        updateJetBeam(serverLevel, DragonCollisionPart.BACK_RIGHT_HAND, 0.0D, 2.0D, 0.0D);
    }

    private void updateHeadLaserBeam(
            ServerLevel serverLevel,
            boolean active
    ) {
        if (!active) {
            return;
        }

        DragonCollisionBox box = getCollisionPartBox(DragonCollisionPart.HEAD);
        if (box == null || box.obb() == null) {
            return;
        }

        Vec3 axisX = box.obb().axisX().normalize();
        Vec3 axisY = box.obb().axisY().normalize();
        Vec3 axisZ = box.obb().axisZ().normalize();

        // まずは手レーザーと同じく axisY を正方向として使う
        Vec3 direction = axisY;

        // 頭の中心から少し前へ出す
        Vec3 start = box.obb().center()
                .add(direction.scale(0.8D));

        double maxLength = 64.0D;

        double length = raycastLaserLength(
                serverLevel,
                start,
                direction,
                maxLength
        );

        TedBeamHitbox beam = new TedBeamHitbox(
                start,
                direction,
                length,
                1.0D
        );

        beam.spawnLaserParticles(serverLevel);

        beam.damageEntities(
                serverLevel,
                this,
                8.0F,
                entity -> entity != this
                        && !(entity instanceof TheEndOfDragonCoreEntity)
                        && !(entity instanceof TheEndOfDragonEntity)
                        && !(entity instanceof TheEndOfDragonDisplayEntity)
                        && !(entity instanceof TheEndOfDragonCollisionEntity)
        );
    }


    private void debugDrawHeadCollision(ServerLevel level) {
        DragonCollisionBox box = getCollisionPartBox(DragonCollisionPart.HEAD);
        if (box == null || box.points() == null || box.points().length < 8) {
            return;
        }

        Vec3[] p = box.points();

        // 8頂点
        for (Vec3 v : p) {
            level.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.FLAME,
                    v.x, v.y, v.z,
                    4,
                    0.03D, 0.03D, 0.03D,
                    0.0D
            );
        }

        // 面と辺
        debugLine(level, p[0], p[1]);
        debugLine(level, p[1], p[2]);
        debugLine(level, p[2], p[3]);
        debugLine(level, p[3], p[0]);

        debugLine(level, p[4], p[5]);
        debugLine(level, p[5], p[6]);
        debugLine(level, p[6], p[7]);
        debugLine(level, p[7], p[4]);

        debugLine(level, p[0], p[4]);
        debugLine(level, p[1], p[5]);
        debugLine(level, p[2], p[6]);
        debugLine(level, p[3], p[7]);
    }

    private void debugLine(ServerLevel level, Vec3 a, Vec3 b) {
        int count = 24;

        for (int i = 0; i <= count; i++) {
            double t = i / (double) count;
            Vec3 p = a.lerp(b, t);

            level.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.END_ROD,
                    p.x, p.y, p.z,
                    1,
                    0.0D, 0.0D, 0.0D,
                    0.0D
            );
        }
    }




    private org.joml.Quaternionf quaternionFromBasis(Vec3 right, Vec3 up, Vec3 forward) {
        Vec3 f = forward.normalize();

        // レーザーモデルは -Z 方向へ伸びる想定
        Vec3 back = f.scale(-1.0D).normalize();

        Vec3 u = up.normalize();
        Vec3 r = u.cross(back).normalize();
        u = back.cross(r).normalize();

        float m00 = (float) r.x;
        float m01 = (float) u.x;
        float m02 = (float) back.x;

        float m10 = (float) r.y;
        float m11 = (float) u.y;
        float m12 = (float) back.y;

        float m20 = (float) r.z;
        float m21 = (float) u.z;
        float m22 = (float) back.z;

        float trace = m00 + m11 + m22;

        float x, y, z, w;

        if (trace > 0.0F) {
            float s = (float) Math.sqrt(trace + 1.0F) * 2.0F;
            w = 0.25F * s;
            x = (m21 - m12) / s;
            y = (m02 - m20) / s;
            z = (m10 - m01) / s;
        } else if (m00 > m11 && m00 > m22) {
            float s = (float) Math.sqrt(1.0F + m00 - m11 - m22) * 2.0F;
            w = (m21 - m12) / s;
            x = 0.25F * s;
            y = (m01 + m10) / s;
            z = (m02 + m20) / s;
        } else if (m11 > m22) {
            float s = (float) Math.sqrt(1.0F + m11 - m00 - m22) * 2.0F;
            w = (m02 - m20) / s;
            x = (m01 + m10) / s;
            y = 0.25F * s;
            z = (m12 + m21) / s;
        } else {
            float s = (float) Math.sqrt(1.0F + m22 - m00 - m11) * 2.0F;
            w = (m10 - m01) / s;
            x = (m02 + m20) / s;
            y = (m12 + m21) / s;
            z = 0.25F * s;
        }

        return new org.joml.Quaternionf(x, y, z, w).normalize();
    }

    private Vec3 offsetFromObb(
            DragonCollisionBox box,
            Vec3 base,
            double x,
            double y,
            double z
    ) {
        return base
                .add(box.obb().axisX().normalize().scale(x))
                .add(box.obb().axisY().normalize().scale(y))
                .add(box.obb().axisZ().normalize().scale(z));
    }

    private void updateRagnarokLaserBeam(
            ServerLevel level,
            DragonCollisionPart part
    ) {
        DragonCollisionBox box = getCollisionPartBox(part);
        if (box == null || box.obb() == null) {
            return;
        }

        Vec3 start = box.obb().center();
        Vec3 direction = box.obb().axisY().normalize();

        double length = raycastLaserLength(level, start, direction, 96.0D);

        TedBeamHitbox beam = new TedBeamHitbox(
                start,
                direction,
                length,
                8.0D
        );

        beam.spawnLaserParticles(level);

        beam.damageEntities(
                level,
                this,
                TedConfig.values.laserDamage * 2.0F * (float) TedConfig.values.damageMultiplier,
                entity -> entity != this
                        && !(entity instanceof TheEndOfDragonCoreEntity)
                        && !(entity instanceof TheEndOfDragonEntity)
                        && !(entity instanceof TheEndOfDragonDisplayEntity)
                        && !(entity instanceof TheEndOfDragonCollisionEntity)
        );

        spawnExtraRagnarokLaserParticles(level, start, direction, length, 8.0D);
        spawnRagnarokImpactParticles(level, start.add(direction.scale(length)), 8.0D);
    }

    private void spawnExtraRagnarokLaserParticles(
            ServerLevel level,
            Vec3 start,
            Vec3 dir,
            double length,
            double radius
    ) {
        int steps = 72;

        for (int i = 0; i <= steps; i++) {
            double t = i / (double) steps;
            Vec3 p = start.add(dir.scale(length * t));

            // レーザーの芯。狭く・多め
            level.sendParticles(
                    ParticleTypes.END_ROD,
                    p.x, p.y, p.z,
                    10,
                    0.18D, 0.18D, 0.18D,
                    0.0D
            );

            // 外側の熱。スケール6〜8相当だが、拡散は狭め
            level.sendParticles(
                    ParticleTypes.FLAME,
                    p.x, p.y, p.z,
                    6,
                    0.55D, 0.55D, 0.55D,
                    0.01D
            );
        }
    }

    private void spawnRagnarokImpactParticles(
            ServerLevel level,
            Vec3 pos,
            double radius
    ) {
        level.sendParticles(
                ParticleTypes.EXPLOSION,
                pos.x, pos.y, pos.z,
                1,
                0.3D, 0.3D, 0.3D,
                0.0D
        );

        level.sendParticles(
                ParticleTypes.FLAME,
                pos.x, pos.y, pos.z,
                35,
                1.2D, 0.5D, 1.2D,
                0.06D
        );

        level.sendParticles(
                ParticleTypes.LARGE_SMOKE,
                pos.x, pos.y, pos.z,
                12,
                0.8D, 0.4D, 0.8D,
                0.03D
        );
    }

    private TedVfxEntity getOrCreateLaserVfx(
            ServerLevel serverLevel,
            int currentId
    ) {
        if (currentId != -1) {
            Entity entity = this.level().getEntity(currentId);
            if (entity instanceof TedVfxEntity vfx && vfx.isAlive()) {
                return vfx;
            }
        }

        TedVfxEntity vfx = ModEntities.TED_VFX.create(serverLevel, EntitySpawnReason.EVENT);
        if (vfx == null) {
            return null;
        }

        vfx.setup(TedVfxType.TED_LASER_BEAM, 1.5F, 0.0F, 999999);
        vfx.snapTo(this.getX(), this.getY(), this.getZ(), 0.0F, 0.0F);

        vfx.setBasis(new Vec3(0, 0, 1), new Vec3(0, 1, 0));

        serverLevel.addFreshEntity(vfx);
        return vfx;
    }

    private TedVfxEntity getLaserVfxForPart(ServerLevel serverLevel, DragonCollisionPart part) {
        TedVfxEntity vfx;

        switch (part) {
            case FRONT_LEFT_HAND -> {
                vfx = getOrCreateLaserVfx(serverLevel, frontLeftLaserVfxId);
                if (vfx != null) frontLeftLaserVfxId = vfx.getId();
                return vfx;
            }
            case FRONT_RIGHT_HAND -> {
                vfx = getOrCreateLaserVfx(serverLevel, frontRightLaserVfxId);
                if (vfx != null) frontRightLaserVfxId = vfx.getId();
                return vfx;
            }
            case BACK_LEFT_HAND -> {
                vfx = getOrCreateLaserVfx(serverLevel, backLeftLaserVfxId);
                if (vfx != null) backLeftLaserVfxId = vfx.getId();
                return vfx;
            }
            case BACK_RIGHT_HAND -> {
                vfx = getOrCreateLaserVfx(serverLevel, backRightLaserVfxId);
                if (vfx != null) backRightLaserVfxId = vfx.getId();
                return vfx;
            }
            default -> {
                return null;
            }
        }
    }


    private Vec3 endpointAlongAxis(DragonCollisionBox box, Vec3 axis, boolean maxSide) {
        Vec3 dir = axis.normalize();

        Vec3[] points = box.points();
        if (points == null || points.length == 0) {
            return box.obb().center();
        }

        double best = maxSide ? -Double.MAX_VALUE : Double.MAX_VALUE;
        Vec3 sum = Vec3.ZERO;
        int count = 0;

        for (Vec3 p : points) {
            double d = p.dot(dir);

            boolean better = maxSide ? d > best + 0.0001D : d < best - 0.0001D;

            if (better) {
                best = d;
                sum = p;
                count = 1;
            } else if (Math.abs(d - best) < 0.0001D) {
                sum = sum.add(p);
                count++;
            }
        }

        return count <= 0 ? box.obb().center() : sum.scale(1.0D / count);
    }









    private double raycastLaserLength(
            ServerLevel serverLevel,
            Vec3 start,
            Vec3 direction,
            double maxLength
    ) {
        Vec3 dir = direction.normalize();
        Vec3 end = start.add(dir.scale(maxLength));

        // レイキャスト方向デバッグ
        for (int i = 0; i <= 64; i++) {
            double t = i / 64.0D;
            Vec3 p = start.lerp(end, t);

            serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.FLAME,
                    p.x,
                    p.y,
                    p.z,
                    1,
                    0.0D,
                    0.0D,
                    0.0D,
                    0.0D
            );
        }

        var hit = serverLevel.clip(new net.minecraft.world.level.ClipContext(
                start,
                end,
                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE,
                this
        ));

        double length = hit.getLocation().distanceTo(start);
        return Math.max(1.0D, Math.min(maxLength, length));
    }







    private DragonCollisionBox getCollisionPartBox(DragonCollisionPart part) {
        TheEndOfDragonCollisionEntity collision =
                this.getChild(this.collisionEntityId, TheEndOfDragonCollisionEntity.class);

        if (collision == null) {
            return null;
        }

        for (var box : collision.getCollisionBoxes()) {
            if (box.part() == part) {
                return box;
            }
        }

        return null;
    }


}