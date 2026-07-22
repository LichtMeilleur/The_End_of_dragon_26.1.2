package com.licht_meilleur.the_end_of_dragon.client.entity.enderman.model;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.entity.enderman.village
        .TedTechEndermanEntity;
import net.minecraft.resources.Identifier;

public final class TedTechEndermanModel
        extends GeoModel<TedTechEndermanEntity> {

    @Override
    public Identifier getModelResource(
            GeoRenderState renderState
    ) {
        return TheEndOfDragon.id(
                "ally_tech_enderman"
        );
    }

    @Override
    public Identifier getTextureResource(
            GeoRenderState renderState
    ) {
        return TheEndOfDragon.id(
                "textures/entity/ally_tech_enderman.png"
        );
    }

    @Override
    public Identifier getAnimationResource(
            TedTechEndermanEntity animatable
    ) {
        return TheEndOfDragon.id(
                "ally_tech_enderman"
        );
    }
}