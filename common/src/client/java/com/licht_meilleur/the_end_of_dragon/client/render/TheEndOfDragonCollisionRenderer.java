package com.licht_meilleur.the_end_of_dragon.client.render;

import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.licht_meilleur.the_end_of_dragon.client.model.TheEndOfDragonCollisionModel;
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


}