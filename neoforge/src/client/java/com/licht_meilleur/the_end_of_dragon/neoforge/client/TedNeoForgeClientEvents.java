package com.licht_meilleur.the_end_of_dragon.neoforge.client;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.client.LightOfDestructionHudRenderer;
import com.licht_meilleur.the_end_of_dragon.client.TedAllyEndermanHud;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(
        modid = TheEndOfDragon.MOD_ID,
        value = Dist.CLIENT
)
public final class TedNeoForgeClientEvents {

    @SubscribeEvent
    public static void onClientTick(
            ClientTickEvent.Post event
    ) {
        LightOfDestructionHudRenderer.clientTick();
        TedAllyEndermanHud.clientTick();
    }


    private TedNeoForgeClientEvents() {
    }
}