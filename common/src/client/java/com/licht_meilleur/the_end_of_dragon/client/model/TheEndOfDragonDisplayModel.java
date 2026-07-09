package com.licht_meilleur.the_end_of_dragon.client.model;

import com.geckolib.cache.model.GeoBone;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.client.render.TedRenderTickets;
import com.licht_meilleur.the_end_of_dragon.entity.TheEndOfDragonDisplayEntity;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AnimationState;

public class TheEndOfDragonDisplayModel extends GeoModel<TheEndOfDragonDisplayEntity> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return TheEndOfDragon.id("the_end_of_dragon");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        int stage = renderState.getOrDefaultGeckolibData(
                TedRenderTickets.CRYSTAL_FADE_STAGE,
                0
        );

        return switch (stage) {
            case 1 -> TheEndOfDragon.id("textures/entity/the_end_of_dragon_crystal_fade_25.png");
            case 2 -> TheEndOfDragon.id("textures/entity/the_end_of_dragon_crystal_fade_50.png");
            case 3 -> TheEndOfDragon.id("textures/entity/the_end_of_dragon_crystal_fade_75.png");
            case 4 -> TheEndOfDragon.id("textures/entity/the_end_of_dragon_crystal_fade_100.png");
            default -> TheEndOfDragon.id("textures/entity/the_end_of_dragon.png");
        };
    }

    @Override
    public Identifier getAnimationResource(TheEndOfDragonDisplayEntity animatable) {
        return TheEndOfDragon.id("the_end_of_dragon");
    }

}