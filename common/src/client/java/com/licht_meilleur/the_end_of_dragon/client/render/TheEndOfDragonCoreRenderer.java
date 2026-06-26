package com.licht_meilleur.the_end_of_dragon.client.render;

import com.licht_meilleur.the_end_of_dragon.entity.TheEndOfDragonCoreEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

public class TheEndOfDragonCoreRenderer
        extends EntityRenderer<TheEndOfDragonCoreEntity, EntityRenderState> {

    public TheEndOfDragonCoreRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }
}