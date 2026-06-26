package com.licht_meilleur.the_end_of_dragon.client.model;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.entity.OldTheEndOfDragonEntity;
import net.minecraft.resources.Identifier;

public class TheEndOfDragonModel extends GeoModel<OldTheEndOfDragonEntity> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return TheEndOfDragon.id("the_end_of_dragon");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return TheEndOfDragon.id("textures/entity/the_end_of_dragon.png");
    }

    @Override
    public Identifier getAnimationResource(OldTheEndOfDragonEntity animatable) {
        return TheEndOfDragon.id("the_end_of_dragon");
    }
}