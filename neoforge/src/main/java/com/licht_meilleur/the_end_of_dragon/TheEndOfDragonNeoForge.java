package com.licht_meilleur.the_end_of_dragon;

import com.licht_meilleur.the_end_of_dragon.command.TEDDebugCommands;
import com.licht_meilleur.the_end_of_dragon.entity.TheEndOfDragonEntity;
import com.licht_meilleur.the_end_of_dragon.registry.ModEntities;
import com.licht_meilleur.the_end_of_dragon.world.EndDragonSpawnHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@Mod(TheEndOfDragon.MOD_ID)
public final class TheEndOfDragonNeoForge {
    public TheEndOfDragonNeoForge(IEventBus modBus) {
        TheEndOfDragon.init();

        modBus.addListener(this::registerAttributes);
        NeoForge.EVENT_BUS.register(this);
    }

    private void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(
                ModEntities.THE_END_OF_DRAGON,
                TheEndOfDragonEntity.createAttributes().build()
        );
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof EnderDragon dragon)) {
            return;
        }

        if (!(dragon.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        EndDragonSpawnHandler.spawn(serverLevel, dragon.position());
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        TEDDebugCommands.register(event.getDispatcher());
    }
}