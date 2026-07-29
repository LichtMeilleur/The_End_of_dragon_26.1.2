package com.licht_meilleur.the_end_of_dragon.neoforge.client;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.client.LightOfDestructionHudRenderer;
import com.licht_meilleur.the_end_of_dragon.client.RechorusJuiceOverlayRenderer;
import com.licht_meilleur.the_end_of_dragon.client.TedAllyEndermanHud;
import com.licht_meilleur.the_end_of_dragon.client.entity.enderman.TedAllyEndermanRenderer;
import com.licht_meilleur.the_end_of_dragon.client.entity.enderman.renderer.TedElderEndermanRenderer;
import com.licht_meilleur.the_end_of_dragon.client.entity.enderman.renderer.TedTechEndermanRenderer;
import com.licht_meilleur.the_end_of_dragon.client.render.*;
import com.licht_meilleur.the_end_of_dragon.client.screen.TedVillageTradeScreen;
import com.licht_meilleur.the_end_of_dragon.neoforge.client.network.TedNeoForgeClientNetwork;
import com.licht_meilleur.the_end_of_dragon.neoforge.registry.NeoForgeModEntities;
import com.licht_meilleur.the_end_of_dragon.neoforge.registry.NeoForgeModFluids;
import com.licht_meilleur.the_end_of_dragon.neoforge.registry.NeoForgeModMenus;
import com.licht_meilleur.the_end_of_dragon.registry.ModEntities;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.util.ARGB;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
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

        event.registerEntityRenderer(
                NeoForgeModEntities.RECHORUS_JUICE_BLOB.get(),
                RechorusJuiceBlobRenderer::new
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

        event.registerAbove(
                VanillaGuiLayers.CHAT,
                TheEndOfDragon.id(
                        "rechorus_juice_overlay"
                ),
                (graphics, deltaTracker) ->
                        RechorusJuiceOverlayRenderer.render(
                                graphics
                        )
        );
    }

    public static void registerFluidModels(
            RegisterFluidModelsEvent event
    ) {
        FluidModel.Unbaked model =
                new FluidModel.Unbaked(
                        new Material(
                                TheEndOfDragon.id(
                                        "block/rechorus_juice_still"
                                )
                        ),
                        new Material(
                                TheEndOfDragon.id(
                                        "block/rechorus_juice_flow"
                                )
                        ),
                        new Material(
                                TheEndOfDragon.id(
                                        "block/rechorus_juice_overlay"
                                )
                        ),
                        BlockTintSources.constant(
                                ARGB.opaque(
                                        0xF2D84B
                                )
                        )
                );

        event.register(
                model,
                NeoForgeModFluids
                        .RECHORUS_JUICE_SOURCE,
                NeoForgeModFluids
                        .RECHORUS_JUICE_FLOWING
        );
    }

    public static void registerMenuScreens(
            RegisterMenuScreensEvent event
    ) {
        event.register(
                NeoForgeModMenus
                        .TED_VILLAGE_TRADE
                        .get(),
                TedVillageTradeScreen::new
        );
    }

    public static void registerClientPayloads(
            RegisterClientPayloadHandlersEvent event
    ) {
        TedNeoForgeClientNetwork.registerClientPayloads(event);
    }
}