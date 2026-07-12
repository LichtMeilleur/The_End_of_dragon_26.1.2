package com.licht_meilleur.the_end_of_dragon.entity;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.util.GeckoLibUtil;
import com.licht_meilleur.the_end_of_dragon.registry.ModItems;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.UUID;

public abstract class TheEndOfDragonEntity extends Monster implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public static final String ANIM_ORB_OF_ANNIHILATION = "animation.model.orb_of_annihilation_6tick_start_55tick_fire";
    public static final String ANIM_ROAR_OF_OBLITERATION = "animation.model.roar_of_obliteration_10tick_start";
    public static final String ANIM_FLAMES_OF_RAGNAROK = "animation.model.flames_of_ragnarok";
    public static final String ANIM_LIGHT_OF_DESTRUCTION = "animation.model.light_of_destruction_20tick_start";
    public static final String ANIM_PHOTON_BLASTER = "animation.model.photon_blaster_27tick_start";
    public static final String ANIM_BLASTER_TACKLE = "animation.model.blaster_tackle_9tick_start";
    public static final String ANIM_FLY_START = "animation.model.fly_start_10tick_start";
    public static final String ANIM_FLY = "animation.model.fly";
    public static final String ANIM_FLY_SHOT = "animation.model.fly_shot_5tick_start";
    public static final String ANIM_FLY_LEFT = "animation.model.fly_left";
    public static final String ANIM_FLY_RIGHT = "animation.model.fly_right";
    public static final String ANIM_FALL = "animation.model.fall";
    public static final String ANIM_LANDING = "animation.model.landing";
    public static final String ANIM_IDLE = "animation.model.idle";
    public static final String ANIM_WALK = "animation.model.walk";
    public static final String ANIM_SUPER_LANDING = "animation.model.super_landing";
    public static final String ANIM_FLY_ASCEND = "animation.model.fly_ascend";
    public static final String ANIM_FLY_DESCEND = "animation.model.fly_descend";
    public static final String ANIM_TAIL_WHIP = "animation.model.tail_whip_6tick_start_12tick_end";

    public static final String ANIM_JUDGMENT_RAY = "animation.model.judgment_ray_25tick_start";
    public static final String ANIM_PHOTON_BUSTER = "animation.model.photon_buster_25tick_start_60tick_end";


    public static final String ANIM_DEAD = "animation.model.dead";

    protected TheEndOfDragonEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.setPersistenceRequired();
    }

    private double prevSyncX;
    private double prevSyncY;
    private double prevSyncZ;
    private boolean hasPrevSyncPos = false;

    private float flightPitch = 0.0F;

    private int localDeadTicks = 0;
    private int localCrystalFadeStage = 0;

    private static final int CHILD_DATA_VERSION = 1;

    private UUID ownerCoreUuid = null;

    public void setOwnerCoreUuid(UUID uuid) {
        this.ownerCoreUuid = uuid;
    }

    public UUID getOwnerCoreUuid() {
        return this.ownerCoreUuid;
    }

    public boolean hasOwnerCoreUuid(UUID uuid) {
        return this.ownerCoreUuid != null && this.ownerCoreUuid.equals(uuid);
    }

    private static final EntityDataAccessor<Integer> DATA_RENDER_STATE =
            SynchedEntityData.defineId(TheEndOfDragonEntity.class, EntityDataSerializers.INT);


    private static final EntityDataAccessor<Boolean> DATA_FLY_SHOT_ANIM =
            SynchedEntityData.defineId(TheEndOfDragonEntity.class, EntityDataSerializers.BOOLEAN);


    private static final EntityDataAccessor<Integer> DATA_CRYSTAL_STAGE =
            SynchedEntityData.defineId(
                    TheEndOfDragonEntity.class,
                    EntityDataSerializers.INT);

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_RENDER_STATE, DragonState.IDLE.ordinal());
        builder.define(DATA_FLY_SHOT_ANIM, false);
        builder.define(DATA_CRYSTAL_STAGE,0);
    }


    protected DragonState getAnimationState() {
        return DragonState.IDLE;
    }

    public DragonState getAnimationStateForCollision() {
        return getAnimationState();
    }

    public void syncFromCore(TheEndOfDragonCoreEntity core) {
        this.setOwnerCoreUuid(core.getUUID());

        this.setPos(core.getX(), core.getY(), core.getZ());

        float yaw = core.getYRot();
        float pitch = core.getVisualPitch();

        this.setYRot(yaw);
        this.setYBodyRot(yaw);
        this.setYHeadRot(yaw);
        this.setXRot(pitch);

        this.yRotO = core.yRotO;
        this.yBodyRotO = core.yBodyRotO;
        this.yHeadRotO = core.yHeadRotO;
        this.xRotO = pitch;


        updateFlightPitchFromMovement();

        this.setXRot(this.flightPitch);
        this.xRotO = this.flightPitch;

        this.entityData.set(DATA_RENDER_STATE, core.getDragonState().ordinal());

        if (core.getDragonState() == DragonState.DEAD && this.tickCount % 20 == 0) {
           // System.out.println("[TED CRYSTAL SYNC] stage=" + core.getCrystalFadeStage());
        }

        entityData.set(DATA_CRYSTAL_STAGE,
                core.getCrystalFadeStage());
        this.entityData.set(DATA_CRYSTAL_STAGE, core.getCrystalFadeStage());
    }


    @Override
    protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput output) {
        super.addAdditionalSaveData(output);

        output.putInt("TedChildDataVersion", CHILD_DATA_VERSION);

        if (this.ownerCoreUuid != null) {
            output.putString("OwnerCoreUuid", this.ownerCoreUuid.toString());
        }
    }

    @Override
    protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput input) {
        super.readAdditionalSaveData(input);

        String uuidText = input.getStringOr("OwnerCoreUuid", "");

        if (!uuidText.isEmpty()) {
            try {
                this.ownerCoreUuid = UUID.fromString(uuidText);
            } catch (IllegalArgumentException ignored) {
                this.ownerCoreUuid = null;
            }
        }
    }

    protected DragonState getSyncedRenderState() {
        int id = this.entityData.get(DATA_RENDER_STATE);
        DragonState[] values = DragonState.values();

        if (id < 0 || id >= values.length) {
            return DragonState.IDLE;
        }

        return values[id];
    }

    public int getCrystalFadeStage() {
        return this.localCrystalFadeStage;
    }

    private static boolean isFlyingState(DragonState state) {
        return switch (state) {
            case FLY_START,
                 FLY,
                 FLY_LEFT,
                 FLY_RIGHT,
                 FLY_SHOT,
                 FLAMES_OF_RAGNAROK,
                 FALL,
                 INTRO_RISE,
                 INTRO_WAIT_PORTAL,
                 INTRO_FLY_TO_PORTAL,
                 INTRO_DIVE_TO_PORTAL -> true;
            default -> false;
        };
    }

    private static boolean isGroundState(DragonState state) {
        return switch (state) {
            case IDLE,
                 WALK,
                 LANDING,
                 INTRO_SUPER_LANDING,
                 SUPER_LANDING,
                 ORB_OF_ANNIHILATION,
                 ROAR_OF_OBLITERATION,
                 LIGHT_OF_DESTRUCTION,
                 PHOTON_BLASTER,
                 BLASTER_TACKLE -> true;
            default -> false;
        };
    }

    public enum DragonMovementMode {
        GROUND,
        FLYING
    }




    private void updateFlightPitchFromMovement() {
        if (!hasPrevSyncPos) {
            this.prevSyncX = this.getX();
            this.prevSyncY = this.getY();
            this.prevSyncZ = this.getZ();
            this.hasPrevSyncPos = true;
            return;
        }

        double dx = this.getX() - this.prevSyncX;
        double dy = this.getY() - this.prevSyncY;
        double dz = this.getZ() - this.prevSyncZ;

        this.prevSyncX = this.getX();
        this.prevSyncY = this.getY();
        this.prevSyncZ = this.getZ();

        DragonState state = getAnimationState();

        if (isGroundState(state)) {
            this.flightPitch = 0.0F;
            return;
        }

        double horizontal = Math.sqrt(dx * dx + dz * dz);

        if (horizontal < 1.0E-5D && Math.abs(dy) < 1.0E-5D) {
            return;
        }

        this.flightPitch = (float) Math.toDegrees(Math.atan2(dy, horizontal));
    }

    public float getFlightPitch() {
        return this.flightPitch;
    }


    @Override
    public void tick() {
        super.tick();
        this.noPhysics = true;
        this.setNoGravity(true);

        if (getSyncedRenderState() == DragonState.DEAD) {
            localDeadTicks++;

            if (localDeadTicks >= 110) {
                localCrystalFadeStage = 4;
            } else if (localDeadTicks >= 90) {
                localCrystalFadeStage = 3;
            } else if (localDeadTicks >= 60) {
                localCrystalFadeStage = 2;
            } else if (localDeadTicks >= 30) {
                localCrystalFadeStage = 1;
            }


        } else {
            localDeadTicks = 0;
            localCrystalFadeStage = 0;
        }

    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<>(
                        "controller",
                        0,
                        state -> {
                            state.controller().setAnimation(animationForState(getAnimationState()));
                            return PlayState.CONTINUE;
                        }
                )
        );
    }

    private RawAnimation animationForState(DragonState state) {
        if (this.entityData.get(DATA_FLY_SHOT_ANIM)) {
            return RawAnimation.begin().thenPlay(ANIM_FLY_SHOT);
        }



        return switch (state) {
            case WALK -> RawAnimation.begin().thenLoop(ANIM_WALK);

            case FLY_START -> RawAnimation.begin().thenPlay(ANIM_FLY_START);
            case FLY -> RawAnimation.begin().thenLoop(ANIM_FLY);
            case FLY_LEFT -> RawAnimation.begin().thenLoop(ANIM_FLY_LEFT);
            case FLY_RIGHT -> RawAnimation.begin().thenLoop(ANIM_FLY_RIGHT);
            case FLY_SHOT -> RawAnimation.begin().thenPlay(ANIM_FLY_SHOT);

            case FALL -> RawAnimation.begin().thenLoop(ANIM_FALL);
            case LANDING -> RawAnimation.begin().thenPlay(ANIM_LANDING);
            case SUPER_LANDING -> RawAnimation.begin().thenPlay(ANIM_SUPER_LANDING);

            case ORB_OF_ANNIHILATION -> RawAnimation.begin().thenPlay(ANIM_ORB_OF_ANNIHILATION);
            case ROAR_OF_OBLITERATION -> RawAnimation.begin().thenPlay(ANIM_ROAR_OF_OBLITERATION);
            case FLAMES_OF_RAGNAROK -> RawAnimation.begin().thenLoop(ANIM_FLAMES_OF_RAGNAROK);
            case LIGHT_OF_DESTRUCTION -> RawAnimation.begin().thenPlay(ANIM_LIGHT_OF_DESTRUCTION);
            case PHOTON_BLASTER -> RawAnimation.begin().thenPlay(ANIM_PHOTON_BLASTER);
            case BLASTER_TACKLE -> RawAnimation.begin().thenPlay(ANIM_BLASTER_TACKLE);
            case TAIL_WHIP -> RawAnimation.begin().thenPlay(ANIM_TAIL_WHIP);
            case JUDGMENT_RAY -> RawAnimation.begin().thenPlay(ANIM_JUDGMENT_RAY);
            case PHOTON_BUSTER -> RawAnimation.begin().thenPlay(ANIM_PHOTON_BUSTER);


            case
                 INTRO_WAIT_PORTAL,
                 INTRO_FLY_TO_PORTAL -> RawAnimation.begin().thenLoop(ANIM_FLY);

            case INTRO_SUPER_LANDING -> RawAnimation.begin().thenPlay(ANIM_SUPER_LANDING);

            case FLY_ASCEND,
                 INTRO_RISE -> RawAnimation.begin().thenLoop(ANIM_FLY_ASCEND);
            case FLY_DESCEND,
                 INTRO_DIVE_TO_PORTAL -> RawAnimation.begin().thenLoop(ANIM_FLY_DESCEND);

            case FIGURE_EIGHT -> RawAnimation.begin().thenLoop(ANIM_FLY);

            case DEAD -> RawAnimation.begin().thenPlayAndHold(ANIM_DEAD);

            case RECOVERY_ASCEND -> RawAnimation.begin().thenLoop(ANIM_FLY_ASCEND);
            case RECOVERY_RETURN -> RawAnimation.begin().thenLoop(ANIM_FLY);
            case RECOVERY_DIVE -> RawAnimation.begin().thenLoop(ANIM_FLY_DESCEND);

            default -> RawAnimation.begin().thenLoop(ANIM_IDLE);
        };
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }
}