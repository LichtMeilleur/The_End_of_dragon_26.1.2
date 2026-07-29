package com.licht_meilleur.the_end_of_dragon.neoforge.client;

import com.licht_meilleur.the_end_of_dragon.TheEndOfDragon;
import com.licht_meilleur.the_end_of_dragon.client.LightOfDestructionHudRenderer;
import com.licht_meilleur.the_end_of_dragon.client.TedAllyEndermanHud;
import com.licht_meilleur.the_end_of_dragon.client.screen.TedVillageTradeScreen;
import com.licht_meilleur.the_end_of_dragon.registry.ModMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

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

    @SubscribeEvent
    public static void registerMenuScreens(
            RegisterMenuScreensEvent event
    ) {
        event.register(
                ModMenus.TED_VILLAGE_TRADE,
                TedVillageTradeScreen::new
        );
    }


    private TedNeoForgeClientEvents() {
    }
}