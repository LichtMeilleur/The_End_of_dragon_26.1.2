package com.licht_meilleur.the_end_of_dragon.client.entity.enderman.renderer;

import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.licht_meilleur.the_end_of_dragon.client.entity.enderman.model.TedTechEndermanModel;
import com.licht_meilleur.the_end_of_dragon.entity.enderman.village
        .TedTechEndermanEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state
        .LivingEntityRenderState;

public final class TedTechEndermanRenderer<
        R extends LivingEntityRenderState
                & GeoRenderState>
        extends GeoEntityRenderer<
        TedTechEndermanEntity,
        R> {

    private static final float MODEL_SCALE =
            1.5F;

    public TedTechEndermanRenderer(
            EntityRendererProvider.Context context
    ) {
        super(
                context,
                new TedTechEndermanModel()
        );

        this.shadowRadius =
                0.55F;
    }

    @Override
    protected void applyRotations(
            RenderPassInfo<R> renderPassInfo,
            PoseStack poseStack,
            float nativeScale
    ) {
        super.applyRotations(
                renderPassInfo,
                poseStack,
                nativeScale
        );

        poseStack.scale(
                MODEL_SCALE,
                MODEL_SCALE,
                MODEL_SCALE
        );
    }
}