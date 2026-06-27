package com.licht_meilleur.the_end_of_dragon.client.model;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.client.render.TedVfxRenderTickets;
import com.licht_meilleur.the_end_of_dragon.entity.vfx.TedVfxEntity;
import com.licht_meilleur.the_end_of_dragon.entity.vfx.TedVfxType;
import net.minecraft.resources.Identifier;

public class TedVfxModel extends GeoModel<TedVfxEntity> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {

        TedVfxType type = getType(renderState);

        return TheEndOfDragon.id(type.modelPath);
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {

        TedVfxType type = getType(renderState);

        return TheEndOfDragon.id(type.texturePath);
    }

    @Override
    public Identifier getAnimationResource(TedVfxEntity animatable) {

        return TheEndOfDragon.id(animatable.getVfxType().animationPath);
    }

    private TedVfxType getType(GeoRenderState renderState) {
        TedVfxType type = renderState.getGeckolibData(TedVfxRenderTickets.VFX_TYPE);
        return type != null ? type : TedVfxType.LIGHT_PROJECTILE;
    }
}