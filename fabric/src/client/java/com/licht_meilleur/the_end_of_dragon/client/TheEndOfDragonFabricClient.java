package com.licht_meilleur.the_end_of_dragon.client;

import com.licht_meilleur.the_end_of_dragon.client.render.TheEndOfDragonCollisionRenderer;
import com.licht_meilleur.the_end_of_dragon.client.render.TheEndOfDragonCoreRenderer;
import com.licht_meilleur.the_end_of_dragon.client.render.TheEndOfDragonDisplayRenderer;
import com.licht_meilleur.the_end_of_dragon.client.render.TheEndOfDragonRenderer;
import com.licht_meilleur.the_end_of_dragon.registry.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public final class TheEndOfDragonFabricClient implements ClientModInitializer {

    private static int debugTick;

    @Override
    public void onInitializeClient() {

        EntityRendererRegistry.register(
                ModEntities.THE_END_OF_DRAGON,
                TheEndOfDragonCoreRenderer::new
        );

        EntityRendererRegistry.register(
                ModEntities.THE_END_OF_DRAGON_DISPLAY,
                TheEndOfDragonDisplayRenderer::new
        );

        EntityRendererRegistry.register(
                ModEntities.THE_END_OF_DRAGON_COLLISION,
                TheEndOfDragonCollisionRenderer::new
        );



    }
}