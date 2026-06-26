package com.licht_meilleur.the_end_of_dragon.client.model;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.entity.TheEndOfDragonDisplayEntity;
import net.minecraft.resources.Identifier;

public class TheEndOfDragonDisplayModel extends GeoModel<TheEndOfDragonDisplayEntity> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return TheEndOfDragon.id("the_end_of_dragon");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return TheEndOfDragon.id("textures/entity/the_end_of_dragon.png");
    }

    @Override
    public Identifier getAnimationResource(TheEndOfDragonDisplayEntity animatable) {
        return TheEndOfDragon.id("the_end_of_dragon");
    }
}