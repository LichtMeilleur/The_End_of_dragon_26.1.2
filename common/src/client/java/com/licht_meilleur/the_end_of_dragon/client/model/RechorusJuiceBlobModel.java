package com.licht_meilleur.the_end_of_dragon.client.model;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.entity.RechorusJuiceBlobEntity;
import net.minecraft.resources.Identifier;

public class RechorusJuiceBlobModel
        extends GeoModel<RechorusJuiceBlobEntity> {

    @Override
    public Identifier getModelResource(
            GeoRenderState renderState
    ) {
        return TheEndOfDragon.id(
                "rechorus_juice_blob"
        );
    }

    @Override
    public Identifier getTextureResource(
            GeoRenderState renderState
    ) {
        return TheEndOfDragon.id(
                "textures/entity/rechorus_juice_blob.png"
        );
    }

    @Override
    public Identifier getAnimationResource(
            RechorusJuiceBlobEntity animatable
    ) {
        return TheEndOfDragon.id(
                "rechorus_juice_blob"
        );
    }
}