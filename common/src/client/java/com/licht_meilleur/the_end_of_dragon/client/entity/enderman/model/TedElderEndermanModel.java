package com.licht_meilleur.the_end_of_dragon.client.entity.enderman.model;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.entity.enderman.village
        .TedElderEndermanEntity;
import net.minecraft.resources.Identifier;

public final class TedElderEndermanModel
        extends GeoModel<TedElderEndermanEntity> {

    @Override
    public Identifier getModelResource(
            GeoRenderState renderState
    ) {
        return TheEndOfDragon.id(
                "ally_elder_enderman"
        );
    }

    @Override
    public Identifier getTextureResource(
            GeoRenderState renderState
    ) {
        return TheEndOfDragon.id(
                "textures/entity/ally_elder_enderman.png"
        );
    }

    @Override
    public Identifier getAnimationResource(
            TedElderEndermanEntity animatable
    ) {
        /*
         * animation.model.idle
         * animation.model.walk
         * を含む共通アニメーション。
         */
        return TheEndOfDragon.id(
                "ally_elder_enderman"
        );
    }
}