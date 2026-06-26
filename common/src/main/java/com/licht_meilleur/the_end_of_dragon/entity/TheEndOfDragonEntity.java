package com.licht_meilleur.the_end_of_dragon.entity;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

public abstract class TheEndOfDragonEntity extends Monster implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public static final String ANIM_ORB_OF_ANNIHILATION = "animation.model.orb_of_annihilation_6tick_start_24tick_fire";
    public static final String ANIM_ROAR_OF_OBLITERATION = "animation.model.roar_of_obliteration_3tick_start";
    public static final String ANIM_FLAMES_OF_RAGNAROK = "animation.model.flames_of_ragnarok";
    public static final String ANIM_LIGHT_OF_DESTRUCTION = "animation.model.light_of_destruction_24tick_start";
    public static final String ANIM_PHOTON_BLASTER = "animation.model.photon_blaster_6tick_start";
    public static final String ANIM_BLASTER_TACKLE = "animation.model.blaster_tackle_12tick_start";
    public static final String ANIM_FLY_START = "animation.model.fly_start_12tick_start";
    public static final String ANIM_FLY = "animation.model.fly";
    public static final String ANIM_FLY_SHOT = "animation.model.fly_shot_6tick_start";
    public static final String ANIM_FLY_LEFT = "animation.model.fly_left";
    public static final String ANIM_FLY_RIGHT = "animation.model.fly_right";
    public static final String ANIM_FALL = "animation.model.fall";
    public static final String ANIM_LANDING = "animation.model.landing";
    public static final String ANIM_IDLE = "animation.model.idle";
    public static final String ANIM_WALK = "animation.model.walk";
    public static final String ANIM_SUPER_LANDING = "animation.model.super_landing";

    private int dragonStateAgeTicks;

    protected static final EntityDataAccessor<Integer> DATA_STATE =
            SynchedEntityData.defineId(TheEndOfDragonEntity.class, EntityDataSerializers.INT);

    protected TheEndOfDragonEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.setPersistenceRequired();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_STATE, DragonState.IDLE.ordinal());
    }

    public void syncFromCore(TheEndOfDragonCoreEntity core) {
        this.setPos(core.getX(), core.getY(), core.getZ());
        this.setYRot(core.getYRot());
        this.setXRot(core.getXRot());

        this.yBodyRot = core.yBodyRot;
        this.yHeadRot = core.yHeadRot;
        this.yRotO = core.yRotO;
        this.xRotO = core.xRotO;
        this.dragonStateAgeTicks = core.getDragonStateAgeTicks();

        this.setDragonState(core.getDragonState());

        if (!this.level().isClientSide() && this.tickCount % 40 == 0) {
            System.out.println(
                    "[TED SYNC] " + this.getClass().getSimpleName()
                            + " id=" + this.getId()
                            + " state=" + this.getDragonState()
            );
        }
    }

    public void setDragonState(DragonState state) {
        this.entityData.set(DATA_STATE, state.ordinal());
    }

    public DragonState getDragonState() {
        int id = this.entityData.get(DATA_STATE);
        DragonState[] values = DragonState.values();

        if (id < 0 || id >= values.length) {
            return DragonState.IDLE;
        }

        return values[id];
    }

    public int getDragonStateAgeTicks() {
        return this.dragonStateAgeTicks;
    }

    @Override
    public void tick() {
        super.tick();
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(
                "main_controller",
                0,
                state -> {
                    DragonState current = this.getDragonState();


                    state.setAnimation(this.animationForState(current));
                    return PlayState.CONTINUE;
                }
        ));
    }

    private RawAnimation animationForState(DragonState state) {
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