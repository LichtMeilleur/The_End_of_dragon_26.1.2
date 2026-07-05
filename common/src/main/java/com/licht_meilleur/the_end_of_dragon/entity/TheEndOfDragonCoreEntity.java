package com.licht_meilleur.the_end_of_dragon.entity;

import com.licht_meilleur.the_end_of_dragon.config.TedConfig;
import com.licht_meilleur.the_end_of_dragon.entity.collision.DragonCollisionBox;
import com.licht_meilleur.the_end_of_dragon.entity.collision.DragonCollisionPart;
import com.licht_meilleur.the_end_of_dragon.entity.hitbox.DragonLocatorSampler;
import com.licht_meilleur.the_end_of_dragon.entity.hitbox.TedBeamHitbox;
import com.licht_meilleur.the_end_of_dragon.entity.projectile.TedProjectileSpec;
import com.licht_meilleur.the_end_of_dragon.entity.projectile.TedProjectileSpecs;
import com.licht_meilleur.the_end_of_dragon.entity.vfx.*;
import com.licht_meilleur.the_end_of_dragon.registry.ModEntities;

import com.licht_meilleur.the_end_of_dragon.entity.ai.*;
import com.licht_meilleur.the_end_of_dragon.world.EndPortalSealHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

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



    private Vec3 introFlyTarget = null;
    private Vec3 introPortalAboveTarget = null;


    private boolean attackMovementLocked = false;

    // 強制チャンクロード
    private final java.util.Set<Long> forcedChunks = new java.util.HashSet<>();

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

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new DragonRecoveryGoal(this));
        this.goalSelector.addGoal(1, new DragonAttackGoal(this));
        this.goalSelector.addGoal(2, new DragonMoveGoal(this));
    }

    private float visualPitch = 0.0F;

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


    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_STATE, DragonState.IDLE.ordinal());
    }

    private int stateStartTick = 0;

    public void setDragonState(DragonState state) {
        if (this.getDragonState() == state) return;

        this.entityData.set(DATA_STATE, state.ordinal());
        this.stateStartTick = this.tickCount;

        if (state == DragonState.SUPER_LANDING
                || state == DragonState.INTRO_SUPER_LANDING) {
            this.superLandingImpacted = false;
        }
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

    @Override
    public void tick() {
        super.tick();



        this.setInvisible(true);

        updatePhysicsMode();

        if (this.level().isClientSide()) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) this.level();

        this.tickChildren();
        this.tickAttackVfx();
        this.tickBodyBlockBreak(serverLevel);
        this.tickAttackStateTimeout();

        this.applyBossPresenceLimits(serverLevel);

        if (!this.level().isClientSide() && this.level() instanceof ServerLevel level) {
            maintainForcedChunks(level);
        }
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



    private void tickChildren() {
        TheEndOfDragonDisplayEntity display =
                this.getChild(this.displayEntityId, TheEndOfDragonDisplayEntity.class);

        TheEndOfDragonCollisionEntity collision =
                this.getChild(this.collisionEntityId, TheEndOfDragonCollisionEntity.class);

        if (display == null) {
            display = new TheEndOfDragonDisplayEntity(
                    ModEntities.THE_END_OF_DRAGON_DISPLAY,
                    this.level()
            );
            display.syncFromCore(this);
            this.level().addFreshEntity(display);
            this.displayEntityId = display.getId();
        }

        if (collision == null) {
            collision = new TheEndOfDragonCollisionEntity(
                    ModEntities.THE_END_OF_DRAGON_COLLISION,
                    this.level()
            );
            collision.syncFromCore(this);
            this.level().addFreshEntity(collision);
            this.collisionEntityId = collision.getId();
        }

        display.syncFromCore(this);
        collision.syncFromCore(this);
    }

    private void updatePhysicsMode() {
        DragonState state = this.getDragonState();

        boolean flying = switch (state) {
            case FLY_START,
                 FLY,
                 FLY_LEFT,
                 FLY_RIGHT,
                 FLY_SHOT,
                 FALL,
                 FLAMES_OF_RAGNAROK,
                 INTRO_RISE,
                 INTRO_WAIT_PORTAL,
                 INTRO_FLY_TO_PORTAL,
                 INTRO_DIVE_TO_PORTAL -> true;
            default -> false;
        };

        this.noPhysics = flying;
        this.setNoGravity(flying);
    }

    private <T extends Entity> T getChild(int entityId, Class<T> type) {
        if (entityId == -1) {
            return null;
        }

        Entity entity = this.level().getEntity(entityId);
        if (type.isInstance(entity) && entity.isAlive()) {
            return type.cast(entity);
        }

        return null;
    }

    public boolean isIntroStateNow() {
        return isIntroState(this.getDragonState());
    }

    public boolean isAttackStateNow() {
        return isAttackState(this.getDragonState());
    }

    public LivingEntity findBossTarget(ServerLevel level) {
        return level.getNearestPlayer(this, 256.0D);
    }

    public void moveBossBy(ServerLevel level, Vec3 move) {
        moveByVector(level, move);
    }

    public Vec3 arenaCenter(ServerLevel level) {
        return getArenaCenter(level);
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!this.level().isClientSide()) {
            Entity display = this.level().getEntity(this.displayEntityId);
            if (display != null) {
                display.discard();
            }

            Entity collision = this.level().getEntity(this.collisionEntityId);
            if (collision != null) {
                collision.discard();
            }

            forcedChunks.clear();
        }


        super.remove(reason);
    }



    public boolean isAttackMovementLocked() {
        return attackMovementLocked;
    }

    public void setAttackMovementLocked(boolean locked) {
        this.attackMovementLocked = locked;
    }

    @Override
    public void die(net.minecraft.world.damagesource.DamageSource damageSource) {
        super.die(damageSource);

        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            EndPortalSealHandler.restorePortal(serverLevel);
        }
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
                .add(Attributes.ARMOR, 20.0D)
                .add(Attributes.FOLLOW_RANGE, 128.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
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
            case SUPER_LANDING -> false;
            case INTRO_RISE,
                 INTRO_WAIT_PORTAL,
                 INTRO_FLY_TO_PORTAL,
                 INTRO_DIVE_TO_PORTAL -> false;
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



    private double getArenaAirY(ServerLevel level) {
        if (this.introPortalCenter != null) {
            return this.introPortalCenter.getY() + 70.0D;
        }

        return 140.0D;
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
                 BLASTER_TACKLE,
                 SUPER_LANDING -> true;
            default -> false;
        };
    }

    @Override
    public boolean hurtServer(
            ServerLevel level,
            net.minecraft.world.damagesource.DamageSource source,
            float damage
    ) {
        damage = reduceIncomingBossDamage(damage);

        return super.hurtServer(level, source, damage);
    }

    private float reduceIncomingBossDamage(float damage) {
        if (damage >= 500.0F) {
            return damage * 0.01F; // 99%カット
        }

        if (damage >= 100.0F) {
            return damage * 0.30F; // 70%カット
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

    private void tickAttackStateTimeout() {
        int age = this.getDragonStateAgeTicks();

        switch (this.getDragonState()) {
            case ORB_OF_ANNIHILATION -> {
                if (age > 65) this.setDragonState(DragonState.IDLE);
            }
            case ROAR_OF_OBLITERATION -> {
                if (age > 40) this.setDragonState(DragonState.IDLE);
            }
            case FLAMES_OF_RAGNAROK -> {
                if (age > 120) {
                    this.ragnarokFalling = true;
                    this.setDragonState(DragonState.FALL);
                }
            }
            case LIGHT_OF_DESTRUCTION -> {
                if (age > 30) this.setDragonState(DragonState.IDLE);
            }
            case PHOTON_BLASTER -> {
                if (age > 70) this.setDragonState(DragonState.IDLE);
            }
            case BLASTER_TACKLE -> {
                if (age > 20) this.setDragonState(DragonState.IDLE);
            }
            case FLY_SHOT -> {
                if (age > 10) {
                    this.setDragonState(DragonState.FLY);
                }
            }
            case SUPER_LANDING -> {
                if (age > 20) this.setDragonState(DragonState.IDLE);
            }

            case FLY_START -> {
                if (age > 10) this.setDragonState(DragonState.FLY);
            }

            case FALL -> {
                if (age > 80) this.setDragonState(DragonState.LANDING);
            }

            case LANDING -> {
                if (age > 25) this.setDragonState(DragonState.IDLE);
            }

            case INTRO_SUPER_LANDING -> {
                if (age > 20) {
                    this.setDragonState(DragonState.IDLE);
                }
            }


            default -> {
            }
        }
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
                    false, // false から true に変更
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
                if (between(age, ORB_CHARGE_START, ORB_FIRE_TICK - 1)) {
                    updateOrbCharge(serverLevel, age);
                }

                if (age == ORB_FIRE_TICK) {
                    fireOrbOfAnnihilation(serverLevel);
                }
            }

            case PHOTON_BLASTER -> {
                boolean firing = between(age, PHOTON_FIRE_START, PHOTON_FIRE_END);

                if (firing) {
                    updatePhotonLasers(serverLevel);
                }
            }

            case FLAMES_OF_RAGNAROK -> {
                float yaw = this.getYRot() + 28.0F;

                this.setYRot(yaw);
                this.setYBodyRot(yaw);
                this.setYHeadRot(yaw);

                this.yRotO = yaw;
                this.yBodyRotO = yaw;
                this.yHeadRotO = yaw;

                boolean firing = between(age, FLAMES_FIRE_START, FLAMES_FIRE_END);

                updateAttachedVfx(serverLevel, TedVfxSpecs.FRONT_LEFT_LASER, firing);
                updateAttachedVfx(serverLevel, TedVfxSpecs.FRONT_RIGHT_LASER, firing);
                updateAttachedVfx(serverLevel, TedVfxSpecs.BACK_LEFT_LASER, firing);
                updateAttachedVfx(serverLevel, TedVfxSpecs.BACK_RIGHT_LASER, firing);

                syncChildrenNow();
            }

            case LIGHT_OF_DESTRUCTION -> {
                if (age >= 20 && age <= 30) {
                    updateLightOfDestruction(serverLevel, age);
                }
            }

            case FLY_SHOT -> {
                //updateFlightJets(serverLevel);

                if (age == FLY_SHOT_FIRE_TICK) {
                    fireLightProjectile(serverLevel);
                }
            }



            case FLY, FLY_LEFT, FLY_RIGHT -> {
                //updateFlightJets(serverLevel);
            }

            case ROAR_OF_OBLITERATION -> {
                if (age == 1) {
                    spawnRoarOfObliterationVfx(serverLevel);
                }

                if (age == 10) {
                    applyRoarOfObliteration(serverLevel);
                }

                // spawnRoarParticles(serverLevel, age);
            }

            case BLASTER_TACKLE -> {
                if (between(age, 7, 8)) {
                    updateFlightJets(serverLevel);
                }

                if (age >= 9) {
                    updateBlasterTackleMove(serverLevel, age);
                }
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


    private void moveByVector(ServerLevel level, Vec3 move) {
        if (move.lengthSqr() < 1.0E-6D) {
            return;
        }

        this.setPos(
                this.getX() + move.x,
                this.getY() + move.y,
                this.getZ() + move.z
        );

        faceMovementDirection(move);

        this.setDeltaMovement(Vec3.ZERO);
        this.hurtMarked = true;

        syncChildrenNow();
    }

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



        // 約5秒待つ。バニラのポータル生成待ち
        if (age >= 160) {
            this.setDragonState(DragonState.INTRO_FLY_TO_PORTAL);
        }
    }

    private void updateIntroRise(ServerLevel level, int age) {
        moveByVector(level, new Vec3(0.0D, 20.0D, 0.0D));
        updateFlightJets(level);

        if (age >= 18) {
            this.setDragonState(DragonState.INTRO_WAIT_PORTAL);
        }
    }

    private void updateIntroFlyToPortal(ServerLevel level) {
        if (this.introPortalAboveTarget == null) {
            this.setDragonState(DragonState.FLY);
            return;
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
        if (this.introPortalCenter != null) {
            Vec3 center = Vec3.atCenterOf(this.introPortalCenter);

            this.setPos(
                    center.x,
                    this.getY(),
                    center.z
            );
        }

        Vec3 move = new Vec3(0.0D, -20.0D, 0.0D);
        moveByVector(level, move);

        level.sendParticles(
                ParticleTypes.SMOKE,
                this.getX(),
                this.getY() + 3.0D,
                this.getZ(),
                20,
                1.5D, 0.8D, 1.5D,
                0.08D
        );

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

    public void startIntroSequence(BlockPos portalCenter) {
        this.introPortalCenter = portalCenter.below(1);

        this.introPortalAboveTarget = Vec3.atCenterOf(this.introPortalCenter)
                .add(0.0D, 1000.0D, 0.0D);

        this.setDragonState(DragonState.INTRO_RISE);
    }


    private void syncChildrenNow() {
        TheEndOfDragonDisplayEntity display =
                this.getChild(this.displayEntityId, TheEndOfDragonDisplayEntity.class);

        TheEndOfDragonCollisionEntity collision =
                this.getChild(this.collisionEntityId, TheEndOfDragonCollisionEntity.class);

        if (display != null) display.syncFromCore(this);
        if (collision != null) collision.syncFromCore(this);
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
                    entity.push(knock.x * 2.4D, 0.6D, knock.z * 2.4D);
                    entity.hurtMarked = true;
                }
            }
        }
    }

    private boolean isRoarTargetItem(ItemStack stack) {
        if (stack.isEmpty()) return false;

        var item = stack.getItem();

        // 食べ物・設置ブロックは除外
        if (stack.has(net.minecraft.core.component.DataComponents.FOOD)) return false;
        if (item instanceof net.minecraft.world.item.BlockItem) return false;

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
        DragonCollisionBox head = getCollisionPartBox(DragonCollisionPart.HEAD);
        if (head == null || head.obb() == null) {
            return;
        }

        Vec3 axisY = head.obb().axisY().normalize();

        // 頭の少し前から発射
        Vec3 start = head.obb().center()
                .add(axisY.scale(2.5D));

        Vec3 shotDir = axisY;

        net.minecraft.world.entity.player.Player target =
                serverLevel.getNearestPlayer(this, 512.0D);

        if (target != null) {
            Vec3 toTarget = target.getEyePosition().subtract(start).normalize();

            double dot = axisY.dot(toTarget);
            double maxAngleCos = Math.cos(Math.toRadians(120.0D));

            if (dot > maxAngleCos) {
                shotDir = toTarget;
            }
        }

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



    private void updateRagnarokFall(ServerLevel level) {
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

    private void hideAllLaserVfx(ServerLevel serverLevel) {
        updateLaserAttachedToHand(serverLevel, DragonCollisionPart.FRONT_LEFT_HAND, false, 0, 0, 0, 1);
        updateLaserAttachedToHand(serverLevel, DragonCollisionPart.FRONT_RIGHT_HAND, false, 0, 0, 0, 1);
        updateLaserAttachedToHand(serverLevel, DragonCollisionPart.BACK_LEFT_HAND, false, 0, 0, 0, 1);
        updateLaserAttachedToHand(serverLevel, DragonCollisionPart.BACK_RIGHT_HAND, false, 0, 0, 0, 1);
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





    private void updateLaserAttachedToHand(
            ServerLevel serverLevel,
            DragonCollisionPart part,
            boolean active,
            double offsetX,
            double offsetY,
            double offsetZ,
            double maxLength
    ) {
        DragonCollisionBox box = getCollisionPartBox(part);
        if (box == null || box.obb() == null) {
            return;
        }

        TedVfxEntity vfx = getLaserVfxForPart(serverLevel, part);
        if (vfx == null) {
            return;
        }

        if (!active) {
            vfx.updateVfx(0.0F, 0.0F);
            return;
        }

        Vec3 axisX = box.obb().axisX().normalize();
        Vec3 axisY = box.obb().axisY().normalize();
        Vec3 axisZ = box.obb().axisZ().normalize();

        Vec3 forward = axisY;
        Vec3 up = axisZ;

        Vec3 start = box.obb().center()
                .add(axisX.scale(offsetX))
                .add(axisY.scale(offsetY))
                .add(axisZ.scale(offsetZ));

        double length = raycastLaserLength(serverLevel, start, forward, maxLength);

        org.joml.Quaternionf q = quaternionFromBasis(axisX, up, forward);

        vfx.updateVfx(1.5F, (float) length);
        vfx.setVfxRotationQuat(q);
        vfx.snapTo(start.x, start.y, start.z, 0.0F, 0.0F);
    }


    private void updateAttachedVfx(
            ServerLevel serverLevel,
            TedVfxSpec spec,
            boolean active
    ) {
        DragonCollisionBox box = getCollisionPartBox(spec.part());
        if (box == null || box.obb() == null) {
            return;
        }

        TedVfxEntity vfx = getLaserVfxForPart(serverLevel, spec.part());

        if (!active) {
            if (vfx != null) {
                vfx.updateVfx(0.0F, 0.0F);
            }
            return;
        }

        Vec3 axisX = box.obb().axisX().normalize();
        Vec3 axisY = box.obb().axisY().normalize();
        Vec3 axisZ = box.obb().axisZ().normalize();

        Vec3 start = box.obb().center()
                .add(axisX.scale(spec.offsetX()))
                .add(axisY.scale(spec.offsetY()))
                .add(axisZ.scale(spec.offsetZ()));

        Vec3 rayDir = axisY.normalize();

        float length = spec.length();

        if (spec.type() == TedVfxType.TED_LASER_BEAM) {
            length = (float) raycastLaserLength(
                    serverLevel,
                    start,
                    rayDir,
                    spec.length()
            );
        }

        TedBeamHitbox beam = new TedBeamHitbox(
                start,
                rayDir,
                length,
                spec.radius()
        );

        beam.damageEntities(
                serverLevel,
                this,
                TedConfig.values.laserDamage * (float) TedConfig.values.damageMultiplier,
                entity -> entity != this
        );

        switch (spec.type()) {
            case TED_LASER_BEAM ->{
                System.out.println("LASER");
                    beam.spawnLaserParticles(serverLevel);
            }
            case TED_JET -> {
                beam.spawnJetParticles(serverLevel);
                placeTemporaryLight(serverLevel, start);
            }
        }

        if (vfx != null) {
            vfx.updateVfx(0.0F, 0.0F);
        }
    }

    private void placeTemporaryLight(ServerLevel level, Vec3 pos) {
        BlockPos blockPos = BlockPos.containing(pos);

        level.setBlock(
                blockPos,
                Blocks.LIGHT.defaultBlockState()
                        .setValue(LightBlock.LEVEL, 15),
                3
        );
    }


}