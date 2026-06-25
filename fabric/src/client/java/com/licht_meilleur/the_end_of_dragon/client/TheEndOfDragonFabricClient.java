package com.licht_meilleur.the_end_of_dragon.client;

import com.licht_meilleur.the_end_of_dragon.client.hitbox.DragonClientHitBoxBuilder;
import com.licht_meilleur.the_end_of_dragon.client.hitbox.DragonHitBoxPushDebug;
import com.licht_meilleur.the_end_of_dragon.client.render.TheEndOfDragonRenderer;
import com.licht_meilleur.the_end_of_dragon.entity.TheEndOfDragonEntity;
import com.licht_meilleur.the_end_of_dragon.registry.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.NoopRenderer;

public final class TheEndOfDragonFabricClient implements ClientModInitializer {

    private static int debugTick;

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(
                ModEntities.THE_END_OF_DRAGON,
                TheEndOfDragonRenderer::new
        );

        EntityRendererRegistry.register(
                ModEntities.DRAGON_HITBOX,
                NoopRenderer::new
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level == null) {
                return;
            }

            if (debugTick++ % 20 != 0) {
                return;
            }

            int dragons = 0;

            for (var entity : client.level.entitiesForRendering()) {
                if (entity instanceof TheEndOfDragonEntity dragon) {
                    dragons++;

                    var boxes = DragonClientHitBoxBuilder.build(dragon);

                    DragonHitBoxPushDebug.pushPlayer(dragon, boxes);

                }
            }


        });
    }
}