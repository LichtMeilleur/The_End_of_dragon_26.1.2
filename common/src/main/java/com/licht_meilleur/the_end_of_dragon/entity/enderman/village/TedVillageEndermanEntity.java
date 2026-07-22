package com.licht_meilleur.the_end_of_dragon.entity.enderman.village;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public abstract class TedVillageEndermanEntity
        extends PathfinderMob
        implements GeoEntity {

    private static final RawAnimation IDLE_ANIMATION =
            RawAnimation.begin()
                    .thenLoop(
                            "animation.model.idle"
                    );

    private static final RawAnimation WALK_ANIMATION =
            RawAnimation.begin()
                    .thenLoop(
                            "animation.model.walk"
                    );

    private final AnimatableInstanceCache animationCache =
            GeckoLibUtil.createInstanceCache(this);

    protected TedVillageEndermanEntity(
            EntityType<? extends PathfinderMob> entityType,
            Level level
    ) {
        super(entityType, level);

        this.setPersistenceRequired();
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(
                0,
                new FloatGoal(this)
        );

        this.goalSelector.addGoal(
                5,
                new WaterAvoidingRandomStrollGoal(
                        this,
                        0.65D,
                        0.001F
                )
        );

        this.goalSelector.addGoal(
                6,
                new LookAtPlayerGoal(
                        this,
                        Player.class,
                        10.0F
                )
        );

        this.goalSelector.addGoal(
                7,
                new RandomLookAroundGoal(this)
        );
    }

    public static AttributeSupplier.Builder
    createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(
                        Attributes.MAX_HEALTH,
                        40.0D
                )
                .add(
                        Attributes.ARMOR,
                        6.0D
                )
                .add(
                        Attributes.MOVEMENT_SPEED,
                        0.25D
                )
                .add(
                        Attributes.FOLLOW_RANGE,
                        32.0D
                )
                .add(
                        Attributes.KNOCKBACK_RESISTANCE,
                        0.25D
                );
    }

    @Override
    public void registerControllers(
            AnimatableManager.ControllerRegistrar controllers
    ) {
        controllers.add(
                new AnimationController<>(
                        "movement",
                        4,
                        state -> {
                            if (state.isMoving()) {
                                state.controller()
                                        .setAnimation(
                                                WALK_ANIMATION
                                        );
                            } else {
                                state.controller()
                                        .setAnimation(
                                                IDLE_ANIMATION
                                        );
                            }

                            return PlayState.CONTINUE;
                        }
                )
        );
    }

    @Override
    public AnimatableInstanceCache
    getAnimatableInstanceCache() {
        return this.animationCache;
    }

    @Override
    public boolean removeWhenFarAway(
            double distanceToClosestPlayer
    ) {
        return false;
    }

    @Override
    public boolean isPersistenceRequired() {
        return true;
    }
}