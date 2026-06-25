package com.licht_meilleur.the_end_of_dragon.entity;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;
import com.licht_meilleur.the_end_of_dragon.entity.hitbox.DragonHitBoxManager;
import com.licht_meilleur.the_end_of_dragon.entity.hitbox.DragonServerDamageHitBoxHandler;
import com.licht_meilleur.the_end_of_dragon.entity.hitbox.DragonServerPushHandler;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class TheEndOfDragonEntity extends Monster implements GeoEntity {
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




    private boolean spawnedHitBoxes;

    public TheEndOfDragonEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 500;
        this.setPersistenceRequired();
    }


    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            this.setBoundingBox(this.makePhysicsBoundingBox());


            if (!this.spawnedHitBoxes) {
                this.spawnedHitBoxes = true;
                DragonHitBoxManager.spawnParts(this);
            }



            DragonServerPushHandler.tick(this);
        }
    }

    private AABB makePhysicsBoundingBox() {
        double width = 1.0D;
        double height = 10.0D;
        double half = width / 2.0D;

        return new AABB(
                this.getX() - half,
                this.getY(),
                this.getZ() - half,
                this.getX() + half,
                this.getY() + height,
                this.getZ() + half
        );
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 600.0D)
                .add(Attributes.ATTACK_DAMAGE, 20.0D)
                .add(Attributes.ARMOR, 20.0D)
                .add(Attributes.FOLLOW_RANGE, 128.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void die(net.minecraft.world.damagesource.DamageSource damageSource) {
        super.die(damageSource);

        if (!this.level().isClientSide() && this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            com.licht_meilleur.the_end_of_dragon.world.EndPortalSealHandler.unseal(serverLevel);
        }
    }
}