package com.licht_meilleur.the_end_of_dragon.neoforge.client;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.client.LightOfDestructionHudRenderer;
import com.licht_meilleur.the_end_of_dragon.client.TedAllyEndermanHud;
import com.licht_meilleur.the_end_of_dragon.client.entity.enderman.TedAllyEndermanRenderer;
import com.licht_meilleur.the_end_of_dragon.client.entity.enderman.renderer.TedElderEndermanRenderer;
import com.licht_meilleur.the_end_of_dragon.client.entity.enderman.renderer.TedTechEndermanRenderer;
import com.licht_meilleur.the_end_of_dragon.client.render.TheEndOfDragonCollisionRenderer;
import com.licht_meilleur.the_end_of_dragon.client.render.TheEndOfDragonCoreRenderer;
import com.licht_meilleur.the_end_of_dragon.client.render.TheEndOfDragonDisplayRenderer;
import com.licht_meilleur.the_end_of_dragon.client.render.TedVfxRenderer;
import com.licht_meilleur.the_end_of_dragon.neoforge.client.network.TedNeoForgeClientNetwork;
import com.licht_meilleur.the_end_of_dragon.neoforge.registry.NeoForgeModEntities;
import com.licht_meilleur.the_end_of_dragon.registry.ModEntities;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;

public final class TheEndOfDragonNeoForgeClient {
    private TheEndOfDragonNeoForgeClient() {
    }

    public static void registerEntityRenderers(
            EntityRenderersEvent.RegisterRenderers event
    ) {
        event.registerEntityRenderer(
                NeoForgeModEntities.THE_END_OF_DRAGON.get(),
                TheEndOfDragonCoreRenderer::new
        );

        event.registerEntityRenderer(
                NeoForgeModEntities.THE_END_OF_DRAGON_DISPLAY.get(),
                TheEndOfDragonDisplayRenderer::new
        );

        event.registerEntityRenderer(
            NeoForgeModEntities.THE_END_OF_DRAGON_COLLISION.get(),
                TheEndOfDragonCollisionRenderer::new
        );

        event.registerEntityRenderer(
                NeoForgeModEntities.TED_VFX.get(),
                TedVfxRenderer::new
        );

        event.registerEntityRenderer(
                NeoForgeModEntities.TED_ALLY_ENDERMAN.get(),
                TedAllyEndermanRenderer::new
        );

        event.registerEntityRenderer(
                NeoForgeModEntities.TED_ELDER_ENDERMAN.get(),
                TedElderEndermanRenderer::new
        );

        event.registerEntityRenderer(
                NeoForgeModEntities.TED_TECH_ENDERMAN.get(),
                TedTechEndermanRenderer::new
        );


    }

    public static void registerGuiLayers(
            RegisterGuiLayersEvent event
    ) {
        event.registerAbove(
                VanillaGuiLayers.CHAT,
                TheEndOfDragon.id("light_of_destruction_hud"),
                (graphics, deltaTracker) ->
                        LightOfDestructionHudRenderer.render(graphics)
        );

        event.registerAbove(
                VanillaGuiLayers.CHAT,
                TheEndOfDragon.id(
                        "ally_enderman_hud"
                ),
                (graphics, deltaTracker) ->
                        TedAllyEndermanHud.render(
                                graphics,
                                deltaTracker
                        )
        );
    }

    public static void registerClientPayloads(
            RegisterClientPayloadHandlersEvent event
    ) {
        TedNeoForgeClientNetwork.registerClientPayloads(event);
    }
}