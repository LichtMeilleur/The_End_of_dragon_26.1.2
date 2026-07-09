package com.licht_meilleur.the_end_of_dragon.client.render;

import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.licht_meilleur.the_end_of_dragon.client.model.TheEndOfDragonDisplayModel;
import com.licht_meilleur.the_end_of_dragon.entity.DragonState;
import com.licht_meilleur.the_end_of_dragon.entity.TheEndOfDragonDisplayEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

import java.util.IdentityHashMap;
import java.util.Map;



public class TheEndOfDragonDisplayRenderer<R extends LivingEntityRenderState & GeoRenderState>
        extends GeoEntityRenderer<TheEndOfDragonDisplayEntity, R> {


    private final Map<R, R> collisionRenderStates = new IdentityHashMap<>();



    public TheEndOfDragonDisplayRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new TheEndOfDragonDisplayModel());
        this.shadowRadius = 4.0F;




    }

    @Override
    public net.minecraft.client.renderer.rendertype.RenderType getRenderType(
            R renderState,
            net.minecraft.resources.Identifier texture
    ) {
        return net.minecraft.client.renderer.rendertype.RenderTypes.entityTranslucent(texture);
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
            TheEndOfDragonDisplayEntity entity,
            R renderState,
            float partialTick
    ) {
        super.extractRenderState(entity, renderState, partialTick);

        renderState.addGeckolibData(
                TedRenderTickets.CRYSTAL_FADE_STAGE,
                entity.getCrystalFadeStage()
        );

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
