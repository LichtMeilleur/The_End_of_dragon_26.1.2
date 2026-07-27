package com.licht_meilleur.the_end_of_dragon.client;

import com.licht_meilleur.the_end_of_dragon.registry.ModFluids;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

public final class RechorusJuiceOverlayRenderer {

    /*
     * ARGB。
     *
     * 66 = 透明度
     * E6C83A = 黄系
     */
    private static final int OVERLAY_COLOR =
            0x66E6C83A;

    private RechorusJuiceOverlayRenderer() {
    }

    public static void render(
            GuiGraphicsExtractor graphics
    ) {
        Minecraft minecraft =
                Minecraft.getInstance();

        Level level =
                minecraft.level;

        if (level == null
                || minecraft.player == null) {
            return;
        }

        if (!isCameraInsideRechorusJuice(
                minecraft,
                level
        )) {
            return;
        }

        graphics.fill(
                0,
                0,
                graphics.guiWidth(),
                graphics.guiHeight(),
                OVERLAY_COLOR
        );
    }

    private static boolean isCameraInsideRechorusJuice(
            Minecraft minecraft,
            Level level
    ) {
        Camera camera =
                minecraft.gameRenderer
                        .getMainCamera();

        Vec3 cameraPosition =
                camera.position();

        BlockPos fluidPosition =
                BlockPos.containing(
                        cameraPosition
                );

        FluidState fluidState =
                level.getFluidState(
                        fluidPosition
                );

        if (!isRechorusJuice(
                fluidState
        )) {
            return false;
        }

        /*
         * 同じブロック座標にいても、カメラが液面より上なら
         * オーバーレイを表示しない。
         */
        double fluidSurfaceY =
                fluidPosition.getY()
                        + fluidState.getHeight(
                        level,
                        fluidPosition
                );

        return cameraPosition.y
                < fluidSurfaceY;
    }

    public static boolean isRechorusJuice(
            FluidState fluidState
    ) {
        if (fluidState == null
                || fluidState.isEmpty()) {
            return false;
        }

        Fluid fluid =
                fluidState.getType();

        return fluid
                == ModFluids.RECHORUS_JUICE_SOURCE
                || fluid
                == ModFluids.RECHORUS_JUICE_FLOWING;
    }
}