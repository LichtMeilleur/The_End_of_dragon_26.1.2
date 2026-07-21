package com.licht_meilleur.the_end_of_dragon.client.entity.enderman;

import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.builtin.ItemInHandGeoLayer;
import com.licht_meilleur.the_end_of_dragon.entity.enderman
        .TedAllyEndermanEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state
        .LivingEntityRenderState;

public class TedAllyEndermanRenderer<
        R extends LivingEntityRenderState
                & GeoRenderState>
        extends GeoEntityRenderer<
        TedAllyEndermanEntity,
        R> {

    private static final float MODEL_SCALE =
            1.5F;

    public TedAllyEndermanRenderer(
            EntityRendererProvider.Context context
    ) {
        super(
                context,
                new TedAllyEndermanModel()
        );

        this.shadowRadius =
                0.55F;

        /*
         * GeckoLib標準名ではなく、
         * 現在のright_hand_locatorを使用する。
         *
         * 左手は使わないのでnull。
         */
        this.getRenderLayers().add(
                new TedInvitationItemLayer<>(
                        context,
                        this,
                        "RightHandItem",
                        "InvitationItem"
                )
        );
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