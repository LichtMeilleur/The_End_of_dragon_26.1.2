package com.licht_meilleur.the_end_of_dragon.client;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.entity.DragonState;
import com.licht_meilleur.the_end_of_dragon.entity.TheEndOfDragonCoreEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

public final class LightOfDestructionHudRenderer {
    private static final Identifier LIGHT_HUD =
            TheEndOfDragon.id("textures/gui/light_hud.png");

    private static int flashTicks = 0;
    private static final int MAX_FLASH_TICKS = 24;

    private static boolean lightWasActive = false;
    private static int lightActiveTicks = 0;

    private LightOfDestructionHudRenderer() {
    }

    public static void triggerFlash() {
        flashTicks = MAX_FLASH_TICKS;
    }



    public static void clientTick() {
        Minecraft minecraft = Minecraft.getInstance();

        boolean lightActiveNow = false;

        if (minecraft.level != null && minecraft.player != null) {
            for (Entity entity : minecraft.level.entitiesForRendering()) {
                if (!(entity instanceof TheEndOfDragonCoreEntity dragon)) continue;
                if (dragon.distanceTo(minecraft.player) > 64.0F) continue;

                if (dragon.getDragonState() == DragonState.LIGHT_OF_DESTRUCTION) {
                    lightActiveNow = true;
                    break;
                }
            }
        }

        if (lightActiveNow) {
            lightActiveTicks++;
        } else {
            lightActiveTicks = 0;
        }

        if (lightActiveNow && !lightWasActive) {
            lightActiveTicks = 1;
        }

        // サーバー側の age 30 相当。発動から30tick後に白フラッシュ
        if (lightActiveTicks == 30) {
            triggerFlash();
        }

        lightWasActive = lightActiveNow;

        if (flashTicks > 0) {
            flashTicks--;
        }
    }

    public static void render(GuiGraphicsExtractor graphics) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.options.hideGui) return;
        if (minecraft.player == null) return;
        if (flashTicks <= 0) return;

        int width = graphics.guiWidth();
        int height = graphics.guiHeight();

        float t = flashTicks / (float) MAX_FLASH_TICKS;
        float alpha = t * t;

        int a = (int) (alpha * 230.0F);
        int color = (a << 24) | 0xFFFFFF;

        // まず白で画面全体をフェード
        graphics.fill(0, 0, width, height, color);

        // さらに用意した白ノイズ画像を重ねる
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                LIGHT_HUD,
                0,
                0,
                0.0F,
                0.0F,
                width,
                height,
                width,
                height
        );
    }
}