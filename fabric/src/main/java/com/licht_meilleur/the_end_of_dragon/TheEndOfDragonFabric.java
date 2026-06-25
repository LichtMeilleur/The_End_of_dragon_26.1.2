package com.licht_meilleur.the_end_of_dragon;

import com.licht_meilleur.the_end_of_dragon.command.TEDDebugCommands;
import com.licht_meilleur.the_end_of_dragon.entity.TheEndOfDragonEntity;
import com.licht_meilleur.the_end_of_dragon.registry.ModEntities;
import com.licht_meilleur.the_end_of_dragon.world.EndDragonSpawnHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;

public final class TheEndOfDragonFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        TheEndOfDragon.init();

        FabricDefaultAttributeRegistry.register(
                ModEntities.THE_END_OF_DRAGON,
                TheEndOfDragonEntity.createAttributes()
        );

        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((level, entity, killedEntity, damageSource) -> {
            if (!(level instanceof ServerLevel serverLevel)) {
                return;
            }

            if (killedEntity instanceof EnderDragon dragon) {
                EndDragonSpawnHandler.spawn(serverLevel, dragon.position());
            }
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            TEDDebugCommands.register(dispatcher);
        });


    }
}