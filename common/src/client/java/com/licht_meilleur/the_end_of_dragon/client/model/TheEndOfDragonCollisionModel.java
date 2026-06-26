package com.licht_meilleur.the_end_of_dragon.client.model;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.entity.TheEndOfDragonCollisionEntity;
import net.minecraft.resources.Identifier;

public class TheEndOfDragonCollisionModel extends GeoModel<TheEndOfDragonCollisionEntity> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return TheEndOfDragon.id("the_end_of_dragon_collision");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return TheEndOfDragon.id("textures/entity/the_end_of_dragon_collision.png");
    }

    @Override
    public Identifier getAnimationResource(TheEndOfDragonCollisionEntity animatable) {
        return TheEndOfDragon.id("the_end_of_dragon_collision");
    }
}