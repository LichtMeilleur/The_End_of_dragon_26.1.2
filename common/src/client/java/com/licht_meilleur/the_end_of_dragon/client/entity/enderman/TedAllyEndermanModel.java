package com.licht_meilleur.the_end_of_dragon.client.entity.enderman;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.entity.enderman.TedAllyEndermanEntity;
import net.minecraft.resources.Identifier;

public class TedAllyEndermanModel
        extends GeoModel<TedAllyEndermanEntity> {

    @Override
    public Identifier getModelResource(
            GeoRenderState renderState
    ) {
        return TheEndOfDragon.id(
                "ally_enderman"
        );
    }

    @Override
    public Identifier getTextureResource(
            GeoRenderState renderState
    ) {
        return TheEndOfDragon.id(
                "textures/entity/ally_enderman.png"
        );
    }

    @Override
    public Identifier getAnimationResource(
            TedAllyEndermanEntity animatable
    ) {
        return TheEndOfDragon.id(
                "ally_enderman"
        );
    }
}