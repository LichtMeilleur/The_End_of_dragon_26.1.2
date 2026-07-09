package com.licht_meilleur.the_end_of_dragon.client.render;

import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.licht_meilleur.the_end_of_dragon.client.model.TheEndOfDragonCollisionModel;
import com.licht_meilleur.the_end_of_dragon.entity.DragonState;
import com.licht_meilleur.the_end_of_dragon.entity.TheEndOfDragonCollisionEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public class TheEndOfDragonCollisionRenderer<R extends LivingEntityRenderState & GeoRenderState>
        extends GeoEntityRenderer<TheEndOfDragonCollisionEntity, R> {

    public TheEndOfDragonCollisionRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new TheEndOfDragonCollisionModel());
        this.shadowRadius = 0.0F;
    }

    @Override
    public RenderType getRenderType(R renderState, Identifier texture) {
        return RenderTypes.entityTranslucent(texture);
    }

    @Override
    protected void applyRotations(
            com.geckolib.renderer.base.RenderPassInfo<R> renderPassInfo,
            com.mojang.blaze3d.vertex.PoseStack poseStack,
            float nativeScale
    ) {
        super.applyRotations(renderPassInfo, poseStack, nativeScale);

        R state = renderPassInfo.renderState();

        if (state instanceof net.minecraft.client.renderer.entity.state.LivingEntityRenderState livingState) {
            poseStack.mulPose(
                    com.mojang.math.Axis.XP.rotationDegrees(livingState.xRot)
            );
        }
    }
    @Override
    public void extractRenderState(
            TheEndOfDragonCollisionEntity entity,
            R renderState,
            float partialTick
    ) {
        super.extractRenderState(entity, renderState, partialTick);

        DragonState state = entity.getDragonState();

        renderState.xRot = switch (state) {
            case FLY,
                 FLY_START,
                 FLY_SHOT,
                 FIGURE_EIGHT,
                 INTRO_RISE,
                 INTRO_FLY_TO_PORTAL,
                 INTRO_DIVE_TO_PORTAL -> entity.getFlightPitch();

            default -> 0.0F;
        };

    }

}