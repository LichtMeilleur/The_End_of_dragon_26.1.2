package com.licht_meilleur.the_end_of_dragon.fabric.client;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.client.LightOfDestructionHudRenderer;
import com.licht_meilleur.the_end_of_dragon.client.TedAllyEndermanHud;
import com.licht_meilleur.the_end_of_dragon.client.entity.enderman.TedAllyEndermanRenderer;
import com.licht_meilleur.the_end_of_dragon.client.entity.enderman.renderer.TedElderEndermanRenderer;
import com.licht_meilleur.the_end_of_dragon.client.entity.enderman.renderer.TedTechEndermanRenderer;
import com.licht_meilleur.the_end_of_dragon.client.quest.TedVillageQuestClientHandler;
import com.licht_meilleur.the_end_of_dragon.client.render.TheEndOfDragonCollisionRenderer;
import com.licht_meilleur.the_end_of_dragon.client.render.TheEndOfDragonCoreRenderer;
import com.licht_meilleur.the_end_of_dragon.client.render.TheEndOfDragonDisplayRenderer;
import com.licht_meilleur.the_end_of_dragon.fabric.client.network.TedFabricClientNetwork;
import com.licht_meilleur.the_end_of_dragon.network.TedOpenQuestLetterPayload;
import com.licht_meilleur.the_end_of_dragon.network.TedQuestClientNetwork;
import com.licht_meilleur.the_end_of_dragon.network.TedSubmitQuestPayload;
import com.licht_meilleur.the_end_of_dragon.registry.ModEntities;
import com.licht_meilleur.the_end_of_dragon.registry.ModFluids;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.impl.client.rendering.EntityRendererRegistryImpl;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.util.ARGB;

public final class TheEndOfDragonFabricClient implements ClientModInitializer {

    private static int debugTick;
    private static final int
            RECHORUS_JUICE_COLOR =
            0xFFF2D84B;
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

        EntityRendererRegistry.register(
                ModEntities.TED_VFX,
                com.licht_meilleur.the_end_of_dragon.client.render.TedVfxRenderer::new
        );

        EntityRendererRegistry.register(
                ModEntities.TED_ALLY_ENDERMAN,
                TedAllyEndermanRenderer::new
        );

        EntityRendererRegistry.register(
                ModEntities.TED_ELDER_ENDERMAN,
                TedElderEndermanRenderer::new
        );

        EntityRendererRegistry.register(
                ModEntities.TED_TECH_ENDERMAN,
                TedTechEndermanRenderer::new
        );



        // HUD描画
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                com.licht_meilleur.the_end_of_dragon.TheEndOfDragon.id("light_of_destruction_hud"),
                (graphics, tickCounter) -> LightOfDestructionHudRenderer.render(graphics)
        );

        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                TheEndOfDragon.id(
                        "ally_enderman_hud"
                ),
                (graphics, deltaTracker) ->
                        TedAllyEndermanHud.render(
                                graphics,
                                deltaTracker
                        )
        );

        FluidRenderingRegistry.register(
                ModFluids.RECHORUS_JUICE_SOURCE,
                ModFluids.RECHORUS_JUICE_FLOWING,
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
                )
        );

        FluidRenderingRegistry.register(
                ModFluids.RECHORUS_JUICE_GUIDE_SOURCE,
                ModFluids.RECHORUS_JUICE_GUIDE_FLOWING,
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
                                        0xFFF29A
                                )
                        )
                )
        );



        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            LightOfDestructionHudRenderer.clientTick();
            TedAllyEndermanHud.clientTick();
        });

        TedFabricClientNetwork.init();






    }


}