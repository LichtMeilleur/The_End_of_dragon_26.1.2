package com.licht_meilleur.the_end_of_dragon.entity.enderman.goal;

import com.licht_meilleur.the_end_of_dragon.entity.enderman.AllyEndermanState;
import com.licht_meilleur.the_end_of_dragon.entity.enderman.TedAllyEndermanEntity;
import com.licht_meilleur.the_end_of_dragon.world.TedAllyEndermanMessageHandler;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class AllyRequestFoodGoal extends Goal {

    /*
     * HPが45％以下になったら食料を要求。
     */
    private static final float REQUEST_HEALTH_RATIO =
            0.45F;

    /*
     * 食料を探す範囲。
     */
    private static final double FOOD_SEARCH_RANGE =
            10.0D;

    /*
     * 投げられた食料に触れたと見なす距離。
     */
    private static final double PICKUP_RANGE_SQR =
            1.8D * 1.8D;

    /*
     * 要求メッセージの再送間隔。
     * 20tick = 1秒。
     */
    private static final int MESSAGE_INTERVAL =
            20 * 15;

    /*
     * 食べるアニメーション内の回復tick。
     */
    private static final int EAT_APPLY_TICK =
            12;

    /*
     * 食事終了tick。
     */
    private static final int EAT_END_TICK =
            28;

    private final TedAllyEndermanEntity ally;

    private ItemEntity targetFood;

    private Phase phase =
            Phase.REQUESTING;

    private int phaseTicks;
    private int messageCooldown;

    private int pendingNutrition;
    private boolean foodApplied;

    private Player requestPlayer;

    private int foodWarpCooldown;

    public AllyRequestFoodGoal(
            TedAllyEndermanEntity ally
    ) {
        this.ally = ally;

        this.setFlags(
                EnumSet.of(
                        Flag.MOVE,
                        Flag.LOOK
                )
        );
    }

    @Override
    public boolean canUse() {
        if (!(this.ally.level()
                instanceof ServerLevel level)) {
            return false;
        }

        if (!this.ally.canRunSupportAi()) {
            return false;
        }

        if (!this.ally.isStoryAlly()) {
            return false;
        }

        float healthRatio =
                this.ally.getHealth()
                        / this.ally.getMaxHealth();

        if (healthRatio
                > REQUEST_HEALTH_RATIO) {
            return false;
        }

        if (this.ally.getFoodLevel()
                >= 18) {
            return false;
        }

        this.requestPlayer =
                AllyEndermanAiUtil.findPlayer(
                        this.ally,
                        level
                );

        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (!this.ally.isAlive()
                || this.ally.isRemoved()) {
            return false;
        }

        /*
         * 食事処理が完了したら、
         * 必ずGoalを終了させる。
         *
         * これがないとMOVE / LOOKフラグを
         * 食料Goalが保持し続け、
         * 追従Goalへ戻れない。
         */
        if (this.phase
                == Phase.FINISHED) {
            return false;
        }

        /*
         * 食事アニメーション中。
         */
        if (this.phase
                == Phase.EATING) {
            return this.phaseTicks
                    < EAT_END_TICK;
        }

        /*
         * 食料要求中。
         */
        float healthRatio =
                this.ally.getHealth()
                        / this.ally.getMaxHealth();

        return healthRatio
                <= REQUEST_HEALTH_RATIO
                && this.ally.getFoodLevel() < 18;
    }

    @Override
    public void start() {
        this.phase =
                Phase.REQUESTING;

        this.phaseTicks = 0;
        this.messageCooldown = 0;

        this.pendingNutrition = 0;
        this.foodApplied = false;
        this.targetFood = null;

        this.foodWarpCooldown = 0;

        this.ally.getNavigation().stop();


        if (this.ally.hasStoredFood()) {
            beginEating();
            return;
        }

        this.phase =
                Phase.REQUESTING;

        this.ally.setAllyState(
                AllyEndermanState.REQUESTING_FOOD
        );

        /*
         * プレイヤーから離れている場合は、
         * 食料を要求する前に近くへワープする。
         */
        if (this.ally.level()
                instanceof ServerLevel level
                && this.requestPlayer != null
                && this.requestPlayer.isAlive()
                && this.ally.distanceToSqr(
                this.requestPlayer
        ) > 8.0D * 8.0D) {

            Vec3 destination =
                    AllyEndermanAiUtil
                            .findSafePositionAround(
                                    this.ally,
                                    level,
                                    this.requestPlayer.position(),
                                    3.0D,
                                    6.0D
                            );

            AllyEndermanAiUtil.teleportAlly(
                    this.ally,
                    level,
                    destination
            );
        }

        this.ally.setAllyState(
                AllyEndermanState.REQUESTING_FOOD
        );
    }

    @Override
    public void tick() {
        if (!(this.ally.level()
                instanceof ServerLevel level)) {
            return;
        }

        this.phaseTicks++;

        switch (this.phase) {
            case REQUESTING ->
                    tickRequesting(level);

            case EATING ->
                    tickEating(level);

            case FINISHED -> {
            }
        }
    }

    private void tickRequesting(
            ServerLevel level
    ) {

        if (this.foodWarpCooldown > 0) {
            this.foodWarpCooldown--;
        }
        if (this.messageCooldown <= 0) {
            TedAllyEndermanMessageHandler
                    .sendFoodRequestMessage(
                            level,
                            this.ally
                    );

            this.messageCooldown =
                    MESSAGE_INTERVAL;
        } else {
            this.messageCooldown--;
        }

        /*
         * 食料がまだ見つからない間は、
         * 要求対象のプレイヤーを見る。
         */
        if (this.requestPlayer != null
                && this.requestPlayer.isAlive()) {

            this.ally.getLookControl()
                    .setLookAt(
                            this.requestPlayer,
                            30.0F,
                            30.0F
                    );
        }

        if (!isValidFoodEntity(
                this.targetFood
        )) {
            this.targetFood =
                    findNearestFood(
                            level
                    );
        }

        if (this.targetFood == null) {
            this.ally.getNavigation().stop();
            return;
        }

        this.ally.getLookControl()
                .setLookAt(
                        this.targetFood,
                        30.0F,
                        30.0F
                );

        double distanceSqr =
                this.ally.distanceToSqr(
                        this.targetFood
                );

        if (distanceSqr
                > PICKUP_RANGE_SQR) {

            Vec3 foodPosition =
                    this.targetFood.position();

            Vec3 destination =
                    AllyEndermanAiUtil
                            .findSafePositionAround(
                                    this.ally,
                                    level,
                                    foodPosition,
                                    0.5D,
                                    1.5D
                            );

            boolean teleported =
                    AllyEndermanAiUtil.teleportAlly(
                            this.ally,
                            level,
                            destination
                    );

            /*
             * ワープ位置を確保できなかった場合のみ、
             * 通常移動で近づく。
             */
            if (!teleported) {
                this.ally.getNavigation()
                        .moveTo(
                                this.targetFood,
                                1.15D
                        );

                return;
            }

            /*
             * ワープ直後に距離を再確認する。
             */
            distanceSqr =
                    this.ally.distanceToSqr(
                            this.targetFood
                    );

            if (distanceSqr
                    > PICKUP_RANGE_SQR) {
                return;
            }
        }


        consumeFoodEntity(
                level,
                this.targetFood
        );
    }

    private void consumeFoodEntity(
            ServerLevel level,
            ItemEntity foodEntity
    ) {
        if (!isValidFoodEntity(
                foodEntity
        )) {
            this.targetFood = null;
            return;
        }

        ItemStack stack =
                foodEntity.getItem();

        FoodProperties food =
                stack.get(
                        DataComponents.FOOD
                );

        if (food == null) {
            this.targetFood = null;
            return;
        }

        this.pendingNutrition =
                Math.max(
                        1,
                        food.nutrition()
                );

        if (!this.ally.tryStoreFood(stack)) {
            return;
        }

        if (stack.isEmpty()) {
            foodEntity.discard();
        } else {
            foodEntity.setItem(
                    stack
            );
        }

        beginEating();

        if (stack.isEmpty()) {
            foodEntity.discard();
        } else {
            foodEntity.setItem(
                    stack
            );
        }

        this.targetFood = null;

        this.phase =
                Phase.EATING;

        this.phaseTicks = 0;
        this.foodApplied = false;

        this.ally.getNavigation().stop();

        this.ally.setDeltaMovement(
                0.0D,
                this.ally.getDeltaMovement().y,
                0.0D
        );

        this.ally.setAllyState(
                AllyEndermanState.EATING
        );

        TedAllyEndermanMessageHandler
                .sendFoodReceivedMessage(
                        level,
                        this.ally
                );
    }

    private void tickEating(
            ServerLevel level
    ) {
        this.ally.getNavigation().stop();

        this.ally.setDeltaMovement(
                0.0D,
                this.ally.getDeltaMovement().y,
                0.0D
        );

        if (!this.foodApplied
                && this.phaseTicks
                >= EAT_APPLY_TICK) {

            this.foodApplied = true;

            applyFoodRecovery();
        }

        if (this.phaseTicks
                < EAT_END_TICK) {
            return;
        }

        /*
         * 次のcanContinueToUse()でfalseになり、
         * stop()からSUPPORT_IDLEへ戻る。
         */
        this.phase =
                Phase.FINISHED;
    }

    private void applyFoodRecovery() {
        FoodProperties consumedFood =
                this.ally.consumeOneStoredFood();

        /*
         * 消費したので手の表示を消す。
         */
        this.ally.clearDisplayedHandItem();

        if (consumedFood == null) {
            this.pendingNutrition = 0;
            return;
        }

        int nutrition =
                Math.max(
                        1,
                        consumedFood.nutrition()
                );

        this.ally.setFoodLevel(
                this.ally.getFoodLevel()
                        + nutrition
        );

        this.ally.heal(
                Math.max(
                        2.0F,
                        nutrition
                )
        );

        /*
         * 食事後は最低35％まで回復。
         */
        float minimumHealth =
                this.ally.getMaxHealth()
                        * 0.35F;

        if (this.ally.getHealth()
                < minimumHealth) {
            this.ally.setHealth(
                    minimumHealth
            );
        }

        this.pendingNutrition = 0;
    }

    private ItemEntity findNearestFood(
            ServerLevel level
    ) {
        AABB searchArea =
                this.ally.getBoundingBox()
                        .inflate(
                                FOOD_SEARCH_RANGE
                        );

        List<ItemEntity> foods =
                level.getEntitiesOfClass(
                        ItemEntity.class,
                        searchArea,
                        this::isValidFoodEntity
                );

        return foods.stream()
                .min(
                        Comparator.comparingDouble(
                                this.ally::distanceToSqr
                        )
                )
                .orElse(null);
    }

    private boolean isValidFoodEntity(
            ItemEntity itemEntity
    ) {
        if (itemEntity == null
                || !itemEntity.isAlive()
                || itemEntity.isRemoved()) {
            return false;
        }

        ItemStack stack =
                itemEntity.getItem();

        return !stack.isEmpty()
                && stack.has(
                DataComponents.FOOD
        );
    }

    @Override
    public void stop() {
        this.ally.getNavigation().stop();

        /*
         * Goal中断時にも食料表示を残さない。
         */
        this.ally.clearDisplayedHandItem();

        if (this.ally.isAlive()
                && !this.ally.isRemoved()) {
            this.ally.setAllyState(
                    AllyEndermanState.SUPPORT_IDLE
            );
        }

        this.targetFood = null;
        this.requestPlayer = null;

        this.phase =
                Phase.REQUESTING;

        this.phaseTicks = 0;
        this.pendingNutrition = 0;
        this.foodApplied = false;
    }

    private enum Phase {
        REQUESTING,
        EATING,
        FINISHED
    }

    private void beginEating() {
        if (!this.ally.hasStoredFood()) {
            this.phase =
                    Phase.FINISHED;

            return;
        }

        ItemStack storedFood =
                this.ally.getStoredFood();

        FoodProperties food =
                storedFood.get(
                        DataComponents.FOOD
                );

        if (food == null) {
            this.phase =
                    Phase.FINISHED;

            return;
        }

        this.pendingNutrition =
                Math.max(
                        1,
                        food.nutrition()
                );

        this.phase =
                Phase.EATING;

        this.phaseTicks = 0;
        this.foodApplied = false;

        this.ally.getNavigation().stop();

        this.ally.setDeltaMovement(
                0.0D,
                this.ally.getDeltaMovement().y,
                0.0D
        );

        /*
         * right_hand_locatorに描画する食料を
         * Entityのメインハンドへ設定。
         */
        this.ally.showStoredFoodInHand();

        this.ally.setAllyState(
                AllyEndermanState.EATING
        );
    }
}