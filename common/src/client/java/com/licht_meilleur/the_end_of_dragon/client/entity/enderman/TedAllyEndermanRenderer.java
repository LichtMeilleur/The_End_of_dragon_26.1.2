package com.licht_meilleur.the_end_of_dragon.client.entity.enderman;

import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.licht_meilleur.the_end_of_dragon.client.entity.enderman.TedAllyEndermanModel;
import com.licht_meilleur.the_end_of_dragon.entity.enderman.TedAllyEndermanEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class TedAllyEndermanRenderer<
        R extends LivingEntityRenderState & GeoRenderState>
        extends GeoEntityRenderer<TedAllyEndermanEntity, R> {

    public TedAllyEndermanRenderer(
            EntityRendererProvider.Context context
    ) {
        super(
                context,
                new TedAllyEndermanModel()
        );

        this.shadowRadius = 0.45F;
    }
}