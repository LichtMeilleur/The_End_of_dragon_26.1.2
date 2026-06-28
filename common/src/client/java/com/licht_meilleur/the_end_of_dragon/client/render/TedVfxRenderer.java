package com.licht_meilleur.the_end_of_dragon.client.render;

import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.licht_meilleur.the_end_of_dragon.client.model.TedVfxModel;
import com.licht_meilleur.the_end_of_dragon.entity.vfx.TedVfxEntity;
import com.licht_meilleur.the_end_of_dragon.entity.vfx.TedVfxType;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

public class TedVfxRenderer<R extends EntityRenderState & GeoRenderState>
        extends GeoEntityRenderer<TedVfxEntity, R> {

    public TedVfxRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new TedVfxModel());
        this.shadowRadius = 0.0F;
    }

    @Override
    public void addRenderData(TedVfxEntity animatable, Void relatedObject, R renderState, float partialTick) {
        super.addRenderData(animatable, relatedObject, renderState, partialTick);
        renderState.addGeckolibData(TedVfxRenderTickets.VFX_TYPE, animatable.getVfxType());
        renderState.addGeckolibData(TedVfxRenderTickets.VFX_SCALE, animatable.getVfxScale());
        renderState.addGeckolibData(TedVfxRenderTickets.VFX_LENGTH, animatable.getVfxLength());
    }

    @Override
    public void adjustModelBonesForRender(
            com.geckolib.renderer.base.RenderPassInfo<R> renderPassInfo,
            com.geckolib.renderer.base.BoneSnapshots snapshots
    ) {
        super.adjustModelBonesForRender(renderPassInfo, snapshots);

        Float scale = renderPassInfo.renderState().getGeckolibData(TedVfxRenderTickets.VFX_SCALE);
        Float length = renderPassInfo.renderState().getGeckolibData(TedVfxRenderTickets.VFX_LENGTH);
        TedVfxType type = renderPassInfo.renderState().getGeckolibData(TedVfxRenderTickets.VFX_TYPE);

        float s = scale != null ? scale : 1.0F;
        float l = length != null ? length : 1.0F;

        snapshots.ifPresent("root", root -> {
            if (type == TedVfxType.TED_LASER_BEAM) {
                root.setScaleX(l); // まずX方向に伸ばす
                root.setScaleY(s);
                root.setScaleZ(s);
            } else if (type == TedVfxType.TED_JET) {
                root.setScaleX(s);
                root.setScaleY(s);
                root.setScaleZ(l);
            } else {
                root.setScaleX(s);
                root.setScaleY(s);
                root.setScaleZ(s);
            }
        });
    }
}