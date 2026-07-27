package com.licht_meilleur.the_end_of_dragon.client.render;

import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.licht_meilleur.the_end_of_dragon.client.model.RechorusJuiceBlobModel;
import com.licht_meilleur.the_end_of_dragon.entity.RechorusJuiceBlobEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public class RechorusJuiceBlobRenderer<
        R extends EntityRenderState & GeoRenderState>
        extends GeoEntityRenderer<
        RechorusJuiceBlobEntity,
        R> {

    public RechorusJuiceBlobRenderer(
            EntityRendererProvider.Context context
    ) {
        super(
                context,
                new RechorusJuiceBlobModel()
        );

        /*
         * Blobには影を表示しない。
         *
         * 少し影を出したければ0.15F程度に変更可能。
         */
        this.shadowRadius = 0.0F;
    }

    @Override
    public RenderType getRenderType(
            R renderState,
            Identifier texture
    ) {
        /*
         * 半透明テクスチャ用。
         */
        return RenderTypes.entityTranslucent(
                texture
        );
    }
}