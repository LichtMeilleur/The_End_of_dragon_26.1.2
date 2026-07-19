package com.licht_meilleur.the_end_of_dragon.entity.enderman;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.util.GeckoLibUtil;
import com.licht_meilleur.the_end_of_dragon.entity.enderman.goal.*;
import com.licht_meilleur.the_end_of_dragon.registry.ModItems;
import com.licht_meilleur.the_end_of_dragon.world.TedAllyEndermanMessageHandler;
import com.licht_meilleur.the_end_of_dragon.world.TedBattleWorldState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class TedAllyEndermanEntity
        extends PathfinderMob
        implements GeoEntity {

    private static final EntityDataAccessor<Integer> DATA_STATE =
            SynchedEntityData.defineId(
                    TedAllyEndermanEntity.class,
                    EntityDataSerializers.INT
            );

    private static final EntityDataAccessor<Integer> DATA_FOOD_LEVEL =
            SynchedEntityData.defineId(
                    TedAllyEndermanEntity.class,
                    EntityDataSerializers.INT
            );



    private static final int MAX_FOOD_LEVEL = 20;
    private static final int RESCUE_REQUIRED_FOOD = 8;

    private UUID handOverTargetUuid = null;
    private boolean handOverItemGiven = false;

    private static final int HAND_OVER_GIVE_TICK = 18;
    private static final int HAND_OVER_END_TICK = 45;


    private int rescueMessageTicks;
    private boolean rescueMessagesStarted;

    /*
     * RECOVERINGの継続時間。
     * 20tick = 1秒。
     */
    private static final int RECOVERING_DURATION = 30;

    private static final RawAnimation IDLE_ANIMATION =
            RawAnimation.begin()
                    .thenLoop("animation.model.idle");

    private static final RawAnimation WALK_ANIMATION =
            RawAnimation.begin()
                    .thenLoop("animation.model.walk");

    private static final RawAnimation DYING_ANIMATION =
            RawAnimation.begin()
                    .thenLoop("animation.model.dying");

    private static final RawAnimation RECOVERING_ANIMATION =
            RawAnimation.begin()
                    .thenPlayAndHold("animation.model.recovering");

    private static final RawAnimation WARP_PUNCH_ANIMATION =
            RawAnimation.begin()
                    .thenPlayAndHold("animation.model.warp_punch");

    private static final RawAnimation WARP_KICK_ANIMATION =
            RawAnimation.begin()
                    .thenPlayAndHold("animation.model.warp_kick");

    private static final RawAnimation WARP_SMASH_ANIMATION =
            RawAnimation.begin()
                    .thenPlayAndHold("animation.model.warp_smash");

    private static final RawAnimation WITH_PLAYER_WARP_ANIMATION =
            RawAnimation.begin()
                    .thenPlayAndHold("animation.model.with_player_warp");

    private static final RawAnimation HAND_OVER_ANIMATION =
            RawAnimation.begin()
                    .thenPlayAndHold(
                            "animation.model.hand_over"
                    );

    private final AnimatableInstanceCache animationCache =
            GeckoLibUtil.createInstanceCache(this);



    /*
     * 状態を切り替えたtick。
     */
    private int stateStartTick = 0;

    /*
     * 自然回復用クールタイム。
     */
    private int foodHealCooldown = 0;


    private int supportAttackCooldown;
    private int selfEvadeCooldown;
    private int playerRescueCooldown;
    private int followWarpCooldown;

    private UUID supportPlayerUuid;
    private UUID combatDragonUuid;

    public TedAllyEndermanEntity(
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
                new AllySelfEvadeGoal(this)
        );

        this.goalSelector.addGoal(
                1,
                new AllyRescuePlayerGoal(this)
        );

        this.goalSelector.addGoal(
                2,
                new AllyHitAndAwayGoal(this)
        );

        this.goalSelector.addGoal(
                3,
                new AllyFollowPlayerGoal(this)
        );

        /*
        this.goalSelector.addGoal(
                4,
                new AllyVillageGuardGoal(this)
        );

        this.goalSelector.addGoal(
                5,
                new AllyVillagePatrolGoal(this)
        );

         */
    }

    public boolean canRunSupportAi() {
        if (!this.isAlive() || this.isRemoved()) {
            return false;
        }

        return this.getAllyState() == AllyEndermanState.SUPPORT_IDLE;
    }

    private void tickSupportCooldowns() {
        if (this.supportAttackCooldown > 0) {
            this.supportAttackCooldown--;
        }

        if (this.selfEvadeCooldown > 0) {
            this.selfEvadeCooldown--;
        }

        if (this.playerRescueCooldown > 0) {
            this.playerRescueCooldown--;
        }

        if (this.followWarpCooldown > 0) {
            this.followWarpCooldown--;
        }
    }

    public int getSupportAttackCooldown() {
        return supportAttackCooldown;
    }

    public void setSupportAttackCooldown(int ticks) {
        this.supportAttackCooldown =
                Math.max(0, ticks);
    }

    public int getSelfEvadeCooldown() {
        return selfEvadeCooldown;
    }

    public void setSelfEvadeCooldown(int ticks) {
        this.selfEvadeCooldown =
                Math.max(0, ticks);
    }

    public int getPlayerRescueCooldown() {
        return playerRescueCooldown;
    }

    public void setPlayerRescueCooldown(int ticks) {
        this.playerRescueCooldown =
                Math.max(0, ticks);
    }

    public int getFollowWarpCooldown() {
        return followWarpCooldown;
    }

    public void setFollowWarpCooldown(int ticks) {
        this.followWarpCooldown =
                Math.max(0, ticks);
    }



    public int getFoodPoints() {
        return this.entityData.get(DATA_FOOD_LEVEL);
    }

    public int getMaxFoodPoints() {
        return MAX_FOOD_LEVEL;
    }

    public void setFoodPoints(
            int foodPoints
    ) {
        this.entityData.set(
                DATA_FOOD_LEVEL,
                Mth.clamp(
                        foodPoints,
                        0,
                        MAX_FOOD_LEVEL
                )
        );
    }


    @Override
    protected void defineSynchedData(
            SynchedEntityData.Builder builder
    ) {
        super.defineSynchedData(builder);

        builder.define(
                DATA_STATE,
                AllyEndermanState.WOUNDED.ordinal()
        );

        builder.define(
                DATA_FOOD_LEVEL,
                0
        );
    }


    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(
                        Attributes.MAX_HEALTH,
                        60.0D
                )
                .add(
                        Attributes.ATTACK_DAMAGE,
                        8.0D
                )
                .add(
                        Attributes.ARMOR,
                        6.0D
                )
                .add(
                        Attributes.MOVEMENT_SPEED,
                        0.30D
                )
                .add(
                        Attributes.FOLLOW_RANGE,
                        64.0D
                )
                .add(
                        Attributes.KNOCKBACK_RESISTANCE,
                        0.35D
                );
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            return;
        }


        tickSupportCooldowns();
        tickRescueMessages();
        tickState();
        tickFoodHealing();
        tickPostBattleHandOver();
    }

    private void tickState() {
        AllyEndermanState state =
                this.getAllyState();

        switch (state) {
            case WOUNDED, DEFEATED -> {
                /*
                 * 倒れている間は移動させない。
                 */
                this.getNavigation().stop();
                this.setDeltaMovement(
                        0.0D,
                        this.getDeltaMovement().y,
                        0.0D
                );

                this.setTarget(null);
            }

            case RECOVERING -> {
                this.getNavigation().stop();

                this.setDeltaMovement(
                        0.0D,
                        this.getDeltaMovement().y,
                        0.0D
                );

                if (this.getStateAgeTicks()
                        >= RECOVERING_DURATION) {
                    finishRecovering();
                }
            }

            case HAND_OVER -> {
                this.getNavigation().stop();

                this.setDeltaMovement(
                        0.0D,
                        this.getDeltaMovement().y,
                        0.0D
                );

                tickHandOver();
            }

            default -> {
            }
        }
    }

    private void finishRecovering() {
        float minimumHealth =
                this.getMaxHealth() * 0.35F;

        if (this.getHealth() < minimumHealth) {
            this.setHealth(minimumHealth);
        }

        if (!(this.level() instanceof ServerLevel serverLevel)) {
            this.setAllyState(
                    AllyEndermanState.SUPPORT_IDLE
            );
            return;
        }

        TedBattleWorldState worldState =
                TedBattleWorldState.get(serverLevel);

        /*
         * 戦闘中に救助された場合。
         */
        if (worldState.isBattleActive()) {
            worldState.setAllyProgress(
                    TedBattleWorldState
                            .TedAllyProgress
                            .ALLY_ACTIVE
            );

            this.setAllyState(
                    AllyEndermanState.SUPPORT_IDLE
            );

            return;
        }

        /*
         * TED討伐後に再び救助された場合。
         */
        if (worldState.getAllyProgress()
                == TedBattleWorldState
                .TedAllyProgress
                .WOUNDED_AFTER_BATTLE) {

            worldState.setAllyProgress(
                    TedBattleWorldState
                            .TedAllyProgress
                            .RECOVERED_AFTER_BATTLE
            );

            Player nearestPlayer =
                    serverLevel.getNearestPlayer(
                            this,
                            64.0D
                    );

            if (nearestPlayer != null
                    && nearestPlayer.isAlive()) {
                this.startHandOver(nearestPlayer);
            } else {
                /*
                 * プレイヤーが近くにいなければ、
                 * 後のtickで再試行する。
                 */
                this.setAllyState(
                        AllyEndermanState.SUPPORT_IDLE
                );
            }

            return;
        }

        this.setAllyState(
                AllyEndermanState.SUPPORT_IDLE
        );
    }

    private void tickFoodHealing() {
        if (!this.isCombatReady()) {
            return;
        }

        if (this.getHealth() >= this.getMaxHealth()) {
            return;
        }

        if (this.getFoodLevel() < 18) {
            return;
        }

        if (this.foodHealCooldown > 0) {
            this.foodHealCooldown--;
            return;
        }

        /*
         * 1.0F = 半ハート。
         */
        this.heal(1.0F);

        this.setFoodLevel(
                this.getFoodLevel() - 1
        );

        this.foodHealCooldown = 20;
    }

    @Override
    protected InteractionResult mobInteract(
            Player player,
            InteractionHand hand
    ) {
        ItemStack stack =
                player.getItemInHand(hand);

        if (this.getAllyState()
                != AllyEndermanState.WOUNDED) {
            return super.mobInteract(
                    player,
                    hand
            );
        }

        FoodProperties food =
                stack.get(DataComponents.FOOD);

        if (food == null) {
            return InteractionResult.PASS;
        }

        if (!this.level().isClientSide()) {
            feedEnderman(
                    player,
                    stack,
                    food
            );
        }

        return InteractionResult.SUCCESS;
    }

    private void feedEnderman(
            Player player,
            ItemStack stack,
            FoodProperties food
    ) {
        int nutrition =
                Math.max(
                        1,
                        food.nutrition()
                );

        this.setFoodLevel(
                this.getFoodLevel()
                        + nutrition
        );

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        if (this.getFoodLevel()
                >= RESCUE_REQUIRED_FOOD) {
            startRecovering();
        }
    }

    private void startRecovering() {
        if (this.getAllyState()
                != AllyEndermanState.WOUNDED) {
            return;
        }

        this.setAllyState(
                AllyEndermanState.RECOVERING
        );
    }

    public AllyEndermanState getAllyState() {
        int id =
                this.entityData.get(DATA_STATE);

        AllyEndermanState[] states =
                AllyEndermanState.values();

        if (id < 0 || id >= states.length) {
            return AllyEndermanState.WOUNDED;
        }

        return states[id];
    }

    public void setAllyState(
            AllyEndermanState state
    ) {
        if (state == null) {
            state = AllyEndermanState.WOUNDED;
        }

        if (this.getAllyState() == state) {
            return;
        }

        this.entityData.set(
                DATA_STATE,
                state.ordinal()
        );

        this.stateStartTick =
                this.tickCount;
    }

    public int getStateAgeTicks() {
        return Math.max(
                0,
                this.tickCount - this.stateStartTick
        );
    }

    public int getFoodLevel() {
        return this.entityData.get(
                DATA_FOOD_LEVEL
        );
    }

    public void setFoodLevel(
            int foodLevel
    ) {
        this.entityData.set(
                DATA_FOOD_LEVEL,
                Math.clamp(
                        foodLevel,
                        0,
                        MAX_FOOD_LEVEL
                )
        );
    }

    public boolean isCombatReady() {
        return switch (this.getAllyState()) {
            case SUPPORT_IDLE,
                 ATTACK_WARP_PREPARE,
                 WARP_PUNCH,
                 WARP_KICK,
                 WARP_SMASH,
                 WITH_PLAYER_WARP,
                 VICTORY -> true;

            default -> false;
        };
    }

    /*
     * 最初のテスト用。
     * コマンドなどで通常状態へ戻す際にも使える。
     */
    public void debugRecoverImmediately() {
        this.setFoodLevel(
                RESCUE_REQUIRED_FOOD
        );

        this.setHealth(
                this.getMaxHealth()
                        * 0.35F
        );

        this.setAllyState(
                AllyEndermanState.SUPPORT_IDLE
        );
    }



    @Override
    public void registerControllers(
            AnimatableManager.ControllerRegistrar controllers
    ) {
        controllers.add(
                new AnimationController<>(
                        "main_controller",
                        3,
                        animationTest -> {
                            AllyEndermanState allyState =
                                    this.getAllyState();

                            switch (allyState) {
                                case WOUNDED, DEFEATED -> {
                                    animationTest.controller().setAnimation(
                                            DYING_ANIMATION
                                    );

                                    return PlayState.CONTINUE;
                                }

                                case RECOVERING -> {
                                    animationTest.controller().setAnimation(
                                            RECOVERING_ANIMATION
                                    );

                                    return PlayState.CONTINUE;
                                }

                                case HAND_OVER -> {
                                    animationTest.controller().setAnimation(
                                            HAND_OVER_ANIMATION
                                    );

                                    return PlayState.CONTINUE;
                                }

                                case WARP_PUNCH -> {
                                    animationTest.controller().setAnimation(
                                            WARP_PUNCH_ANIMATION
                                    );

                                    return PlayState.CONTINUE;
                                }

                                case WARP_KICK -> {
                                    animationTest.controller().setAnimation(
                                            WARP_KICK_ANIMATION
                                    );

                                    return PlayState.CONTINUE;
                                }

                                case WARP_SMASH -> {
                                    animationTest.controller().setAnimation(
                                            WARP_SMASH_ANIMATION
                                    );

                                    return PlayState.CONTINUE;
                                }

                                case WITH_PLAYER_WARP -> {
                                    animationTest.controller().setAnimation(
                                            WITH_PLAYER_WARP_ANIMATION
                                    );

                                    return PlayState.CONTINUE;
                                }

                                default -> {
                                    double horizontalMovement =
                                            this.getDeltaMovement()
                                                    .horizontalDistanceSqr();

                                    if (horizontalMovement > 0.0004D) {
                                        animationTest.controller().setAnimation(
                                                WALK_ANIMATION
                                        );
                                    } else {
                                        animationTest.controller().setAnimation(
                                                IDLE_ANIMATION
                                        );
                                    }

                                    return PlayState.CONTINUE;
                                }
                            }
                        }
                )
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animationCache;
    }

    @Override
    protected void addAdditionalSaveData(
            net.minecraft.world.level.storage.ValueOutput output
    ) {
        super.addAdditionalSaveData(output);

        output.putString(
                "AllyState",
                this.getAllyState().name()
        );

        output.putInt(
                "FoodLevel",
                this.getFoodLevel()
        );
    }

    @Override
    protected void readAdditionalSaveData(
            net.minecraft.world.level.storage.ValueInput input
    ) {
        super.readAdditionalSaveData(input);

        String savedState =
                input.getStringOr(
                        "AllyState",
                        AllyEndermanState.WOUNDED.name()
                );

        try {
            this.setAllyState(
                    AllyEndermanState.valueOf(
                            savedState
                    )
            );
        } catch (IllegalArgumentException exception) {
            this.setAllyState(
                    AllyEndermanState.WOUNDED
            );
        }

        this.setFoodLevel(
                input.getIntOr(
                        "FoodLevel",
                        0
                )
        );
    }
    public void startHandOver(
            Player player
    ) {
        if (player == null || !player.isAlive()) {
            return;
        }

        this.handOverTargetUuid =
                player.getUUID();

        this.handOverItemGiven = false;

        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);

        this.setAllyState(
                AllyEndermanState.HAND_OVER
        );
    }

    private void tickHandOver() {
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }

        Player player = null;

        if (this.handOverTargetUuid != null) {
            player = level.getPlayerByUUID(
                    this.handOverTargetUuid
            );
        }

        if (player != null && player.isAlive()) {
            this.getLookControl().setLookAt(
                    player,
                    30.0F,
                    30.0F
            );
        }

        int age =
                this.getStateAgeTicks();

        if (!this.handOverItemGiven
                && age >= HAND_OVER_GIVE_TICK) {

            this.handOverItemGiven = true;

            if (player != null) {
                giveInvitationItem(player);
            }
        }

        if (age >= HAND_OVER_END_TICK) {
            this.handOverTargetUuid = null;
            this.handOverItemGiven = false;

            this.setAllyState(
                    AllyEndermanState.SUPPORT_IDLE
            );
        }
    }

    private void giveInvitationItem(
            Player player
    ) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        TedBattleWorldState worldState =
                TedBattleWorldState.get(serverLevel);

        /*
         * 二重配布防止。
         */
        if (worldState.getAllyProgress()
                == TedBattleWorldState
                .TedAllyProgress
                .ITEM_GIVEN) {
            return;
        }

        ItemStack gateway =
                new ItemStack(
                        ModItems.ENDERMAN_VILLAGE_GATEWAY
                );

        if (!player.getInventory().add(gateway)) {
            player.drop(
                    gateway,
                    false
            );
        }

        worldState.setAllyProgress(
                TedBattleWorldState
                        .TedAllyProgress
                        .ITEM_GIVEN
        );
    }

    public void setWoundedForBattle() {
        this.getNavigation().stop();
        this.setTarget(null);

        this.setFoodLevel(0);

        this.setHealth(
                Math.max(
                        1.0F,
                        this.getMaxHealth()
                                * 0.08F
                )
        );

        this.setAllyState(
                AllyEndermanState.WOUNDED
        );

        this.setDeltaMovement(
                Vec3.ZERO
        );

        if (this.level()
                instanceof ServerLevel serverLevel) {

            TedBattleWorldState worldState =
                    TedBattleWorldState.get(serverLevel);

            worldState.setAllyProgress(
                    TedBattleWorldState
                            .TedAllyProgress
                            .WOUNDED_DURING_BATTLE
            );
        }
    }

    public void setWoundedAfterBattle() {
        this.getNavigation().stop();
        this.setTarget(null);

        this.setFoodLevel(0);

        this.setHealth(
                Math.max(
                        1.0F,
                        this.getMaxHealth()
                                * 0.08F
                )
        );

        this.setDeltaMovement(
                Vec3.ZERO
        );

        this.setAllyState(
                AllyEndermanState.WOUNDED
        );

        this.rescueMessagesStarted = false;
        this.rescueMessageTicks = 0;

        if (this.level()
                instanceof ServerLevel serverLevel) {

            TedBattleWorldState worldState =
                    TedBattleWorldState.get(serverLevel);

            worldState.setAllyProgress(
                    TedBattleWorldState
                            .TedAllyProgress
                            .WOUNDED_AFTER_BATTLE
            );
        }
    }

    private void tickRescueMessages() {
        if (!(this.level()
                instanceof ServerLevel serverLevel)) {
            return;
        }

        if (this.getAllyState()
                != AllyEndermanState.WOUNDED) {
            return;
        }

        if (!rescueMessagesStarted) {
            rescueMessagesStarted = true;
            rescueMessageTicks = 0;
        }

        rescueMessageTicks++;

        if (rescueMessageTicks == 1) {
            TedAllyEndermanMessageHandler
                    .sendHelpMessage(
                            serverLevel,
                            this
                    );
        }

        if (rescueMessageTicks == 50) {
            TedAllyEndermanMessageHandler
                    .sendDetectedMessage(
                            serverLevel,
                            this
                    );
        }

        if (rescueMessageTicks == 100) {
            TedAllyEndermanMessageHandler
                    .sendFeedMessage(
                            serverLevel,
                            this
                    );
        }
    }

    private void tickPostBattleHandOver() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (this.getAllyState()
                != AllyEndermanState.SUPPORT_IDLE) {
            return;
        }

        /*
         * 毎tickプレイヤー検索を行わない。
         * 1秒に1回だけ確認する。
         */
        if (this.tickCount % 20 != 0) {
            return;
        }

        TedBattleWorldState worldState =
                TedBattleWorldState.get(serverLevel);

        if (worldState.getAllyProgress()
                != TedBattleWorldState
                .TedAllyProgress
                .RECOVERED_AFTER_BATTLE) {
            return;
        }

        Player nearestPlayer =
                serverLevel.getNearestPlayer(
                        this,
                        64.0D
                );

        if (nearestPlayer == null
                || !nearestPlayer.isAlive()) {
            return;
        }

        this.startHandOver(nearestPlayer);
    }

    @Override
    public void die(
            DamageSource source
    ) {
        if (!this.level().isClientSide()
                && this.level()
                instanceof ServerLevel serverLevel) {

            TedBattleWorldState worldState =
                    TedBattleWorldState.get(serverLevel);

            /*
             * TED戦闘中に死亡した場合は、
             * 討伐後再出現の対象として保存する。
             */
            if (worldState.isBattleActive()) {
                worldState.setAllyProgress(
                        TedBattleWorldState
                                .TedAllyProgress
                                .DIED_DURING_BATTLE
                );
            }
        }
        this.setInvisible(true);
        this.setInvulnerable(true);

        super.die(source);

        discard();
    }


}