package com.licht_meilleur.the_end_of_dragon.neoforge.client;

import com.licht_meilleur.the_end_of_dragon.client.render.TheEndOfDragonCollisionRenderer;
import com.licht_meilleur.the_end_of_dragon.client.render.TheEndOfDragonCoreRenderer;
import com.licht_meilleur.the_end_of_dragon.client.render.TheEndOfDragonDisplayRenderer;
import com.licht_meilleur.the_end_of_dragon.client.render.TedVfxRenderer;
import com.licht_meilleur.the_end_of_dragon.neoforge.client.network.TedNeoForgeClientNetwork;
import com.licht_meilleur.the_end_of_dragon.registry.ModEntities;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;

public final class TheEndOfDragonNeoForgeClient {
    private TheEndOfDragonNeoForgeClient() {
    }

    public static void registerEntityRenderers(
            EntityRenderersEvent.RegisterRenderers event
    ) {
        event.registerEntityRenderer(
                ModEntities.THE_END_OF_DRAGON,
                TheEndOfDragonCoreRenderer::new
        );

        event.registerEntityRenderer(
                ModEntities.THE_END_OF_DRAGON_DISPLAY,
                TheEndOfDragonDisplayRenderer::new
        );

        event.registerEntityRenderer(
                ModEntities.THE_END_OF_DRAGON_COLLISION,
                TheEndOfDragonCollisionRenderer::new
        );

        event.registerEntityRenderer(
                ModEntities.TED_VFX,
                TedVfxRenderer::new
        );
    }

    public static void registerClientPayloads(
            RegisterClientPayloadHandlersEvent event
    ) {
        TedNeoForgeClientNetwork.registerClientPayloads(event);
    }
}