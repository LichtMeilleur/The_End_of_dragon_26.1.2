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
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.entity.item.ItemEntity;
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

    private static final EntityDataAccessor<ItemStack>
            DATA_DISPLAYED_FOOD =
            SynchedEntityData.defineId(
                    TedAllyEndermanEntity.class,
                    EntityDataSerializers.ITEM_STACK
            );



    private static final int MAX_FOOD_LEVEL = 20;
    private static final int RESCUE_REQUIRED_FOOD = 8;

    private UUID handOverTargetUuid = null;
    private boolean handOverItemGiven = false;

    private static final int HAND_OVER_GIVE_TICK = 18;


    private int rescueMessageTicks;
    private boolean rescueMessagesStarted;


    /*
     * このEntity独自NBTの現在バージョン。
     *
     * 保存項目や意味を変更した場合は、
     * 2、3……と増やしていく。
     */
    private static final int CURRENT_ENTITY_DATA_VERSION = 3;

    /*
     * この個体の恒久的な役割。
     *
     * 現在のTED戦用個体はSTORY_ALLY。
     * 将来、村へ生成する個体には
     * VILLAGE_RESIDENTなどを設定する。
     */
    private TedAllyEndermanRole allyRole =
            TedAllyEndermanRole.STORY_ALLY;

    /*
     * 戦闘中に弓矢を渡したか。
     *
     * 弓矢補給を実装した際の二重配布防止用。
     */
    private boolean combatSupplyGiven;


    /*
     * エンダーマンが内部に保管している食料。
     *
     * 現在は1種類だけ保管する。
     * 同じ食料なら最大スタック数までまとめられる。
     */
    private ItemStack storedFood =
            ItemStack.EMPTY;

    /*
     * 食料要求を連続で行わないための
     * クールタイム。
     */
    private int foodRequestCooldown;
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
    private static final RawAnimation HAND_OVER_LOOP =
            RawAnimation.begin()
                    .thenLoop(
                            "animation.model.hand_over"
                    );
    private static final RawAnimation EAT_ANIMATION =
            RawAnimation.begin()
                    .thenPlay(
                            "animation.model.eat"
                    );
    private static final RawAnimation ANGER_ANIMATION =
            RawAnimation.begin()
                    .thenPlay(
                            "animation.model.anger"
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

        /*
         * 即死回避を最優先。
         */
        this.goalSelector.addGoal(
                0,
                new AllySelfEvadeGoal(this)
        );

        /*
         * プレイヤー救出。
         */
        this.goalSelector.addGoal(
                1,
                new AllyRescuePlayerGoal(this)
        );

        /*
         * 自身の回復要求。
         */
        this.goalSelector.addGoal(
                2,
                new AllyRequestFoodGoal(this)
        );

        /*
         * 戦闘開始後の弓矢補給。
         */
        this.goalSelector.addGoal(
                3,
                new AllyCombatSupplyGoal(this)
        );

        /*
         * 攻撃コンボ。
         */
        this.goalSelector.addGoal(
                4,
                new AllyHitAndAwayGoal(this)
        );

        /*
         * 通常追従。
         */
        this.goalSelector.addGoal(
                5,
                new AllyFollowPlayerGoal(this)
        );
    }

    public boolean canRunSupportAi() {
        if (!this.isAlive() || this.isRemoved()) {
            return false;
        }

        return this.getAllyState() == AllyEndermanState.SUPPORT_IDLE;
    }

    public boolean canRunEmergencySupportAi() {
        if (!this.isAlive() || this.isRemoved()) {
            return false;
        }

        return switch (this.getAllyState()) {
            case SUPPORT_IDLE,
                 ATTACK_WARP_PREPARE,
                 WARP_PUNCH,
                 WARP_KICK,
                 WARP_SMASH -> true;

            default -> false;
        };
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

        if (this.foodRequestCooldown > 0) {
            this.foodRequestCooldown--;
        }
    }

    public int getFoodRequestCooldown() {
        return this.foodRequestCooldown;
    }

    public void setFoodRequestCooldown(
            int ticks
    ) {
        this.foodRequestCooldown =
                Math.max(
                        0,
                        ticks
                );
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


    public TedAllyEndermanRole getAllyRole() {
        return this.allyRole;
    }

    public void setAllyRole(
            TedAllyEndermanRole allyRole
    ) {
        this.allyRole =
                allyRole != null
                        ? allyRole
                        : TedAllyEndermanRole.UNKNOWN;
    }

    public boolean isStoryAlly() {
        return this.allyRole
                == TedAllyEndermanRole.STORY_ALLY;
    }

    public boolean isVillageResident() {
        return switch (this.allyRole) {
            case VILLAGE_RESIDENT,
                 VILLAGE_GUARD -> true;

            default -> false;
        };
    }

    public boolean hasGivenCombatSupply() {
        return this.combatSupplyGiven;
    }

    public void setCombatSupplyGiven(
            boolean combatSupplyGiven
    ) {
        this.combatSupplyGiven =
                combatSupplyGiven;
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

    public ItemStack getDisplayedFood() {
        return this.entityData.get(
                DATA_DISPLAYED_FOOD
        );
    }

    private void setDisplayedFood(
            ItemStack stack
    ) {
        ItemStack safeStack =
                stack != null
                        ? stack
                        : ItemStack.EMPTY;

        this.entityData.set(
                DATA_DISPLAYED_FOOD,
                safeStack.copy()
        );
    }

    private void syncDisplayedFood() {
        if (this.getAllyState()
                == AllyEndermanState.EATING
                && !this.storedFood.isEmpty()) {

            this.setDisplayedFood(
                    this.storedFood.copyWithCount(1)
            );

            return;
        }

        this.setDisplayedFood(
                ItemStack.EMPTY
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

        builder.define(
                DATA_DISPLAYED_FOOD,
                ItemStack.EMPTY
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
                        10.0D
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

        tickWaitingHandOver();
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
        if (worldState.isBattleCompleted()
                && worldState.getAllyProgress()
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

    public ItemStack getStoredFood() {
        return this.storedFood;
    }

    public boolean hasStoredFood() {
        return !this.storedFood.isEmpty()
                && this.storedFood.has(
                DataComponents.FOOD
        );
    }

    public boolean tryStoreFood(
            ItemStack sourceStack
    ) {
        if (sourceStack == null
                || sourceStack.isEmpty()) {
            return false;
        }

        if (!sourceStack.has(
                DataComponents.FOOD
        )) {
            return false;
        }

        /*
         * 何も持っていなければ、
         * 1個だけ取り出して保存。
         */
        if (this.storedFood.isEmpty()) {
            this.storedFood =
                    sourceStack.copyWithCount(1);

            sourceStack.shrink(1);

            syncDisplayedFood();

            return true;
        }

        /*
         * 違う種類の食料は、
         * 1スロット方式では保存しない。
         */
        if (!ItemStack.isSameItemSameComponents(
                this.storedFood,
                sourceStack
        )) {
            return false;
        }

        int freeSpace =
                this.storedFood.getMaxStackSize()
                        - this.storedFood.getCount();

        if (freeSpace <= 0) {
            return false;
        }

        int moved =
                Math.min(
                        freeSpace,
                        sourceStack.getCount()
                );

        this.storedFood.grow(
                moved
        );

        sourceStack.shrink(
                moved
        );

        syncDisplayedFood();

        return moved > 0;
    }

    public void showStoredFoodInHand() {
        if (!this.hasStoredFood()) {
            this.setItemInHand(
                    InteractionHand.MAIN_HAND,
                    ItemStack.EMPTY
            );

            return;
        }

        this.setItemInHand(
                InteractionHand.MAIN_HAND,
                this.storedFood.copyWithCount(1)
        );
    }

    public void showInvitationGatewayInHand() {
        this.setItemInHand(
                InteractionHand.MAIN_HAND,
                new ItemStack(
                        ModItems.ENDERMAN_VILLAGE_GATEWAY
                )
        );
    }

    public void clearDisplayedHandItem() {
        this.setItemInHand(
                InteractionHand.MAIN_HAND,
                ItemStack.EMPTY
        );
    }



    public FoodProperties consumeOneStoredFood() {
        if (!this.hasStoredFood()) {
            return null;
        }

        FoodProperties food =
                this.storedFood.get(
                        DataComponents.FOOD
                );

        if (food == null) {
            this.storedFood =
                    ItemStack.EMPTY;

            syncDisplayedFood();

            return null;
        }

        this.storedFood.shrink(1);

        if (this.storedFood.isEmpty()) {
            this.storedFood =
                    ItemStack.EMPTY;
        }

        syncDisplayedFood();

        return food;
    }

    @Override
    protected InteractionResult mobInteract(
            Player player,
            InteractionHand hand
    ) {
        AllyEndermanState state =
                this.getAllyState();

        /*
         * 討伐後、村の門アイテムを手渡す状態。
         *
         * hand_overアニメを続けながら、
         * プレイヤーが右クリックするまで待つ。
         */
        if (state
                == AllyEndermanState.WAITING_HAND_OVER) {

            if (hand != InteractionHand.MAIN_HAND) {
                return InteractionResult.PASS;
            }

            /*
             * 手渡し対象が保存されている場合は、
             * そのプレイヤーだけ受け取れる。
             */
            if (this.handOverTargetUuid != null
                    && !this.handOverTargetUuid.equals(
                    player.getUUID()
            )) {
                return InteractionResult.PASS;
            }

            if (!this.level().isClientSide()) {
                giveInvitationItem(player);
            }

            return InteractionResult.SUCCESS;
        }

        /*
         * 瀕死状態でなければ、
         * 通常のEntity右クリック処理へ渡す。
         */
        if (state
                != AllyEndermanState.WOUNDED) {
            return super.mobInteract(
                    player,
                    hand
            );
        }

        ItemStack stack =
                player.getItemInHand(hand);

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

    private void tickWaitingHandOver() {
        if (this.getAllyState()
                != AllyEndermanState.WAITING_HAND_OVER) {
            return;
        }

        /*
         * チャンク再読み込み後も門表示を復元する。
         */
        ItemStack mainHand =
                this.getItemInHand(
                        InteractionHand.MAIN_HAND
                );

        if (!mainHand.is(
                ModItems.ENDERMAN_VILLAGE_GATEWAY
        )) {
            showInvitationGatewayInHand();
        }

        this.getNavigation().stop();
        this.setTarget(null);

        /*
         * 水平方向だけ止める。
         * Y方向は残し、重力による落下を許可する。
         */
        this.setDeltaMovement(
                0.0D,
                this.getDeltaMovement().y,
                0.0D
        );

        Player target =
                getHandOverTargetPlayer();

        /*
         * 対象プレイヤーが見つからない場合は、
         * 近くのプレイヤーを見る。
         *
         * アイテムを受け取れる人物の制限とは別。
         */
        if (target == null) {
            target =
                    this.level()
                            .getNearestPlayer(
                                    this,
                                    12.0D
                            );
        }

        if (target != null
                && target.isAlive()) {
            this.getLookControl()
                    .setLookAt(
                            target,
                            30.0F,
                            30.0F
                    );
        }
    }


    private ServerPlayer getHandOverTargetPlayer() {
        if (!(this.level()
                instanceof ServerLevel serverLevel)) {
            return null;
        }

        if (this.handOverTargetUuid == null) {
            return null;
        }

        return serverLevel.getServer()
                .getPlayerList()
                .getPlayer(
                        this.handOverTargetUuid
                );
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

        syncDisplayedFood();
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

                                case WAITING_HAND_OVER -> {
                                    animationTest.controller()
                                            .setAnimation(
                                                    HAND_OVER_LOOP
                                            );

                                    return PlayState.CONTINUE;
                                }

                                case REQUESTING_FOOD -> {
                                    /*
                                     * 食料要求中は専用モーションがないため、
                                     * idleを使用する。
                                     */
                                    animationTest.controller()
                                            .setAnimation(
                                                    IDLE_ANIMATION
                                            );

                                    return PlayState.CONTINUE;
                                }

                                case EATING -> {
                                    animationTest.controller()
                                            .setAnimation(
                                                    EAT_ANIMATION
                                            );

                                    return PlayState.CONTINUE;
                                }

                                case COMBAT_SUPPLY_HAND_OVER -> {
                                    animationTest.controller()
                                            .setAnimation(
                                                    HAND_OVER_ANIMATION
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

    //エンダーマンセーブデータ
    @Override
    protected void addAdditionalSaveData(
            net.minecraft.world.level.storage.ValueOutput output
    ) {
        super.addAdditionalSaveData(output);

        /*
         * Entity独自NBTのバージョン。
         */
        output.putInt(
                "TedEntityDataVersion",
                CURRENT_ENTITY_DATA_VERSION
        );

        /*
         * 恒久的な個体種別。
         */
        output.putString(
                "AllyRole",
                this.getAllyRole().name()
        );

        /*
         * 現在の行動状態。
         */
        output.putString(
                "AllyState",
                this.getAllyState().name()
        );

        output.putInt(
                "FoodLevel",
                this.getFoodLevel()
        );

        if (!this.storedFood.isEmpty()) {
            output.store(
                    "StoredFood",
                    ItemStack.CODEC,
                    this.storedFood
            );
        }

        output.putBoolean(
                "CombatSupplyGiven",
                this.combatSupplyGiven
        );

        /*
         * 戦闘支援対象。
         */
        if (this.supportPlayerUuid != null) {
            output.putString(
                    "SupportPlayerUuid",
                    this.supportPlayerUuid.toString()
            );
        }

        /*
         * 戦闘対象の龍。
         */
        if (this.combatDragonUuid != null) {
            output.putString(
                    "CombatDragonUuid",
                    this.combatDragonUuid.toString()
            );
        }

        /*
         * 手渡し処理中のプレイヤー。
         */
        if (this.handOverTargetUuid != null) {
            output.putString(
                    "HandOverTargetUuid",
                    this.handOverTargetUuid.toString()
            );
        }

        output.putBoolean(
                "HandOverItemGiven",
                this.handOverItemGiven
        );

        /*
         * リロード直後に連続行動しないよう、
         * 各クールタイムも保存する。
         */
        output.putInt(
                "SupportAttackCooldown",
                Math.max(
                        0,
                        this.supportAttackCooldown
                )
        );

        output.putInt(
                "SelfEvadeCooldown",
                Math.max(
                        0,
                        this.selfEvadeCooldown
                )
        );

        output.putInt(
                "PlayerRescueCooldown",
                Math.max(
                        0,
                        this.playerRescueCooldown
                )
        );

        output.putInt(
                "FollowWarpCooldown",
                Math.max(
                        0,
                        this.followWarpCooldown
                )
        );

        output.putInt(
                "FoodRequestCooldown",
                Math.max(
                        0,
                        this.foodRequestCooldown
                )
        );
    }

    @Override
    protected void readAdditionalSaveData(
            net.minecraft.world.level.storage.ValueInput input
    ) {
        super.readAdditionalSaveData(input);

        int savedDataVersion =
                input.getIntOr(
                        "TedEntityDataVersion",
                        0
                );

        /*
         * バージョン0の既存EntityにはAllyRoleがない。
         *
         * 現在まで存在していた専用エンダーマンは、
         * すべてTED戦用個体なのでSTORY_ALLYとして移行する。
         */
        TedAllyEndermanRole loadedRole =
                readAllyRole(
                        input,
                        savedDataVersion
                );

        this.setAllyRole(
                loadedRole
        );

        AllyEndermanState loadedState =
                readAllyState(
                        input
                );

        /*
         * 攻撃途中や手渡し途中の状態を、
         * チャンク再読み込み後に途中から再開すると危険。
         *
         * 永続化に適した安全な状態へ変換する。
         */
        this.setAllyState(
                sanitizeLoadedState(
                        loadedState
                )
        );

        this.setFoodLevel(
                input.getIntOr(
                        "FoodLevel",
                        0
                )
        );

        this.storedFood =
                input.read(
                        "StoredFood",
                        ItemStack.CODEC
                ).orElse(
                        ItemStack.EMPTY
                );

        if (!this.storedFood.isEmpty()
                && !this.storedFood.has(
                DataComponents.FOOD
        )) {
            this.storedFood =
                    ItemStack.EMPTY;
        }

        this.combatSupplyGiven =
                input.getBooleanOr(
                        "CombatSupplyGiven",
                        false
                );

        this.supportPlayerUuid =
                readUuid(
                        input,
                        "SupportPlayerUuid"
                );

        this.combatDragonUuid =
                readUuid(
                        input,
                        "CombatDragonUuid"
                );

        this.handOverTargetUuid =
                readUuid(
                        input,
                        "HandOverTargetUuid"
                );

        this.handOverItemGiven =
                input.getBooleanOr(
                        "HandOverItemGiven",
                        false
                );

        this.supportAttackCooldown =
                Math.max(
                        0,
                        input.getIntOr(
                                "SupportAttackCooldown",
                                0
                        )
                );

        this.selfEvadeCooldown =
                Math.max(
                        0,
                        input.getIntOr(
                                "SelfEvadeCooldown",
                                0
                        )
                );

        this.playerRescueCooldown =
                Math.max(
                        0,
                        input.getIntOr(
                                "PlayerRescueCooldown",
                                0
                        )
                );

        this.followWarpCooldown =
                Math.max(
                        0,
                        input.getIntOr(
                                "FollowWarpCooldown",
                                0
                        )
                );

        this.foodRequestCooldown =
                Math.max(
                        0,
                        input.getIntOr(
                                "FoodRequestCooldown",
                                0
                        )
                );

        /*
         * 読み込み時点を状態開始時刻として扱う。
         *
         * 保存前のtickCountとの差を引き継がない。
         */
        this.stateStartTick =
                this.tickCount;

        migrateEntityDataIfNeeded(
                savedDataVersion
        );

        /*
         * 永続化された手渡し待機状態なら、
         * クライアント描画用の門アイテムも復元する。
         */
        if (this.getAllyState()
                == AllyEndermanState.HAND_OVER
                || this.getAllyState()
                == AllyEndermanState.WAITING_HAND_OVER) {

            if (this.level()
                    instanceof ServerLevel serverLevel) {

                TedBattleWorldState worldState =
                        TedBattleWorldState.get(
                                serverLevel
                        );

                if (worldState.isBattleCompleted()
                        && worldState
                        .hasInvitationGatewayToGive()) {

                    showInvitationGatewayInHand();

                } else {
                    clearDisplayedHandItem();
                }
            }
        }
    }

    private TedAllyEndermanRole readAllyRole(
            net.minecraft.world.level.storage.ValueInput input,
            int savedDataVersion
    ) {
        /*
         * バージョン0にはAllyRoleが存在しない。
         *
         * 既存個体はすべてイベント用だったため、
         * STORY_ALLYとして扱う。
         */
        if (savedDataVersion < 1) {
            return TedAllyEndermanRole.STORY_ALLY;
        }

        String savedRole =
                input.getStringOr(
                        "AllyRole",
                        TedAllyEndermanRole.UNKNOWN.name()
                );

        try {
            return TedAllyEndermanRole.valueOf(
                    savedRole
            );
        } catch (IllegalArgumentException exception) {
            return TedAllyEndermanRole.UNKNOWN;
        }
    }

    private AllyEndermanState readAllyState(
            net.minecraft.world.level.storage.ValueInput input
    ) {
        String savedState =
                input.getStringOr(
                        "AllyState",
                        AllyEndermanState.WOUNDED.name()
                );

        try {
            return AllyEndermanState.valueOf(
                    savedState
            );
        } catch (IllegalArgumentException exception) {
            return AllyEndermanState.WOUNDED;
        }
    }

    private UUID readUuid(
            net.minecraft.world.level.storage.ValueInput input,
            String key
    ) {
        String savedUuid =
                input.getStringOr(
                        key,
                        ""
                );

        if (savedUuid.isBlank()) {
            return null;
        }

        try {
            return UUID.fromString(
                    savedUuid
            );
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private AllyEndermanState sanitizeLoadedState(
            AllyEndermanState loadedState
    ) {
        if (loadedState == null) {
            return AllyEndermanState.WOUNDED;
        }

        return switch (loadedState) {

            /*
             * そのまま維持してよい恒久状態。
             */
            case WOUNDED,
                 DEFEATED,
                 SUPPORT_IDLE,
                 WAITING_HAND_OVER -> loadedState;

            /*
             * 回復途中はそのまま再開。
             */
            case RECOVERING ->
                    AllyEndermanState.RECOVERING;

            /*
             * 討伐後の門手渡し開始中だった場合は、
             * 右クリック待機へ進める。
             */
            case HAND_OVER ->
                    AllyEndermanState.WAITING_HAND_OVER;

            /*
             * 食事や戦闘補給の途中状態は、
             * Goal内部の進行値が保存されないため
             * 安全な待機状態へ戻す。
             */
            case REQUESTING_FOOD,
                 EATING,
                 COMBAT_SUPPLY_HAND_OVER,
                 ATTACK_WARP_PREPARE,
                 WARP_PUNCH,
                 WARP_KICK,
                 WARP_SMASH,
                 WITH_PLAYER_WARP,
                 VICTORY ->
                    AllyEndermanState.SUPPORT_IDLE;

            default ->
                    AllyEndermanState.SUPPORT_IDLE;
        };
    }



    private void migrateEntityDataIfNeeded(
            int savedDataVersion
    ) {
        if (savedDataVersion < 1) {
            this.allyRole =
                    TedAllyEndermanRole.STORY_ALLY;

            this.combatSupplyGiven = false;
        }

        /*
         * バージョン2:
         * 食料要求クールタイム追加。
         */
        if (savedDataVersion < 2) {
            this.foodRequestCooldown = 0;
        }

        /*
         * バージョン3:
         * 食料ストック追加。
         */
        if (savedDataVersion < 3) {
            this.storedFood =
                    ItemStack.EMPTY;
        }
    }




    public void startHandOver(
            Player player
    ) {
        if (player == null
                || !player.isAlive()) {
            return;
        }

        if (!(this.level()
                instanceof ServerLevel serverLevel)) {
            return;
        }

        TedBattleWorldState worldState =
                TedBattleWorldState.get(
                        serverLevel
                );

        /*
         * 討伐後かつ未配布の門がある場合だけ、
         * 手渡しを開始する。
         */
        if (!worldState.isBattleCompleted()
                || !worldState
                .hasInvitationGatewayToGive()) {
            return;
        }

        this.handOverTargetUuid =
                player.getUUID();

        this.handOverItemGiven = false;

        this.getNavigation().stop();

        this.setDeltaMovement(
                0.0D,
                this.getDeltaMovement().y,
                0.0D
        );

        /*
         * hand_over開始時から門を右手に表示。
         */
        showInvitationGatewayInHand();

        this.setAllyState(
                AllyEndermanState.HAND_OVER
        );
    }

    private void tickHandOver() {
        if (!(this.level()
                instanceof ServerLevel level)) {
            return;
        }

        Player player = null;

        if (this.handOverTargetUuid != null) {
            player =
                    level.getPlayerByUUID(
                            this.handOverTargetUuid
                    );
        }

        if (player != null
                && player.isAlive()) {
            this.getLookControl()
                    .setLookAt(
                            player,
                            30.0F,
                            30.0F
                    );
        }

        int age =
                this.getStateAgeTicks();

        /*
         * 最初のhand_overモーションを一定時間見せた後、
         * 右クリック待機状態へ移行する。
         */
        if (age < HAND_OVER_GIVE_TICK) {
            return;
        }

        beginInvitationHandOver(
                player
        );
    }

    private void beginInvitationHandOver(
            Player player
    ) {
        if (player != null
                && player.isAlive()) {
            this.handOverTargetUuid =
                    player.getUUID();
        }

        this.handOverItemGiven = false;

        this.getNavigation().stop();
        this.setTarget(null);

        this.setDeltaMovement(
                0.0D,
                this.getDeltaMovement().y,
                0.0D
        );

        /*
         * 右クリック待機中も門を表示し続ける。
         */
        showInvitationGatewayInHand();

        this.setAllyState(
                AllyEndermanState.WAITING_HAND_OVER
        );
    }

    private void giveInvitationItem(
            Player player
    ) {
        if (!(this.level()
                instanceof ServerLevel serverLevel)) {
            return;
        }

        TedBattleWorldState worldState =
                TedBattleWorldState.get(
                        serverLevel
                );

        /*
         * 討伐後だけ配布可能。
         */
        if (!worldState.isBattleCompleted()) {
            return;
        }

        /*
         * 未配布の門が残っていなければ、
         * Entity側の待機状態だけ終了させる。
         */
        if (!worldState
                .hasInvitationGatewayToGive()) {

            worldState.setAllyProgress(
                    TedBattleWorldState
                            .TedAllyProgress
                            .ITEM_GIVEN
            );

            finishInvitationHandOver();
            return;
        }

        if (this.getAllyState()
                != AllyEndermanState.WAITING_HAND_OVER) {
            return;
        }

        ItemStack gateway =
                new ItemStack(
                        ModItems.ENDERMAN_VILLAGE_GATEWAY
                );

        /*
         * 先にプレイヤーへ渡す。
         */
        if (!player.getInventory()
                .add(gateway)) {

            ItemEntity dropped =
                    player.drop(
                            gateway,
                            false
                    );

            /*
             * インベントリ追加にもドロップにも
             * 失敗した場合は個数を減らさない。
             */
            if (dropped == null) {
                return;
            }
        }

        /*
         * 実際に配布できた後でだけ残数を減らす。
         */
        if (!worldState
                .consumeInvitationGateway()) {
            return;
        }

        worldState.setAllyProgress(
                TedBattleWorldState
                        .TedAllyProgress
                        .ITEM_GIVEN
        );

        finishInvitationHandOver();
    }

    private void finishInvitationHandOver() {
        this.handOverItemGiven = true;
        this.handOverTargetUuid = null;

        /*
         * 配布完了後は右手表示を消す。
         */
        clearDisplayedHandItem();

        this.setAllyState(
                AllyEndermanState.SUPPORT_IDLE
        );

        this.getNavigation().stop();
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
        if (!(this.level()
                instanceof ServerLevel serverLevel)) {
            return;
        }

        if (this.getAllyState()
                != AllyEndermanState.SUPPORT_IDLE) {
            return;
        }

        /*
         * 1秒に1回だけ確認する。
         */
        if (this.tickCount % 20 != 0) {
            return;
        }

        TedBattleWorldState worldState =
                TedBattleWorldState.get(
                        serverLevel
                );

        /*
         * TED討伐後でなければ、
         * 門手渡し処理は開始しない。
         */
        if (!worldState.isBattleCompleted()) {
            return;
        }

        /*
         * 渡す門が残っていなければ、
         * 進行状態を配布済みに揃える。
         */
        if (!worldState
                .hasInvitationGatewayToGive()) {

            if (worldState.getAllyProgress()
                    != TedBattleWorldState
                    .TedAllyProgress
                    .ITEM_GIVEN) {

                worldState.setAllyProgress(
                        TedBattleWorldState
                                .TedAllyProgress
                                .ITEM_GIVEN
                );
            }

            return;
        }

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

        this.startHandOver(
                nearestPlayer
        );
    }

    @Override
    public void die(
            DamageSource source
    ) {
        if (!this.level().isClientSide()
                && this.level()
                instanceof ServerLevel serverLevel
                && this.isStoryAlly()) {

            TedBattleWorldState worldState =
                    TedBattleWorldState.get(
                            serverLevel
                    );

            if (worldState.isBattleActive()) {

                /*
                 * TED戦中に死亡。
                 *
                 * TED討伐時に瀕死状態で再生成する。
                 */
                worldState.setAllyProgress(
                        TedBattleWorldState
                                .TedAllyProgress
                                .DIED_DURING_BATTLE
                );

            } else if (worldState.isBattleCompleted()
                    && worldState
                    .hasInvitationGatewayToGive()) {

                /*
                 * TED討伐後、門を渡す前に死亡。
                 *
                 * Entityはこれから消えるため、
                 * 「すでに瀕死で存在する」状態ではなく
                 * 再生成待ちとして保存する。
                 */
                worldState.setAllyProgress(
                        TedBattleWorldState
                                .TedAllyProgress
                                .RESPAWN_AFTER_BATTLE_PENDING
                );
            }
        }

        this.setInvisible(true);
        this.setInvulnerable(true);

        super.die(
                source
        );

        this.discard();
    }


}