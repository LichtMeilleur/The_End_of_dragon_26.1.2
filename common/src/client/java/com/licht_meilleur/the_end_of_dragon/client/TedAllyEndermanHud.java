package com.licht_meilleur.the_end_of_dragon.client;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.entity.enderman.AllyEndermanState;
import com.licht_meilleur.the_end_of_dragon.entity.enderman.TedAllyEndermanEntity;
import com.licht_meilleur.the_end_of_dragon.registry.ModSounds;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

public final class TedAllyEndermanHud {

    /*
     * バニラHUDスプライト
     */
    private static final Identifier HEART_CONTAINER =
            Identifier.withDefaultNamespace("hud/heart/container");

    private static final Identifier HEART_FULL =
            Identifier.withDefaultNamespace("hud/heart/full");

    private static final Identifier HEART_HALF =
            Identifier.withDefaultNamespace("hud/heart/half");

    private static final Identifier FOOD_EMPTY =
            Identifier.withDefaultNamespace("hud/food_empty");

    private static final Identifier FOOD_FULL =
            Identifier.withDefaultNamespace("hud/food_full");

    private static final Identifier FOOD_HALF =
            Identifier.withDefaultNamespace("hud/food_half");

    /*
     * MOD独自矢印
     */
    private static final Identifier ARROW_UP =
            TheEndOfDragon.id("textures/gui/arrow_up.png");

    private static final Identifier ARROW_DOWN =
            TheEndOfDragon.id("textures/gui/arrow_down.png");

    private static final Identifier ARROW_LEFT =
            TheEndOfDragon.id("textures/gui/arrow_left.png");

    private static final Identifier ARROW_RIGHT =
            TheEndOfDragon.id("textures/gui/arrow_right.png");

    private static final double SEARCH_RANGE = 512.0D;
    private static final double STATUS_RENDER_RANGE = 64.0D;

    private static final int ICON_SIZE = 9;
    private static final int ICON_SPACING = 8;
    private static final int ICON_COUNT = 10;

    private static final int ARROW_SIZE = 24;
    private static final int SCREEN_MARGIN = 28;

    /*
     * ワールド座標を画面座標へ変換する際の基準FOV。
     * まずは固定値で動作確認します。
     */
    private static final double HUD_FOV_DEGREES = 70.0D;


    private static int arrowDisplayTicks = 0;

    private static int arrowCooldownTicks = 0;

    private TedAllyEndermanHud() {
    }

    public static void clientTick() {

        if (arrowDisplayTicks > 0) {
            arrowDisplayTicks--;
        }

        if (arrowCooldownTicks > 0) {
            arrowCooldownTicks--;
        }
    }

    public static void render(
            GuiGraphicsExtractor graphics,
            DeltaTracker deltaTracker
    ) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.options.hideGui) {
            return;
        }

        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;

        if (player == null || level == null) {
            return;
        }

        TedAllyEndermanEntity ally =
                findNearestWoundedEnderman(
                        player,
                        level
                );

        if (ally == null) {
            return;
        }

        float partialTick =
                deltaTracker.getGameTimeDeltaPartialTick(false);

        ScreenProjection projection =
                projectToScreen(
                        minecraft,
                        ally,
                        partialTick,
                        graphics.guiWidth(),
                        graphics.guiHeight()
                );

        double distance =
                player.distanceTo(ally);

        /*
         * 画面内かつカメラ前方ならステータス表示。
         */
        if (projection.inFront()
                && projection.onScreen()) {

            if (distance <= STATUS_RENDER_RANGE) {
                renderStatus(
                        graphics,
                        ally,
                        projection.x(),
                        projection.y()
                );
            }

            return;
        }

        /*
         * 画面外または後方なら誘導矢印。
         */

        if (!shouldDisplayArrow()) {
            return;
        }
        renderDirectionArrow(
                graphics,
                projection,
                distance
        );
    }

    private static TedAllyEndermanEntity findNearestWoundedEnderman(
            LocalPlayer player,
            ClientLevel level
    ) {
        AABB searchBox =
                player.getBoundingBox()
                        .inflate(SEARCH_RANGE);

        List<TedAllyEndermanEntity> allies =
                level.getEntitiesOfClass(
                        TedAllyEndermanEntity.class,
                        searchBox,
                        ally ->
                                ally.isAlive()
                                        && ally.getAllyState()
                                        == AllyEndermanState.WOUNDED
                );

        return allies.stream()
                .min(
                        Comparator.comparingDouble(
                                player::distanceToSqr
                        )
                )
                .orElse(null);
    }

    private static ScreenProjection projectToScreen(
            Minecraft minecraft,
            TedAllyEndermanEntity ally,
            float partialTick,
            int screenWidth,
            int screenHeight
    ) {
        Camera camera =
                minecraft.gameRenderer.getMainCamera();

        /*
         * 1.21.8ではgetPosition()ではなくposition()。
         */
        Vec3 cameraPosition =
                camera.position();

        double targetX =
                Mth.lerp(
                        partialTick,
                        ally.xOld,
                        ally.getX()
                );

        double targetY =
                Mth.lerp(
                        partialTick,
                        ally.yOld,
                        ally.getY()
                )
                        + ally.getBbHeight()
                        + 0.65D;

        double targetZ =
                Mth.lerp(
                        partialTick,
                        ally.zOld,
                        ally.getZ()
                );

        Vec3 targetPosition =
                new Vec3(
                        targetX,
                        targetY,
                        targetZ
                );

        Vec3 relative =
                targetPosition.subtract(
                        cameraPosition
                );

        /*
         * 1.21.8ではgetYRot()/getXRot()ではなく、
         * yRot()/xRot()。
         */
        float yaw =
                camera.yRot();

        float pitch =
                camera.xRot();

        Vec3 forward =
                Vec3.directionFromRotation(
                        pitch,
                        yaw
                ).normalize();

        Vec3 right =
                Vec3.directionFromRotation(
                        0.0F,
                        yaw + 90.0F
                ).normalize();

        /*
         * crossの順番で上下が反転した場合は、
         * forward.cross(right)へ変更してください。
         */
        Vec3 up =
                right.cross(forward)
                        .normalize();

        double cameraX =
                relative.dot(right);

        double cameraY =
                relative.dot(up);

        double cameraZ =
                relative.dot(forward);

        boolean inFront =
                cameraZ > 0.05D;

        /*
         * 後方の場合も画面端の方向計算ができるように補正。
         */
        if (!inFront) {
            cameraX = -cameraX;
            cameraY = -cameraY;
            cameraZ = Math.abs(cameraZ);
        }

        double fovRadians =
                Math.toRadians(
                        HUD_FOV_DEGREES
                );

        double focalLength =
                screenHeight * 0.5D
                        / Math.tan(
                        fovRadians * 0.5D
                );

        double safeCameraZ =
                Math.max(
                        cameraZ,
                        0.05D
                );

        double screenX =
                screenWidth * 0.5D
                        + cameraX
                        / safeCameraZ
                        * focalLength;

        double screenY =
                screenHeight * 0.5D
                        - cameraY
                        / safeCameraZ
                        * focalLength;

        boolean onScreen =
                screenX >= SCREEN_MARGIN
                        && screenX <= screenWidth - SCREEN_MARGIN
                        && screenY >= SCREEN_MARGIN
                        && screenY <= screenHeight - SCREEN_MARGIN;

        return new ScreenProjection(
                screenX,
                screenY,
                cameraX,
                cameraY,
                inFront,
                onScreen
        );
    }

    private static void renderStatus(
            GuiGraphicsExtractor graphics,
            TedAllyEndermanEntity ally,
            double projectedX,
            double projectedY
    ) {
        int rowWidth =
                (ICON_COUNT - 1)
                        * ICON_SPACING
                        + ICON_SIZE;

        int startX =
                Mth.floor(projectedX)
                        - rowWidth / 2;

        int healthY =
                Mth.floor(projectedY) - 22;

        int foodY =
                healthY + 10;

        renderHealthIcons(
                graphics,
                startX,
                healthY,
                ally.getHealth(),
                ally.getMaxHealth()
        );

        renderFoodIcons(
                graphics,
                startX,
                foodY,
                ally.getFoodPoints(),
                ally.getMaxFoodPoints()
        );
    }

    private static void renderHealthIcons(
            GuiGraphicsExtractor graphics,
            int startX,
            int y,
            float health,
            float maxHealth
    ) {
        if (maxHealth <= 0.0F) {
            return;
        }

        int displayedPoints =
                Mth.clamp(
                        Mth.ceil(
                                health
                                        / maxHealth
                                        * ICON_COUNT
                                        * 2.0F
                        ),
                        0,
                        ICON_COUNT * 2
                );

        for (int i = 0; i < ICON_COUNT; i++) {
            int x =
                    startX
                            + i * ICON_SPACING;

            graphics.blitSprite(
                    RenderPipelines.GUI_TEXTURED,
                    HEART_CONTAINER,
                    x,
                    y,
                    ICON_SIZE,
                    ICON_SIZE
            );

            int iconPoint =
                    i * 2;

            if (displayedPoints
                    >= iconPoint + 2) {

                graphics.blitSprite(
                        RenderPipelines.GUI_TEXTURED,
                        HEART_FULL,
                        x,
                        y,
                        ICON_SIZE,
                        ICON_SIZE
                );

            } else if (displayedPoints
                    == iconPoint + 1) {

                graphics.blitSprite(
                        RenderPipelines.GUI_TEXTURED,
                        HEART_HALF,
                        x,
                        y,
                        ICON_SIZE,
                        ICON_SIZE
                );
            }
        }
    }

    private static void renderFoodIcons(
            GuiGraphicsExtractor graphics,
            int startX,
            int y,
            int foodPoints,
            int maxFoodPoints
    ) {
        if (maxFoodPoints <= 0) {
            return;
        }

        int displayedPoints =
                Mth.clamp(
                        Mth.ceil(
                                (float) foodPoints
                                        / (float) maxFoodPoints
                                        * ICON_COUNT
                                        * 2.0F
                        ),
                        0,
                        ICON_COUNT * 2
                );

        for (int i = 0; i < ICON_COUNT; i++) {
            int x =
                    startX
                            + i * ICON_SPACING;

            graphics.blitSprite(
                    RenderPipelines.GUI_TEXTURED,
                    FOOD_EMPTY,
                    x,
                    y,
                    ICON_SIZE,
                    ICON_SIZE
            );

            int iconPoint =
                    i * 2;

            if (displayedPoints
                    >= iconPoint + 2) {

                graphics.blitSprite(
                        RenderPipelines.GUI_TEXTURED,
                        FOOD_FULL,
                        x,
                        y,
                        ICON_SIZE,
                        ICON_SIZE
                );

            } else if (displayedPoints
                    == iconPoint + 1) {

                graphics.blitSprite(
                        RenderPipelines.GUI_TEXTURED,
                        FOOD_HALF,
                        x,
                        y,
                        ICON_SIZE,
                        ICON_SIZE
                );
            }
        }
    }

    private static void renderDirectionArrow(
            GuiGraphicsExtractor graphics,
            ScreenProjection projection,
            double distance
    ) {
        int screenWidth =
                graphics.guiWidth();

        int screenHeight =
                graphics.guiHeight();

        double centerX =
                screenWidth * 0.5D;

        double centerY =
                screenHeight * 0.5D;

        double directionX =
                projection.x() - centerX;

        double directionY =
                projection.y() - centerY;

        if (Math.abs(directionX) < 0.001D
                && Math.abs(directionY) < 0.001D) {
            directionY = 1.0D;
        }

        double maxX =
                centerX - SCREEN_MARGIN;

        double maxY =
                centerY - SCREEN_MARGIN;

        double scaleX =
                Math.abs(directionX) < 0.001D
                        ? Double.MAX_VALUE
                        : maxX / Math.abs(directionX);

        double scaleY =
                Math.abs(directionY) < 0.001D
                        ? Double.MAX_VALUE
                        : maxY / Math.abs(directionY);

        double scale =
                Math.min(
                        scaleX,
                        scaleY
                );

        int arrowCenterX =
                Mth.floor(
                        centerX
                                + directionX * scale
                );

        int arrowCenterY =
                Mth.floor(
                        centerY
                                + directionY * scale
                );

        int arrowX =
                Mth.clamp(
                        arrowCenterX
                                - ARROW_SIZE / 2,
                        4,
                        screenWidth
                                - ARROW_SIZE
                                - 4
                );

        int arrowY =
                Mth.clamp(
                        arrowCenterY
                                - ARROW_SIZE / 2,
                        4,
                        screenHeight
                                - ARROW_SIZE
                                - 4
                );

        Identifier arrowTexture =
                chooseArrowTexture(
                        directionX,
                        directionY
                );

        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                arrowTexture,
                arrowX,
                arrowY,
                0.0F,
                0.0F,
                ARROW_SIZE,
                ARROW_SIZE,
                ARROW_SIZE,
                ARROW_SIZE
        );

        Component distanceText =
                Component.literal(
                        Mth.floor(distance)
                                + "m"
                );

        Font font =
                Minecraft.getInstance().font;

        int textWidth =
                font.width(distanceText);

        int textX =
                arrowCenterX
                        - textWidth / 2;

        int textY =
                arrowY
                        + ARROW_SIZE
                        + 2;

        if (textY + 9 >= screenHeight) {
            textY =
                    arrowY - 10;
        }

        graphics.textWithBackdrop(
                font,
                distanceText,
                textX,
                textY,
                textWidth,
                0xFFFFFFFF
        );
    }

    private static Identifier chooseArrowTexture(
            double directionX,
            double directionY
    ) {
        if (Math.abs(directionX)
                > Math.abs(directionY)) {

            return directionX < 0.0D
                    ? ARROW_LEFT
                    : ARROW_RIGHT;
        }

        return directionY < 0.0D
                ? ARROW_UP
                : ARROW_DOWN;
    }

    private record ScreenProjection(
            double x,
            double y,
            double cameraX,
            double cameraY,
            boolean inFront,
            boolean onScreen
    ) {
    }

    private static boolean shouldDisplayArrow() {

        if (arrowDisplayTicks > 0) {
            return true;
        }

        if (arrowCooldownTicks > 0) {
            return false;
        }

        arrowCooldownTicks = 200; //10秒

        arrowDisplayTicks = 40; //2秒

        Minecraft minecraft = Minecraft.getInstance();

        playSonarSound();

        return true;
    }

    private static void playSonarSound() {
        Minecraft minecraft =
                Minecraft.getInstance();

        if (minecraft.level == null
                || minecraft.player == null
                || ModSounds.ALLY_ENDERMAN_SONAR == null) {
            return;
        }

        minecraft.level.playLocalSound(
                minecraft.player.getX(),
                minecraft.player.getY(),
                minecraft.player.getZ(),
                ModSounds.ALLY_ENDERMAN_SONAR,
                SoundSource.PLAYERS,
                1.0F,
                1.0F,
                false
        );
    }




}