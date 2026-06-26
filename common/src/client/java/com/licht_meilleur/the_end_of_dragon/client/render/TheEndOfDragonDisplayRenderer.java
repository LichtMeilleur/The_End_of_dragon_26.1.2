package com.licht_meilleur.the_end_of_dragon.client.render;

import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.licht_meilleur.the_end_of_dragon.client.model.TheEndOfDragonDisplayModel;
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
}
